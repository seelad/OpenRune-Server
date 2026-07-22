package dev.openrune.tables.skills

import dev.openrune.definition.constants.ConstantProvider
import dev.openrune.definition.util.VarType
import dev.openrune.tables.production.ProductionColumns
import dev.openrune.tables.production.ProductionTableRowScope
import dev.openrune.tables.production.ProductionTableScope
import dev.openrune.tables.production.productionTable

/**
 * Columns that aren't specified defer to the section default.
 *
 * Optional columns:
 * - `success_low`/`success_high`: chance numerators out of 256 at levels 1 and 99 (see MathSkillUtiils.computeSkillingSuccess.
 *   Higher above 256 means success is guaranteed before 99). When omitted, the craft can never fail.
 * - `fail_xp`/`fail_item`: experience (tenths) and obj produced on a failed roll.
 * - `anim`/`spotanim`: The (spot) animation gameval.
 * - `ticks`: Craft time in ticks.
 * - `triggers`: for combines, the inputs that may be clicked on one another to start the craft;
 *   inputs left out (the bone staff's chaos runes) are consumed but inert to clicks.
 * - `xp_extra`: Experience paid out to any second skill (see noxious halberd for an example).
 * - `cost`: coin cost per item (tanning only).
 * - `tool`/`sound`/`message`/`action_name`: Recipe overrides of the section's tool list, craft sound, success message,
 * and level-gate phrase. `{input}`/`{output}` will interpolate.
 */
object Crafting {

    /** Section the recipe belongs to. */
    const val COL_SECTION = 7

    /** Mould obj required in inventory. */
    const val COL_MOULD = 8

    const val COL_SUCCESS_LOW = 8
    const val COL_SUCCESS_HIGH = 9

    const val COL_TICKS = 10

    const val COL_FAIL_XP = 10
    const val COL_FAIL_ITEM = 11
    const val COL_ANIM = 12
    const val COL_SPOTANIM = 13
    const val COL_TRIGGERS = 14
    const val COL_XP_EXTRA = 15

    /** Coin cost per item (tanning). */
    const val COL_COST = 16

    /** An extra tool the recipe requires beyond any listed in it's section. */
    const val COL_TOOL = 17

    const val COL_SOUND = 18

    /** Success message override. {output} and {input} get interpolated in. */
    const val COL_MESSAGE = 19

    const val COL_ACTION_NAME = 20


    fun facilities() = craftingTable(
        "dbtable.crafting_facilities",
        extraColumns = {
            column("section", COL_SECTION, VarType.STRING)
            column("success_low", COL_SUCCESS_LOW, VarType.INT)
            column("success_high", COL_SUCCESS_HIGH, VarType.INT)
            column("ticks", COL_TICKS, VarType.INT)
        },
    ) {
        section("Spinning", category = "Spin") {
            row("dbrow.crafting_spin_ball_of_wool") {
                production {
                    input("obj.wool")
                    statReq("stat.crafting", 1)
                    xp(25)
                    output("obj.ball_of_wool")
                }
            }
            row("dbrow.crafting_spin_bow_string") {
                production {
                    input("obj.flax")
                    statReq("stat.crafting", 10)
                    xp(150)
                    output("obj.bow_string")
                }
            }
            row("dbrow.crafting_spin_crossbow_string") {
                production {
                    input("obj.xbows_sinew")
                    statReq("stat.crafting", 10)
                    xp(150)
                    output("obj.xbows_crossbow_string")
                }
            }
            row("dbrow.crafting_spin_crossbow_string_roots") {
                production {
                    input("obj.oak_roots")
                    statReq("stat.crafting", 10)
                    xp(150)
                    output("obj.xbows_crossbow_string")
                }
            }
            row("dbrow.crafting_spin_magic_string") {
                production {
                    input("obj.magic_roots")
                    statReq("stat.crafting", 19)
                    xp(300)
                    output("obj.magic_string")
                }
            }
            row("dbrow.crafting_spin_rope") {
                production {
                    input("obj.yak_hair")
                    statReq("stat.crafting", 30)
                    xp(250)
                    output("obj.rope")
                }
            }
            row("dbrow.crafting_spin_linen_yarn") {
                production {
                    input("obj.flax")
                    statReq("stat.crafting", 12)
                    xp(160)
                    output("obj.linen_yarn")
                }
            }
            row("dbrow.crafting_spin_hemp_yarn") {
                production {
                    input("obj.hemp")
                    statReq("stat.crafting", 39)
                    xp(600)
                    output("obj.hemp_yarn")
                }
            }
            row("dbrow.crafting_spin_cotton_yarn") {
                production {
                    input("obj.cotton_boll")
                    statReq("stat.crafting", 73)
                    xp(1050)
                    output("obj.cotton_yarn")
                }
            }
        }
        section("Weaving", category = "Weave") {
            row("dbrow.crafting_weave_strip_of_cloth") {
                production {
                    input("obj.ball_of_wool", 4)
                    statReq("stat.crafting", 10)
                    xp(120)
                    output("obj.regicide_cloth")
                }
            }
            row("dbrow.crafting_weave_bolt_of_linen") {
                production {
                    input("obj.linen_yarn", 2)
                    statReq("stat.crafting", 12)
                    xp(200)
                    output("obj.bolt_of_linen")
                }
            }
            row("dbrow.crafting_weave_empty_sack") {
                production {
                    input("obj.jute_fibre", 4)
                    statReq("stat.crafting", 21)
                    xp(380)
                    output("obj.sack_empty")
                }
            }
            row("dbrow.crafting_weave_drift_net") {
                production {
                    input("obj.jute_fibre", 2)
                    statReq("stat.crafting", 26)
                    xp(550)
                    output("obj.fossil_drift_net")
                }
            }
            row("dbrow.crafting_weave_basket") {
                production {
                    input("obj.willow_branch", 6)
                    statReq("stat.crafting", 36)
                    xp(560)
                    output("obj.basket_empty")
                }
                column(COL_TICKS, 4)
            }
            row("dbrow.crafting_weave_bolt_of_canvas") {
                production {
                    input("obj.hemp_yarn", 2)
                    statReq("stat.crafting", 39)
                    xp(750)
                    output("obj.bolt_of_canvas")
                }
            }
            row("dbrow.crafting_weave_bolt_of_cotton") {
                production {
                    input("obj.cotton_yarn", 2)
                    statReq("stat.crafting", 73)
                    xp(1320)
                    output("obj.bolt_of_cotton")
                }
            }
        }
        section("PotteryShaping", category = "Shape") {
            row("dbrow.crafting_shape_pot") {
                production {
                    input("obj.softclay")
                    statReq("stat.crafting", 1)
                    xp(63)
                    output("obj.pot_unfired")
                }
            }
            row("dbrow.crafting_shape_cup") {
                production {
                    input("obj.softclay")
                    statReq("stat.crafting", 3)
                    xp(85)
                    output("obj.cup_unfired", 4)
                }
            }
            row("dbrow.crafting_shape_pie_dish") {
                production {
                    input("obj.softclay")
                    statReq("stat.crafting", 7)
                    xp(150)
                    output("obj.piedish_unfired")
                }
            }
            row("dbrow.crafting_shape_bowl") {
                production {
                    input("obj.softclay")
                    statReq("stat.crafting", 8)
                    xp(180)
                    output("obj.bowl_unfired")
                }
            }
            row("dbrow.crafting_shape_plant_pot") {
                production {
                    input("obj.softclay")
                    statReq("stat.crafting", 19)
                    xp(200)
                    output("obj.plantpot_unfired")
                }
            }

            row("dbrow.crafting_shape_pot_lid") {
                production {
                    input("obj.softclay")
                    statReq("stat.crafting", 25)
                    xp(200)
                    output("obj.potlid_unfired")
                }
            }
        }
        section("PotteryFiring", category = "Fire") {
            row("dbrow.crafting_fire_pot") {
                production {
                    input("obj.pot_unfired")
                    statReq("stat.crafting", 1)
                    xp(63)
                    output("obj.pot_empty")
                }
                column(COL_SUCCESS_LOW, 180)
                column(COL_SUCCESS_HIGH, 789)
            }
            row("dbrow.crafting_fire_cup") {
                production {
                    input("obj.cup_unfired")
                    statReq("stat.crafting", 3)
                    xp(85)
                    output("obj.cup_empty")
                }
            }
            row("dbrow.crafting_fire_pie_dish") {
                production {
                    input("obj.piedish_unfired")
                    statReq("stat.crafting", 7)
                    xp(100)
                    output("obj.piedish")
                }
                column(COL_SUCCESS_LOW, 180)
                column(COL_SUCCESS_HIGH, 789)
            }
            row("dbrow.crafting_fire_bowl") {
                production {
                    input("obj.bowl_unfired")
                    statReq("stat.crafting", 8)
                    xp(150)
                    output("obj.bowl_empty")
                }
                column(COL_SUCCESS_LOW, 180)
                column(COL_SUCCESS_HIGH, 789)
            }
            row("dbrow.crafting_fire_plant_pot") {
                production {
                    input("obj.plantpot_unfired")
                    statReq("stat.crafting", 19)
                    xp(175)
                    output("obj.plantpot_empty")
                }
                column(COL_SUCCESS_LOW, 180)
                column(COL_SUCCESS_HIGH, 789)
            }
            row("dbrow.crafting_fire_pot_lid") {
                production {
                    input("obj.potlid_unfired")
                    statReq("stat.crafting", 25)
                    xp(200)
                    output("obj.potlid")
                }
                column(COL_SUCCESS_LOW, 180)
                column(COL_SUCCESS_HIGH, 789)
            }
        }
        section("GlassSmelting", category = "Smelt") {
            row("dbrow.crafting_molten_glass") {
                production {
                    input("obj.bucket_sand")
                    input("obj.soda_ash")
                    statReq("stat.crafting", 1)
                    xp(200)
                    output("obj.molten_glass")
                    output("obj.bucket_empty")
                }
            }
        }

        section("SandPit", category = "Fill") {
            row("dbrow.crafting_fill_bucket_sand") {
                production {
                    input("obj.bucket_empty")
                    statReq("stat.crafting", 1)
                    xp(0)
                    output("obj.bucket_sand")
                }
            }
        }
    }

