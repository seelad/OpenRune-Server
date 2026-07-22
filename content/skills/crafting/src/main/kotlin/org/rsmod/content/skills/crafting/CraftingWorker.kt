package org.rsmod.content.skills.crafting

import dev.openrune.ServerCacheManager
import jakarta.inject.Inject
import org.rsmod.api.attr.AttributeKey
import org.rsmod.api.player.output.ChatType
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.world.WorldRepository
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.content.skills.SkillMultiConfig
import org.rsmod.content.skills.SkillMultiEntry
import org.rsmod.content.skills.openSkillMulti
import org.rsmod.content.skills.crafting.util.CraftingConfig
import org.rsmod.content.skills.crafting.util.CraftingConstants
import org.rsmod.content.skills.crafting.util.CraftingGamevals
import org.rsmod.api.player.stat.craftingLvl
import org.rsmod.content.skills.crafting.util.hasCraftingTool
import org.rsmod.content.skills.crafting.util.holdsCostumeNeedle
import org.rsmod.content.skills.crafting.util.holdsImcandoHammer
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext
import skillSuccess

/**
 * Remaining uses on the player's current spool of thread (1 spool = 5 crafts). A persistent
 * attribute rather than a varbit: no suitable `varbit.crafting_thread_uses` exists in the cache,
 * and referencing a missing varbit throws. Nothing else touches the storage directly.
 */
private val THREAD_USES_ATTR = AttributeKey<Int>(persistenceKey = "crafting_thread_uses")

private fun ProtectedAccess.threadUses(): Int = player.attr.getOrDefault(THREAD_USES_ATTR, 0)

private fun ProtectedAccess.setThreadUses(value: Int) {
    player.attr[THREAD_USES_ATTR] = value
}

/**
 * The shared crafting engine. Every section funnels through the same entry points:
 * - [selectCraftingProduct]: the "what/how many" prompt, then production.
 * - [startCrafting]: enqueues the production loop on the shared `queue.crafting_make` queue.
 * - [craftInstantly]: one craft per click, no queue (limestone, the combines).
 *
 * The per-cycle work - level re-validation, tool/material checks, thread accounting, failure
 * rolls, animation/sound, inventory transactions, xp - is entirely data-driven from
 * [CraftingProduct] (itself resolved against its [CraftingSection]'s defaults), so no section
 * carries production logic of its own.
 *
 * Cycle pacing:
 * - Cycle 1 begins inline on the click tick T: [startCrafting] fires [beginCycle] (anim, sound,
 *   spotanim, facility loc anim, `startMessage`) and queues END for tick T + max(1, ticks - 1).
 *   The first craft is a tick quicker than the steady-state spacing (clamped so a 1-tick recipe
 *   still takes a tick), matching observed behaviour.
 * - [CraftingPhase.END] at cycle end: fires `craftOnce` (inputs consumed, failure roll, output,
 *   `successMessage`). If more crafts remain, how the next cycle is scheduled depends on whether
 *   the section has a `startMessage`:
 *     - No start message (the pottery wheel and most sections): the animation just continues, so
 *       [beginCycle] is replayed inline on this same tick and the next END is queued `ticks` ticks
 *       later. There is no BEGIN phase; the anim replays on the very tick each item lands.
 *     - Has a start message (pottery firing's "You put ..."): the next cycle is a distinct
 *       [CraftingPhase.BEGIN] one tick later, so its start message lands on its own tick after this
 *       cycle's "You remove ..." end message. Its END then follows `ticks - 1` later.
 *   Either way the product-to-product spacing is exactly `ticks`.
 *
 * Net timing: item K lands at T + max(1, ticks-1) + (K-1)*ticks; spacing between items is `ticks`.
 */
