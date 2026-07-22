package org.rsmod.content.skills.crafting.scripts

import com.github.michaelbull.logging.InlineLogger
import dev.openrune.ServerCacheManager
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import kotlin.random.Random
import org.rsmod.api.combat.commons.magic.MagicSpell
import org.rsmod.api.combat.manager.MagicRuneManager
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.content.skills.crafting.util.CraftingConstants
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Superglass Make (Lunar spellbook): converts every bucket of sand paired with an alkali - soda
 * ash, seaweed, or giant seaweed (worth three units) - into molten glass in a single cast.
 * Buckets are consumed entirely (unlike furnace smelting), each pair has a
 * [CraftingConstants.SUPERGLASS_BONUS_CHANCE] chance of a bonus glass (~1.3x average), and each
 * glass grants [CraftingConstants.SUPERGLASS_XP_PER_GLASS] Crafting xp plus the spell's Magic xp.
 * Spell access (level, spellbook, runes) is enforced by [MagicRuneManager.attemptCast], keeping
 * requirements consistent with every other spell.
 */
class SuperglassMakeScript
@Inject
constructor(
    private val spells: MagicSpellRegistry,
    private val runes: MagicRuneManager,
) : PluginScript() {

    private val logger = InlineLogger()

    override fun ScriptContext.startup() {
        val spell = resolveSpell()
        if (spell == null) {
            logger.warn {
                "Superglass Make spell obj could not be resolved " +
                    "(${CraftingConstants.SPELL_SUPERGLASS_MAKE}); spell disabled."
            }
            return
        }
        onIfOverlayButton(spell.component) { castSuperglassMake(spell) }
    }

    private fun resolveSpell(): MagicSpell? {
        val type =
            ServerCacheManager.getItem(
                CraftingConstants.SPELL_SUPERGLASS_MAKE.asRSCM(RSCMType.OBJ)
            ) ?: return null
        return spells.getObjSpell(type)
    }

    private suspend fun ProtectedAccess.castSuperglassMake(spell: MagicSpell) {
        val sand = inv.count(CraftingConstants.BUCKET_OF_SAND)
        val sodaAsh = inv.count(CraftingConstants.SODA_ASH)
        val seaweed = inv.count(CraftingConstants.SEAWEED)
        val giantSeaweed = inv.count(CraftingConstants.GIANT_SEAWEED)

        val alkaliUnits = sodaAsh + seaweed + giantSeaweed * GIANT_SEAWEED_UNITS
        val pairs = minOf(sand, alkaliUnits)
        if (pairs <= 0) {
            mes("You need buckets of sand and soda ash or seaweed to cast this spell.")
            return
        }

        // Validates Magic level, spellbook, and runes; consumes the runes on success.
        if (runes.attemptCast(player, spell).isFailure()) {
            return
        }

        anim(CraftingConstants.SUPERGLASS_ANIM)
        soundSynth(CraftingConstants.SUPERGLASS_SOUND)

        // Consume the sand (buckets included) and enough alkali to cover every pair, preferring
        // soda ash, then seaweed, then giant seaweed (a giant seaweed is spent whole even if
        // fewer than three of its units are needed - matching authentic behaviour).
        invDel(inv, CraftingConstants.BUCKET_OF_SAND, pairs)

        var remaining = pairs
        val useSodaAsh = minOf(remaining, sodaAsh)
        if (useSodaAsh > 0) {
            invDel(inv, CraftingConstants.SODA_ASH, useSodaAsh)
            remaining -= useSodaAsh
        }
        val useSeaweed = minOf(remaining, seaweed)
        if (useSeaweed > 0) {
            invDel(inv, CraftingConstants.SEAWEED, useSeaweed)
            remaining -= useSeaweed
        }
        if (remaining > 0) {
            val useGiant = (remaining + GIANT_SEAWEED_UNITS - 1) / GIANT_SEAWEED_UNITS
            invDel(inv, CraftingConstants.GIANT_SEAWEED, useGiant)
        }

        var glassMade = pairs
        repeat(pairs) {
            if (Random.nextDouble() < CraftingConstants.SUPERGLASS_BONUS_CHANCE) {
                glassMade++
            }
        }

        // Bonus glass can exceed the slots freed by consumption; any overflow is lost.
        val glassAdded = minOf(glassMade, inv.freeSpace())
        if (glassAdded > 0) {
            invAdd(inv, CraftingConstants.MOLTEN_GLASS, glassAdded)
        }

        statAdvance(
            CraftingConstants.STAT_CRAFTING,
            CraftingConstants.SUPERGLASS_XP_PER_GLASS * glassAdded,
        )
        statAdvance("stat.magic", spell.castXp)
        mes("You cast Superglass Make, fusing the sand and alkali into molten glass.")
    }

    private companion object {
        const val GIANT_SEAWEED_UNITS = 3
    }
}
