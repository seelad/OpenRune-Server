package org.rsmod.content.skills.crafting

import dev.openrune.types.ItemServerType
import dev.openrune.types.StatType
import org.rsmod.api.table.Tuple2
import org.rsmod.api.table.crafting.CraftingFacilitiesRow
import org.rsmod.api.table.crafting.CraftingGoldRow
import org.rsmod.api.table.crafting.CraftingHandRow
import org.rsmod.api.table.crafting.CraftingSilverRow
import org.rsmod.content.skills.Material
import org.rsmod.content.skills.crafting.util.CraftingConstants
import org.rsmod.content.skills.crafting.util.CraftingGamevals

/**
 * A single crafting recipe, normalised from any of the crafting tables and fully resolved against
 * its [CraftingSection]'s defaults - the module's common currency, consumed by the shared
 * selection/worker pipeline in `CraftingWorker`.
 *
 * By the time a product exists, everything is concrete: [ticks] is the recipe's own timing or the
 * section default, [anim]/[sound] are the recipe's own or the section's, [failure] is null unless
 * the recipe's row declared success numerators, and the player messages have been rendered from
 * the section's templates.
 */
data class CraftingProduct(
    /** The section this recipe belongs to; decides how it is registered and started. */
    val section: CraftingSection,
    /** Output obj gameval. */
    val output: String,
    /** Number of [output] produced per craft. */
    val outputCount: Int = 1,
    /** Consumed materials per craft. */
    val inputs: List<Material>,
    /** Required (effective/boosted) Crafting level. */
    val level: Int,
    /**
     * Experience per successful craft, in *tenths* of a point (25 == 2.5xp). CraftingWorker
     * divides by [CraftingConstants.FINE_XP_DIVISOR] once, at the moment it grants the xp - this
     * is the only place the conversion happens.
     */
    val xp: Double,
    /** Requirements in skills other than Crafting (the noxious halberd's 72 Smithing). */
    val extraReqs: List<CraftingStatReq> = emptyList(),
    /** Experience granted in skills other than Crafting (the halberd's 100 Smithing). */
    val extraXp: List<CraftingStatXp> = emptyList(),
    /**
     * The inputs that can be clicked on one another to start the craft. When empty (nearly every
     * recipe), every input is a click target; the bone staff names its triggers so its 1,000
     * chaos runes are consumed but inert to clicks. See registerHeldCrafting.
     */
    val triggers: List<String> = emptyList(),
    /** Game ticks per craft, already resolved (recipe override or section default). */
    val ticks: Int,
    /** Player animation gameval played each cycle, or null for none. */
    val anim: String? = null,
    /** Animation played instead of [anim] while an imcando hammer is held; see CraftingTools. */
    val imcandoAnim: String? = null,
    /** Facility (loc) animation gameval played on the crafting facility each cycle, if any. */
    val locAnim: String? = null,
    /** Sound synth gameval played each cycle, or null for none. */
    val sound: String? = null,
    /** Spot animation gameval played each cycle (e.g. battlestaff orbs), or null for none. */
    val spotanim: String? = null,
    /** Tools that must be held but are never consumed; equivalents accepted (CraftingTools). */
    val tools: List<String> = emptyList(),
    /** Whether this craft consumes thread charges (1 spool = 5 crafts). */
    val consumesThread: Boolean = false,
    /** Items returned to the player on a successful craft (e.g. the empty bucket from glass). */
    val byproducts: List<Material> = emptyList(),
    /** Failure semantics; null (the default) means the craft is guaranteed to succeed. */
    val failure: CraftingFailure? = null,
    /** Message sent at cycle start (when the anim plays), or null for none. */
    val startMessage: String? = null,
    /** Message sent on a successful craft, or null for none. */
    val successMessage: String? = null,
    /**
     * Dialogue to show (as `mesbox`) when the recipe is picked without its inputs held. Null
     * falls through to the standard "You don't have enough X for that!" spam line. Section-driven
     * via [CraftingSection.missingInputMessage] with `{output}` interpolated from the recipe.
     */
    val missingInputMessage: String? = null,
    /**
     * When true, the recipe only appears in the selection menu while its inputs are held (the
     * loom's material-gated bolts). When false (the default), the recipe is always shown and an
     * uncraftable pick messages the deficient item instead.
     */
    val requiresMaterialsToShow: Boolean = false,
    /** Completes "You need a Crafting level of at least X to <actionName>." */
    val actionName: String,
) {
    fun maxCraftable(counts: (String) -> Int): Int =
        inputs.minOfOrNull { counts(it.internal) / it.count } ?: 0

    /** The first input the player is short on, or null if they have everything. */
    fun deficientInput(counts: (String) -> Int): Material? =
        inputs.firstOrNull { counts(it.internal) < it.count }
}

