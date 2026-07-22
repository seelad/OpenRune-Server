package org.rsmod.content.skills.crafting

import dev.openrune.ServerCacheManager
import org.rsmod.api.player.back
import org.rsmod.api.player.stat.baseCraftingLvl
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj

private val guildAprons = setOf("obj.brown_apron", "obj.golden_apron")

private val craftingSkillcapes = setOf("obj.skillcape_crafting", "obj.skillcape_crafting_trimmed")
private val maxSkillcapes = setOf("obj.skillcape_max", "obj.skillcape_max_firecape", "obj.skillcape_max_saradomin", "obj.skillcape_max_zamorak"
    , "obj.skillcape_max_guthix", "obj.skillcape_max_anma", "obj.skillcape_max_worn", "obj.skillcape_max_ardy", "obj.skillcape_max_infernalcape"
    , "obj.skillcape_max_saradomin2", "obj.skillcape_max_zamorak2", "obj.skillcape_max_guthix2", "obj.skillcape_max_assembler"
    , "obj.skillcape_max_infernalcape_trouver", "obj.skillcape_max_firecape_trouver", "obj.skillcape_max_assembler_trouver", "obj.skillcape_max_saradomin2_trouver"
    , "obj.skillcape_max_zamorak2_trouver", "obj.skillcape_max_guthix2_trouver", "obj.skillcape_max_mythical", "obj.skillcape_max_assembler_masori"
    , "obj.skillcape_max_assembler_masori_trouver", "obj.skillcape_max_dizanas", "obj.skillcape_max_dizanas_trouver")


private fun InvObj?.hasItemContent(content: String): Boolean {
    val obj = this ?: return false
    val type = ServerCacheManager.getItem(obj.id) ?: return false
    return type.isContentType(content)
}

private fun Player.wearingMaxCape(): Boolean = maxSkillcapes.any { it in worn }
internal fun Player.wearingCraftingSkillcape(): Boolean = craftingSkillcapes.any { it in worn }
internal fun Player.wearingCraftingApron(): Boolean = guildAprons.any { it in worn }

internal fun Player.ownsCraftingSkillcape(): Boolean = craftingSkillcapes.any { it in inv || it in worn }
internal fun Player.ownsCraftingHood(): Boolean = "obj.skillcape_crafting_hood" in inv || "obj.skillcape_crafting_hood" in worn

internal fun Player.hasGuildEntryOutfit(): Boolean = wearingCraftingApron() || wearingCraftingSkillcape() || wearingMaxCape()

internal fun Player.canUseGuildBank(): Boolean = baseCraftingLvl >= 99 || hasFaladorHardDiary()

// TODO: replace with a real hard Falador Diary completion check once diaries are implemented.
internal fun Player.hasFaladorHardDiary(): Boolean = false
