package org.rsmod.content.skills.crafting

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLoc1
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class CraftingGuildBank : PluginScript() {

    override fun ScriptContext.startup() {
        onOpLoc1(BANK_CHEST) { useBankChest() }
        onOpLoc1(DEPOSIT_BOX) { useDepositBox() }
    }

    private suspend fun ProtectedAccess.useBankChest() {
        arriveDelay()
        if (!requirementMet()) {
            return
        }
        ifOpenMainSidePair(main = "interface.bankmain", side = "interface.bankside")
    }

    private suspend fun ProtectedAccess.useDepositBox() {
        arriveDelay()
        if (!requirementMet()) {
            return
        }
        ifOpenMainModal("interface.bank_depositbox")
    }

    private fun ProtectedAccess.requirementMet(): Boolean {
        if (player.canUseGuildBank()) {
            return true
        }
        mes(REQUIREMENT_DENIED)
        return false
    }

    private companion object {
        private const val BANK_CHEST = "loc.diary_guild_bankchest"
        private const val DEPOSIT_BOX = "loc.diary_guild_deposit_box"
        private const val REQUIREMENT_DENIED = "Only those who have completed the hard Falador Diary or have level 99 Crafting may use this."
    }
}