/**
 * A requirement in a skill other than Crafting. [stat] is the stat gameval ("stat.smithing");
 * [displayName] is what the player is shown ("Smithing").
 */
data class CraftingStatReq(val stat: String, val displayName: String, val level: Int)

/**
 * Experience granted in a skill other than Crafting on a successful craft. In tenths of a point,
 * like [CraftingProduct.xp]; CraftingWorker applies [CraftingConstants.FINE_XP_DIVISOR].
 */
data class CraftingStatXp(val stat: String, val xp: Double)

/**
 * Failure semantics, expressed the way OSRS expresses them - through the standard skilling
 * success roll: [low]/[high] are the success numerators out of 256 at levels 1 and 99, clamped to
 * 0..1 against the effective level (see MathSkillUtiils.computeSkillingSuccess; a [high] above
 * 256 makes success guaranteed before 99). On a failed roll the inputs are already spent; [item]
 * (if any) is produced instead of the output, along with [xp] and [message].
 */
data class CraftingFailure(
    /** Success numerator out of 256 at level 1. */
    val low: Int,
    /** Success numerator out of 256 at level 99 (>256 = guaranteed before 99). */
    val high: Int,
    /** Obj gameval produced on failure, or null when a failure produces nothing (pottery). */
    val item: String? = null,
    /** Number of [item] produced on failure. */
    val itemCount: Int = 1,
    /** Experience granted on failure, in tenths (see [CraftingProduct.xp]). */
    val xp: Double = 0.0,
    /** Message sent on failure, or null for none. */
    val message: String? = null,
    /** Sound synth gameval played on failure (e.g. the gem crush), or null for none. */
    val sound: String? = null,
)

/**
 * The one place a table row becomes a [CraftingProduct], resolving every optional recipe field
 * against the [section]'s defaults:
 * - [ticks]/[anim]/[sound]/[locAnim] fall back to the section default when the row omits them.
 * - [successLow]/[successHigh]/[failItem]/[failXp] build a [CraftingFailure]; when [successLow]
 *   is absent the product is guaranteed to succeed.
 * - Action names and success messages come from the section's templates unless the row carries
 *   a [message]/[actionName] override, in which case `{input}`/`{output}` are interpolated.
 *
 * Every product built here is also added to [CraftingRecipes], giving the module a complete
 * recipe index for free - see `CraftingApi`.
 */
