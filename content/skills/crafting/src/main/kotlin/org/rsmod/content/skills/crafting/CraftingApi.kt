package org.rsmod.content.skills.crafting

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpHeldU
import org.rsmod.content.skills.crafting.util.CraftingConstants
import org.rsmod.content.skills.crafting.util.toolEquivalents
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Every recipe the module knows about, keyed by the obj it produces.
 *
 * Populated by [craftingProduct] itself, so a section is listed here simply by existing - there
 * is nothing to opt in to, and recipes registered by other modules through the same builder show
 * up too. Lets content outside the module (admin tooling, skill guides) ask what an item is made
 * from without duplicating the tables.
 */
object CraftingRecipes {
    private val byOutput = LinkedHashMap<String, LinkedHashSet<CraftingProduct>>()

    internal fun register(product: CraftingProduct) {
        byOutput.getOrPut(product.output) { LinkedHashSet() } += product
    }

    /** Recipes producing [output]; some objs have more than one (bow string from flax or roots). */
    fun forOutput(output: String): List<CraftingProduct> = byOutput[output]?.toList() ?: emptyList()

    /** Every obj the module can craft. */
    fun outputs(): Set<String> = byOutput.keys

    /**
     * Everything needed to make [crafts] of [output] - consumed inputs, thread, and the recipe's
     * tools - or null when nothing crafts [output].
     *
     * Returns [CraftingMaterial] rather than the recipe itself so callers don't need the module's
     * internals on their classpath: `CraftingProduct.inputs` is typed on `Material` from
     * `content.skills.utils`, which is an `implementation` dependency here and therefore not
     * visible to anything downstream. This is what `::craftmat` is built on.
     */
    fun materialsFor(output: String, crafts: Int): List<CraftingMaterial>? {
        val product = forOutput(output).firstOrNull() ?: return null

        val inputs = product.inputs.map { CraftingMaterial(it.internal, it.count * crafts) }
        // Thread is not an input: one reel covers five crafts (see CraftingWorker).
        val thread = if (product.consumesThread) {
            val perSpool = CraftingConstants.THREAD_USES_PER_SPOOL
            listOf(CraftingMaterial(CraftingConstants.THREAD, (crafts + perSpool - 1) / perSpool))
        } else {
            emptyList()
        }
        val tools = product.tools.map { CraftingMaterial(it, count = 1, tool = true) }

        return inputs + thread + tools
    }
}

/** One line of a recipe's shopping list. A [tool] is required but never consumed. */
data class CraftingMaterial(val obj: String, val count: Int, val tool: Boolean = false)

/**
 * Registers [products] as held (inventory) crafting recipes. Every recipe gets the same, uniform
 * set of ways to start it - a handler per pair of click targets:
 * - **ingredient on ingredient**: any of the recipe's click targets used on any other, in either
 *   order. Targets are the recipe's [CraftingProduct.triggers] when it names them (the bone
 *   staff's 1,000 chaos runes are consumed but inert to clicks) and simply its inputs otherwise.
 * - **tool on ingredient**: any of the recipe's tools - or any [toolEquivalents] stand-in, so an
 *   imcando hammer works wherever a hammer does - used on any click target, in either order.
 *
 * So a birdhouse starts from log-on-clockwork, hammer-on-log, chisel-on-clockwork, or the reverse
 * of any of them; a gem cut starts from chisel-on-gem or gem-on-chisel; and the slayer helmet is
 * "use any component on any other".
 *
 * Pairs shared by several recipes register once. A shared pair whose recipes all belong to one
 * [CraftingMode.MENU] section opens a single menu of them (a hammer on a clockwork offers every
 * birdhouse); otherwise the click resolves to the recipe whose materials the player is actually
 * carrying - a chisel on a magic fang makes a toxic staff if they hold a staff of the dead and a
 * trident of the swamp if they hold an uncharged trident. When they hold materials for none, the
 * first recipe is used so the craft fails with the normal "you need ..." message rather than
 * silently doing nothing.
 *
 * Pair ordering matters when a target owns a default "use X on anything" handler elsewhere (logs:
 * barbarian firemaking) - the engine tries the dragged item's handlers first, so such a pair is
 * keyed under that target ([CraftingSection.ownsDefaultHandler]) to win from either direction.
 *
 * This is also the module's entry point for recipes owned by other modules: build the product
 * with [craftingProduct] and register it here from your own script - no code in the crafting
 * module changes. [combine] handles [CraftingMode.COMBINE] recipes and defaults to the shared
 * instant-craft pipeline; pass your own to gate a recipe or resolve conditional inputs first (see
 * `HeldCraftingScript`).
 */
