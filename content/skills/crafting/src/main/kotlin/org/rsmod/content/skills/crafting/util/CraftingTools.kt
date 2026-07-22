package org.rsmod.content.skills.crafting.util

import org.rsmod.api.player.protect.ProtectedAccess

/**
 * Tool equivalency.
 *
 * A recipe's tools are named by a single canonical gameval (the section's `tools` list, or a
 * per-recipe extra such as a mould), but some tools have accepted stand-ins. [TOOL_EQUIVALENTS]
 * maps a canonical tool to the full list of objs that satisfy it: if a recipe needs a tool from
 * the list and the player has *any one* of them, the recipe works - and any of them clicked on an
 * ingredient starts the craft (see registerHeldCrafting). Tools without an entry are only
 * satisfied by themselves.
 *
 * Today the only group is the hammer: the imcando hammer counts everywhere a hammer does, in its
 * held form and its off-hand form (`imcando_hammer_offhand`, a distinct obj). Both are also
 * wieldable, so they additionally satisfy the requirement from a worn slot ([WORN_TOOLS]) -
 * though a worn one can't be the *dragged* item, since the engine has no "use worn item on
 * inventory item".
 *
 * The one deliberately non-generic imcando behaviour is its animation swap: a recipe with an
 * `imcandoAnim` plays it whenever [holdsImcandoHammer] is true (see CraftingWorker.craftAnim).
 * That is the only tool-specific rule in the module, and the only place it would ever grow.
 */

/** Every obj that satisfies a canonical tool, keyed by the tool's gameval. */
private val TOOL_EQUIVALENTS: Map<String, List<String>> = mapOf(
    CraftingConstants.HAMMER to listOf(
        CraftingConstants.HAMMER,
        CraftingConstants.IMCANDO_HAMMER,
        CraftingConstants.IMCANDO_HAMMER_OFFHAND,
    ),
    // The costume needle stands in for a needle everywhere. Holding it also waives the thread
    // requirement entirely (see CraftingWorker.holdsCostumeNeedle).
    CraftingConstants.NEEDLE to listOf(
        CraftingConstants.NEEDLE,
        CraftingConstants.COSTUME_NEEDLE,
    ),
)

/** Tools that also satisfy a requirement from a worn slot (either hand). */
private val WORN_TOOLS: Set<String> = setOf(
    CraftingConstants.IMCANDO_HAMMER,
    CraftingConstants.IMCANDO_HAMMER_OFFHAND,
)

/** The objs that satisfy [tool], the tool itself included. */
fun toolEquivalents(tool: String): List<String> = TOOL_EQUIVALENTS[tool] ?: listOf(tool)

/** Whether the player has [tool] or any of its equivalents - in the inventory, or worn for [WORN_TOOLS]. */
fun ProtectedAccess.hasCraftingTool(tool: String): Boolean =
    toolEquivalents(tool).any { inv.contains(it) || (it in WORN_TOOLS && it in player.worn) }

/** True if the player is holding an imcando hammer - in the inventory or worn (either hand). */
fun ProtectedAccess.holdsImcandoHammer(): Boolean =
    IMCANDO_HAMMERS.any { inv.contains(it) || it in player.worn }

/** True if the player is holding a costume needle, which waives the thread requirement. */
fun ProtectedAccess.holdsCostumeNeedle(): Boolean =
    inv.contains(CraftingConstants.COSTUME_NEEDLE)

private val IMCANDO_HAMMERS =
    listOf(CraftingConstants.IMCANDO_HAMMER, CraftingConstants.IMCANDO_HAMMER_OFFHAND)
