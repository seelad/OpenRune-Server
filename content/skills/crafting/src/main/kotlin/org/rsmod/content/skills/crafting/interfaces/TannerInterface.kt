package org.rsmod.content.skills.crafting.interfaces

import dev.openrune.ServerCacheManager
import dev.openrune.definition.type.widget.IfEvent
import java.awt.Color
import org.rsmod.api.attr.AttributeKey
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.ui.setColour
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.table.crafting.CraftingHandRow
import org.rsmod.content.skills.crafting.CraftingSection
import org.rsmod.content.skills.crafting.util.CraftingConstants
import org.rsmod.content.skills.crafting.util.CraftingGamevals
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * This one is packed in the legacy if1 format (`v3 = false`), which predates cs2 - there are no `onLoad` hooks to hang scripts on.
 *
 * The same goes for the ops. If1 clicks arrive as `If1Button` (component only, no op), which
 * `If1ButtonHandler` normalizes to `IfModalButton`+Op1. Op1 isn't in the components' baked events,
 * so [openTanner] grants it at runtime. Each of the four ops per hide (4 x 8 grid) is a distinct component,
 * so the grid is 32 buttons rather than 8 with four ops each.
 */
class TannerInterfaceScript : PluginScript() {
    override fun ScriptContext.startup() {
        // 8 slots x 4 ops = 32 handlers.
        for ((slot, row) in tannerSlotRows) {
            for (op in TannerOp.entries) {
                onIfModalButton(CraftingConstants.tannerSlotOp(slot.letter, op.suffix)) {
                    performTan(slot, row, op)
                }
            }
        }
    }
}

/**
 * Opens the tanner and paints the grid.
 *
 * [prices] is what the *opening NPC* charges, and is remembered while the interface stays open, so
 * the prices on the grid and the coins a click takes come from the same schedule. Tanners on the
 * standard table (Ellis, the Crafting Guild tanner) leave it defaulted; premium tanners pass theirs.
 */
fun ProtectedAccess.openTanner(prices: TannerPrices = TannerPrices.Table) {
    if (tannerSlotRows.isEmpty()) {
        return
    }
    player.attr[TANNER_PRICES] = prices
    ifOpenMainModal(CraftingConstants.INTERFACE_TANNER)
    for ((slot, _) in tannerSlotRows) {
        for (op in TannerOp.entries) {
            ifSetEvents(CraftingConstants.tannerSlotOp(slot.letter, op.suffix), -1..-1, IfEvent.Op1)
        }
    }
    renderTannerSlots(prices)
}

/**
 * What one tanner charges per hide. Every schedule in the game keys off the same five buckets, so
 * [of] fans those five across the eight slots; anything unset falls back to the table's `cost`.
 */
class TannerPrices private constructor(private val bySlot: Map<Char, Int>) {
    internal fun priceOf(slot: TannerSlot, row: TanningRecipe): Int =
        bySlot[slot.letter] ?: row.cost

    companion object {
        /** The tanning table's own prices - Ellis and the Crafting Guild tanner. */
        val Table: TannerPrices = TannerPrices(emptyMap())

        fun of(
            soft: Int,
            hard: Int,
            snakeskin: Int,
            swampSnakeskin: Int,
            dragonhide: Int,
        ): TannerPrices =
            TannerPrices(
                mapOf(
                    'a' to soft,
                    'b' to hard,
                    'c' to swampSnakeskin,
                    'd' to snakeskin,
                    'e' to dragonhide,
                    'f' to dragonhide,
                    'g' to dragonhide,
                    'h' to dragonhide,
                ),
            )
    }
}

/** Prices the open interface was opened with; absent (reconnect, older code) falls back to table. */
private val TANNER_PRICES = AttributeKey<TannerPrices>()

/**
 * Tans [row]'s hide, up to [requested], at [pricePerHide] coins each. One tick, one transaction:
 * coin debit, hide consumption, and leather addition land together, so the client sees a single
 * inventory update. Returns how many were tanned, so callers can auto-close on any success.
 */
fun ProtectedAccess.tanHides(
    row: TanningRecipe,
    requested: Int,
    pricePerHide: Int = row.cost,
): Int {
    val hide = row.input
    val held = inv.count(hide)
    if (held == 0) {
        mes("You don't have any ${itemName(hide)} to tan.")
        return 0
    }

    val coinsHeld = inv.count(CraftingConstants.COINS)
    val affordable = if (pricePerHide > 0) coinsHeld / pricePerHide else Int.MAX_VALUE
    if (affordable == 0) {
        mes("You haven't got enough coins to pay for ${itemName(row.output)}.")
        return 0
    }

    val amount = minOf(requested, held, affordable)
    if (amount <= 0) {
        return 0
    }

    val totalCost = pricePerHide * amount
    if (!invDel(inv, CraftingConstants.COINS, totalCost).success) {
        return 0
    }
    if (!invDel(inv, hide, amount).success) {
        invAdd(inv, CraftingConstants.COINS, totalCost)
        return 0
    }
    invAdd(inv, row.output, amount)

    val message =
        when {
            requested > held -> "You have run out of ${itemName(hide)}."
            amount < requested ->
                "You haven't got enough coins to pay for ${itemName(row.output)}."
            else -> "The tanner tans your ${itemName(hide)}."
        }
    mes(message)
    return amount
}

