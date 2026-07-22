package org.rsmod.content.skills.crafting.scripts

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLocCategoryU
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.table.crafting.CraftingFacilitiesRow
import org.rsmod.content.skills.crafting.CraftingProduct
import org.rsmod.content.skills.crafting.CraftingSection
import org.rsmod.content.skills.crafting.beginCraft
import org.rsmod.content.skills.crafting.selectCraftingProduct
import org.rsmod.content.skills.crafting.toCraftingProduct
import org.rsmod.content.skills.crafting.util.CraftingConstants
import org.rsmod.content.skills.crafting.util.CraftingGamevals
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Facility crafting: every recipe of the `crafting_facilities` table, one [Facility] per way of
 * crafting at a loc. A facility is its locs plus its section's products; clicking one opens the
 * selection prompt with the loc passed along so the machine animates alongside the player (the
 * section's `locAnim`).
 *
 * Two facilities don't cover their whole section's recipes on a plain click:
 * - The loom carries four always-shown default recipes; every other weaving recipe is gated by
 *   its material being held ([CraftingProduct.requiresMaterialsToShow]).
 * - The pottery oven carries three always-shown default recipes (pot / pie dish / bowl); every
 *   other firing recipe is gated by its unfired material being held. And unlike the wheel, the
 *   oven also handles item-on-loc: dragging an unfired item onto the oven opens *just that
 *   recipe's* menu - or fires it straight through with no prompt when only one of that item is
 *   held (see [openForInputOnOven]).
 *
 * The pottery wheel has its own item-on-loc quirk: hard dry clay on the wheel is a dead end, so
 * that click prompts the softening advice with the clay's model rather than opening any menu.
 *
 * Glass smelting is the odd one out: the furnace belongs to the Smithing module's "Smelt" op, so
 * it registers item-on-loc against the shared furnace *category* instead of an op - using a
 * bucket of sand or soda ash on any furnace opens the smelting menu, and the two never compete.
 * (The jewellery interfaces reach the furnace the same way; see JewelleryScript.)
 */
class FacilityCraftingScript : PluginScript() {

    private val rowsBySection: Map<String, List<CraftingFacilitiesRow>> by lazy {
        CraftingFacilitiesRow.all().groupBy { it.section }
    }

    /** One crafting facility: the locs it exists as, and the products a given loc offers. */
    private class Facility(
        val section: CraftingSection,
        val locs: List<String>,
        val products: (loc: String) -> List<CraftingProduct>,
    )

    /**
     * The facility list. Recipe data lives on the table rows and section defaults in
     * [CraftingSection]; anything here is a genuinely per-facility quirk - the spinning wheel's
     * two animation variants and the loom's fixed default menu.
     */
    private fun facilities(): List<Facility> {
        // Two spinning product lists, one per player-animation variant (60- vs 90-frame wheels).
        val spinning60 = spinningProducts(CraftingConstants.ANIM_SPINNING_60)
        val spinning90 = spinningProducts(CraftingConstants.ANIM_SPINNING_90)

        return listOf(
            Facility(CraftingSection.SPINNING, CraftingConstants.SPINNING_WHEELS) { loc ->
                if (loc in CraftingConstants.SPINNING_WHEELS_90) spinning90 else spinning60
            },
            Facility(CraftingSection.WEAVING, CraftingConstants.LOOMS) { weavingProducts },
            Facility(CraftingSection.POTTERY_SHAPING, CraftingConstants.POTTERY_WHEELS) { shapingProducts },
            Facility(CraftingSection.POTTERY_FIRING, CraftingConstants.POTTERY_OVENS) { firingProducts },
        )
    }

    /**
     * The spinning menu, like the loom, always lists its standard recipes in a fixed order (see
     * [SPINNING_DEFAULT_RECIPES]); every other recipe (the yarns, golden wool, ...) is hidden until
     * its material is in the inventory via `requiresMaterialsToShow`. When the player holds nothing
     * spinnable at all, SPINNING.emptyMenuMessage reports it instead of opening the menu. The [anim]
     * differs per wheel model (60- vs 90-frame).
     */
    private fun spinningProducts(anim: String): List<CraftingProduct> =
        products(CraftingSection.SPINNING) { row ->
                row.toCraftingProduct(
                    anim = anim,
                    requiresMaterialsToShow =
                        row.output.first().internalName !in SPINNING_DEFAULT_RECIPES,
                )
            }
            // Stable sort: the standard recipes lead, in menu order; gated recipes keep table order.
            .sortedBy { product ->
                val index = SPINNING_DEFAULT_RECIPES.indexOf(product.output)
                if (index == -1) SPINNING_DEFAULT_RECIPES.size else index
            }

    /**
     * The loom lists only the recipes the player can currently weave: every recipe is material-
     * gated via `requiresMaterialsToShow`, so a recipe appears only while its material is in the
     * inventory. When none can be woven, WEAVING.emptyMenuMessage answers instead of opening an
     * empty menu.
     */
    private val weavingProducts: List<CraftingProduct> by lazy {
        products(CraftingSection.WEAVING) {
            it.toCraftingProduct(requiresMaterialsToShow = true)
        }
    }

    private val shapingProducts: List<CraftingProduct> by lazy {
        // All wheel recipes take soft clay; requiring materials to show turns the "no soft clay"
        // case into an empty shown list, which POTTERY_SHAPING.emptyMenuMessage answers with the
        // dialogue "You don't have anything suitable to craft with." instead of an empty menu.
        products(CraftingSection.POTTERY_SHAPING) {
            it.toCraftingProduct(requiresMaterialsToShow = true)
        }
    }

    /**
     * Same policy as the loom - three always-shown default recipes (pot / pie dish / bowl); every
     * other firing recipe requires its unfired material in the inventory.
     */
    private val firingProducts: List<CraftingProduct> by lazy {
        products(CraftingSection.POTTERY_FIRING) { row ->
                row.toCraftingProduct(
                    requiresMaterialsToShow = row.output.first().internalName !in OVEN_DEFAULT_RECIPES,
                )
            }
            .sortedBy { product ->
                val index = OVEN_DEFAULT_RECIPES.indexOf(product.output)
                if (index == -1) OVEN_DEFAULT_RECIPES.size else index
            }
    }

    /**
     * Furnace smelting returns the sand's bucket as a byproduct; that empty bucket is the glass
     * recipe's second `output(...)` in the table, so it flows through automatically.
     */
    private val smeltingProducts: List<CraftingProduct> by lazy {
        products(CraftingSection.GLASS_SMELTING) { it.toCraftingProduct() }
    }

    private fun products(
        section: CraftingSection,
        adapt: (CraftingFacilitiesRow) -> CraftingProduct,
    ): List<CraftingProduct> = rowsBySection[section.id].orEmpty().map(adapt)

    override fun ScriptContext.startup() {
        for (facility in facilities()) {
            for (loc in CraftingGamevals.filterResolvable(facility.locs)) {
                val products = facility.products(loc)
                onOpLoc1(loc) { selectCraftingProduct(facility.section, products, facility = it.loc) }
                onOpLoc2(loc) { selectCraftingProduct(facility.section, products, facility = it.loc) }
            }
        }

        // Pottery oven: item-on-loc for each firing recipe. The click resolves to *just* that
        // recipe (not the section's default menu), and when the player only holds one of the
        // dragged item, we skip the quantity prompt and fire it straight through - matching OSRS.
        for (loc in CraftingGamevals.filterResolvable(CraftingConstants.POTTERY_OVENS)) {
            for (product in firingProducts) {
                val input = product.inputs.firstOrNull()?.internal ?: continue
                if (!CraftingGamevals.exists(input)) continue
                onOpLocU(loc, input) { openForInputOnOven(product, facility = it.loc) }
            }
        }

        // Pottery wheel: dry clay is a dead end (softening prompt); soft clay opens the menu, the
        // same as a plain click. Both go through onOpLocU so they don't collide with the shared
        // held-crafting registrations for softclay (which handle needlework thread, etc).
        for (loc in CraftingGamevals.filterResolvable(CraftingConstants.POTTERY_WHEELS)) {
            onOpLocU(loc, CraftingConstants.CLAY) { promptDryClay() }
            onOpLocU(loc, CraftingConstants.SOFT_CLAY) {
                selectCraftingProduct(CraftingSection.POTTERY_SHAPING, shapingProducts, facility = it.loc)
            }
        }

        // Glass smelting: item-on-furnace-category, per the class doc.
        for (item in listOf(CraftingConstants.BUCKET_OF_SAND, CraftingConstants.SODA_ASH)) {
            onOpLocCategoryU(CraftingConstants.CATEGORY_FURNACE, item) {
                selectCraftingProduct(CraftingSection.GLASS_SMELTING, smeltingProducts)
            }
        }
    }

    /**
     * Handles an unfired item dragged onto the oven. The recipe is fixed to that item's; when the
     * player holds exactly one of it, the "how many" prompt is redundant and we craft directly
     * (this is what OSRS does for item-on-oven with a single input).
     */
    private suspend fun ProtectedAccess.openForInputOnOven(
        product: CraftingProduct,
        facility: BoundLocInfo,
    ) {
        val input = product.inputs.firstOrNull()?.internal ?: return
        if (inv.count(input) == 1) {
            beginCraft(product, amount = 1, facility = facility)
        } else {
            selectCraftingProduct(product.section, listOf(product), facility = facility)
        }
    }

    private suspend fun ProtectedAccess.promptDryClay() {
        objbox(
            CraftingConstants.CLAY,
            "This clay is too hard to craft.<br>You'll need to soften it with some water.",
        )
    }
}

private val SPINNING_DEFAULT_RECIPES: List<String> = listOf(
    "obj.ball_of_wool",
    "obj.bow_string",
    "obj.rope",
    "obj.xbows_crossbow_string",
    "obj.magic_string",
)

private val OVEN_DEFAULT_RECIPES: List<String> = listOf(
    "obj.pot_empty",
    "obj.piedish",
    "obj.bowl_empty",
)
