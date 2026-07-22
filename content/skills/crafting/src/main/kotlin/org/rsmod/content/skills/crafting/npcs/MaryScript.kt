package org.rsmod.content.skills.crafting.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpcU
import org.rsmod.content.skills.crafting.interfaces.TannerPrices
import org.rsmod.content.skills.crafting.interfaces.openTanner
import org.rsmod.content.skills.crafting.interfaces.tannableHideObjs
import org.rsmod.content.skills.crafting.interfaces.tannedLeatherObjs
import org.rsmod.content.skills.crafting.util.CraftingConstants
import org.rsmod.content.skills.crafting.util.CraftingGamevals
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**  Mary, on the farm north of Hosidius. */
class MaryScript : PluginScript() {

    override fun ScriptContext.startup() {
        if (CraftingGamevals.exists(CraftingConstants.TANNER_MARY_PREQUEST)) {
            onOpNpc1(CraftingConstants.TANNER_MARY_PREQUEST) { preQuestChat(it.npc) }
        }
        if (!CraftingGamevals.exists(CraftingConstants.TANNER_MARY)) {
            return
        }
        onOpNpc1(CraftingConstants.TANNER_MARY) { greet(it.npc) }
        onOpNpc3(CraftingConstants.TANNER_MARY) { openTanner(MARY_PRICES) }
        onOpNpcU(CraftingConstants.TANNER_MARY) { event ->
            usedItemOnMary(event.npc, event.objType?.internalName)
        }
    }

    /** The pre-quest Mary: standard dialogue, no options, no tanning. */
    private suspend fun ProtectedAccess.preQuestChat(npc: Npc) {
        startDialogue(npc) {
            chatPlayer(quiz, "Hello there. Is this your home?")
            chatNpc(neutral, "It is. What brings you here?")
            chatPlayer(neutral, "I'm just looking around.")
            chatNpc(neutral, "Well you won't find anything too exciting here I'm afraid. Just farming.")
            chatPlayer(happy, "Well you have fun with it.")
        }
    }

    private suspend fun ProtectedAccess.greet(npc: Npc) {
        val firstMeeting = firstPostQuestMeeting()
        startDialogue(npc) {
            if (firstMeeting) {
                gettingAheadAftermath()
            } else {
                returningCustomer()
            }
        }
    }

    /** The one-off conversation straight after Getting Ahead, which unlocks her tanning. */
    private suspend fun Dialogue.gettingAheadAftermath() {
        chatNpc(neutral, "Well I have to say that mounted head looks awful.")
        chatPlayer(neutral, "Sorry.")
        chatNpc(
            happy,
            "Not to worry. If it keeps Gordon happy, so be it. Anyway, thank you for keeping our " +
                "farm safe. With the beast dealt with, I've been able to get back to tanning again.",
        )
        access.unlockTanning()

        val tan = choice2(
            "Could you tan something for me?", true,
            "Happy to have helped. All the best.", false,
        )
        if (tan) {
            tanRequest()
        } else {
            chatPlayer(happy, "Happy to have helped. All the best.")
        }
    }

    /** Every conversation after the first. */
    private suspend fun Dialogue.returningCustomer() {
        chatNpc(happy, "Good to see you again! Anything I can do for you?")
        val tan = choice2("Could you tan something for me?", true, "I'm good.", false)
        if (tan) {
            tanRequest()
        } else {
            chatPlayer(neutral, "I'm good.")
        }
    }

    private suspend fun Dialogue.tanRequest() {
        chatPlayer(quiz, "Could you tan something for me?")
        chatNpc(happy, "Of course.")
        access.openTanner(MARY_PRICES)
    }

    private suspend fun ProtectedAccess.usedItemOnMary(npc: Npc, obj: String?) {
        when {
            obj != null && obj in tannableHideObjs -> openTanner(MARY_PRICES)
            obj != null && obj in tannedLeatherObjs ->
                startDialogue(npc) { chatNpc(neutral, "Er... I have no use for that, I make the stuff!") }
            else -> startDialogue(npc) { chatNpc(neutral, "Er... Thanks, but no thanks!") }
        }
    }

    /**
     * True only for the first post-quest conversation. If the unlock varbit isn't in the cache we
     * can't remember that the speech has been given, so she falls back to the returning-customer
     * greeting rather than repeating the aftermath speech on every click.
     */
    private fun ProtectedAccess.firstPostQuestMeeting(): Boolean =
        unlockVarbitExists && !player.maryTanningUnlocked

    private fun ProtectedAccess.unlockTanning() {
        if (unlockVarbitExists) {
            player.maryTanningUnlocked = true
        }
    }
}

private val unlockVarbitExists: Boolean by lazy {
    CraftingGamevals.exists(CraftingConstants.VARBIT_MARY_TANNING_UNLOCKED)
}

private var Player.maryTanningUnlocked by boolVarBit(CraftingConstants.VARBIT_MARY_TANNING_UNLOCKED)

internal val MARY_PRICES: TannerPrices = TannerPrices.of(
    soft = 1,
    hard = 3,
    snakeskin = 15,
    swampSnakeskin = 20,
    dragonhide = 20,
)
