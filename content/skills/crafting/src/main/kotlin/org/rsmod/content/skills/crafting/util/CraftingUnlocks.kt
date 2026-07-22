package org.rsmod.content.skills.crafting.util

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.game.entity.Player

/**
 * The non-skill gates a few crafting recipes have: quest completion and Slayer reward unlocks.
 *
 * This should only exist until a centralized way to handle quest completion is created.
 */
private var Player.porcineProgress by intVarBit(CraftingConstants.VARBIT_PORCINE)
private var Player.slayerHelmUnlocked by intVarBit(CraftingConstants.VARBIT_SLAYER_HELM_UNLOCKED)
private var Player.slayerRingUnlocked by intVarBit(CraftingConstants.VARBIT_SLAYER_RING_UNLOCKED)

/**
 * True once A Porcine of Interest is complete, which is what adds reinforced goggles to the slayer
 * helmet recipe.
 *
 * The varbit is read through [CraftingGamevals.exists] first: referencing a varbit that isn't in
 * the cache throws, and a missing quest varbit should mean "we don't know" rather than crashing a
 * craft. When it can't be read we fall back to [CraftingConstants.PORCINE_COMPLETE_FALLBACK].
 */
fun ProtectedAccess.porcineOfInterestComplete(): Boolean {
    if (!CraftingGamevals.exists(CraftingConstants.VARBIT_PORCINE)) {
        return CraftingConstants.PORCINE_COMPLETE_FALLBACK
    }
    return player.porcineProgress >= CraftingConstants.PORCINE_COMPLETE_VALUE
}

/**
 * True once the "Malevolent masquerade" Slayer reward is unlocked, without which the slayer helmet
 * cannot be assembled. Driven by the `slayer_helm_unlocked` varbit (1 = unlocked).
 */
fun ProtectedAccess.malevolentMasqueradeUnlocked(): Boolean =
    isVarbitSet(CraftingConstants.VARBIT_SLAYER_HELM_UNLOCKED) { player.slayerHelmUnlocked }

/**
 * True once the "Ring bling" Slayer reward is unlocked, without which the slayer ring can't be
 * crafted at a furnace. Driven by the `slayer_ring_unlocked` varbit (1 = unlocked).
 */
fun ProtectedAccess.ringBlingUnlocked(): Boolean =
    isVarbitSet(CraftingConstants.VARBIT_SLAYER_RING_UNLOCKED) { player.slayerRingUnlocked }

/**
 * Reads an unlock varbit as a boolean (>= 1 is unlocked). The varbit is checked with
 * [CraftingGamevals.exists] first - referencing one that isn't in the cache throws, and a missing
 * unlock varbit should read as "not unlocked" rather than crash a craft.
 */
private inline fun ProtectedAccess.isVarbitSet(gameval: String, value: () -> Int): Boolean =
    CraftingGamevals.exists(gameval) && value() >= 1

