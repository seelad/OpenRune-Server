package org.rsmod.content.skills.crafting.scripts

import org.rsmod.api.player.output.soundSynth
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.table.crafting.CraftingHandRow
import org.rsmod.content.skills.Material
import org.rsmod.content.skills.crafting.CraftingMode
import org.rsmod.content.skills.crafting.CraftingProduct
import org.rsmod.content.skills.crafting.canCraft
import org.rsmod.content.skills.crafting.craftInstantly
import org.rsmod.content.skills.crafting.craftOnce
import org.rsmod.content.skills.crafting.registerHeldCrafting
import org.rsmod.content.skills.crafting.toCraftingProduct
import org.rsmod.content.skills.crafting.util.CraftingConstants
import org.rsmod.content.skills.crafting.util.malevolentMasqueradeUnlocked
import org.rsmod.content.skills.crafting.util.porcineOfInterestComplete
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Held (inventory) crafting: every recipe of the `crafting_hand` table, registered uniformly.
 *
 * There is deliberately nothing per-section here. Each row names its section, the shared adapter
 * resolves it against the section's defaults, and `registerHeldCrafting` wires up the same click
 * behaviour for every recipe - tool on ingredient, ingredient on ingredient, either order. How a
 * click then plays out is the section's [CraftingMode]: a selection menu (most sections), one
 * craft per click (limestone), or the combine flow below (slayer helmet, noxious halberd, fang
 * staves, ...). Rows of [CraftingMode.SERVICE] sections (tanning) aren't crafts and are skipped;
 * the tanner content reads them itself.
 *
 * The combine flow is the only bespoke logic left: a Slayer-unlock gate on the slayer helmet, the
 * helmet's quest-conditional goggles, and a confirmation prompt for irreversible recipes.
 */
class HeldCraftingScript : PluginScript() {

    override fun ScriptContext.startup() {
        val products = CraftingHandRow.all()
            .map { it.toCraftingProduct() }
            .filter { it.section.mode != CraftingMode.SERVICE }
        registerHeldCrafting(products) { combine(it) }
    }

    /** The prompt shown before an irreversible combine. */
    /**
     * A yes/no confirmation for a combine. [warning] is an optional message box shown before the
     * prompt; [title] is the prompt itself. If [secondTitle] is set, a confirmed first prompt is
     * followed by a second one whose options are reversed (No first, then Yes) - the craft only goes
     * ahead if that one is confirmed too. After a confirmed craft, a non-null [resultDialogue] is
     * shown in an item box (and the row's spam line suppressed); a null one leaves the row's spam
     * line to play instead.
     */
    private class Confirmation(
        val title: String,
        val warning: String? = null,
        val resultDialogue: String? = null,
        val secondTitle: String? = null,
    )

    /**
     * Recipes that ask before they commit, because the parts can't be recovered. The amulet of
     * rancour joins the noxious halberd here once it exists; nothing else needs to change.
     */
    private val confirmations: Map<String, Confirmation> = mapOf(
        CraftingConstants.NOXIOUS_HALBERD to Confirmation(
            title = "Do you wish to create a noxious halberd?",
            warning = "Do you wish to combine all three pieces to create a noxious halberd?<br>" +
                "This process is non-reversible",
            resultDialogue = "You successfully create a noxious halberd.",
        ),
        // Masori fortifying: confirm, then a result box (no spam line).
        CraftingConstants.MASORI_BODY_FORTIFIED to Confirmation(
            title = "Fortify your Masori body with 4 Armadylean plates?",
            resultDialogue = "You use 4 Armadylean plates to fortify the Masori body.",
        ),
        CraftingConstants.MASORI_CHAPS_FORTIFIED to Confirmation(
            title = "Fortify your Masori chaps with 3 Armadylean plates?",
            resultDialogue = "You use 3 Armadylean plates to fortify the Masori chaps.",
        ),
        CraftingConstants.MASORI_MASK_FORTIFIED to Confirmation(
            title = "Fortify your Masori mask with 1 Armadylean plate?",
            resultDialogue = "You use 1 Armadylean plate to fortify the Masori mask.",
        ),
    )

