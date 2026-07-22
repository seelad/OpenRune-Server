package org.rsmod.content.skills.crafting.items

import dev.openrune.util.Wearpos
import org.rsmod.api.player.output.ChatType
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.worn.WornUnequipResult
import org.rsmod.api.script.onOpHeld3
import org.rsmod.api.script.onOpWorn4
import org.rsmod.game.inv.isType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Imcando hammer: swaps freely between the standard hammer (worn in the right hand) and its off-hand
 * variant (worn in the left hand), in either direction, whether the hammer is in the inventory or
 * worn.
 *
 * Op mapping (see HeldOpScript / WornOpScript):
 * - Inventory swap: `if_buttonx` op 4 -> held op 3 -> [onOpHeld3]. A plain one-for-one item swap.
 * - Worn swap: `if_buttonx` op 4 -> worn op 4 -> [onOpWorn4]. The variant is unequipped, swapped,
 *   then re-equipped into the other hand if that slot is free; if it is occupied the swapped variant
 *   is left in the inventory instead (we don't displace whatever is equipped there). If neither is
 *   possible the swap is voided with a message.
 *
 * Note: the worn path unequips into the inventory first (to reuse the tested equip/unequip pipeline,
 * which keeps equipment bonuses, appearance and events correct), so it needs one free inventory slot
 * even when the destination hand is empty. With a completely full inventory a worn swap into an
 * empty hand reports "You don't have any inventory space." rather than moving in place.
 */
class ImcandoHammerScript : PluginScript() {

    override fun ScriptContext.startup() {
        // Inventory swaps (both directions).
        onOpHeld3(STANDARD) { invReplace(inv, STANDARD, 1, OFFHAND) }
        onOpHeld3(OFFHAND) { invReplace(inv, OFFHAND, 1, STANDARD) }

        // Worn swaps (both directions).
        onOpWorn4(STANDARD) { swapWorn(it.slot, STANDARD, OFFHAND, Wearpos.LeftHand) }
        onOpWorn4(OFFHAND) { swapWorn(it.slot, OFFHAND, STANDARD, Wearpos.RightHand) }
    }

    /**
     * Swaps a worn [fromType] for [toType], placing the result in [targetWearpos] when that slot is
     * free, otherwise the inventory, otherwise voiding the operation.
     */
    private fun ProtectedAccess.swapWorn(
        fromSlot: Int,
        fromType: String,
        toType: String,
        targetWearpos: Wearpos,
    ) {
        if (worn[fromSlot]?.isType(fromType) != true) {
            return
        }
        // The destination hand is a different slot from the one being unequipped, so its occupancy
        // is unaffected by the unequip below - safe to read up front.
        val targetFree = worn[targetWearpos.slot] == null

        // Unequip the current variant into the inventory (keeps bonuses/appearance/events correct).
        if (wornUnequip(fromSlot) is WornUnequipResult.Fail) {
            mes("You don't have any inventory space.", ChatType.Spam)
            return
        }
        // Swap it for the other variant, still in the inventory.
        invReplace(inv, fromType, 1, toType)

        // Re-equip into the destination hand only if it's free; if it's occupied, leave the swapped
        // variant in the inventory rather than displacing whatever is worn there.
        if (targetFree) {
            val slot = inv.indices.firstOrNull { inv[it]?.isType(toType) == true } ?: return
            invEquip(slot)
        }
    }

    private companion object {
        private const val STANDARD = "obj.imcando_hammer"
        private const val OFFHAND = "obj.imcando_hammer_offhand"
    }
}
