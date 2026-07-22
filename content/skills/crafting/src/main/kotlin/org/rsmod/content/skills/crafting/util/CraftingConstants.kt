package org.rsmod.content.skills.crafting.util

object CraftingConstants {

    const val STAT_CRAFTING = "stat.crafting"

    /**
     * How much xp is multiplied when establishing a recipe and how much it gets divided by. This is done to support fractional xp in the int fields.
     */
    const val FINE_XP_DIVISOR = 10

    /** Shared production queue used by every crafting section. */
    const val QUEUE_CRAFTING_MAKE = "queue.crafting_make"

    /** Crafts per spool of thread. */
    const val THREAD_USES_PER_SPOOL = 5

    const val NEEDLE = "obj.needle"
    const val THREAD = "obj.thread"
    const val CHISEL = "obj.chisel"
    const val HAMMER = "obj.hammer"
    const val KNIFE = "obj.knife"
    const val GLASSBLOWING_PIPE = "obj.glassblowingpipe"

    const val IMCANDO_HAMMER = "obj.imcando_hammer"
    const val IMCANDO_HAMMER_OFFHAND = "obj.imcando_hammer_offhand"

    const val CLOCKWORK = "obj.poh_clockwork_mechanism"

    const val ANIM_BIRDHOUSE = "seq.birdhouse_make"
    const val ANIM_BIRDHOUSE_IMCANDO = "seq.birdhouse_make_imcando_hammer"

    const val COINS = "obj.coins"

    const val GOLD_BAR = "obj.gold_bar"
    const val SILVER_BAR = "obj.silver_bar"

    const val CLAY = "obj.clay"
    const val SOFT_CLAY = "obj.softclay"
    const val BUCKET_EMPTY = "obj.bucket_empty"
    const val MOLTEN_GLASS = "obj.molten_glass"
    const val GIANT_SEAWEED = "obj.giant_seaweed"
    const val SEAWEED = "obj.seaweed"
    const val SODA_ASH = "obj.soda_ash"
    const val BUCKET_OF_SAND = "obj.bucket_sand"

    const val SLAYER_HELM = "obj.slayer_helm"

    const val REINFORCED_GOGGLES = "obj.slayer_reinforced_goggles"

    const val NOXIOUS_HALBERD = "obj.noxious_halberd"

    const val SERPENTINE_HELM = "obj.serpentine_helm"

    const val ARMADYL_CHESTPLATE = "obj.armadyl_chestplate"
    const val ARMADYL_CHAINSKIRT = "obj.armadyl_skirt"
    const val ARMADYL_HELMET = "obj.armadyl_helmet"
    const val MASORI_BODY_FORTIFIED = "obj.masori_body_fortified"
    const val MASORI_CHAPS_FORTIFIED = "obj.masori_chaps_fortified"
    const val MASORI_MASK_FORTIFIED = "obj.masori_mask_fortified"

    const val COSTUME_NEEDLE = "obj.costumeneedle"

    /**
     * A Porcine of Interest progress varbit. Completing the quest is what adds reinforced goggles
     * to the slayer helmet recipe.
     *
     * TODO(quest): confirm [PORCINE_COMPLETE_VALUE]. The varbit is a 5-bit field, so the completed
     *   value is somewhere in 1..31; 30 is a placeholder, not a verified number. Until the varbit
     *   resolves against the cache at all, [PORCINE_COMPLETE_FALLBACK] decides the answer.
     */
    const val VARBIT_PORCINE = "varbit.porcine"

    const val PORCINE_COMPLETE_VALUE = 30

    /** Used when [VARBIT_PORCINE] doesn't resolve against the cache. */
    const val PORCINE_COMPLETE_FALLBACK = true

    const val VARBIT_SLAYER_HELM_UNLOCKED = "varbit.slayer_helm_unlocked"
    const val VARBIT_SLAYER_RING_UNLOCKED = "varbit.slayer_ring_unlocked"

