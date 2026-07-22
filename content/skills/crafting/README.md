# Crafting

The Crafting skill, per the [wiki's crafting standard](https://oldschool.runescape.wiki/w/Crafting).

## Data model: sections and recipes

Everything is built on two ideas:

- **Recipes** live on production-table rows in `or-cache` (`dev.openrune.tables.skills.Crafting`),
  one row per craftable item: inputs, output, level, xp, and any optional columns that make the
  recipe special (its own `ticks`, `anim`, failure roll, ...). Two conventions worth knowing: the
  base `xp` column is carried in *tenths* of a point (25 == 2.5xp) and divided once, at grant time,
  by `CraftingConstants.FINE_XP_DIVISOR`; and the first `output(...)` is the recipe's main product,
  while any further `output(...)` entries are returned as byproducts (soft clay's empty container,
  glass smelting's empty bucket).
- **Sections** (the `CraftingSection` enum) define the defaults for one kind of crafting: tick
  speed, animations, sounds, tools, thread use, mode, and player messages. Every row names its
  section, and anything the row leaves unset resolves to the section's default. Resolution happens
  once, in `craftingProduct` / the `toCraftingProduct` row adapters, producing the module's common
  currency: `CraftingProduct`.

  Sections are *default bundles*, not mechanical variants - every hand section runs the identical
  registration/production pipeline, and only `CraftingMode` forks behaviour. The rule for adding
  one: a **group** of recipes sharing defaults no existing section provides earns a section; a
  **one-off** deviation goes on the recipe's own row via the override columns (`ticks`, `anim`,
  `spotanim`, `tool`, `sound`, `message`, `action_name`). That rule is why gems, amethyst, carving
  and limestone are separate (each a group with its own pacing/anim/sound/message/failure family)
  while the old Assembly/ChiselAttachment/Attachment/MiscStringing quartet is one `Combining`
  section whose few odd recipes carry a chisel `tool` or their own `message` on the row.

Four tables carry every recipe:

| Table                 | Sections                                                                 |
|-----------------------|--------------------------------------------------------------------------|
| `crafting_facilities` | Spinning, Weaving, PotteryShaping, PotteryFiring, GlassSmelting, SandPit |
| `crafting_hand`       | Needlework, Shields, Carving, Gems, Amethyst, Limestone, Glassblowing, Battlestaves, AmuletStringing, Birdhouses, Combining, SoftClayMixing, PheasantCostume, Tanning |
| `crafting_silver`     | Jewellery (furnace + mould; category "Silver")                           |
| `crafting_gold`       | Jewellery (furnace + mould; category "Gold")                             |

Adding a recipe is a table row; adding a new kind of crafting is a table section plus a
`CraftingSections` entry. Scripts contain no recipe data. One loc interaction that isn't a
"facility" click - filling a bucket at a sand pit - still rides this pipeline: its recipe lives in
`crafting_facilities` under its own section, and `SandPitScript` builds the product and hooks the
loc.

## The pipeline

`CraftingWorker` is the shared engine every section funnels through: the selection prompt
(`selectCraftingProduct`), the queued production loop (`queue.crafting_make`, evenly paced by the
product's resolved `ticks`), and the single-craft path (`craftInstantly`). Per-cycle work - level
re-validation, tool/material checks, thread accounting (1 spool = 5 crafts), failure rolls,
anims/sounds, inventory transactions, xp - is entirely data-driven from `CraftingProduct`.

How a recipe *starts* depends on where it lives:

- **`FacilityCraftingScript`** - recipes made at a *facility* (a crafting loc: spinning wheel,
  loom, potter's wheel, pottery oven). Clicking the facility opens the menu; the loc is passed
  along so the machine animates with the player. Glass smelting joins via item-on-furnace-category
  so it never competes with Smithing's "Smelt" op.
- **`HeldCraftingScript`** + `registerHeldCrafting` (`CraftingApi`) - every in-inventory recipe,
  registered uniformly: a handler per pair of click targets, so a craft starts from *any tool on
  any ingredient, or any ingredient on any other ingredient, in either order*. A birdhouse starts
  from log-on-clockwork, hammer-on-log, chisel-on-clockwork, or any reverse. Pairs shared by
  several recipes open one menu (hammer on clockwork offers every birdhouse) or resolve by the
  materials actually held (chisel on magic fang). The section's `CraftingMode` decides what a
  click does: menu, instant (limestone - one brick per click), or the combine flow (confirmation
  prompts, the slayer helmet's unlock gate and quest-conditional goggles).
- **Interfaces** - gold/silver jewellery run through their bespoke furnace interfaces
  (`interfaces/GoldCraftingInterface`, `interfaces/SilverCraftingInterface`, shared quantity
  column in `MakeQuantity`); tanning through the if1 tanner interface
  (`interfaces/TannerInterface`) opened by the tanner NPCs (`npcs/`), each with its own price
  schedule. Tanning rows are a `CraftingMode.SERVICE` section - a price list, not a craft.

`registerHeldCrafting` is also the public entry point for recipes owned by other modules: build a
product with `craftingProduct` and register it from your own script - the same shape would carry
a tool-plus-materials skill like Construction. Every product ever built is indexed in
`CraftingRecipes` (backing `::craftmat`); the scripts and interfaces all build their products at
startup precisely so that index is complete before any command or module asks it a question.

## Tools and the imcando hammer

A recipe names canonical tools; `CraftingTools.TOOL_EQUIVALENTS` maps a tool to every obj that
satisfies it. If the player has any one of them the recipe works, and any of them used on an
ingredient starts the craft. The hammer group is the imcando hammer (main-hand and its distinct
off-hand obj, `imcando_hammer_offhand`), which also counts from a worn slot. The one deliberately
non-generic imcando rule is its animation: recipes with an `imcandoAnim` (birdhouses) swap to it
whenever an imcando is held - see `CraftingWorker.craftAnim`.

## Everything else

- `SoftClayScript` - water container on clay.
- `SuperglassMakeScript` - the Lunar spell alternative to furnace smelting.
- `JewelleryScript` - gold/silver bar on a furnace opens the matching interface; Smithing's
  smelt op falls back to them when no ore is held.
- `npcs/CraftingTutorScript` - the Lumbridge crafting tutor's full dialogue tree.
- `util/CraftingGamevals` - defensive gameval resolution: a missing anim/sound is a logged no-op,
  a missing loc/npc is skipped, never a crash.
- `util/CraftingUnlocks` - quest/Slayer gates, stubbed until those systems exist.
- Gameval convention: recipe items live on the table rows; every other gameval (tools, locs,
  npcs, anims, sounds, varbits, interfaces) lives in `util/CraftingConstants`. Numeric
  dbtable/dbrow ids live in `src/main/resources/gamevals.toml` (63000-64000 block).