/**
 * One slot click. Any success closes the interface; a zero-tan (out of hide or coins) keeps it open
 * and re-renders, so the newly-red label is there beside the error message.
 */
private suspend fun ProtectedAccess.performTan(
    slot: TannerSlot,
    row: TanningRecipe,
    op: TannerOp,
) {
    val requested =
        when (op) {
            TannerOp.Tan1 -> 1
            TannerOp.Tan5 -> 5
            TannerOp.TanAll -> Int.MAX_VALUE
            TannerOp.TanX -> countDialog().coerceAtLeast(0)
        }
    val prices = player.attr.getOrDefault(TANNER_PRICES, TannerPrices.Table)
    val tanned = if (requested > 0) tanHides(row, requested, prices.priceOf(slot, row)) else 0
    if (tanned > 0) {
        ifClose()
    } else if (player.ui.containsModal(CraftingConstants.INTERFACE_TANNER)) {
        renderTannerSlots(prices)
    }
}

/** Paints the 8 slots, colouring the labels by whether the hide is held. */
private fun ProtectedAccess.renderTannerSlots(prices: TannerPrices) {
    for ((slot, row) in tannerSlotRows) {
        val name = CraftingConstants.tannerSlotName(slot.letter)
        val price = CraftingConstants.tannerSlotPrice(slot.letter)
        ifSetObj(
            CraftingConstants.tannerSlotModel(slot.letter),
            slot.input,
            CraftingConstants.TANNER_MODEL_ZOOM,
        )
        ifSetText(name, slot.label)
        ifSetText(price, coins(prices.priceOf(slot, row)))
        val colour = if (inv.count(slot.input) > 0) LABEL_AVAILABLE else LABEL_UNAVAILABLE
        player.setColour(name, colour)
        player.setColour(price, colour)
    }
}

/**
 * One slot of the grid. Keyed by (input, output) because both snakeskin recipes share an output, and
 * both leather recipes share an input.
 */
internal data class TannerSlot(
    val letter: Char,
    val label: String,
    val input: String,
    val output: String,
)

/** Which op stack position was clicked. Ordered to match the interface's stacking. */
internal enum class TannerOp(val suffix: String) {
    Tan1("1"),
    Tan5("5"),
    TanX("x"),
    TanAll("all"),
}

/** The grid, in display order: top row a-d, bottom row e-h. */
private val TANNER_SLOTS =
    listOf(
        TannerSlot('a', "Soft leather", "obj.cow_hide", "obj.leather"),
        TannerSlot('b', "Hard leather", "obj.cow_hide", "obj.hard_leather"),
        TannerSlot('c', "Snakeskin", "obj.templetrek_swamp_snake_hide", "obj.village_snake_skin"),
        TannerSlot('d', "Snakeskin", "obj.village_snake_hide", "obj.village_snake_skin"),
        TannerSlot('e', "Green d'hide", "obj.dragonhide_green", "obj.dragon_leather"),
        TannerSlot('f', "Blue d'hide", "obj.dragonhide_blue", "obj.dragon_leather_blue"),
        TannerSlot('g', "Red d'hide", "obj.dragonhide_red", "obj.dragon_leather_red"),
        TannerSlot('h', "Black d'hide", "obj.dragonhide_black", "obj.dragon_leather_black"),
    )

/** Label color when the hide is held - the JSON's baked blue. */
private val LABEL_AVAILABLE: Color = Color(0, 207, 255)

/** Label color when the player has none of the required hide */
private val LABEL_UNAVAILABLE: Color = Color(254, 0, 0)

/**
 * One tanning exchange, off a `crafting_hand` row of the Tanning section: [input] hide + [cost]
 * coins per item -> [output] leather. The section is a price list, not a craft - see
 * CraftingSection.TANNING.
 */
data class TanningRecipe(val input: String, val output: String, val cost: Int)

/** Every tanning row; Thakkrad's yak curing reads this catalogue too. */
internal val tanningRecipes: List<TanningRecipe> by lazy {
    CraftingHandRow.all()
        .filter { it.section == CraftingSection.TANNING.id }
        .map { TanningRecipe(it.input.first().internalName, it.output.first().internalName, it.cost ?: 0) }
}

/** Slots paired with their tanning recipes; NPC scripts iterate this catalogue too. */
internal val tannerSlotRows: List<Pair<TannerSlot, TanningRecipe>> by lazy {
    TANNER_SLOTS.mapNotNull { slot ->
        val row = tanningRecipes.firstOrNull { it.input == slot.input && it.output == slot.output }
        row?.let { slot to it }
    }
}

/** Tannable hide objs - for NPC "used-item-on" and greeting logic. */
internal val tannableHideObjs: Set<String> by lazy { TANNER_SLOTS.map { it.input }.toSet() }

/** Leather (output) objs. */
internal val tannedLeatherObjs: Set<String> by lazy { TANNER_SLOTS.map { it.output }.toSet() }

private fun coins(amount: Int): String = if (amount == 1) "1 coin" else "$amount coins"

/** Lowercased item name, falling back to the raw gameval. */
internal fun itemName(internal: String): String {
    val id = CraftingGamevals.objOrNull(internal) ?: return internal
    return ServerCacheManager.getItem(id)?.name?.lowercase() ?: internal
}