class CraftingWorkerScript @Inject constructor(
    private val worldRepo: WorldRepository,
) : PluginScript() {

    override fun ScriptContext.startup() {
        CraftingRuntime.worldRepo = worldRepo
        onPlayerQueueWithArgs<CraftingTask>(CraftingConstants.QUEUE_CRAFTING_MAKE) {
            processCraftingTask(it.args)
        }
    }

    private suspend fun ProtectedAccess.processCraftingTask(task: CraftingTask) {
        when (task.phase) {
            CraftingPhase.BEGIN -> processBegin(task)
            CraftingPhase.END -> processEnd(task)
        }
    }

    /**
     * BEGIN phase, used only by sections with a start message (pottery firing). Fires the cycle's
     * start effects (anim + "You put ...") and queues END `ticks - 1` later, so this cycle's item
     * lands `ticks` ticks after the previous one. Sections without a start message never reach this
     * path - they replay the animation inline from [processEnd] instead.
     */
    private suspend fun ProtectedAccess.processBegin(task: CraftingTask) {
        val product = task.product
        if (!canCraft(product, verbose = false)) {
            resetAnim()
            return
        }
        beginCycle(product, task.facility)
        weakQueue(
            CraftingConstants.QUEUE_CRAFTING_MAKE,
            (product.ticks - 1) + QUEUE_HANDLER_COMPENSATION,
            task.copy(phase = CraftingPhase.END),
        )
    }

    /**
     * END phase: complete the cycle - consume inputs, roll failure, add output, send success
     * message. If more crafts are queued and still craftable, schedule the next BEGIN 1 tick
     * later so the next cycle's start message lands on a distinct tick from this cycle's success
     * message.
     */
    private suspend fun ProtectedAccess.processEnd(task: CraftingTask) {
        val product = task.product
        if (!craftOnce(product)) {
            resetAnim()
            return
        }
        val completed = task.completed + 1
        if (completed >= task.amount) {
            // Natural completion: let the anim we started at cycle-begin play out.
            return
        }
        if (product.startMessage != null) {
            // Two-message sections (pottery firing): the start message ("You put ...") must land on
            // its own tick, one after this cycle's end message ("You remove ..."). So the next cycle
            // is a distinct BEGIN a tick later; its END then follows ticks-1 later, keeping the
            // product-to-product spacing at exactly `ticks`.
            weakQueue(
                CraftingConstants.QUEUE_CRAFTING_MAKE,
                1 + QUEUE_HANDLER_COMPENSATION,
                task.copy(completed = completed, phase = CraftingPhase.BEGIN),
            )
        } else {
            // Single-message sections (the pottery wheel and everything else): the animation simply
            // continues, replaying on the very tick the item lands - no gap. The next item follows
            // `ticks` ticks later, so there is no separate BEGIN. This is why the first craft is a
            // tick quicker than the rest: cycle 1 spans ticks-1 (click to product), every later
            // cycle spans a full `ticks` (product to product).
            beginCycle(product, task.facility)
            weakQueue(
                CraftingConstants.QUEUE_CRAFTING_MAKE,
                product.ticks + QUEUE_HANDLER_COMPENSATION,
                task.copy(completed = completed, phase = CraftingPhase.END),
            )
        }
    }
}

/**
 * +1 for every `weakQueue` call scheduled from within a queue handler. The engine's
 * `PlayerQueueProcessor.publishExpiredWeakQueues` has an outer while loop that re-iterates the
 * queue list within the same tick, so a queue added mid-loop has its first decrement in the same
 * tick it was scheduled - passing N gives N-1 real ticks. The engine itself notes the same
 * off-by-one convention (see the `GameCycle.postTick` comment about `loc_del(100)` firing after
 * 99 cycles). All scheduling from `processCraftingTask` (both phases) is inside this loop.
 */
private const val QUEUE_HANDLER_COMPENSATION = 1

/** Which half of a craft cycle a queued task represents. */
enum class CraftingPhase {
    /**
     * Fire anim/sound/spotanim/locAnim/startMessage, then schedule END `ticks-1` later. Only
     * scheduled for sections with a start message; others replay the anim inline from END.
     */
    BEGIN,
    /** Fire craftOnce (inputs, roll, output, successMessage), then schedule the next cycle. */
    END,
}

/**
 * Runtime references the shared [CraftingWorkerScript] pipeline reaches for during a craft but
 * doesn't own itself. `WorldRepository` is injected into the worker at startup and stashed here
 * so [beginCycle] can play a facility (loc) animation from any call site (the free `startCrafting`
 * included) without threading the repo through every intermediate hop.
 */
internal object CraftingRuntime {
    lateinit var worldRepo: WorldRepository
}

/**
 * Everything that fires at the *start* of a craft cycle: player anim, sound, spot anim, facility
 * anim, and the section's start message. Called from the BEGIN phase of the queue handler for
 * every queued cycle, and from [craftInstantly] for INSTANT/COMBINE mode crafts which resolve
 * start and end effects in the same tick.
 */
