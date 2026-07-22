package org.rsmod.content.skills.crafting.interfaces

import dev.openrune.definition.type.widget.IfEvent
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.table.crafting.CraftingGoldRow
import org.rsmod.content.skills.crafting.CraftingProduct
import org.rsmod.content.skills.crafting.beginCraft
import org.rsmod.content.skills.crafting.hasCraftingMaterials
import org.rsmod.content.skills.crafting.toCraftingProduct
import org.rsmod.content.skills.crafting.util.CraftingConstants
import org.rsmod.content.skills.crafting.util.CraftingGamevals
import org.rsmod.content.skills.crafting.util.ringBlingUnlocked
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The gold crafting interface, opened by using a gold bar on a furnace (`JewelleryScript`) or by
 * the furnace's smelt op with no smeltable ore (Smithing's `SmeltingScript`).
 *
 * The interface draws itself. Its `onLoad` scripts register inv-transmit listeners, so the client
 * paints the product models, sets their "Make <item>" ops, swaps a slot for its blank when the
 * materials are gone, and shows or hides each mould section's "You need a ... mould" panel - all of
 * it live. The quantity column is the shared [MakeQuantityColumn].
 *
 * That leaves the server three jobs, as with silver: grant the ops, craft on click, and record the
 * last recipe for the highlight.
 */
class GoldCraftingInterfaceScript : PluginScript() {
    override fun ScriptContext.startup() {
        // Build (and thereby index) the gold products now rather than on first interface open:
        // CraftingRecipes must be complete at startup for ::craftmat and other consumers.
        check(goldProducts.isNotEmpty()) { "No gold crafting recipes resolved" }

        for (slot in goldSlots) {
            onIfModalButton(slot.component) { craftGoldSlot(slot) }
        }
        for (quantity in GOLD_COLUMN.buttons) {
            onIfModalButton(GOLD_COLUMN.component(quantity.button)) {
                selectMakeQuantity(GOLD_COLUMN, quantity)
            }
        }
        // The created button is only shown while it is the selection, so clicking it changes
        // nothing. Registered so the click is answered rather than dropped.
        if (CraftingGamevals.exists(GOLD_COLUMN.someButton)) {
            onIfModalButton(GOLD_COLUMN.someButton) {}
        }
    }
}

/** Slot the highlight box is drawn behind; see [GoldSlot.lastType]. */
private var Player.goldLastType by intVarBit(CraftingConstants.VARBIT_GOLD_LASTTYPE)

fun ProtectedAccess.openGoldCrafting() {
    resetMakeQuantity()
    ifOpenMainModal(CraftingConstants.INTERFACE_GOLD_CRAFTING)
    for (slot in goldSlots) {
        ifSetEvents(slot.component, -1..-1, IfEvent.Op1)
    }

    // When ring bling isn't unlocked, we hide the slayer ring from the interface here.
    if (CraftingGamevals.exists(SLAYER_RING_COMPONENT)) {
        ifSetHide(SLAYER_RING_COMPONENT, hide = !ringBlingUnlocked())
    }
}

/** The gold interface's slayer/eternal ring slot component; gated on the "Ring bling" unlock. */
private val SLAYER_RING_COMPONENT = CraftingConstants.goldComponent("slayer_ring")

/** Bars alone are enough to open: the interface tells the player which moulds it still needs. */
fun ProtectedAccess.hasGoldCraftingBars(): Boolean = inv.contains(CraftingConstants.GOLD_BAR)

/**
 * The recipe a slot would make right now, or null if the player can't make any of them - in which
 * case the client has already cleared the slot's op, so the click cannot arrive in the first place.
 *
 * Only the slayer ring slot has more than one: the eternal ring is listed first and so wins when an
 * eternal gem is held, the same precedence the client applies when it picks the model.
 */
private fun ProtectedAccess.craftableProduct(slot: GoldSlot): CraftingProduct? =
    slot.outputs.firstNotNullOfOrNull { output ->
        goldProducts[output]?.takeIf { hasCraftingMaterials(it) }
    }

/** [beginCraft] re-validates level, mould, and materials, and caps the amount to the inventory. */
private suspend fun ProtectedAccess.craftGoldSlot(slot: GoldSlot) {
    // Defence in depth for the hidden slayer-ring slot: refuse the craft if "Ring bling" isn't owned.
    if (slot.component == SLAYER_RING_COMPONENT && !ringBlingUnlocked()) {
        return
    }
    val product = craftableProduct(slot) ?: return
    player.goldLastType = slot.lastType
    ifClose()
    beginCraft(product, makeQuantity())
}

/** Gold's column is stacked down the left of the interface. */
private val GOLD_COLUMN =
    MakeQuantityColumn(
        component = CraftingConstants::goldComponent,
        countedObj = CraftingConstants.GOLD_BAR,
        stepX = 0,
        stepY = 40,
    )

/**
 * A mould section, and the value its first slot is numbered from. The sections are otherwise the
 * client's business - it owns the mould panels - so a base is all that is left of them here.
 */
internal enum class GoldSection(val lastTypeBase: Int) {
    Rings(1),
    Necklaces(10),
    Amulets(18),
    Bracelets(26),
}

internal data class GoldSlot(
    val section: GoldSection,
    val component: String,
    /** In priority order: the first the player can make is the one the slot shows. */
    val outputs: List<String>,
    /**
     * Value `crafting_gold_item_lasttype` holds when this slot is the player's last pick. Numbered
     * from the section's base by declaration order in [GOLD_SLOTS] - *not* the component id.
     */
    val lastType: Int,
)