    fun hand() = craftingTable(
        "dbtable.crafting_hand",
        extraColumns = {
            column("section", COL_SECTION, VarType.STRING)
            column("success_low", COL_SUCCESS_LOW, VarType.INT)
            column("success_high", COL_SUCCESS_HIGH, VarType.INT)
            column("fail_xp", COL_FAIL_XP, VarType.INT)
            column("fail_item", COL_FAIL_ITEM, VarType.OBJ)
            column("anim", COL_ANIM, VarType.STRING)
            column("spotanim", COL_SPOTANIM, VarType.STRING)
            column("triggers", COL_TRIGGERS, VarType.OBJ)
            column("xp_extra", COL_XP_EXTRA, VarType.STAT, VarType.INT)
            column("cost", COL_COST, VarType.INT)
            column("tool", COL_TOOL, VarType.OBJ)
            column("sound", COL_SOUND, VarType.STRING)
            column("message", COL_MESSAGE, VarType.STRING)
            column("action_name", COL_ACTION_NAME, VarType.STRING)
        },
    ) {

        section("Needlework") {
            row("dbrow.crafting_leather_gloves") {
                production {
                    input("obj.leather")
                    statReq("stat.crafting", 1)
                    xp(138)
                    output("obj.leather_gloves")
                    category("Leather")
                }
            }
            row("dbrow.crafting_leather_boots") {
                production {
                    input("obj.leather")
                    statReq("stat.crafting", 7)
                    xp(163)
                    output("obj.leather_boots")
                    category("Leather")
                }
            }
            row("dbrow.crafting_leather_cowl") {
                production {
                    input("obj.leather")
                    statReq("stat.crafting", 9)
                    xp(185)
                    output("obj.leather_cowl")
                    category("Leather")
                }
            }
            row("dbrow.crafting_leather_vambraces") {
                production {
                    input("obj.leather")
                    statReq("stat.crafting", 11)
                    xp(220)
                    output("obj.leather_vambraces")
                    category("Leather")
                }
            }
            row("dbrow.crafting_leather_body") {
                production {
                    input("obj.leather")
                    statReq("stat.crafting", 14)
                    xp(250)
                    output("obj.leather_armour")
                    category("Leather")
                }
            }
            row("dbrow.crafting_leather_chaps") {
                production {
                    input("obj.leather")
                    statReq("stat.crafting", 18)
                    xp(270)
                    output("obj.leather_chaps")
                    category("Leather")
                }
            }
            row("dbrow.crafting_hardleather_body") {
                production {
                    input("obj.hard_leather")
                    statReq("stat.crafting", 28)
                    xp(350)
                    output("obj.hardleather_body")
                    category("Leather")
                }
            }
            row("dbrow.crafting_coif") {
                production {
                    input("obj.leather")
                    statReq("stat.crafting", 38)
                    xp(370)
                    output("obj.coif")
                    category("Leather")
                }
            }
            row("dbrow.crafting_studded_body") {
                production {
                    input("obj.leather_armour")
                    input("obj.studs")
                    statReq("stat.crafting", 41)
                    xp(400)
                    output("obj.studded_body")
                    category("Studded")
                }
            }
            row("dbrow.crafting_studded_chaps") {
                production {
                    input("obj.leather_chaps")
                    input("obj.studs")
                    statReq("stat.crafting", 44)
                    xp(420)
                    output("obj.studded_chaps")
                    category("Studded")
                }
            }

            row("dbrow.crafting_spiky_vambraces") {
                production {
                    input("obj.leather_vambraces")
                    input("obj.huntingbeast_claws")
                    statReq("stat.crafting", 32)
                    xp(55)
                    output("obj.spiked_vambraces")
                    category("Studded")
                }
                column(COL_ANIM, "seq.human_crafting_spikedvambraces")
            }

            row("dbrow.crafting_green_dhide_vambraces") {
                production {
                    input("obj.dragon_leather")
                    statReq("stat.crafting", 57)
                    xp(620)
                    output("obj.dragon_vambraces")
                    category("Dragonhide")
                }
            }
            row("dbrow.crafting_green_dhide_chaps") {
                production {
                    input("obj.dragon_leather", 2)
                    statReq("stat.crafting", 60)
                    xp(1240)
                    output("obj.dragonhide_chaps")
                    category("Dragonhide")
                }
            }
            row("dbrow.crafting_green_dhide_body") {
                production {
                    input("obj.dragon_leather", 3)
                    statReq("stat.crafting", 63)
                    xp(1860)
                    output("obj.dragonhide_body")
                    category("Dragonhide")
                }
            }
            row("dbrow.crafting_blue_dhide_vambraces") {
                production {
                    input("obj.dragon_leather_blue")
                    statReq("stat.crafting", 66)
                    xp(700)
                    output("obj.blue_dragon_vambraces")
                    category("Dragonhide")
                }
            }
            row("dbrow.crafting_blue_dhide_chaps") {
                production {
                    input("obj.dragon_leather_blue", 2)
                    statReq("stat.crafting", 68)
                    xp(1400)
                    output("obj.blue_dragonhide_chaps")
                    category("Dragonhide")
                }
            }
            row("dbrow.crafting_blue_dhide_body") {
                production {
                    input("obj.dragon_leather_blue", 3)
                    statReq("stat.crafting", 71)
                    xp(2100)
                    output("obj.blue_dragonhide_body")
                    category("Dragonhide")
                }
            }
            row("dbrow.crafting_red_dhide_vambraces") {
                production {
                    input("obj.dragon_leather_red")
                    statReq("stat.crafting", 73)
                    xp(780)
                    output("obj.red_dragon_vambraces")
                    category("Dragonhide")
                }
            }
            row("dbrow.crafting_red_dhide_chaps") {
                production {
                    input("obj.dragon_leather_red", 2)
                    statReq("stat.crafting", 75)
                    xp(1560)
                    output("obj.red_dragonhide_chaps")
                    category("Dragonhide")
                }
            }
            row("dbrow.crafting_red_dhide_body") {
                production {
                    input("obj.dragon_leather_red", 3)
                    statReq("stat.crafting", 77)
                    xp(2340)
                    output("obj.red_dragonhide_body")
                    category("Dragonhide")
                }
            }
            row("dbrow.crafting_black_dhide_vambraces") {
                production {
                    input("obj.dragon_leather_black")
                    statReq("stat.crafting", 79)
                    xp(860)
                    output("obj.black_dragon_vambraces")
                    category("Dragonhide")
                }
            }
            row("dbrow.crafting_black_dhide_chaps") {
                production {
                    input("obj.dragon_leather_black", 2)
                    statReq("stat.crafting", 82)
                    xp(1720)
                    output("obj.black_dragonhide_chaps")
                    category("Dragonhide")
                }
            }
            row("dbrow.crafting_black_dhide_body") {
                production {
                    input("obj.dragon_leather_black", 3)
                    statReq("stat.crafting", 84)
                    xp(2580)
                    output("obj.black_dragonhide_body")
                    category("Dragonhide")
                }
            }


            row("dbrow.crafting_snakeskin_boots") {
                production {
                    input("obj.village_snake_skin", 6)
                    statReq("stat.crafting", 45)
                    xp(300)
                    output("obj.snakeskin_boots")
                    category("Snakeskin")
                }
            }
            row("dbrow.crafting_snakeskin_vambraces") {
                production {
                    input("obj.village_snake_skin", 8)
                    statReq("stat.crafting", 47)
                    xp(350)
                    output("obj.snakeskin_vambraces")
                    category("Snakeskin")
                }
            }
            row("dbrow.crafting_snakeskin_bandana") {
                production {
                    input("obj.village_snake_skin", 5)
                    statReq("stat.crafting", 48)
                    xp(450)
                    output("obj.snakeskin_bandana")
                    category("Snakeskin")
                }
            }
            row("dbrow.crafting_snakeskin_chaps") {
                production {
                    input("obj.village_snake_skin", 12)
                    statReq("stat.crafting", 51)
                    xp(500)
                    output("obj.snakeskin_chaps")
                    category("Snakeskin")
                }
            }
            row("dbrow.crafting_snakeskin_body") {
                production {
                    input("obj.village_snake_skin", 15)
                    statReq("stat.crafting", 53)
                    xp(550)
                    output("obj.snakeskin_body")
                    category("Snakeskin")
                }
            }


            row("dbrow.crafting_yak_legs") {
                production {
                    input("obj.yak_hide_cured")
                    statReq("stat.crafting", 43)
                    xp(320)
                    output("obj.yak_hide_armour_greaves")
                    category("Yak")
                }
            }
            row("dbrow.crafting_yak_top") {
                production {
                    input("obj.yak_hide_cured", 2)
                    statReq("stat.crafting", 46)
                    xp(320)
                    output("obj.yak_hide_armour_body")
                    category("Yak")
                }
            }


            row("dbrow.crafting_xerician_hat") {
                production {
                    input("obj.xeric_fabric", 3)
                    statReq("stat.crafting", 14)
                    xp(660)
                    output("obj.xeric_hat")
                    category("Xerician")
                }
            }
            row("dbrow.crafting_xerician_robe") {
                production {
                    input("obj.xeric_fabric", 4)
                    statReq("stat.crafting", 17)
                    xp(880)
                    output("obj.xeric_robe")
                    category("Xerician")
                }
            }
            row("dbrow.crafting_xerician_top") {
                production {
                    input("obj.xeric_fabric", 5)
                    statReq("stat.crafting", 22)
                    xp(1100)
                    output("obj.xeric_top")
                    category("Xerician")
                }
            }


            row("dbrow.crafting_splitbark_gauntlets") {
                production {
                    input("obj.hollow_bark", 1)
                    input("obj.fine_cloth", 1)
                    statReq("stat.crafting", 60)
                    xp(620)
                    output("obj.splitbark_gauntlets")
                    category("Splitbark")
                }
            }
            row("dbrow.crafting_splitbark_boots") {
                production {
                    input("obj.hollow_bark", 1)
                    input("obj.fine_cloth", 1)
                    statReq("stat.crafting", 60)
                    xp(620)
                    output("obj.splitbark_greaves")
                    category("Splitbark")
                }
            }
            row("dbrow.crafting_splitbark_helm") {
                production {
                    input("obj.hollow_bark", 2)
                    input("obj.fine_cloth", 2)
                    statReq("stat.crafting", 61)
                    xp(1240)
                    output("obj.splitbark_helm")
                    category("Splitbark")
                }
            }
            row("dbrow.crafting_splitbark_legs") {
                production {
                    input("obj.hollow_bark", 3)
                    input("obj.fine_cloth", 3)
                    statReq("stat.crafting", 62)
                    xp(1860)
                    output("obj.splitbark_legs")
                    category("Splitbark")
                }
            }
            row("dbrow.crafting_splitbark_body") {
                production {
                    input("obj.hollow_bark", 4)
                    input("obj.fine_cloth", 4)
                    statReq("stat.crafting", 62)
                    xp(2480)
                    output("obj.splitbark_body")
                    category("Splitbark")
                }
            }


            row("dbrow.crafting_hueycoatl_vambraces") {
                production {
                    input("obj.huey_hide", 1)
                    statReq("stat.crafting", 76)
                    xp(950)
                    output("obj.huey_vambraces")
                    category("Hueycoatl")
                }
            }
            row("dbrow.crafting_hueycoatl_coif") {
                production {
                    input("obj.huey_hide", 2)
                    statReq("stat.crafting", 76)
                    xp(1900)
                    output("obj.huey_coif")
                    category("Hueycoatl")
                }
            }
            row("dbrow.crafting_hueycoatl_chaps") {
                production {
                    input("obj.huey_hide", 2)
                    statReq("stat.crafting", 77)
                    xp(1900)
                    output("obj.huey_chaps")
                    category("Hueycoatl")
                }
            }
            row("dbrow.crafting_hueycoatl_body") {
                production {
                    input("obj.huey_hide", 3)
                    statReq("stat.crafting", 78)
                    xp(2850)
                    output("obj.huey_body")
                    category("Hueycoatl")
                }
            }


            row("dbrow.crafting_mixed_hide_cape") {
                production {
                    input("obj.hg_mixedhide_base")
                    input("obj.varlamore_jaguar_fur")
                    statReq("stat.crafting", 68)
                    xp(620)
                    output("obj.hide_cape")
                    category("MixedHide")
                }
            }
            row("dbrow.crafting_mixed_hide_boots") {
                production {
                    input("obj.hg_mixedhide_base")
                    input("obj.hunting_antelopesun_fur")
                    statReq("stat.crafting", 69)
                    xp(750)
                    output("obj.hide_boots")
                    category("MixedHide")
                }
            }
            row("dbrow.crafting_mixed_hide_legs") {
                production {
                    input("obj.hg_mixedhide_base")
                    input("obj.hunting_fennecfox_fur", 3)
                    statReq("stat.crafting", 71)
                    xp(2100)
                    output("obj.hide_legs")
                    category("MixedHide")
                }
            }
            row("dbrow.crafting_mixed_hide_top") {
                production {
                    input("obj.hg_mixedhide_base")
                    input("obj.hunting_antelopesun_fur", 2)
                    statReq("stat.crafting", 72)
                    xp(1500)
                    output("obj.hide_top")
                    category("MixedHide")
                }
            }
        }

        section("Shields", category = "Shield") {
            row("dbrow.crafting_hard_leather_shield") {
                production {
                    input("obj.hard_leather", 2)
                    input("obj.oak_shield", 1)
                    input("obj.nails_bronze", 15)
                    statReq("stat.crafting", 41)
                    xp(700)
                    output("obj.leather_shield")
                }
                column(COL_ANIM, "seq.human_shield_crafting_leather")
            }
            row("dbrow.crafting_snakeskin_shield") {
                production {
                    input("obj.village_snake_skin", 2)
                    input("obj.willow_shield", 1)
                    input("obj.nails_iron", 15)
                    statReq("stat.crafting", 56)
                    xp(1000)
                    output("obj.snakeskin_shield")
                }
                column(COL_ANIM, "seq.human_shield_crafting_snakeskin")
            }
            row("dbrow.crafting_green_dhide_shield") {
                production {
                    input("obj.dragon_leather", 2)
                    input("obj.maple_shield", 1)
                    input("obj.nails", 15)
                    statReq("stat.crafting", 62)
                    xp(1240)
                    output("obj.green_dhide_shield")
                }
                column(COL_ANIM, "seq.human_shield_crafting_green_dhide")
            }
            row("dbrow.crafting_blue_dhide_shield") {
                production {
                    input("obj.dragon_leather_blue", 2)
                    input("obj.yew_shield", 1)
                    input("obj.nails_mithril", 15)
                    statReq("stat.crafting", 69)
                    xp(1400)
                    output("obj.blue_dhide_shield")
                }
                column(COL_ANIM, "seq.human_shield_crafting_blue_dhide")
            }
            row("dbrow.crafting_red_dhide_shield") {
                production {
                    input("obj.dragon_leather_red", 2)
                    input("obj.magic_shield", 1)
                    input("obj.nails_adamant", 15)
                    statReq("stat.crafting", 76)
                    xp(1560)
                    output("obj.red_dhide_shield")
                }
                column(COL_ANIM, "seq.human_shield_crafting_red_dhide")
            }
            row("dbrow.crafting_black_dhide_shield") {
                production {
                    input("obj.dragon_leather_black", 2)
                    input("obj.redwood_shield", 1)
                    input("obj.nails_rune", 15)
                    statReq("stat.crafting", 83)
                    xp(1720)
                    output("obj.black_dhide_shield")
                }
                column(COL_ANIM, "seq.human_shield_crafting_black_dhide")
            }
            row("dbrow.crafting_broodoo_shield_blue") {
                production {
                    input("obj.village_snake_skin", 2)
                    input("obj.broodoo_combatshield", 1)
                    input("obj.nails", 8)
                    statReq("stat.crafting", 35)
                    xp(1000)
                    output("obj.broodoo_combatshield")
                }
                column(COL_ANIM, "seq.human_shield_crafting_disease")
            }
            row("dbrow.crafting_broodoo_shield_green") {
                production {
                    input("obj.village_snake_skin", 2)
                    input("obj.broodoo_poisonshield", 1)
                    input("obj.nails", 8)
                    statReq("stat.crafting", 35)
                    xp(1000)
                    output("obj.broodoo_poisonshield")
                }
                column(COL_ANIM, "seq.human_shield_crafting_poison")
            }
            row("dbrow.crafting_broodoo_shield_orange") {
                production {
                    input("obj.village_snake_skin", 2)
                    input("obj.broodoo_diseaseshield", 1)
                    input("obj.nails", 8)
                    statReq("stat.crafting", 35)
                    xp(1000)
                    output("obj.broodoo_diseaseshield")
                }
                column(COL_ANIM, "seq.human_shield_crafting_combat")
            }
        }

        section("Carving", category = "Carve") {
            val snelms = listOf(
                Triple("dbrow.crafting_snelm_red_pointed", "obj.shellpoint_red+black", "obj.snelm_point_red+black"),
                Triple("dbrow.crafting_snelm_red_round", "obj.shellround_red+black", "obj.snelm_round_red+black"),
                Triple("dbrow.crafting_snelm_bark", "obj.shellround_orange", "obj.snelm_round_orange"),
                Triple("dbrow.crafting_snelm_blue_pointed", "obj.shellpoint_blue", "obj.snelm_point_blue"),
                Triple("dbrow.crafting_snelm_blue_round", "obj.shellround_blue", "obj.snelm_round_blue"),
                Triple("dbrow.crafting_snelm_myre_pointed", "obj.shellpoint_swamp", "obj.snelm_point_swamp"),
                Triple("dbrow.crafting_snelm_myre_round", "obj.shellround_swamp", "obj.snelm_round_swamp"),
                Triple("dbrow.crafting_snelm_ochre_pointed", "obj.shellpoint_yellow", "obj.snelm_point_yellow"),
                Triple("dbrow.crafting_snelm_ochre_round", "obj.shellround_yellow", "obj.snelm_round_yellow"),
            )
            for ((rowId, shell, snelm) in snelms) {
                row(rowId) {
                    production {
                        input(shell)
                        statReq("stat.crafting", 15)
                        xp(325)
                        output(snelm)
                    }
                }
            }
            row("dbrow.crafting_crab_helmet") {
                production {
                    input("obj.hundred_pirate_crab_shell_head")
                    statReq("stat.crafting", 15)
                    xp(325)
                    output("obj.hundred_pirate_crab_shell_helm")
                }
            }
            row("dbrow.crafting_crab_claw") {
                production {
                    input("obj.hundred_pirate_crab_shell_claw")
                    statReq("stat.crafting", 15)
                    xp(325)
                    output("obj.hundred_pirate_crab_shell_gauntlet")
                }
            }
        }
        section("Knife", category = "Cut") {
            row("dbrow.crafting_dramen_staff") {
                production {
                    input("obj.dramen_branch")
                    statReq("stat.crafting", 31)
                    xp(0)
                    output("obj.dramen_staff")
                }
                column(COL_TICKS, 0)
                column(COL_MESSAGE, "You carve the branch into a staff.")
            }

            row("dbrow.crafting_sinew") {
                production {
                    input("obj.damaged_ballista_rope")
                    statReq("stat.crafting", 10)
                    xp(150)
                    output("obj.xbows_sinew")
                }
            }
        }
        section("Gems", category = "Cut") {
            row("dbrow.crafting_cut_opal") {
                production {
                    input("obj.uncut_opal")
                    statReq("stat.crafting", 1)
                    xp(150)
                    output("obj.opal")
                }
                column(COL_ANIM, "seq.human_opalcutting")
                column(COL_SUCCESS_LOW, 100)
                column(COL_SUCCESS_HIGH, 252)
                column(COL_FAIL_XP, 38)
                columnRSCM(COL_FAIL_ITEM, "obj.crushed_gemstone")
            }
            row("dbrow.crafting_cut_jade") {
                production {
                    input("obj.uncut_jade")
                    statReq("stat.crafting", 13)
                    xp(200)
                    output("obj.jade")
                }
                column(COL_ANIM, "seq.human_jadecutting")
                column(COL_SUCCESS_LOW, 120)
                column(COL_SUCCESS_HIGH, 252)
                column(COL_FAIL_XP, 50)
                columnRSCM(COL_FAIL_ITEM, "obj.crushed_gemstone")
            }
            row("dbrow.crafting_cut_red_topaz") {
                production {
                    input("obj.uncut_red_topaz")
                    statReq("stat.crafting", 16)
                    xp(250)
                    output("obj.red_topaz")
                }
                column(COL_ANIM, "seq.human_redtopazcutting")
                column(COL_SUCCESS_LOW, 140)
                column(COL_SUCCESS_HIGH, 252)
                column(COL_FAIL_XP, 63)
                columnRSCM(COL_FAIL_ITEM, "obj.crushed_gemstone")
            }
            row("dbrow.crafting_cut_sapphire") {
                production {
                    input("obj.uncut_sapphire")
                    statReq("stat.crafting", 20)
                    xp(500)
                    output("obj.sapphire")
                }
                column(COL_ANIM, "seq.human_sapphirecutting")
            }
            row("dbrow.crafting_cut_emerald") {
                production {
                    input("obj.uncut_emerald")
                    statReq("stat.crafting", 27)
                    xp(675)
                    output("obj.emerald")
                }
                column(COL_ANIM, "seq.human_emeraldcutting")
            }
            row("dbrow.crafting_cut_ruby") {
                production {
                    input("obj.uncut_ruby")
                    statReq("stat.crafting", 34)
                    xp(850)
                    output("obj.ruby")
                }
                column(COL_ANIM, "seq.human_rubycutting")
            }
            row("dbrow.crafting_cut_diamond") {
                production {
                    input("obj.uncut_diamond")
                    statReq("stat.crafting", 43)
                    xp(1075)
                    output("obj.diamond")
                }
                column(COL_ANIM, "seq.human_diamondcutting")
            }
            row("dbrow.crafting_cut_dragonstone") {
                production {
                    input("obj.uncut_dragonstone")
                    statReq("stat.crafting", 55)
                    xp(1375)
                    output("obj.dragonstone")
                }
                column(COL_ANIM, "seq.human_dragonstonecutting")
            }
            row("dbrow.crafting_cut_onyx") {
                production {
                    input("obj.uncut_onyx")
                    statReq("stat.crafting", 67)
                    xp(1675)
                    output("obj.onyx")
                }
                column(COL_ANIM, "seq.human_onyxcutting")
            }
            row("dbrow.crafting_cut_zenyte") {
                production {
                    input("obj.uncut_zenyte")
                    statReq("stat.crafting", 89)
                    xp(2000)
                    output("obj.zenyte")
                }
                column(COL_ANIM, "seq.human_zenytecutting")
            }
        }
        section("Amethyst", category = "Cut") {
            row("dbrow.crafting_amethyst_bolt_tips") {
                production {
                    input("obj.amethyst")
                    statReq("stat.crafting", 83)
                    xp(600)
                    output("obj.xbows_bolt_tips_amethyst", 15)
                }
            }
            row("dbrow.crafting_amethyst_arrowtips") {
                production {
                    input("obj.amethyst")
                    statReq("stat.crafting", 85)
                    xp(600)
                    output("obj.amethyst_arrowheads", 15)
                }
            }
            row("dbrow.crafting_amethyst_javelin_heads") {
                production {
                    input("obj.amethyst")
                    statReq("stat.crafting", 87)
                    xp(600)
                    output("obj.amethyst_javelin_head", 5)
                }
            }
            row("dbrow.crafting_amethyst_dart_tips") {
                production {
                    input("obj.amethyst")
                    statReq("stat.crafting", 89)
                    xp(600)
                    output("obj.amethyst_dart_tip", 8)
                }
            }
        }
        section("Limestone", category = "Cut") {
            // 137/434 out of 256 reproduces the wiki curve: ~33% failure at level 12 (the minimum) and guaranteed to succeed at level 40.
            row("dbrow.crafting_limestone_brick") {
                production {
                    input("obj.limestone")
                    statReq("stat.crafting", 12)
                    xp(60)
                    output("obj.limestonebrick")
                }
                column(COL_SUCCESS_LOW, 137)
                column(COL_SUCCESS_HIGH, 434)
                columnRSCM(COL_FAIL_ITEM, "obj.rock")
            }
        }
        section("Glassblowing", category = "Blow") {
            row("dbrow.crafting_glass_beer_glass") {
                production {
                    input("obj.molten_glass")
                    statReq("stat.crafting", 1)
                    xp(175)
                    output("obj.beer_glass")
                }
            }
            row("dbrow.crafting_glass_candle_lantern") {
                production {
                    input("obj.molten_glass")
                    statReq("stat.crafting", 4)
                    xp(190)
                    output("obj.candle_lantern_empty")
                }
            }
            row("dbrow.crafting_glass_oil_lamp") {
                production {
                    input("obj.molten_glass")
                    statReq("stat.crafting", 12)
                    xp(250)
                    output("obj.oil_lamp_empty")
                }
            }
            row("dbrow.crafting_glass_vial") {
                production {
                    input("obj.molten_glass")
                    statReq("stat.crafting", 33)
                    xp(350)
                    output("obj.vial_empty")
                }
            }
            row("dbrow.crafting_glass_fishbowl") {
                production {
                    input("obj.molten_glass")
                    statReq("stat.crafting", 42)
                    xp(425)
                    output("obj.fishbowl_empty")
                }
            }
            row("dbrow.crafting_glass_unpowered_orb") {
                production {
                    input("obj.molten_glass")
                    statReq("stat.crafting", 46)
                    xp(525)
                    output("obj.stafforb")
                }
            }
            row("dbrow.crafting_glass_lantern_lens") {
                production {
                    input("obj.molten_glass")
                    statReq("stat.crafting", 49)
                    xp(550)
                    output("obj.bullseye_lantern_lens")
                }
            }
            row("dbrow.crafting_glass_light_orb") {
                production {
                    input("obj.molten_glass")
                    statReq("stat.crafting", 87)
                    xp(700)
                    output("obj.dorgesh_lightbulb_nofilament")
                }
            }
        }

        section("Battlestaves", category = "Attach") {
            row("dbrow.crafting_water_battlestaff") {
                production {
                    input("obj.battlestaff")
                    input("obj.water_orb")
                    statReq("stat.crafting", 54)
                    xp(1000)
                    output("obj.water_battlestaff")
                }
                column(COL_SPOTANIM, "spotanim.battlestaff_water_crafting_spotanim")
            }
            row("dbrow.crafting_earth_battlestaff") {
                production {
                    input("obj.battlestaff")
                    input("obj.earth_orb")
                    statReq("stat.crafting", 58)
                    xp(1125)
                    output("obj.earth_battlestaff")
                }
                column(COL_SPOTANIM, "spotanim.battlestaff_earth_crafting_spotanim")
            }
            row("dbrow.crafting_fire_battlestaff") {
                production {
                    input("obj.battlestaff")
                    input("obj.fire_orb")
                    statReq("stat.crafting", 62)
                    xp(1250)
                    output("obj.fire_battlestaff")
                }
                column(COL_SPOTANIM, "spotanim.battlestaff_fire_crafting_spotanim")
            }
            row("dbrow.crafting_air_battlestaff") {
                production {
                    input("obj.battlestaff")
                    input("obj.air_orb")
                    statReq("stat.crafting", 66)
                    xp(1375)
                    output("obj.air_battlestaff")
                }
                column(COL_SPOTANIM, "spotanim.battlestaff_air_crafting_spotanim")
            }
        }

        section("AmuletStringing", category = "String") {
            row("dbrow.crafting_string_gold_amulet") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.unstrung_gold_amulet")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.strung_gold_amulet")
                }
            }
            row("dbrow.crafting_string_sapphire_amulet") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.unstrung_sapphire_amulet")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.strung_sapphire_amulet")
                }
            }
            row("dbrow.crafting_string_emerald_amulet") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.unstrung_emerald_amulet")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.strung_emerald_amulet")
                }
            }
            row("dbrow.crafting_string_ruby_amulet") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.unstrung_ruby_amulet")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.strung_ruby_amulet")
                }
            }
            row("dbrow.crafting_string_diamond_amulet") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.unstrung_diamond_amulet")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.strung_diamond_amulet")
                }
            }
            row("dbrow.crafting_string_dragonstone_amulet") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.unstrung_dragonstone_amulet")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.strung_dragonstone_amulet")
                }
            }
            row("dbrow.crafting_string_onyx_amulet") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.unstrung_onyx_amulet")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.strung_onyx_amulet")
                }
            }
            row("dbrow.crafting_string_zenyte_amulet") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.unstrung_zenyte_amulet")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.zenyte_amulet")
                }
            }
            row("dbrow.crafting_string_opal_amulet") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.unstrung_opal_amulet")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.strung_opal_amulet")
                }
            }
            row("dbrow.crafting_string_jade_amulet") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.unstrung_jade_amulet")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.strung_jade_amulet")
                }
            }
            row("dbrow.crafting_string_topaz_amulet") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.unstrung_topaz_amulet")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.strung_topaz_amulet")
                }
            }

            row("dbrow.crafting_string_emblem") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.nostringsnake")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.stringsnake")
                }
            }

            row("dbrow.crafting_string_symbol") {
                production {
                    input("obj.ball_of_wool")
                    input("obj.nostringstar")
                    statReq("stat.crafting", 1)
                    xp(40)
                    output("obj.stringstar")
                }
            }
        }
        section("Birdhouses", category = "Birdhouse") {
            fun birdhouse(name: String, log: String, level: Int, fineXp: Int) =
                row("dbrow.crafting_$name") {
                    production {
                        input("obj.$log")
                        input("obj.poh_clockwork_mechanism")
                        statReq("stat.crafting", level)
                        xp(fineXp)
                        output("obj.$name")
                    }
                }

            birdhouse("birdhouse_normal", "logs", 5, 150)
            birdhouse("birdhouse_oak", "oak_logs", 15, 200)
            birdhouse("birdhouse_willow", "willow_logs", 25, 250)
            birdhouse("birdhouse_teak", "teak_logs", 35, 300)
            birdhouse("birdhouse_maple", "maple_logs", 45, 350)
            birdhouse("birdhouse_mahogany", "mahogany_logs", 50, 400)
            birdhouse("birdhouse_yew", "yew_logs", 60, 450)
            birdhouse("birdhouse_magic", "magic_logs", 75, 500)
            birdhouse("birdhouse_redwood", "redwood_logs", 90, 550)
        }

        section("Combining") {
            row("dbrow.crafting_slayer_helm") {
                production {
                    category("Assembly")
                    input("obj.harmless_black_mask")
                    input("obj.slayer_earmuffs")
                    input("obj.slayer_facemask")
                    input("obj.slayer_nosepeg")
                    input("obj.wallbeast_spike_helmet")
                    input("obj.slayer_gem")
                    input("obj.slayer_reinforced_goggles") //Technically optional based on a quest, so some code handles the optionality later.
                    statReq("stat.crafting", 55)
                    xp(0)
                    output("obj.slayer_helm")
                }
                columnRSCM(
                    COL_TRIGGERS,
                    "obj.harmless_black_mask",
                    "obj.slayer_earmuffs",
                    "obj.slayer_facemask",
                    "obj.slayer_nosepeg",
                    "obj.wallbeast_spike_helmet",
                    "obj.slayer_gem",
                    "obj.slayer_reinforced_goggles",
                )
                column(COL_MESSAGE, "You combine the pieces to make a {output}.")
            }

            row("dbrow.crafting_noxious_halberd") {
                production {
                    category("Assembly")
                    input("obj.noxious_halberd_part_1")
                    input("obj.noxious_halberd_part_2")
                    input("obj.noxious_halberd_part_3")
                    statReq("stat.crafting", 72)
                    statReq("stat.smithing", 72)
                    xp(1000)
                    output("obj.noxious_halberd")
                }
                columnRSCM(
                    COL_TRIGGERS,
                    "obj.noxious_halberd_part_1",
                    "obj.noxious_halberd_part_2",
                    "obj.noxious_halberd_part_3",
                )
                column(COL_ANIM, "seq.human_fletching_noxious_halberd")
                column(COL_XP_EXTRA, ConstantProvider.getMapping("stat.smithing"), 1000)
                column(COL_MESSAGE, "You combine the pieces to make a {output}.")
            }

            row("dbrow.crafting_toxic_staff_of_the_dead") {
                production {
                    category("Chisel")
                    input("obj.magic_fang")
                    input("obj.sotd")
                    statReq("stat.crafting", 59)
                    xp(0)
                    output("obj.toxic_sotd")
                }
                columnRSCM(COL_TRIGGERS, "obj.magic_fang", "obj.sotd")
                columnRSCM(COL_TOOL, "obj.chisel")
                column(COL_SOUND, "synth.chisel")
            }
            row("dbrow.crafting_trident_of_the_swamp") {
                production {
                    category("Chisel")
                    input("obj.magic_fang")
                    input("obj.tots_uncharged")
                    statReq("stat.crafting", 59)
                    xp(0)
                    output("obj.toxic_tots_uncharged")
                }
                columnRSCM(COL_TRIGGERS, "obj.magic_fang", "obj.tots_uncharged")
                columnRSCM(COL_TOOL, "obj.chisel")
                column(COL_SOUND, "synth.chisel")
            }

            row("dbrow.crafting_bone_staff") {
                production {
                    category("Chisel")
                    input("obj.rat_boss_spine")
                    input("obj.battlestaff")
                    input("obj.chaosrune", 1000)
                    statReq("stat.crafting", 35)
                    xp(0)
                    output("obj.rat_bone_staff")
                }
                columnRSCM(COL_TRIGGERS, "obj.rat_boss_spine", "obj.battlestaff")
                columnRSCM(COL_TOOL, "obj.chisel")
                column(COL_SOUND, "synth.chisel")
            }

            row("dbrow.crafting_accursed_sceptre") {
                production {
                    category("Attach")
                    input("obj.wbr_vetion_skull")
                    input("obj.wild_cave_sceptre_uncharged")
                    statReq("stat.crafting", 85)
                    xp(0)
                    output("obj.wild_cave_accursed_uncharged")
                }
                columnRSCM(COL_TRIGGERS, "obj.wbr_vetion_skull", "obj.wild_cave_sceptre_uncharged")
            }

            row("dbrow.crafting_strung_rabbit_foot") {
                production {
                    category("String")
                    input("obj.hunting_rabbit_foot")
                    input("obj.ball_of_wool")
                    statReq("stat.crafting", 37)
                    xp(40)
                    output("obj.hunting_strung_rabbit_foot")
                }
                columnRSCM(COL_TRIGGERS, "obj.hunting_rabbit_foot", "obj.ball_of_wool")
                column(COL_SOUND, "synth.stringing")
                column(COL_MESSAGE, "You string the {input}.")
                column(COL_ACTION_NAME, "string a {input}")
            }

            row("dbrow.crafting_serpentine_helm") {
                production {
                    category("Chisel")
                    input("obj.serpentine_visage")
                    statReq("stat.crafting", 52)
                    xp(1200)
                    output("obj.serpentine_helm")
                }
                columnRSCM(COL_TOOL, "obj.chisel")
                column(COL_SOUND, "synth.chisel")
            }

            row("dbrow.crafting_break_armadyl_chestplate") {
                production {
                    category("Chisel")
                    input("obj.armadyl_chestplate")
                    statReq("stat.crafting", 90)
                    xp(8400)
                    output("obj.armadylean_component", 4)
                }
                columnRSCM(COL_TOOL, "obj.chisel")
                column(COL_MESSAGE, "You use your chisel to break apart the armour down into its base components.")
            }
            row("dbrow.crafting_break_armadyl_skirt") {
                production {
                    category("Chisel")
                    input("obj.armadyl_skirt")
                    statReq("stat.crafting", 90)
                    xp(6300)
                    output("obj.armadylean_component", 3)
                }
                columnRSCM(COL_TOOL, "obj.chisel")
                column(COL_MESSAGE, "You use your chisel to break apart the armour down into its base components.")
            }
            row("dbrow.crafting_break_armadyl_helmet") {
                production {
                    category("Chisel")
                    input("obj.armadyl_helmet")
                    statReq("stat.crafting", 90)
                    xp(2100)
                    output("obj.armadylean_component", 1)
                }
                columnRSCM(COL_TOOL, "obj.chisel")
                column(COL_MESSAGE, "You use your chisel to break apart the armour down into its base components.")
            }
            row("dbrow.crafting_fortify_masori_body") {
                production {
                    category("Hammer")
                    input("obj.masori_body")
                    input("obj.armadylean_component", 4)
                    statReq("stat.crafting", 90)
                    xp(33200)
                    output("obj.masori_body_fortified")
                }
                columnRSCM(COL_TOOL, "obj.hammer")
            }
            row("dbrow.crafting_fortify_masori_chaps") {
                production {
                    category("Hammer")
                    input("obj.masori_chaps")
                    input("obj.armadylean_component", 3)
                    statReq("stat.crafting", 90)
                    xp(24900)
                    output("obj.masori_chaps_fortified")
                }
                columnRSCM(COL_TOOL, "obj.hammer")
            }
            row("dbrow.crafting_fortify_masori_mask") {
                production {
                    category("Hammer")
                    input("obj.masori_mask")
                    input("obj.armadylean_component", 1)
                    statReq("stat.crafting", 90)
                    xp(8300)
                    output("obj.masori_mask_fortified")
                }
                columnRSCM(COL_TOOL, "obj.hammer")
            }
        }

        section("SoftClayMixing") {
            row("dbrow.crafting_soft_clay_bucket_water") {
                production {
                    input("obj.clay")
                    input("obj.bucket_water")
                    statReq("stat.crafting", 1)
                    xp(10)
                    output("obj.softclay")
                    output("obj.bucket_empty")
                }
            }
            row("dbrow.crafting_soft_clay_jug_water") {
                production {
                    input("obj.clay")
                    input("obj.jug_water")
                    statReq("stat.crafting", 1)
                    xp(10)
                    output("obj.softclay")
                    output("obj.jug_empty")
                }
            }
            row("dbrow.crafting_soft_clay_bowl_water") {
                production {
                    input("obj.clay")
                    input("obj.bowl_water")
                    statReq("stat.crafting", 1)
                    xp(10)
                    output("obj.softclay")
                    output("obj.bowl_empty")
                }
            }
            row("dbrow.crafting_soft_clay_cup_water") {
                production {
                    input("obj.clay")
                    input("obj.cup_water")
                    statReq("stat.crafting", 1)
                    xp(10)
                    output("obj.softclay")
                    output("obj.cup_empty")
                }
            }
        }

        section("PheasantCostume") {
            row("dbrow.crafting_pheasant_boots") {
                production {
                    input("obj.forestry_pheasant_feathers", 15)
                    statReq("stat.crafting", 2)
                    xp(150)
                    output("obj.forestry_pheasant_boots")
                }
            }
            row("dbrow.crafting_pheasant_hat") {
                production {
                    input("obj.forestry_pheasant_feathers", 15)
                    statReq("stat.crafting", 2)
                    xp(150)
                    output("obj.forestry_pheasant_hat")
                }
            }
            row("dbrow.crafting_pheasant_legs") {
                production {
                    input("obj.forestry_pheasant_feathers", 15)
                    statReq("stat.crafting", 2)
                    xp(150)
                    output("obj.forestry_pheasant_legs")
                }
            }
            row("dbrow.crafting_pheasant_cape") {
                production {
                    input("obj.forestry_pheasant_feathers", 15)
                    statReq("stat.crafting", 2)
                    xp(150)
                    output("obj.forestry_pheasant_cape")
                }
            }
        }

        section("Tanning", category = "Tan") {
            row("dbrow.crafting_tan_soft_leather") {
                production {
                    input("obj.cow_hide")
                    statReq("stat.crafting", 1)
                    xp(0)
                    output("obj.leather")
                }
                column(COL_COST, 1)
            }
            row("dbrow.crafting_tan_hard_leather") {
                production {
                    input("obj.cow_hide")
                    statReq("stat.crafting", 1)
                    xp(0)
                    output("obj.hard_leather")
                }
                column(COL_COST, 3)
            }
            row("dbrow.crafting_tan_snakeskin") {
                production {
                    input("obj.village_snake_hide")
                    statReq("stat.crafting", 1)
                    xp(0)
                    output("obj.village_snake_skin")
                }
                column(COL_COST, 15)
            }
            row("dbrow.crafting_tan_snakeskin_swamp") {
                production {
                    input("obj.templetrek_swamp_snake_hide")
                    statReq("stat.crafting", 1)
                    xp(0)
                    output("obj.village_snake_skin")
                }
                column(COL_COST, 20)
            }
            row("dbrow.crafting_tan_green_dhide") {
                production {
                    input("obj.dragonhide_green")
                    statReq("stat.crafting", 1)
                    xp(0)
                    output("obj.dragon_leather")
                }
                column(COL_COST, 20)
            }
            row("dbrow.crafting_tan_blue_dhide") {
                production {
                    input("obj.dragonhide_blue")
                    statReq("stat.crafting", 1)
                    xp(0)
                    output("obj.dragon_leather_blue")
                }
                column(COL_COST, 20)
            }
            row("dbrow.crafting_tan_red_dhide") {
                production {
                    input("obj.dragonhide_red")
                    statReq("stat.crafting", 1)
                    xp(0)
                    output("obj.dragon_leather_red")
                }
                column(COL_COST, 20)
            }
            row("dbrow.crafting_tan_black_dhide") {
                production {
                    input("obj.dragonhide_black")
                    statReq("stat.crafting", 1)
                    xp(0)
                    output("obj.dragon_leather_black")
                }
                column(COL_COST, 20)
            }
            row("dbrow.crafting_cure_yak_hide") {
                production {
                    input("obj.yak_hide")
                    statReq("stat.crafting", 1)
                    xp(0)
                    output("obj.yak_hide_cured")
                }
                column(COL_COST, 5)
            }
        }
    }

    fun silver() = craftingTable(
        "dbtable.crafting_silver",
        extraColumns = {
            column("section", COL_SECTION, VarType.STRING)
            column("mould", COL_MOULD, VarType.OBJ)
        },
    ) {
        section("Jewellery", category = "Silver") {
            row("dbrow.crafting_unstrung_symbol") {
                production {
                    input("obj.silver_bar")
                    statReq("stat.crafting", 16)
                    xp(500)
                    output("obj.nostringstar")
                }
                columnRSCM(COL_MOULD, "obj.holy_symbol_mould")
            }
            row("dbrow.crafting_unstrung_emblem") {
                production {
                    input("obj.silver_bar")
                    statReq("stat.crafting", 17)
                    xp(500)
                    output("obj.nostringsnake")
                }
                columnRSCM(COL_MOULD, "obj.unholy_symbol_mould")
            }
            row("dbrow.crafting_silver_sickle") {
                production {
                    input("obj.silver_bar")
                    statReq("stat.crafting", 18)
                    xp(500)
                    output("obj.silver_sickle")
                }
                columnRSCM(COL_MOULD, "obj.sickle_mould")
            }
            row("dbrow.crafting_silver_bolts") {
                production {
                    input("obj.silver_bar")
                    statReq("stat.crafting", 21)
                    xp(500)
                    output("obj.xbows_crossbow_bolts_silver_unfeathered", 10)
                }
                columnRSCM(COL_MOULD, "obj.xbows_silver_bolt_mould")
            }
            row("dbrow.crafting_conductor") {
                production {
                    input("obj.silver_bar")
                    statReq("stat.crafting", 20)
                    xp(500)
                    output("obj.fenk_conductor")
                }
                columnRSCM(COL_MOULD, "obj.fenk_lightning_mould")
            }
            row("dbrow.crafting_silvthrill_rod") {
                production {
                    input("obj.silver_bar")
                    input("obj.mithril_bar")
                    input("obj.sapphire")
                    statReq("stat.crafting", 25)
                    xp(550)
                    output("obj.burgh_rod_command1")
                }
                columnRSCM(COL_MOULD, "obj.burgh_rod_clay")
            }
            row("dbrow.crafting_demonic_sigil") {
                production {
                    input("obj.silver_bar")
                    statReq("stat.crafting", 30)
                    xp(500)
                    output("obj.agrith_sigil")
                }
                columnRSCM(COL_MOULD, "obj.agrith_sigil_mould")
            }
            row("dbrow.crafting_tiara") {
                production {
                    input("obj.silver_bar")
                    statReq("stat.crafting", 23)
                    xp(525)
                    output("obj.tiara")
                }
                columnRSCM(COL_MOULD, "obj.tiara_mould")
            }


            row("dbrow.crafting_opal_ring") {
                production {
                    input("obj.silver_bar")
                    input("obj.opal")
                    statReq("stat.crafting", 1)
                    xp(100)
                    output("obj.opal_ring")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_opal_necklace") {
                production {
                    input("obj.silver_bar")
                    input("obj.opal")
                    statReq("stat.crafting", 16)
                    xp(350)
                    output("obj.opal_necklace")
                }
                columnRSCM(COL_MOULD, "obj.necklace_mould")
            }
            row("dbrow.crafting_opal_bracelet") {
                production {
                    input("obj.silver_bar")
                    input("obj.opal")
                    statReq("stat.crafting", 22)
                    xp(450)
                    output("obj.opal_bracelet")
                }
                columnRSCM(COL_MOULD, "obj.jewl_bracelet_mould")
            }
            row("dbrow.crafting_opal_amulet") {
                production {
                    input("obj.silver_bar")
                    input("obj.opal")
                    statReq("stat.crafting", 27)
                    xp(550)
                    output("obj.unstrung_opal_amulet")
                }
                columnRSCM(COL_MOULD, "obj.amulet_mould")
            }

            row("dbrow.crafting_jade_ring") {
                production {
                    input("obj.silver_bar")
                    input("obj.jade")
                    statReq("stat.crafting", 13)
                    xp(320)
                    output("obj.jade_ring")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_jade_necklace") {
                production {
                    input("obj.silver_bar")
                    input("obj.jade")
                    statReq("stat.crafting", 25)
                    xp(540)
                    output("obj.jade_necklace")
                }
                columnRSCM(COL_MOULD, "obj.necklace_mould")
            }
            row("dbrow.crafting_jade_bracelet") {
                production {
                    input("obj.silver_bar")
                    input("obj.jade")
                    statReq("stat.crafting", 29)
                    xp(600)
                    output("obj.jade_bracelet")
                }
                columnRSCM(COL_MOULD, "obj.jewl_bracelet_mould")
            }
            row("dbrow.crafting_jade_amulet") {
                production {
                    input("obj.silver_bar")
                    input("obj.jade")
                    statReq("stat.crafting", 34)
                    xp(700)
                    output("obj.unstrung_jade_amulet")
                }
                columnRSCM(COL_MOULD, "obj.amulet_mould")
            }

            row("dbrow.crafting_topaz_ring") {
                production {
                    input("obj.silver_bar")
                    input("obj.red_topaz")
                    statReq("stat.crafting", 16)
                    xp(350)
                    output("obj.topaz_ring")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_topaz_necklace") {
                production {
                    input("obj.silver_bar")
                    input("obj.red_topaz")
                    statReq("stat.crafting", 32)
                    xp(700)
                    output("obj.topaz_necklace")
                }
                columnRSCM(COL_MOULD, "obj.necklace_mould")
            }
            row("dbrow.crafting_topaz_bracelet") {
                production {
                    input("obj.silver_bar")
                    input("obj.red_topaz")
                    statReq("stat.crafting", 38)
                    xp(750)
                    output("obj.topaz_bracelet")
                }
                columnRSCM(COL_MOULD, "obj.jewl_bracelet_mould")
            }
            row("dbrow.crafting_topaz_amulet") {
                production {
                    input("obj.silver_bar")
                    input("obj.red_topaz")
                    statReq("stat.crafting", 45)
                    xp(800)
                    output("obj.unstrung_topaz_amulet")
                }
                columnRSCM(COL_MOULD, "obj.amulet_mould")
            }
        }
    }

    fun gold() = craftingTable(
        "dbtable.crafting_gold",
        extraColumns = {
            column("section", COL_SECTION, VarType.STRING)
            column("mould", COL_MOULD, VarType.OBJ)
        },
    ) {
        section("Jewellery", category = "Gold") {
            row("dbrow.crafting_gold_ring") {
                production {
                    input("obj.gold_bar")
                    statReq("stat.crafting", 5)
                    xp(150)
                    output("obj.gold_ring")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_gold_necklace") {
                production {
                    input("obj.gold_bar")
                    statReq("stat.crafting", 6)
                    xp(200)
                    output("obj.gold_necklace")
                }
                columnRSCM(COL_MOULD, "obj.necklace_mould")
            }
            row("dbrow.crafting_gold_bracelet") {
                production {
                    input("obj.gold_bar")
                    statReq("stat.crafting", 7)
                    xp(250)
                    output("obj.jewl_gold_bracelet")
                }
                columnRSCM(COL_MOULD, "obj.jewl_bracelet_mould")
            }
            row("dbrow.crafting_gold_amulet") {
                production {
                    input("obj.gold_bar")
                    statReq("stat.crafting", 8)
                    xp(300)
                    output("obj.unstrung_gold_amulet")
                }
                columnRSCM(COL_MOULD, "obj.amulet_mould")
            }

            row("dbrow.crafting_sapphire_ring") {
                production {
                    input("obj.gold_bar")
                    input("obj.sapphire")
                    statReq("stat.crafting", 20)
                    xp(400)
                    output("obj.sapphire_ring")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_sapphire_necklace") {
                production {
                    input("obj.gold_bar")
                    input("obj.sapphire")
                    statReq("stat.crafting", 22)
                    xp(550)
                    output("obj.sapphire_necklace")
                }
                columnRSCM(COL_MOULD, "obj.necklace_mould")
            }
            row("dbrow.crafting_sapphire_bracelet") {
                production {
                    input("obj.gold_bar")
                    input("obj.sapphire")
                    statReq("stat.crafting", 23)
                    xp(600)
                    output("obj.jewl_sapphire_bracelet")
                }
                columnRSCM(COL_MOULD, "obj.jewl_bracelet_mould")
            }
            row("dbrow.crafting_sapphire_amulet") {
                production {
                    input("obj.gold_bar")
                    input("obj.sapphire")
                    statReq("stat.crafting", 24)
                    xp(650)
                    output("obj.unstrung_sapphire_amulet")
                }
                columnRSCM(COL_MOULD, "obj.amulet_mould")
            }

            row("dbrow.crafting_emerald_ring") {
                production {
                    input("obj.gold_bar")
                    input("obj.emerald")
                    statReq("stat.crafting", 27)
                    xp(550)
                    output("obj.emerald_ring")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_emerald_necklace") {
                production {
                    input("obj.gold_bar")
                    input("obj.emerald")
                    statReq("stat.crafting", 29)
                    xp(600)
                    output("obj.emerald_necklace")
                }
                columnRSCM(COL_MOULD, "obj.necklace_mould")
            }
            row("dbrow.crafting_emerald_bracelet") {
                production {
                    input("obj.gold_bar")
                    input("obj.emerald")
                    statReq("stat.crafting", 30)
                    xp(650)
                    output("obj.jewl_emerald_bracelet")
                }
                columnRSCM(COL_MOULD, "obj.jewl_bracelet_mould")
            }
            row("dbrow.crafting_emerald_amulet") {
                production {
                    input("obj.gold_bar")
                    input("obj.emerald")
                    statReq("stat.crafting", 31)
                    xp(700)
                    output("obj.unstrung_emerald_amulet")
                }
                columnRSCM(COL_MOULD, "obj.amulet_mould")
            }

            row("dbrow.crafting_ruby_ring") {
                production {
                    input("obj.gold_bar")
                    input("obj.ruby")
                    statReq("stat.crafting", 34)
                    xp(700)
                    output("obj.ruby_ring")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_ruby_necklace") {
                production {
                    input("obj.gold_bar")
                    input("obj.ruby")
                    statReq("stat.crafting", 40)
                    xp(750)
                    output("obj.ruby_necklace")
                }
                columnRSCM(COL_MOULD, "obj.necklace_mould")
            }
            row("dbrow.crafting_ruby_bracelet") {
                production {
                    input("obj.gold_bar")
                    input("obj.ruby")
                    statReq("stat.crafting", 42)
                    xp(800)
                    output("obj.jewl_ruby_bracelet")
                }
                columnRSCM(COL_MOULD, "obj.jewl_bracelet_mould")
            }
            row("dbrow.crafting_ruby_amulet") {
                production {
                    input("obj.gold_bar")
                    input("obj.ruby")
                    statReq("stat.crafting", 50)
                    xp(850)
                    output("obj.unstrung_ruby_amulet")
                }
                columnRSCM(COL_MOULD, "obj.amulet_mould")
            }

            row("dbrow.crafting_diamond_ring") {
                production {
                    input("obj.gold_bar")
                    input("obj.diamond")
                    statReq("stat.crafting", 43)
                    xp(850)
                    output("obj.diamond_ring")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_diamond_necklace") {
                production {
                    input("obj.gold_bar")
                    input("obj.diamond")
                    statReq("stat.crafting", 56)
                    xp(900)
                    output("obj.diamond_necklace")
                }
                columnRSCM(COL_MOULD, "obj.necklace_mould")
            }
            row("dbrow.crafting_diamond_bracelet") {
                production {
                    input("obj.gold_bar")
                    input("obj.diamond")
                    statReq("stat.crafting", 58)
                    xp(950)
                    output("obj.jewl_diamond_bracelet")
                }
                columnRSCM(COL_MOULD, "obj.jewl_bracelet_mould")
            }
            row("dbrow.crafting_diamond_amulet") {
                production {
                    input("obj.gold_bar")
                    input("obj.diamond")
                    statReq("stat.crafting", 70)
                    xp(1000)
                    output("obj.unstrung_diamond_amulet")
                }
                columnRSCM(COL_MOULD, "obj.amulet_mould")
            }

            row("dbrow.crafting_dragonstone_ring") {
                production {
                    input("obj.gold_bar")
                    input("obj.dragonstone")
                    statReq("stat.crafting", 55)
                    xp(1000)
                    output("obj.dragonstone_ring")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_dragonstone_necklace") {
                production {
                    input("obj.gold_bar")
                    input("obj.dragonstone")
                    statReq("stat.crafting", 72)
                    xp(1050)
                    output("obj.dragonstone_necklace")
                }
                columnRSCM(COL_MOULD, "obj.necklace_mould")
            }
            row("dbrow.crafting_dragonstone_bracelet") {
                production {
                    input("obj.gold_bar")
                    input("obj.dragonstone")
                    statReq("stat.crafting", 74)
                    xp(1100)
                    output("obj.jewl_dragonstone_bracelet")
                }
                columnRSCM(COL_MOULD, "obj.jewl_bracelet_mould")
            }
            row("dbrow.crafting_dragonstone_amulet") {
                production {
                    input("obj.gold_bar")
                    input("obj.dragonstone")
                    statReq("stat.crafting", 80)
                    xp(1500)
                    output("obj.unstrung_dragonstone_amulet")
                }
                columnRSCM(COL_MOULD, "obj.amulet_mould")
            }

            row("dbrow.crafting_onyx_ring") {
                production {
                    input("obj.gold_bar")
                    input("obj.onyx")
                    statReq("stat.crafting", 67)
                    xp(1150)
                    output("obj.onyx_ring")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_onyx_necklace") {
                production {
                    input("obj.gold_bar")
                    input("obj.onyx")
                    statReq("stat.crafting", 82)
                    xp(1200)
                    output("obj.onyx_necklace")
                }
                columnRSCM(COL_MOULD, "obj.necklace_mould")
            }
            row("dbrow.crafting_onyx_bracelet") {
                production {
                    input("obj.gold_bar")
                    input("obj.onyx")
                    statReq("stat.crafting", 84)
                    xp(1250)
                    output("obj.jewl_onyx_bracelet")
                }
                columnRSCM(COL_MOULD, "obj.jewl_bracelet_mould")
            }
            row("dbrow.crafting_onyx_amulet") {
                production {
                    input("obj.gold_bar")
                    input("obj.onyx")
                    statReq("stat.crafting", 90)
                    xp(1650)
                    output("obj.unstrung_onyx_amulet")
                }
                columnRSCM(COL_MOULD, "obj.amulet_mould")
            }

            row("dbrow.crafting_zenyte_ring") {
                production {
                    input("obj.gold_bar")
                    input("obj.zenyte")
                    statReq("stat.crafting", 89)
                    xp(1500)
                    output("obj.zenyte_ring")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_zenyte_necklace") {
                production {
                    input("obj.gold_bar")
                    input("obj.zenyte")
                    statReq("stat.crafting", 92)
                    xp(1650)
                    output("obj.zenyte_necklace")
                }
                columnRSCM(COL_MOULD, "obj.necklace_mould")
            }
            row("dbrow.crafting_zenyte_bracelet") {
                production {
                    input("obj.gold_bar")
                    input("obj.zenyte")
                    statReq("stat.crafting", 95)
                    xp(1800)
                    output("obj.zenyte_bracelet")
                }
                columnRSCM(COL_MOULD, "obj.jewl_bracelet_mould")
            }
            row("dbrow.crafting_zenyte_amulet") {
                production {
                    input("obj.gold_bar")
                    input("obj.zenyte")
                    statReq("stat.crafting", 98)
                    xp(2000)
                    output("obj.unstrung_zenyte_amulet")
                }
                columnRSCM(COL_MOULD, "obj.amulet_mould")
            }

            row("dbrow.crafting_slayer_ring") {
                production {
                    input("obj.gold_bar")
                    input("obj.slayer_gem")
                    statReq("stat.crafting", 75)
                    xp(150)
                    output("obj.slayer_ring_8")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
            row("dbrow.crafting_slayer_ring_eternal") {
                production {
                    input("obj.gold_bar")
                    input("obj.slayer_eternal_gem")
                    statReq("stat.crafting", 75)
                    xp(150)
                    output("obj.slayer_ring_eternal")
                }
                columnRSCM(COL_MOULD, "obj.ring_mould")
            }
        }
    }

}

/**
 * A production table whose rows are grouped into [CraftingSectionScope]s. A section stamps every
 * row with its name ([Crafting.COL_SECTION]) and, when given one, a default `category` that a row
 * may still override inside its `production {}` block.
 */
private fun craftingTable(
    tableId: String,
    extraColumns: dev.openrune.definition.dbtables.DBTableBuilder.() -> Unit,
    block: CraftingTableScope.() -> Unit,
) = productionTable(tableId, serverOnly = true, extraColumns = extraColumns) {
    CraftingTableScope(this).block()
}

private class CraftingTableScope(private val table: ProductionTableScope) {
    fun section(name: String, category: String? = null, block: CraftingSectionScope.() -> Unit) {
        CraftingSectionScope(table, name, category).block()
    }
}

private class CraftingSectionScope(
    private val table: ProductionTableScope,
    private val section: String,
    private val category: String?,
) {
    /**
     * The section/category columns are written before [block] runs, so a row's own
     * `category(...)` (needlework's hide families) still wins - later writes replace earlier
     * ones for the same column.
     */
    fun row(rowId: String, block: ProductionTableRowScope.() -> Unit) {
        table.row(rowId) {
            column(Crafting.COL_SECTION, section)
            category?.let { column(ProductionColumns.COL_CATEGORY, it) }
            block()
        }
    }
}