private fun ProtectedAccess.beginCycle(product: CraftingProduct, facility: BoundLocInfo?) {
    craftAnim(product)?.let { anim(it) }
    product.sound?.let { soundSynth(it) }
    product.spotanim?.let { spotanim(it) }
    if (product.locAnim != null && facility != null) {
        locAnim(CraftingRuntime.worldRepo, facility, product.locAnim)
    }
    product.startMessage?.let { mes(it, ChatType.Spam) }
}

/**
 * A queued crafting job. [facility] is only set for facility-based sections (spinning, weaving,
 * pottery) so the machine animates alongside the player via the product's `locAnim`; hand
 * crafting and furnace sections leave it null. [phase] tracks which half of the two-phase cycle
 * this queued invocation represents (see [CraftingWorkerScript]).
 */
data class CraftingTask(
    val product: CraftingProduct,
    val amount: Int,
    val completed: Int,
    val facility: BoundLocInfo? = null,
    val phase: CraftingPhase = CraftingPhase.END,
)

/**
 * Validates that [product] can currently be crafted. When [verbose], missing requirements are
 * messaged to the player - including the specific deficient item when materials are short.
 */
suspend fun ProtectedAccess.canCraft(product: CraftingProduct, verbose: Boolean): Boolean {
    if (player.craftingLvl < product.level) {
        if (verbose) {
            mesbox("You need a Crafting level of at least ${product.level} to ${product.actionName}.")
        }
        return false
    }

    // Requirements in other skills (the noxious halberd's Smithing), checked against the effective
    // level exactly like the Crafting requirement above.
    for (req in product.extraReqs) {
        if (stat(req.stat) < req.level) {
            if (verbose) {
                mesbox(
                    "You need a ${req.displayName} level of at least ${req.level} " +
                        "to ${product.actionName}.",
                )
            }
            return false
        }
    }

    for (tool in product.tools) {
        if (!hasCraftingTool(tool)) {
            if (verbose) {
                mes("You don't have the required tool to do that.", ChatType.Spam)
            }
            return false
        }
    }

    if (product.consumesThread && !holdsCostumeNeedle() && !inv.contains(CraftingConstants.THREAD)) {
        if (verbose) {
            mes("You need some thread to make that.", ChatType.Spam)
        }
        return false
    }

    val deficient = product.deficientInput { inv.count(it) }
    if (deficient != null) {
        if (verbose) {
            val dialogue = product.missingInputMessage
            if (dialogue != null) {
                mesbox(dialogue)
            } else {
                mes("You don't have enough ${itemName(deficient.internal)} for that!", ChatType.Spam)
            }
        }
        return false
    }

    return true
}

/**
 * Performs a single craft of [product]: consumes inputs (rolling back on partial failure), rolls
 * the recipe's [CraftingFailure] (if any), adds the output and byproducts, charges thread, and
 * grants xp. Returns true if the cycle completed (a failed roll counts - the inputs are spent and
 * any failure item was produced), false if it aborted (no inventory space or a consumption
 * failure). Does NOT play animations - callers own that so the queue worker animates once per
 * cycle.
 */
suspend fun ProtectedAccess.craftOnce(product: CraftingProduct): Boolean {
    // Consume inputs, rolling back on partial failure (mirrors herblore's transaction shape).
    val removed = mutableListOf<Pair<String, Int>>()
    for (material in product.inputs) {
        if (invDel(inv, material.internal, material.count).success) {
            removed += material.internal to material.count
        } else {
            removed.forEach { (obj, count) -> invAdd(inv, obj, count) }
            return false
        }
    }

    // Failure roll: the standard skilling success roll against the effective level. Inputs are
    // already spent; on failure the recipe's failure item (crushed gem, rock, or nothing at all
    // for pottery) is produced instead of the output. Counts as a completed cycle so a batch
    // continues to the next item.
    val failure = product.failure

    if (failure != null && !skillSuccess(failure.low, failure.high, player.craftingLvl)) {
        failure.sound?.let { soundSynth(it) }
        failure.item?.let { invAdd(inv, it, failure.itemCount) }
        advanceCraftingXp(CraftingConstants.STAT_CRAFTING, failure.xp)
        failure.message?.let { mes(it, ChatType.Spam) }
        return true
    }

    if (invAdd(inv, product.output, product.outputCount).failure) {
        removed.forEach { (obj, count) -> invAdd(inv, obj, count) }
        mes("You don't have enough inventory space to do that.", ChatType.Spam)
        return false
    }

    product.byproducts.forEach { invAdd(inv, it.internal, it.count) }

    if (product.consumesThread && !holdsCostumeNeedle()) {
        consumeThreadCharge()
    }

    advanceCraftingXp(CraftingConstants.STAT_CRAFTING, product.xp)
    // Recipes that pay out in more than one skill (the noxious halberd's 100 Smithing xp).
    product.extraXp.forEach { advanceCraftingXp(it.stat, it.xp) }
    product.successMessage?.let { mes(it, ChatType.Spam) }
    return true
}