    const val CATEGORY_FURNACE = "category.furnace"

    val SPINNING_WHEELS: List<String> = listOf(
        "loc.viking_spinningwheel",
        "loc.elf_village_spinning_wheel",
        "loc.spinningwheel",
        "loc.contact_spinning_wheel",
        "loc.iznot_spinning_wheel",
        "loc.kr_spinningwheel",
        "loc.murder_qip_spinning_wheel",
        "loc.fossil_spinning_wheel_built",
        "loc.sw_spinningwheel_fixed",
        "loc.spinningwheel_quetzacali",
        "loc.spinningwheel_2",
        "loc.amenity_spinning_wheel_built",
    )

    /** Loom locs. */
    val LOOMS: List<String> = listOf(
        "loc.loom",
        "loc.regicide_loom",
        "loc.fossil_loom_built",
        "loc.amenity_loom_built",
    )

    /** Potter's wheel locs. */
    val POTTERY_WHEELS: List<String> = listOf( //Note: Category 377 are pottery wheels, but includes unbuilt and broken
        "loc.viking_potterywheel",
        "loc.potterywheel",
        "loc.contact_potterywheel",
        "loc.darkm_poor_potterywheel",
        "loc.sw_potterywheel_fixed",
        "loc.potterywheel_2",
        "loc.amenity_potterywheel_built",
    )

    /** Pottery oven locs. */
    val POTTERY_OVENS: List<String> = listOf( //Note: No category data on the ovens
        "loc.potteryoven",
        "loc.viking_potteryoven",
        "loc.amenity_potteryoven_built",
        "loc.fai_barbarian_pottery_oven",
        "loc.darkm_poor_pottery_oven",
    )

    /** Ellis - the Al Kharid tanner. */
    const val TANNER_ELLIS = "npc.ellis_tanner"
    const val TANNER_GUILD = "npc.tanner"

    /** Sbott - the Canifis tanner. */
    const val TANNER_SBOTT = "npc.werewolftanner"

    /** Chouani - the Great Kourend tanner. */
    const val TANNER_CHOUANI = "npc.auburn_tanner"

    /**  Mary, on the farm north of Hosidius. */
    const val TANNER_MARY_PREQUEST = "npc.ga_mary_1op"
    const val TANNER_MARY = "npc.ga_mary_2op"

    /** Eodan - tans in the Lizardman Caves. */
    const val TANNER_EODAN = "npc.hosdun_eodan"

    val KOUREND_DIARY_VARBITS: List<String> = listOf(
        "varbit.kourend_diary_easy_complete",
        "varbit.kourend_diary_medium_complete",
        "varbit.kourend_diary_hard_complete",
        "varbit.kourend_diary_elite_complete",
    )

    /**
     * Set once Mary has given her post-Getting Ahead speech, after which she greets the player as
     * a returning customer instead. Read/written defensively (see [CraftingGamevals.exists]); if
     * the varbit isn't in the cache she simply always uses the returning-customer greeting.
     *
     * TODO(gameval): confirm the real varbit name - this is the only place it is referenced.
     */
    const val VARBIT_MARY_TANNING_UNLOCKED = "varbit.ga_mary_tanning_unlocked"

    /** Active spellbook varbit; 2 = Lunar. Used for Eodan's magic-tanning aside. */
    const val VARBIT_SPELLBOOK = "varbit.spellbook"
    const val SPELLBOOK_LUNAR = 2

    /**
     * Every tanner NPC, for the handlers they all share (item-on-npc). Per-NPC dialogue is wired
     * individually in TanningScript; a new tanner with stock dialogue only needs a constant here
     * and a one-line registration there.
     */
    val TANNERS: List<String> = listOf(TANNER_ELLIS, TANNER_SBOTT, TANNER_GUILD, TANNER_CHOUANI)

    /** Thakkrad Sigmundson */
    const val YAK_CURER = "npc.fris_r_engineer"