/**
 * Each slot's component and the obj(s) a `dbtable.crafting_gold` row makes there; the row carries
 * the level, xp, gem, and mould. A slot with no row (none, currently) simply never becomes a button.
 *
 * Declared in [GoldSlot.lastType] order, which for every section but the rings is display order.
 * Component names don't always match obj names: dragonstone jewellery is `dragon_*`, and amulets are
 * cast unstrung.
 */
private val GOLD_SLOTS: List<GoldSlot> = withLastTypes(
    listOf(
        // Rings number 1-9, and the slayer ring is numbered last despite being drawn eighth. The slayer and eternal rings share the slot (obviously).
        rawSlot(GoldSection.Rings, "gold_ring", "obj.gold_ring"),
        rawSlot(GoldSection.Rings, "sapphire_ring", "obj.sapphire_ring"),
        rawSlot(GoldSection.Rings, "emerald_ring", "obj.emerald_ring"),
        rawSlot(GoldSection.Rings, "ruby_ring", "obj.ruby_ring"),
        rawSlot(GoldSection.Rings, "diamond_ring", "obj.diamond_ring"),
        rawSlot(GoldSection.Rings, "dragon_ring", "obj.dragonstone_ring"),
        rawSlot(GoldSection.Rings, "onyx_ring", "obj.onyx_ring"),
        rawSlot(GoldSection.Rings, "zenyte_ring", "obj.zenyte_ring"),
        rawSlot(GoldSection.Rings, "slayer_ring", "obj.slayer_ring_eternal", "obj.slayer_ring_8"),

        rawSlot(GoldSection.Necklaces, "gold_necklace", "obj.gold_necklace"),
        rawSlot(GoldSection.Necklaces, "sapphire_necklace", "obj.sapphire_necklace"),
        rawSlot(GoldSection.Necklaces, "emerald_necklace", "obj.emerald_necklace"),
        rawSlot(GoldSection.Necklaces, "ruby_necklace", "obj.ruby_necklace"),
        rawSlot(GoldSection.Necklaces, "diamond_necklace", "obj.diamond_necklace"),
        rawSlot(GoldSection.Necklaces, "dragon_necklace", "obj.dragonstone_necklace"),
        rawSlot(GoldSection.Necklaces, "onyx_necklace", "obj.onyx_necklace"),
        rawSlot(GoldSection.Necklaces, "zenyte_necklace", "obj.zenyte_necklace"),

        rawSlot(GoldSection.Amulets, "gold_amulet", "obj.unstrung_gold_amulet"),
        rawSlot(GoldSection.Amulets, "sapphire_amulet", "obj.unstrung_sapphire_amulet"),
        rawSlot(GoldSection.Amulets, "emerald_amulet", "obj.unstrung_emerald_amulet"),
        rawSlot(GoldSection.Amulets, "ruby_amulet", "obj.unstrung_ruby_amulet"),
        rawSlot(GoldSection.Amulets, "diamond_amulet", "obj.unstrung_diamond_amulet"),
        rawSlot(GoldSection.Amulets, "dragon_amulet", "obj.unstrung_dragonstone_amulet"),
        rawSlot(GoldSection.Amulets, "onyx_amulet", "obj.unstrung_onyx_amulet"),
        rawSlot(GoldSection.Amulets, "zenyte_amulet", "obj.unstrung_zenyte_amulet"),

        rawSlot(GoldSection.Bracelets, "gold_bracelet", "obj.jewl_gold_bracelet"),
        rawSlot(GoldSection.Bracelets, "sapphire_bracelet", "obj.jewl_sapphire_bracelet"),
        rawSlot(GoldSection.Bracelets, "emerald_bracelet", "obj.jewl_emerald_bracelet"),
        rawSlot(GoldSection.Bracelets, "ruby_bracelet", "obj.jewl_ruby_bracelet"),
        rawSlot(GoldSection.Bracelets, "diamond_bracelet", "obj.jewl_diamond_bracelet"),
        rawSlot(GoldSection.Bracelets, "dragon_bracelet", "obj.jewl_dragonstone_bracelet"),
        rawSlot(GoldSection.Bracelets, "onyx_bracelet", "obj.jewl_onyx_bracelet"),
        rawSlot(GoldSection.Bracelets, "zenyte_bracelet", "obj.zenyte_bracelet"),
    ),
)

private fun rawSlot(section: GoldSection, component: String, vararg outputs: String): GoldSlot =
    GoldSlot(section, CraftingConstants.goldComponent(component), outputs.toList(), lastType = 0)

private fun withLastTypes(slots: List<GoldSlot>): List<GoldSlot> {
    val next = mutableMapOf<GoldSection, Int>()
    return slots.map { slot ->
        val index = next.getOrDefault(slot.section, 0)
        next[slot.section] = index + 1
        slot.copy(lastType = slot.section.lastTypeBase + index)
    }
}

/** Unresolvable components are dropped rather than thrown, so one bad name can't abort startup. */
internal val goldSlots: List<GoldSlot> by lazy {
    GOLD_SLOTS.filter { CraftingGamevals.exists(it.component) }
}

/** Every gold recipe, keyed by the obj it makes - how a slot finds its recipe. */
internal val goldProducts: Map<String, CraftingProduct> by lazy {
    CraftingGoldRow.all().associate { it.output.internalName to it.toCraftingProduct() }
}
