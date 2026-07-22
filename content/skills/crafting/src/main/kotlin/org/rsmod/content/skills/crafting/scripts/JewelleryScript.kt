package org.rsmod.content.skills.crafting.scripts

import org.rsmod.api.script.onOpLocCategoryU
import org.rsmod.content.skills.crafting.interfaces.openGoldCrafting
import org.rsmod.content.skills.crafting.interfaces.openSilverCrafting
import org.rsmod.content.skills.crafting.util.CraftingConstants
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Jewellery: using a gold or silver bar on a furnace opens that bar's crafting interface, which owns
 * its own rendering, recipes, and clicks (`interfaces/GoldCraftingInterface`,
 * `interfaces/SilverCraftingInterface`).
 *
 * Registered against the furnace *category* via item-on-loc, so it never competes with the Smithing
 * module's "Smelt" OpLoc handler - which is the other way into both interfaces, when the smelt op is
 * clicked with no smeltable ore.
 */
class JewelleryScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLocCategoryU(CraftingConstants.CATEGORY_FURNACE, CraftingConstants.GOLD_BAR) {
            openGoldCrafting()
        }
        onOpLocCategoryU(CraftingConstants.CATEGORY_FURNACE, CraftingConstants.SILVER_BAR) {
            openSilverCrafting()
        }
    }
}
