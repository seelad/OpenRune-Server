package org.rsmod.content.skills.crafting.interfaces

import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.content.skills.crafting.util.CraftingConstants
import org.rsmod.content.skills.crafting.util.CraftingGamevals
import org.rsmod.game.entity.Player

/**
 * The `skillmain` quantity column, shared by the gold and silver crafting interfaces.
 *
 * The client owns it: each button writes [makeQuantity] and redraws the column itself, deciding
 * which buttons exist from how many bars are held and creating the custom-amount button as needed.
 * The server only prompts for "X", which the client cannot do, and reads the result back.
 */
internal class MakeQuantityColumn(
    /** Resolves a component of the owning interface by its name. */
    val component: (String) -> String,
    /** The bar `skillmain` counts to decide which buttons exist. */
    val countedObj: String,
    /** Offsets it lays the buttons out with: gold stacks them, silver puts them in a row. */
    val stepX: Int,
    val stepY: Int,
) {
    val buttons: List<MakeQuantity> by lazy {
        MakeQuantity.entries.filter { CraftingGamevals.exists(component(it.button)) }
    }

    /** The created button. The client makes and wipes it; the server never touches it. */
    val someButton: String by lazy { component("make_some") }
}

/** A fixed quantity button, and the amount it selects (null for "X", which asks first). */
internal enum class MakeQuantity(val button: String, val amount: Int?) {
    One("make_1", 1),
    Five("make_5", 5),
    Ten("make_10", 10),
    X("make_x", null),
    All("make_all", MAX_QUANTITY),
}

/** Selected quantity. Client-owned: it writes this on a button click and draws the column from it. */
private var Player.makeQuantity by intVarp(CraftingConstants.VARP_MAKEX_CRAFTING)

/** Reset before opening, so the client's on-load draw picks up the right button. */
internal fun ProtectedAccess.resetMakeQuantity() {
    player.makeQuantity = 1
}

internal fun ProtectedAccess.makeQuantity(): Int = player.makeQuantity.coerceAtLeast(1)

/**
 * The client has already moved its own selection by the time this runs, so a fixed button only needs
 * its value mirroring into the varp. "X" is the exception: the client parks the selection on 0 (drawn
 * as "?") and leaves the rest to [promptMakeQuantity].
 */
internal suspend fun ProtectedAccess.selectMakeQuantity(
    column: MakeQuantityColumn,
    quantity: MakeQuantity,
) {
    val amount = quantity.amount
    if (amount == null) {
        promptMakeQuantity(column)
    } else {
        player.makeQuantity = amount
    }
}

/**
 * Everything the interface does with the answer falls out of `skillmain_setup` once the varp holds
 * it: over the maximum presses "All", an amount a button already offers presses that button, and
 * anything else creates the sixth button carrying it. Only zero has to be handled here, since the
 * client would leave the column parked on "?".
 *
 * The client wrote 0 on its way in; mirroring that before prompting means the answer is always a
 * change to the varp, so re-entering the amount you already had still redraws.
 */
private suspend fun ProtectedAccess.promptMakeQuantity(column: MakeQuantityColumn) {
    player.makeQuantity = 0
    val max = minOf(inv.count(column.countedObj), MAX_QUANTITY)
    val input = countDialog("Enter amount: (1-$max)")
    player.makeQuantity = input.coerceAtLeast(1)
    refreshMakeQuantity(column)
}

/**
 * Rebuilds the quantity column around the amount just entered, by re-running the script the
 * interface itself calls on load with the arguments it bakes in; see [SKILLMAIN_INIT].
 */
private fun ProtectedAccess.refreshMakeQuantity(column: MakeQuantityColumn) {
    runClientScript(
        SKILLMAIN_INIT,
        column.component("makex").asRSCM(RSCMType.COMPONENT),
        column.stepX,
        column.stepY,
        column.countedObj.asRSCM(RSCMType.OBJ),
        // Fixed signature: the five buttons in column order, then the created one. Taken from the
        // enum rather than [MakeQuantityColumn.buttons], which is filtered.
        *MakeQuantity.entries
            .map { column.component(it.button).asRSCM(RSCMType.COMPONENT) }
            .toTypedArray(),
        column.someButton.asRSCM(RSCMType.COMPONENT),
    )
}

/**
 * `clientscript,skillmain_init`, the shared make-quantity column used across the game's skill
 * interfaces. It (re)creates the quantity buttons from the varp, drawing the selected one pressed
 * and creating or wiping the custom-amount button, and decides which buttons exist at all from how
 * many of an obj the player holds. Each interface calls it on load with its own arguments; the
 * server re-runs it only after the "X" prompt, which the client cannot do itself.
 */
private const val SKILLMAIN_INIT = 2926

/** Most `skillmain_init` will select: a full inventory. */
internal const val MAX_QUANTITY = 28