    /**
     * Confirmations keyed by input piece rather than output, for combines whose output isn't unique.
     * The Armadyl breakdowns all produce Armadylean plates, so they're identified by the piece being
     * broken down; each ends on the row's spam line (no result dialogue).
     */
    private val confirmationsByInput: Map<String, Confirmation> = mapOf(
        CraftingConstants.ARMADYL_CHESTPLATE to Confirmation(
            title = "Break apart your Armadyl chestplate into 4 Armadylean plates?",
            secondTitle = "Really break apart your Armadyl chestplate into 4 Armadylean plates?",
        ),
        CraftingConstants.ARMADYL_CHAINSKIRT to Confirmation(
            title = "Break apart your Armadyl chainskirt into 3 Armadylean plates?",
            secondTitle = "Really break apart your Armadyl chainskirt into 3 Armadylean plates?",
        ),
        CraftingConstants.ARMADYL_HELMET to Confirmation(
            title = "Break apart your Armadyl helmet into 1 Armadylean plate?",
            secondTitle = "Really break apart your Armadyl helmet into 1 Armadylean plate?",
        ),
    )

    /**
     * Combines that end on a result dialogue instead of a chat line - the same item box the
     * confirmed combines show, but with no confirmation prompt and no spam message. Keyed by output.
     */
    private val resultDialogues: Map<String, String> = mapOf(
        CraftingConstants.SERPENTINE_HELM to "You adapt the visage to fit on a human head.",
    )

    private suspend fun ProtectedAccess.combine(product: CraftingProduct) {
        // The slayer helmet is the only combine with a non-skill gate.
        if (product.output == CraftingConstants.SLAYER_HELM && !malevolentMasqueradeUnlocked()) {
            mesbox(
                "You need to learn how to combine these items first. Speak to a Slayer master " +
                    "about the 'Malevolent masquerade' ability.",
            )
            return
        }

        val resolved = product.copy(inputs = resolveInputs(product))
        val confirm =
            confirmations[product.output]
                ?: product.inputs.firstNotNullOfOrNull { confirmationsByInput[it.internal] }
        if (confirm == null) {
            val dialogue = resultDialogues[product.output]
            if (dialogue != null) {
                craftWithResultDialogue(resolved, dialogue)
            } else {
                craftInstantly(resolved)
            }
            return
        }

        if (!canCraft(resolved, verbose = true)) {
            return
        }
        confirm.warning?.let { mesbox(it) }
        if (!choice2("Yes.", true, "No.", false, title = confirm.title)) {
            return
        }
        confirm.secondTitle?.let { secondTitle ->
            if (!choice2("No.", false, "Yes.", true, title = secondTitle)) {
                return
            }
        }

        resolved.anim?.let { anim(it) }
        delay(CONFIRM_CRAFT_DELAY)

        // A result dialogue replaces the row's spam line with an item box; without one, the row's
        // spam line plays. craftOnce re-checks materials, which matters here - the player has had
        // several ticks to drop or bank a component.
        val dialogue = confirm.resultDialogue
        if (dialogue != null) {
            if (craftOnce(resolved.copy(successMessage = null))) {
                objbox(resolved.output, dialogue)
            }
        } else {
            craftOnce(resolved)
        }
    }

    /**
     * Crafts one item and shows [dialogue] in a result box instead of the section's chat line - no
     * confirmation, no spam. The animation and sound (if the recipe has them) play up front, the
     * same order beginCycle would use; the section success message is suppressed so the box stands
     * alone.
     */
    private suspend fun ProtectedAccess.craftWithResultDialogue(
        product: CraftingProduct,
        dialogue: String,
    ) {
        if (!canCraft(product, verbose = true)) {
            return
        }
        product.anim?.let { anim(it) }
        product.sound?.let { player.soundSynth(it) }
        if (craftOnce(product.copy(successMessage = null))) {
            objbox(product.output, dialogue)
        }
    }

    /**
     * A combine's inputs as they apply to *this* player. Only the slayer helmet differs:
     * reinforced goggles are a component only after A Porcine of Interest, so before the quest
     * they are dropped from the inputs (not required, not consumed) and the helmet is built from
     * the other six items. They stay a trigger either way, which costs nothing - clicking them on
     * a black mask pre-quest simply builds the six-piece helmet, which is what the game does too.
     */
    private fun ProtectedAccess.resolveInputs(product: CraftingProduct): List<Material> =
        product.inputs.filter {
            it.internal != CraftingConstants.REINFORCED_GOGGLES || porcineOfInterestComplete()
        }

    private companion object {
        /**
         * Ticks held after a confirmed craft's animation starts, before the item is handed over
         * and the result box is shown - long enough for the assembly animation to play out.
         */
        private const val CONFIRM_CRAFT_DELAY = 4
    }
}