/**
 * Grants crafting-pipeline experience. Every xp value in the module is carried in *tenths* of a
 * point (see [CraftingProduct.xp]); this is the single place that conversion happens - it divides
 * by [CraftingConstants.FINE_XP_DIVISOR] once, just before the xp reaches the player. A non-positive
 * amount grants nothing.
 */
private fun ProtectedAccess.advanceCraftingXp(stat: String, fineXp: Double) {
    if (fineXp <= 0.0) return
    statAdvance(stat, fineXp / CraftingConstants.FINE_XP_DIVISOR)
}

/** One spool of thread lasts [CraftingConstants.THREAD_USES_PER_SPOOL] crafts. */
private fun ProtectedAccess.consumeThreadCharge() {
    // A value of 0 means no spool has been started yet - treat it as a fresh spool.
    val uses = threadUses().takeIf { it > 0 } ?: CraftingConstants.THREAD_USES_PER_SPOOL
    if (uses <= 1) {
        if (invDel(inv, CraftingConstants.THREAD, 1).success) {
            mes("You use up one of your reels of thread.", ChatType.Spam)
        }
        setThreadUses(CraftingConstants.THREAD_USES_PER_SPOOL)
    } else {
        setThreadUses(uses - 1)
    }
}

/** Human-readable item name for messages, falling back to the raw gameval if unresolved. */
private fun itemName(internal: String): String {
    val id = CraftingGamevals.objOrNull(internal) ?: return internal
    return ServerCacheManager.getItem(id)?.name?.lowercase() ?: internal
}

/** Materials-and-tools check without level gating; used to decide what a prompt should display. */
fun ProtectedAccess.hasCraftingMaterials(product: CraftingProduct): Boolean {
    val hasTools = product.tools.all { hasCraftingTool(it) }
    val hasThread =
        !product.consumesThread || holdsCostumeNeedle() || inv.contains(CraftingConstants.THREAD)
    val hasInputs = product.inputs.all { inv.count(it.internal) >= it.count }
    return hasTools && hasThread && hasInputs
}

/**
 * Starts the production loop for [amount] of [product], optionally animating [facility]. Pacing
 * is the product's resolved [CraftingProduct.ticks].
 *
 * Cycle 1 begins inline on this same tick (anim/sound/start message fire immediately), and END
 * is queued for T + ticks. Subsequent cycles run through the queue handler; see the class kdoc.
 */
fun ProtectedAccess.startCrafting(
    product: CraftingProduct,
    amount: Int,
    facility: BoundLocInfo? = null,
) {
    val capped = minOf(amount, product.maxCraftable { inv.count(it) })
    if (capped <= 0) {
        return
    }
    beginCycle(product, facility)
    weakQueue(
        CraftingConstants.QUEUE_CRAFTING_MAKE,
        // Cycle 1's END fires ticks-1 real ticks after the click - the first craft is a tick
        // quicker than the steady-state spacing (observed on the pottery wheel: click -> product
        // is 2 ticks for a 3-tick recipe). Clamped to a 1-tick minimum so a 1-tick recipe still
        // takes a tick rather than resolving on the click tick itself.
        //
        // Compensation for the tick-phase this call is coming from:
        //   - Menu-resume paths run in PlayerInputProcess *before* PlayerMainProcess.processQueues,
        //     so a queue added now hits publishExpiredWeakQueues' same-tick decrement. +1.
        //   - Direct op-handler paths run in processInteractions *after* processQueues, so their
        //     queues are safe. +0.
        // Distinguisher: processedMapClock is set at the start of PlayerMainProcess.processAll,
        // so it matches currentMapClock only once we're inside PlayerMainProcess.
        maxOf(1, product.ticks - 1) + if (player.currentMapClock != player.processedMapClock) 1 else 0,
        CraftingTask(
            product = product,
            amount = capped,
            completed = 0,
            facility = facility,
            phase = CraftingPhase.END,
        ),
    )
}