    const val CRAFTING_TUTOR = "npc.aide_tutor_crafting"


    /** Superglass Make spell obj (Lunar spellbook). */
    const val SPELL_SUPERGLASS_MAKE = "obj.77_superglass"

    /** Crafting xp per molten glass produced by Superglass Make. */
    const val SUPERGLASS_XP_PER_GLASS = 10.0

    /** Average molten glass multiplier for Superglass Make (1.3x, rolled per pair). */
    const val SUPERGLASS_BONUS_CHANCE = 0.3

    const val SUPERGLASS_ANIM = "seq.human_castlunar"
    const val SUPERGLASS_SOUND = "synth.superglass_make_cast"


    // region Animations / sounds
    //
    // These are the easily-swappable animation definitions. Sequences are all seq.* gamevals from
    // research; swap a name here if the wrong one is playing. Every anim/sound is resolved through
    // CraftingGamevals.optional(), so a missing name is a silent no-op rather than a crash.
    //
    // Two kinds of animation are involved for facility crafting:
    //  - the PLAYER animation (ANIM_*), played on the player each craft cycle, and
    //  - the LOC animation (LOC_ANIM_*), played on the facility loc itself.
    // Section DEFAULTS are defined here and wired up in CraftingSection; recipes with unique
    // animations (dragonhide shields, gem cutting, spiky vambraces) carry theirs on their table
    // row via the `anim` column.

    // --- Spinning -----------------------------------------------------------------------------
    // Player spinning animation differs by wheel: some locs use the 60-frame variant, others the
    // 90-frame variant. Map specific wheel locs to the 90 variant here; everything else uses 60.
    const val ANIM_SPINNING_60 = "seq.human_spinningwheel_60"
    const val ANIM_SPINNING_90 = "seq.human_spinningwheel_90"
    const val ANIM_SPINNING = ANIM_SPINNING_60

    /** Wheel locs that use the 90-frame spinning animation instead of the default 60-frame one. */
    val SPINNING_WHEELS_90: Set<String> = setOf(
        // e.g. "loc.spinning_wheel_lumbridge" - populate as confirmed.
    )

    const val LOC_ANIM_SPINNING = "seq.spinningwheel"
    const val SOUND_SPINNING = "synth.spinning"

    const val ANIM_WEAVING = "seq.farming_useloom"
    const val LOC_ANIM_WEAVING = "seq.loom"
    const val SOUND_WEAVING = "synth.loom_weave"

    const val ANIM_POTTERY_WHEEL = "seq.human_potterywheel"
    const val LOC_ANIM_POTTERY_WHEEL = "seq.potterywheel"
    const val SOUND_POTTERY_WHEEL = "synth.crafting_pottery_wheel_craft"
    const val ANIM_POTTERY_OVEN = "seq.potteryoven_quick"

    const val ANIM_LEATHER_CRAFT = "seq.human_leather_crafting"
    const val SOUND_LEATHER_CRAFT = "synth.stiching"

    const val ANIM_PHEASANT_COSTUME = "seq.human_pheasant_feathers_crafting"

    const val ANIM_GEM_CUTTING = "seq.human_gem_cutting"
    const val SOUND_GEM_CUTTING = "synth.chisel"
    const val SOUND_GEM_CRUSH = "synth.smash_gem"
    const val ANIM_AMETHYST_CUT = "seq.human_amethystcutting"
    const val ANIM_SNAIL_SHELL_CUT = "seq.human_snailshellcutting"
    const val ANIM_KNIFE_CUTTING = "seq.human_cutting_knife"
    const val ANIM_LIMESTONE_CUT = "seq.human_limestonecutting"

    // --- Glass --------------------------------------------------------------------------------
    const val ANIM_GLASSBLOWING = "seq.human_glassblowing"
    const val SOUND_GLASSBLOWING = "synth.glassblowing"