fun ScriptContext.registerHeldCrafting(
    products: List<CraftingProduct>,
    combine: suspend ProtectedAccess.(CraftingProduct) -> Unit = { craftInstantly(it) },
) {
    val registrations = LinkedHashMap<Set<String>, PairRegistration>()
    for (product in products) {
        for (pair in product.clickPairs()) {
            val (first, second) = pair
            val reg = registrations.getOrPut(setOf(first, second)) { PairRegistration(first, second) }
            // A recipe demanding priority ordering wins over whichever order arrived first.
            if (product.ownsDefaultHandler(first) && reg.first != first) {
                reg.first = first
                reg.second = second
            }
            if (product !in reg.products) {
                reg.products += product
            }
        }
    }
    for (reg in registrations.values) {
        onOpHeldU(reg.first, reg.second) { craftFromClick(reg.products, combine) }
    }
}

/** Registers a single held recipe. See [registerHeldCrafting] above. */
fun ScriptContext.registerHeldCrafting(
    product: CraftingProduct,
    combine: suspend ProtectedAccess.(CraftingProduct) -> Unit = { craftInstantly(it) },
): Unit = registerHeldCrafting(listOf(product), combine)

/** One `onOpHeldU` registration: the pair (in registration order) and the recipes it can start. */
private class PairRegistration(var first: String, var second: String) {
    val products = mutableListOf<CraftingProduct>()
}

/** Every click pair that should start this recipe. */
private fun CraftingProduct.clickPairs(): List<Pair<String, String>> {
    val ingredients = triggers.ifEmpty { inputs.map { it.internal } }.distinct()
    val pairs = mutableListOf<Pair<String, String>>()
    for (i in ingredients.indices) {
        for (j in i + 1 until ingredients.size) {
            pairs += orderPair(ingredients[i], ingredients[j])
        }
    }
    for (tool in tools) {
        for (trigger in toolEquivalents(tool)) {
            for (ingredient in ingredients) {
                pairs += orderPair(trigger, ingredient)
            }
        }
    }
    return pairs
}

private fun CraftingProduct.orderPair(a: String, b: String): Pair<String, String> =
    if (ownsDefaultHandler(b) && !ownsDefaultHandler(a)) b to a else a to b

/**
 * Whether [obj] is one of this recipe's *inputs* that owns a competing default handler (see
 * [CraftingSection.ownsDefaultHandler]). Scoped to the inputs so a tool trigger can never match -
 * a birdhouse's "everything but the clockwork" predicate should single out its log, not the
 * hammer being used on it.
 */
private fun CraftingProduct.ownsDefaultHandler(obj: String): Boolean =
    section.ownsDefaultHandler(obj) && inputs.any { it.internal == obj }

/**
 * Acts on a click pair: resolves which recipe was meant (materials held, else the first), then
 * starts it the way its section says to - the combine flow, an instant craft, or the selection
 * menu of every same-section recipe this pair can make.
 */
private suspend fun ProtectedAccess.craftFromClick(
    candidates: List<CraftingProduct>,
    combine: suspend ProtectedAccess.(CraftingProduct) -> Unit,
) {
    val product = candidates.singleOrNull()
        ?: candidates.firstOrNull { hasCraftingMaterials(it) }
        ?: candidates.first()
    when (product.section.mode) {
        CraftingMode.COMBINE -> combine(product)
        CraftingMode.INSTANT -> craftInstantly(product)
        else -> selectCraftingProduct(product.section, candidates.filter { it.section === product.section })
    }
}
