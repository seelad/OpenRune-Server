package org.rsmod.content.skills.crafting

import org.rsmod.content.skills.SkillingActionType
import org.rsmod.content.skills.crafting.util.CraftingConstants

/**
 * Display names a [CraftingSection]'s message templates can interpolate: the (lowercased) names
 * of a recipe's primary input and its output.
 */
data class CraftingNames(val input: String, val output: String)

/** How a section's recipes are started and produced. */
enum class CraftingMode {
    /** Selection prompt then the queued production loop (the default). */
    MENU,
    /** One craft per click, no menu, no queue - as fast as the player can click (limestone). */
    INSTANT,
    /** Single component-on-component craft, resolved instantly (slayer helmet, fang staves, ...). */
    COMBINE,
    /** Not a craft at all: rows are a price list read by other content (tanning). */
    SERVICE,
}

/**
 * A crafting *section*: the bundle of defaults shared by a group of recipes - tick pacing,
 * animations, sounds, tools, thread use, mode, and message templates. Every table row names its
 * section, and anything the row leaves unset resolves to these defaults; the resolution happens
 * once, in [craftingProduct].
 *
 * Sections are NOT mechanical variants. Every hand section runs through the identical
 * registration and production pipeline (`registerHeldCrafting` + `CraftingWorker`), and every
 * facility section through the same loc wiring; the only mechanical fork is [mode]. What
 * separates two sections is purely which defaults their recipes fall back to.
 *
 * **When to add a section vs. a row override:** add a section when a *group* of recipes shares
 * defaults no existing section provides - a distinct message family, tool set, pacing, or
 * animation/sound family (shields hammer-and-nail while needlework stitches; gems cut in 2 ticks
 * with a crush-on-failure sound while needlework takes 3 and never fails). A *one-off* deviation
 * never justifies a section: recipes override their own `ticks`, `anim`, `spotanim`, `tool`,
 * `sound`, `message`, and `action_name` directly on their table row (the bone staff's chisel, the
 * rabbit foot's "You string the {input}." line). When a candidate group is one or two recipes,
 * default to row overrides on the closest existing section - that is how the old
 * Assembly/ChiselAttachment/Attachment/MiscStringing quartet collapsed into [COMBINING].
 *
 * The same shape extends beyond Crafting: a skill with tool + materials + optional facility
 * recipes (Construction, say) can reuse [craftingProduct]/`registerHeldCrafting` wholesale -
 * define its sections, point rows at them, override per-recipe quirks on the rows.
 */