    /** Furnace work (glass smelting + jewellery) shares the smithing furnace animation. */
    const val ANIM_FURNACE = "seq.human_furnace"
    const val SOUND_FURNACE = "synth.furnace"

    // --- Battlestaves -------------------------------------------------------------------------
    /** Battlestaff assembly sequence (7531 human_battlestaff_crafting, 3 ticks). */
    const val ANIM_BATTLESTAFF = "seq.human_battlestaff_crafting"
    /** Orb-attach sound, played when an orb is fixed to a battlestaff. */
    const val SOUND_BATTLESTAFF_ATTACH = "synth.attach_orb"

    // Per-orb spot animations (306/1370/1371/1372) live on the battlestaves table rows via the
    // `spotanim` column - not here.

    // --- Amulet stringing ---------------------------------------------------------------------
    /** Stringing sound, shared by amulet stringing and the strung rabbit foot. */
    const val SOUND_AMULET_STRINGING = "synth.stringing"

    // --- Sand pit (fill an empty bucket with sand) --------------------------------------------
    /** Player animation for filling a bucket at a sand pit. */
    const val ANIM_SAND_PIT = "seq.human_fillbucket_sandpit"
    /** Sound played on each bucket fill. */
    const val SOUND_SAND_BUCKET = "synth.sand_bucket"
    /**
     * Content group shared by every sand pit loc (tagged in .data/raw-cache/server/loc.toml);
     * SandPitScript hooks the empty-bucket click against this group so all sand pits behave alike.
     */
    const val CONTENT_SAND_PIT = "content.sandpit"

    // endregion
    // region Interfaces

    const val INTERFACE_TANNER = "interface.tanner"
    const val INTERFACE_SILVER_CRAFTING = "interface.silver_crafting"
    const val INTERFACE_GOLD_CRAFTING = "interface.crafting_gold"

    // The tanner interface's 4x2 hide grid uses one slot group per letter a-h; each group has a
    // model, a name text, a price text, and four stacked op buttons (Tan 1 / 5 / X / All).

    /** Model zoom for the tanner interface's slot model components. Higher = larger */
    const val TANNER_MODEL_ZOOM = 260

    fun tannerSlotModel(letter: Char): String = "component.tanner:tanning_${letter}_model"

    fun tannerSlotName(letter: Char): String = "component.tanner:tanning_${letter}_text"

    fun tannerSlotPrice(letter: Char): String = "component.tanner:tanning_${letter}_price"

    /** [op] is one of "1", "5", "x", "all". */
    fun tannerSlotOp(letter: Char, op: String): String = "component.tanner:tanning_${letter}_$op"

    /**
     * A component of the gold crafting interface, by its name in the interface JSON - the product
     * slots (`gold_ring`, `sapphire_amulet`, ...), the per-section mould layers (`need_ring_mould`
     * / `got_ring_mould`), and the quantity buttons (`make_1` ... `make_all`).
     */
    fun goldComponent(name: String): String = "component.crafting_gold:$name"

    /** A component of the silver crafting interface, by its name in the interface JSON. */
    fun silverComponent(name: String): String = "component.silver_crafting:$name"


    /**
     * Last-selected-item varbits for the jewellery interfaces - these already exist in the cache
     * (13892 / 13893, confirmed against rev 239) and must NOT be re-declared in gamevals.toml.
     * The gold interface writes its one to highlight the last recipe; silver is deferred.
     */
    const val VARBIT_GOLD_LASTTYPE = "varbit.crafting_gold_item_lasttype"
    const val VARBIT_SILVER_LASTTYPE = "varbit.crafting_silver_item_lasttype"

    /**
     * Selected make-quantity, shared by the `skillmain` quantity column across skill interfaces.
     * Both sides write it: the client on a button click (locally - those writes never reach the
     * server), and the server for anything the client can't work out alone, such as the "X" amount.
     */
    const val VARP_MAKEX_CRAFTING = "varp.makexcrafting"

    // endregion
}