fun craftingProduct(
    section: CraftingSection,
    output: ItemServerType,
    outputCount: Int,
    input: List<ItemServerType>,
    inputAmount: List<Int>,
    statReq: List<Tuple2<StatType, Int>>,
    fineXp: Int,
    anim: String? = null,
    ticks: Int? = null,
    sound: String? = null,
    locAnim: String? = null,
    spotanim: String? = null,
    successLow: Int? = null,
    successHigh: Int? = null,
    failItem: ItemServerType? = null,
    failItemCount: Int = 1,
    failXp: Int? = null,
    extraTools: List<String> = emptyList(),
    byproducts: List<Material> = emptyList(),
    requiresMaterialsToShow: Boolean = false,
    xpExtra: List<Tuple2<StatType, Int>> = emptyList(),
    triggers: List<ItemServerType> = emptyList(),
    message: String? = null,
    actionName: String? = null,
): CraftingProduct {
    val names = CraftingNames(
        input = input.firstOrNull()?.name?.lowercase().orEmpty(),
        output = output.name.lowercase(),
    )
    // A recipe can fail only if its row declares the success numerators; everything else about
    // the failure (item, xp) is equally optional.
    val failure = if (successLow != null && successLow > 0) {
        CraftingFailure(
            low = successLow,
            high = successHigh ?: successLow,
            item = failItem?.internalName,
            itemCount = failItemCount,
            // Stored in tenths, exactly like the success xp; CraftingWorker divides once at grant.
            xp = (failXp ?: 0).toDouble(),
            message = section.failureMessage(names),
            sound = CraftingGamevals.optional(section.failureSound),
        )
    } else {
        null
    }
    // The Crafting entry of `stat_req` becomes `level`; any other skill becomes an `extraReq`.
    val (craftingLevel, extraReqs) = statReq.splitCraftingReq()
    return CraftingProduct(
        section = section,
        output = output.internalName,
        outputCount = outputCount,
        inputs = input.mapIndexed { i, obj -> Material(obj.internalName, inputAmount.getOrElse(i) { 1 }) },
        level = craftingLevel,
        // xp is carried in tenths of a point right up to the grant; CraftingWorker divides once,
        // in one place, by CraftingConstants.FINE_XP_DIVISOR. Same for the extra-skill xp below.
        xp = fineXp.toDouble(),
        extraReqs = extraReqs,
        extraXp = xpExtra.map { CraftingStatXp(it.t0.internalName, it.t1.toDouble()) },
        triggers = triggers.map { it.internalName },
        ticks = ticks ?: section.ticks,
        // Resolve cosmetics defensively: a missing anim/sound/spotanim becomes null (skipped)
        // rather than crashing the craft when it plays. See CraftingGamevals.
        anim = CraftingGamevals.optional(anim ?: section.anim),
        imcandoAnim = CraftingGamevals.optional(section.imcandoAnim),
        locAnim = CraftingGamevals.optional(locAnim ?: section.locAnim),
        sound = CraftingGamevals.optional(sound ?: section.sound),
        spotanim = CraftingGamevals.optional(spotanim),
        tools = section.tools + extraTools,
        consumesThread = section.consumesThread,
        byproducts = byproducts,
        failure = failure,
        startMessage = section.startMessage(names),
        successMessage = message?.render(names) ?: section.successMessage(names),
        missingInputMessage = section.missingInputMessage(names),
        requiresMaterialsToShow = requiresMaterialsToShow,
        actionName = actionName?.render(names) ?: section.actionName(names),
    ).also(CraftingRecipes::register)
}

/** Renders a row's `message`/`action_name` template: `{input}`/`{output}` become the item names. */
private fun String.render(names: CraftingNames): String =
    replace("{input}", names.input).replace("{output}", names.output)

// region Table-row adapters
//
// One adapter per crafting table. Every column is passed through here and nowhere else, so a
// change to the generated column shapes is a one-file fix. Sections resolve from each row's
// `section` column; per-facility deviations (the spinning wheel's animation variant, the loom's
// gated recipes, glass smelting's returned bucket) are the caller-supplied parameters.

/**
 * Splits a row's output list into its main product and its byproducts. The first `output(...)` is
 * the recipe's main product; any further outputs (the empty container from soft clay, the empty
 * bucket from glass smelting) are returned as byproducts, each paired with its output amount.
 */
private fun splitOutputs(
    output: List<ItemServerType>,
    outputAmount: List<Int>,
): Triple<ItemServerType, Int, List<Material>> {
    val main = output.first()
    val mainCount = outputAmount.firstOrNull() ?: 1
    val byproducts =
        output.drop(1).mapIndexed { i, obj ->
            Material(obj.internalName, outputAmount.getOrElse(i + 1) { 1 })
        }
    return Triple(main, mainCount, byproducts)
}