enum class CraftingSection(
    /** Matches the table rows' `section` column. */
    val id: String,
    /** Verb shown by the selection prompt ("spin", "cut", "fire", ...). */
    val verb: String,
    /** Action type used by the shared multiskill selection prompt. */
    val actionType: SkillingActionType,
    /** Default game ticks per craft; a recipe's `ticks` column overrides this. */
    val ticks: Int,
    val mode: CraftingMode = CraftingMode.MENU,
    /** Default player animation gameval; a recipe's `anim` column overrides this. */
    val anim: String? = null,
    /**
     * Animation played instead of [anim] when the player holds an imcando hammer (worn or in the
     * inventory). Only birdhouses have one; every other section always uses [anim].
     */
    val imcandoAnim: String? = null,
    /** Default facility (loc) animation gameval, for facility-based sections. */
    val locAnim: String? = null,
    /** Default sound synth gameval played each craft cycle; a recipe's `sound` column overrides. */
    val sound: String? = null,
    /** Sound synth gameval played when a craft's success roll fails (e.g. the gem crush). */
    val failureSound: String? = null,
    /**
     * Tools that must be held but are never consumed. Each tool accepts its equivalents (the
     * imcando hammer for the hammer) - see CraftingTools. A recipe's `tool` column adds to this.
     */
    val tools: List<String> = emptyList(),
    /** Whether crafts of this section consume thread charges (1 spool = 5 crafts). */
    val consumesThread: Boolean = false,
    /**
     * True for inputs that own a default "use on anything" handler elsewhere (birdhouse logs:
     * barbarian firemaking). A click pair containing one is keyed under it, so this section's
     * handler wins from either drag direction - see registerHeldCrafting.
     */
    val ownsDefaultHandler: (input: String) -> Boolean = { false },
    /** Completes "You need a Crafting level of at least X to <actionName>." */
    val actionName: (CraftingNames) -> String,
    /**
     * Message sent at cycle *start* (when the anim/sound fire), or null for none. Only pottery
     * firing has one today - it messages "You put the {output} in the oven." here so the oven's
     * two-message flow ("put in" then "remove") plays out over the full cycle.
     */
    val startMessage: (CraftingNames) -> String? = { null },
    /** Message sent on a successful craft, or null for none; `message` column overrides. */
    val successMessage: (CraftingNames) -> String? = { null },
    /** Message sent when the success roll fails, or null for none. */
    val failureMessage: (CraftingNames) -> String? = { null },
    /**
     * Dialogue shown (as an OSRS `mesbox`) when the player opens the section's selection menu
     * and no recipe is currently pickable - i.e., the recipe filter comes back empty. Null
     * (default) leaves the menu silently unopened, matching the existing behavior for sections
     * that always have at least one always-shown recipe.
     */
    val emptyMenuMessage: () -> String? = { null },
    /**
     * Dialogue shown (as an OSRS `mesbox`) when the player picks a recipe from the selection menu
     * but doesn't hold its inputs. When null (default), the deficient-input message falls through
     * to the plain "You don't have enough X for that!" spam line - which is what most crafting
     * sections want. Pottery firing overrides this because its always-shown defaults (pot, pie
     * dish, bowl) can be picked without the unfired input in hand, and OSRS answers that with a
     * dedicated dialogue rather than a chat line.
     */
    val missingInputMessage: (CraftingNames) -> String? = { null },
) {

    // ---- Facilities (crafting locs) ----

    SPINNING(
        id = "Spinning",
        verb = "spin",
        actionType = SkillingActionType.SPIN,
        ticks = 3,
        anim = CraftingConstants.ANIM_SPINNING,
        locAnim = CraftingConstants.LOC_ANIM_SPINNING,
        sound = CraftingConstants.SOUND_SPINNING,
        actionName = { "spin ${it.output}" },
        successMessage = { "You spin the ${it.input} into ${it.output}." },
        emptyMenuMessage = { "You don't have anything suitable to spin at this spinning wheel." },
    ),

    WEAVING(
        id = "Weaving",
        verb = "weave",
        actionType = SkillingActionType.WEAVE,
        ticks = 3,
        anim = CraftingConstants.ANIM_WEAVING,
        locAnim = CraftingConstants.LOC_ANIM_WEAVING,
        sound = CraftingConstants.SOUND_WEAVING,
        actionName = { "weave ${it.output}" },
        // The loom sends no chat line on a successful weave.
        successMessage = { null },
        emptyMenuMessage = {
            "You either don't have the required items or don't have enough of them to weave " +
                "anything at this loom."
        },
    ),

    POTTERY_SHAPING(
        id = "PotteryShaping",
        verb = "make",
        actionType = SkillingActionType.MAKE,
        ticks = 3,
        anim = CraftingConstants.ANIM_POTTERY_WHEEL,
        locAnim = CraftingConstants.LOC_ANIM_POTTERY_WHEEL,
        sound = CraftingConstants.SOUND_POTTERY_WHEEL,
        actionName = { "make ${it.output}" },
        // Strip the "unfired " prefix off the output name: shaping outputs are all `unfired ...`
        // in the cache, but the in-game message names them by the finished form ("a bowl" not
        // "a unfired bowl"). Article picked by the same withArticle vowel test as glassblowing.
        successMessage = { "You make the clay into ${it.output.removePrefix("unfired ").withArticle()}." },
        emptyMenuMessage = { "You don't have anything suitable to craft with." },
    ),

    /**
     * Pottery oven. Two intentional deviations from the wheel:
     * - No `locAnim`. The oven doesn't animate at all - the "tiny movement" seen when we tried
     *   to play one on it is the loc being nudged into/out of an animation slot. Leave it alone.
     * - Two messages, one per cycle end: [startMessage] "You put the {output} in the oven." fires
     *   with the anim/sound; [successMessage] "You remove the {output} from the oven." fires when
     *   the fired item lands in the inventory, at cycle end.
     *
     * Item names use the *output* rather than the input - "unfired pot" -> "pot" in both lines,
     * as in-game.
     */
    POTTERY_FIRING(
        id = "PotteryFiring",
        verb = "fire",
        actionType = SkillingActionType.FIRE,
        ticks = 7,
        anim = CraftingConstants.ANIM_POTTERY_OVEN,
        sound = CraftingConstants.SOUND_FURNACE,
        actionName = { "fire ${it.output}" },
        startMessage = { "You put the ${it.output} in the oven." },
        successMessage = { "You remove the ${it.output} from the oven." },
        failureMessage = { "The clay cracks in the oven and is ruined." },
        missingInputMessage = { "You don't have any ${it.output.plural()} which need firing." },
    ),

    GLASS_SMELTING(
        id = "GlassSmelting",
        verb = "smelt",
        actionType = SkillingActionType.SMELT,
        ticks = 3,
        anim = CraftingConstants.ANIM_FURNACE,
        sound = CraftingConstants.SOUND_FURNACE,
        actionName = { "smelt molten glass" },
        successMessage = { "You heat the sand and soda ash in the furnace to make glass." },
    ),

    // ---- Hand crafting (tool or ingredient on an ingredient, in the inventory) ----

    NEEDLEWORK(
        id = "Needlework",
        verb = "make",
        actionType = SkillingActionType.MAKE,
        ticks = 3,
        anim = CraftingConstants.ANIM_LEATHER_CRAFT,
        sound = CraftingConstants.SOUND_LEATHER_CRAFT,
        tools = listOf(CraftingConstants.NEEDLE),
        consumesThread = true,
        actionName = { "make ${it.output}" },
        successMessage = { "You make ${it.output}." },
    ),

    /**
     * Pheasant costume: needlework in all but its animation - needle and thread on pheasant
     * feathers, using the Forestry sewing sequence. Shares the needlework tool and thread rules, so
     * the costume needle removes the thread requirement here too (see CraftingWorker).
     */
    PHEASANT_COSTUME(
        id = "PheasantCostume",
        verb = "make",
        actionType = SkillingActionType.MAKE,
        ticks = 3,
        anim = CraftingConstants.ANIM_PHEASANT_COSTUME,
        sound = CraftingConstants.SOUND_LEATHER_CRAFT,
        tools = listOf(CraftingConstants.NEEDLE),
        consumesThread = true,
        actionName = { "make ${it.output}" },
        successMessage = { "You make ${it.output}." },
    ),

    /** Hammered shields: every recipe carries its own animation via the table's `anim` column. */
    SHIELDS(
        id = "Shields",
        verb = "make",
        actionType = SkillingActionType.MAKE,
        ticks = 3,
        sound = CraftingConstants.SOUND_LEATHER_CRAFT,
        tools = listOf(CraftingConstants.HAMMER),
        actionName = { "make a ${it.output}" },
        successMessage = { "You nail the pieces together to make a ${it.output}." },
    ),

    /** Snelm and crab-armour carving. */
    CARVING(
        id = "Carving",
        verb = "cut",
        actionType = SkillingActionType.CUT,
        ticks = 3,
        anim = CraftingConstants.ANIM_SNAIL_SHELL_CUT,
        sound = CraftingConstants.SOUND_GEM_CUTTING,
        tools = listOf(CraftingConstants.CHISEL),
        actionName = { "carve a ${it.output}" },
        successMessage = { "You carve the ${it.input} into a ${it.output}." },
    ),

    KNIFE(
        id = "Knife",
        verb = "cut",
        actionType = SkillingActionType.CUT,
        ticks = 1,
        anim = CraftingConstants.ANIM_KNIFE_CUTTING,
        tools = listOf(CraftingConstants.KNIFE),
        actionName = { "make a ${it.output}" },
        // No section-wide success line: the dramen staff sets its own via the message column, and
        // the sinew recipe has none.
        successMessage = { null },
    ),

    GEMS(
        id = "Gems",
        verb = "cut",
        actionType = SkillingActionType.CUT,
        ticks = 2,
        anim = CraftingConstants.ANIM_GEM_CUTTING,
        sound = CraftingConstants.SOUND_GEM_CUTTING,
        failureSound = CraftingConstants.SOUND_GEM_CRUSH,
        tools = listOf(CraftingConstants.CHISEL),
        actionName = { "cut ${it.output}s" },
        successMessage = { "You cut the ${it.output}." },
        failureMessage = { "You mis-hit the chisel and smash the ${it.output} to pieces!" },
    ),

    AMETHYST(
        id = "Amethyst",
        verb = "cut",
        actionType = SkillingActionType.CUT,
        ticks = 2,
        anim = CraftingConstants.ANIM_AMETHYST_CUT,
        sound = CraftingConstants.SOUND_GEM_CUTTING,
        tools = listOf(CraftingConstants.CHISEL),
        actionName = { "cut ${it.output}" },
        successMessage = { "You carefully cut the amethyst into ${it.output}." },
    ),

    /** One brick per click ([CraftingMode.INSTANT]), so [ticks] is nominal. */
    LIMESTONE(
        id = "Limestone",
        verb = "cut",
        actionType = SkillingActionType.CUT,
        ticks = 1,
        mode = CraftingMode.INSTANT,
        anim = CraftingConstants.ANIM_LIMESTONE_CUT,
        sound = CraftingConstants.SOUND_GEM_CUTTING,
        tools = listOf(CraftingConstants.CHISEL),
        actionName = { "cut the limestone" },
        successMessage = { "You cut the limestone into a brick." },
        failureMessage = { "You accidentally crush the limestone to bits." },
    ),

    GLASSBLOWING(
        id = "Glassblowing",
        verb = "make",
        actionType = SkillingActionType.MAKE,
        ticks = 3,
        anim = CraftingConstants.ANIM_GLASSBLOWING,
        sound = CraftingConstants.SOUND_GLASSBLOWING,
        tools = listOf(CraftingConstants.GLASSBLOWING_PIPE),
        actionName = { "make ${it.output}" },
        successMessage = { "You make ${it.output.withArticle()}." },
    ),

    BATTLESTAVES(
        id = "Battlestaves",
        verb = "make",
        actionType = SkillingActionType.MAKE,
        ticks = 2,
        anim = CraftingConstants.ANIM_BATTLESTAFF,
        sound = CraftingConstants.SOUND_BATTLESTAFF_ATTACH,
        actionName = { "make a ${it.output}" },
        successMessage = { "You attach the orb to the staff, making a ${it.output}." },
    ),

    AMULET_STRINGING(
        id = "AmuletStringing",
        verb = "string",
        actionType = SkillingActionType.MAKE,
        ticks = 2,
        sound = CraftingConstants.SOUND_AMULET_STRINGING,
        actionName = { "string ${it.output}" },
        successMessage = { "You string the amulet." },
    ),

    /**
     * Birdhouses: a log and a clockwork, with a hammer and chisel held. Mechanically an ordinary
     * hand section; what earns it a section is its default bundle - two tools, its own animation
     * with an imcando variant, and the log-priority quirk: logs own a default "use logs on
     * anything" handler (barbarian firemaking), so [ownsDefaultHandler] keys every click pair
     * under the log.
     */
    BIRDHOUSES(
        id = "Birdhouses",
        verb = "make",
        actionType = SkillingActionType.MAKE,
        ticks = 3,
        anim = CraftingConstants.ANIM_BIRDHOUSE,
        imcandoAnim = CraftingConstants.ANIM_BIRDHOUSE_IMCANDO,
        tools = listOf(CraftingConstants.HAMMER, CraftingConstants.CHISEL),
        ownsDefaultHandler = { it != CraftingConstants.CLOCKWORK },
        actionName = { "make a ${it.output}" },
    ),

    /**
     * Soft clay: use any water container on clay. Each row is a distinct water source (bowl,
     * bucket, jug, cup) so the empty container comes back as the recipe's second `output(...)`;
     * auto-craft can only continue from the source that started it (one recipe per source). Runs
     * through the shared multiskill menu at 2 ticks per craft. The two OSRS lines are folded into
     * one `<br>`-delimited message here - both fire in the same tick, so a start-then-success
     * split (which would put them on different ticks) would visibly mis-time the second line.
     */
    SOFT_CLAY_MIXING(
        id = "SoftClayMixing",
        verb = "mix",
        actionType = SkillingActionType.MAKE,
        ticks = 2,
        actionName = { "mix soft clay" },
        successMessage = { "You mix the clay and water.<br>You now have some soft workable clay." },
    ),

    /**
     * Component-on-component combines: the slayer helmet, the noxious halberd, the fang staves,
     * the accursed sceptre, the strung rabbit foot. One section for all of them - the defaults
     * are the plain "attach" shape (no tool, attach-style message, instant), and the recipes that
     * deviate say so on their rows: the fang staves and bone staff carry a chisel `tool` and the
     * chisel `sound`, the assemblies and the rabbit foot carry their own `message`/`action_name`.
     */
    COMBINING(
        id = "Combining",
        verb = "make",
        actionType = SkillingActionType.MAKE,
        ticks = 1,
        mode = CraftingMode.COMBINE,
        actionName = { "make a ${it.output}" },
        successMessage = { "You attach the ${it.input}, making a ${it.output}." },
    ),

    // ---- Furnace jewellery (bespoke interfaces; silent by design) ----

    /** Both the silver and gold tables; their `category` column tells the two apart. */
    JEWELLERY(
        id = "Jewellery",
        verb = "make",
        actionType = SkillingActionType.MAKE,
        ticks = 3,
        anim = CraftingConstants.ANIM_FURNACE,
        sound = CraftingConstants.SOUND_FURNACE,
        actionName = { "make ${it.output}" },
    ),

    // ---- Loc interactions riding the crafting pipeline ----

    /**
     * Sand pit: fill an empty bucket with sand. A loc interaction rather than a real skill, but it
     * rides the crafting pipeline for the menu, animation, and per-fill sound. SandPitScript builds
     * it from the SandPit table rows and hooks the empty-bucket click on the sand-pit content group.
     */
    SAND_PIT(
        id = "SandPit",
        verb = "fill",
        actionType = SkillingActionType.MAKE,
        ticks = 3,
        anim = CraftingConstants.ANIM_SAND_PIT,
        sound = CraftingConstants.SOUND_SAND_BUCKET,
        actionName = { "fill a bucket with sand" },
        successMessage = { "You fill the bucket with sand." },
    ),

    // ---- Services ----

    /** Tanning price list - not a craft; the tanner interface and NPCs read the rows directly. */
    TANNING(
        id = "Tanning",
        verb = "tan",
        actionType = SkillingActionType.MAKE,
        ticks = 0,
        mode = CraftingMode.SERVICE,
        actionName = { "tan ${it.output}" },
    );

    companion object {
        private val byId: Map<String, CraftingSection> = entries.associateBy { it.id }

        /** The section a table row names; unknown values are a data error worth failing loudly on. */
        fun byId(id: String): CraftingSection =
            requireNotNull(byId[id]) { "Unknown crafting section: '$id'" }
    }
}

/**
 * Simple English pluralization for item names used in section message templates. Handles the
 * ends-in-sibilant case ("pie dish" -> "pie dishes") that a naive `+"s"` misses; every other
 * pottery-firing output currently pluralizes via the default `+"s"` path.
 */
internal fun String.plural(): String = when {
    endsWith("s") || endsWith("x") || endsWith("z") ||
        endsWith("sh") || endsWith("ch") -> "${this}es"
    else -> "${this}s"
}

/**
 * Prepends the correct indefinite article: "an orb", "a beer glass". First-letter vowel test,
 * so it stumbles on "an hour" / "a university" edge cases - none of which appear in the current
 * crafting outputs, and any that do arrive later can override the message on their row.
 */
internal fun String.withArticle(): String =
    if (firstOrNull()?.lowercaseChar() in setOf('a', 'e', 'i', 'o', 'u')) "an $this" else "a $this"