/**
 * The animation for this craft: the recipe's imcando-hammer variant when the player is holding an
 * imcando hammer and the recipe has one, otherwise its normal animation. Resolved per craft
 * rather than baked into the product - the player may equip or bank the imcando between clicks.
 */
private fun ProtectedAccess.craftAnim(product: CraftingProduct): String? =
    product.imcandoAnim?.takeIf { holdsImcandoHammer() } ?: product.anim

/**
 * Performs exactly one craft of [product] right now - no menu, no queue, no tick pacing. Used by
 * [CraftingMode.INSTANT] and [CraftingMode.COMBINE] sections, where each click crafts one item.
 * The full [craftOnce] pipeline still applies: requirement checks, input transaction, the failure
 * roll (and failure item), xp, and messages.
 */
suspend fun ProtectedAccess.craftInstantly(product: CraftingProduct) {
    if (!canCraft(product, verbose = true)) {
        return
    }
    beginCycle(product, facility = null)
    craftOnce(product)
}

/**
 * Presents the "what would you like to make / how many" chatbox prompt for [products] and starts
 * production with the player's selection. The prompt's verb and action type come from [section].
 *
 * Display policy: every recipe is shown by default, whether or not the player can currently make
 * it - picking an uncraftable one messages the deficient item rather than silently doing nothing.
 * The exception is recipes flagged [CraftingProduct.requiresMaterialsToShow], hidden unless their
 * inputs are held.
 *
 * When [CraftingConfig.SKIP_SINGLE_RECIPE_PROMPT] is enabled and exactly one recipe would be
 * shown, the prompt is skipped and the maximum craftable amount is made immediately.
 */
suspend fun ProtectedAccess.selectCraftingProduct(
    section: CraftingSection,
    products: List<CraftingProduct>,
    facility: BoundLocInfo? = null,
) {
    // Gated recipes only show when their materials are held; everything else always shows.
    val shown = products.filter { !it.requiresMaterialsToShow || hasCraftingMaterials(it) }
    // A configured empty-menu message is shown whenever the player can't make anything here - even
    // for facilities that always list some recipes (the spinning wheel lists its standard recipes
    // but still says "nothing suitable to spin" when no material is held). Facilities without such a
    // message just keep showing their always-shown defaults.
    val emptyMessage = section.emptyMenuMessage()
    if (emptyMessage != null && products.none { hasCraftingMaterials(it) }) {
        mesbox(emptyMessage)
        return
    }
    if (shown.isEmpty()) {
        return
    }

    val productsByOutput = shown.associateBy { it.output }

    if (shown.size == 1 && CraftingConfig.SKIP_SINGLE_RECIPE_PROMPT) {
        beginCraft(shown.single(), Int.MAX_VALUE, facility)
        return
    }

    openSkillMultiForProducts(section, shown) { selected, amount ->
        productsByOutput[selected]?.let { beginCraft(it, amount, facility) }
    }
}

/**
 * Validates the chosen [product] and either starts crafting or messages why it can't. This is the
 * single gate every menu selection passes through, so an uncraftable pick can never slip past.
 */
suspend fun ProtectedAccess.beginCraft(
    product: CraftingProduct,
    amount: Int,
    facility: BoundLocInfo? = null,
) {
    if (!canCraft(product, verbose = true)) {
        return
    }
    startCrafting(product, amount, facility)
}

private suspend fun ProtectedAccess.openSkillMultiForProducts(
    section: CraftingSection,
    shown: List<CraftingProduct>,
    onSelect: suspend (output: String, amount: Int) -> Unit,
) {
    val entries = shown.map { product ->
        SkillMultiEntry(product.output, product.inputs)
    }
    // Display quantity: the craftable amount, but at least 1 so uncraftable recipes still appear
    // (they'd otherwise be filtered out by openSkillMulti). Actual craftability is enforced in
    // beginCraft, not here.
    openSkillMulti(
        SkillMultiConfig(
            verb = section.verb,
            actionType = section.actionType,
            entries = entries,
            maxCountProvider = { inventory, entry ->
                val product = shown.firstOrNull { it.output == entry.internal }
                product?.maxCraftable { inventory.count(it) }?.coerceAtLeast(1) ?: 1
            },
        ),
    ) { selection ->
        onSelect(selection.entry.internal, selection.amount)
    }
}