/** A `crafting_hand` row: needlework, gems, combines, birdhouses, and the rest. */
fun CraftingHandRow.toCraftingProduct(): CraftingProduct {
    val (mainOutput, mainCount, extraOutputs) = splitOutputs(output, outputAmount)
    return craftingProduct(
        section = CraftingSection.byId(section),
        output = mainOutput,
        outputCount = mainCount,
        input = input,
        inputAmount = inputAmount,
        statReq = statReq,
        fineXp = xp,
        anim = anim,
        sound = sound,
        spotanim = spotanim,
        successLow = successLow,
        successHigh = successHigh,
        failItem = failItem,
        failXp = failXp,
        extraTools = listOfNotNull(tool?.internalName),
        xpExtra = xpExtra,
        triggers = triggers,
        message = message,
        actionName = actionName,
        byproducts = extraOutputs,
    )
}

/** A `crafting_facilities` row: spinning, weaving, pottery, and glass smelting. */
fun CraftingFacilitiesRow.toCraftingProduct(
    anim: String? = null,
    requiresMaterialsToShow: Boolean = false,
): CraftingProduct {
    val (mainOutput, mainCount, extraOutputs) = splitOutputs(output, outputAmount)
    return craftingProduct(
        section = CraftingSection.byId(section),
        output = mainOutput,
        outputCount = mainCount,
        input = input,
        inputAmount = inputAmount,
        statReq = statReq,
        fineXp = xp,
        anim = anim,
        ticks = ticks,
        successLow = successLow,
        successHigh = successHigh,
        byproducts = extraOutputs,
        requiresMaterialsToShow = requiresMaterialsToShow,
    )
}

/** A `crafting_silver` row; the required mould rides along as an extra tool. */
fun CraftingSilverRow.toCraftingProduct(): CraftingProduct =
    jewelleryProduct(section, output, outputAmount, input, inputAmount, statReq, xp, mould)

/** A `crafting_gold` row; the required mould rides along as an extra tool. */
fun CraftingGoldRow.toCraftingProduct(): CraftingProduct =
    jewelleryProduct(section, output, outputAmount, input, inputAmount, statReq, xp, mould)

private fun jewelleryProduct(
    section: String,
    output: ItemServerType,
    outputAmount: Int,
    input: List<ItemServerType>,
    inputAmount: List<Int>,
    statReq: List<Tuple2<StatType, Int>>,
    fineXp: Int,
    mould: ItemServerType?,
): CraftingProduct =
    craftingProduct(
        section = CraftingSection.byId(section),
        output = output,
        outputCount = outputAmount,
        input = input,
        inputAmount = inputAmount,
        statReq = statReq,
        fineXp = fineXp,
        extraTools = listOfNotNull(mould?.internalName),
    )

// endregion

/**
 * Player-facing name for a stat: the cache's [StatType.displayName] when populated, else the
 * title-cased gameval suffix ("stat.smithing" -> "Smithing").
 */
private fun StatType.playerName(): String =
    displayName.ifBlank {
        internalName.substringAfterLast('.').replaceFirstChar(Char::uppercase)
    }

/**
 * Splits a row's `stat_req` column into the Crafting requirement (which becomes
 * [CraftingProduct.level]) and everything else ([CraftingProduct.extraReqs]). *Finds* the
 * Crafting entry rather than taking the first, so a row may list requirements in any order.
 */
private fun List<Tuple2<StatType, Int>>.splitCraftingReq(): Pair<Int, List<CraftingStatReq>> {
    val crafting = firstOrNull { it.t0.isType(CraftingConstants.STAT_CRAFTING) }
    val extras = filter { it !== crafting }
        .map { CraftingStatReq(it.t0.internalName, it.t0.playerName(), it.t1) }
    return (crafting?.t1 ?: 1) to extras
}
