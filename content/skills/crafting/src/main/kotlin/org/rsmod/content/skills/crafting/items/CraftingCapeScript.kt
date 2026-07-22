package org.rsmod.content.skills.crafting.items

import org.rsmod.api.player.hook.TeleportType
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpHeld3
import org.rsmod.api.script.onOpWorn3
import org.rsmod.content.skills.crafting.util.CraftingGamevals
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Crafting cape (and its trimmed variant): teleports to the Crafting Guild.
 *
 * Op mapping (see HeldOpScript / WornOpScript):
 * - Inventory "Teleport": `if_buttonx` op 4 -> held op 3 -> [onOpHeld3].
 * - Worn "Teleport": `if_buttonx` op 3 -> worn op 3 -> [onOpWorn3].
 *
 * The teleport animation plays first; the move happens once it finishes ([TELEPORT_DELAY] ticks).
 *
 * The skill-boost effect of the cape is intentionally out of scope here.
 */
class CraftingCapeScript : PluginScript() {

    override fun ScriptContext.startup() {
        for (cape in CAPES) {
            onOpHeld3(cape) { teleportToGuild() }
            onOpWorn3(cape) { teleportToGuild() }
        }
    }

    private suspend fun ProtectedAccess.teleportToGuild() {
        CraftingGamevals.optional(TELEPORT_ANIM)?.let { anim(it) }
        spotanim(CraftingGamevals.optional(TELEPORT_SPOTANIM))
        delay(TELEPORT_DELAY)
        // Standard teleport type: routed through the same wilderness check as spell/tablet
        // teleports, so it's blocked above level 20 Wilderness.
        telejump(GUILD_TELEPORT, TeleportType.Standard)
    }

    private companion object {
        private val CAPES = listOf("obj.skillcape_crafting", "obj.skillcape_crafting_trimmed")

        /** Standard skillcape teleport target inside the guild (`::tele 2931 3286 0`). */
        private val GUILD_TELEPORT = CoordGrid(2931, 3286, 0)

        /** Ticks the teleport animation plays before the player is moved. */
        private const val TELEPORT_DELAY = 3

        // Teleport animation + graphic gamevals - placeholders for the standard "up" teleport.
        // Populate with the correct gameval names; unresolved names are skipped (see
        // CraftingGamevals.optional), so the teleport still works without the effect until then.
        private const val TELEPORT_ANIM = "seq.skillcape_teleport"
        private const val TELEPORT_SPOTANIM = "spotanim.skillcape_teleport"
    }
}
