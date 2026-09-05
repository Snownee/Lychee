# Update Notes

## 6.6

- `set_item` reworked: now repeatable and actually consumes inputs
    - `set_item` is now repeatable — output scales with how much material you supply (e.g. a 4-ingredient input can transform a full stack at once).
    - The input material is now genuinely consumed (per its recipe ingredient count) instead of being refunded. Previously the target item was replaced and the old material was returned to you.
    - The transformed item is placed back at the original position when the input is fully consumed; otherwise leftover material stays in place and the output goes to your inventory (or drops on the ground if your inventory is full).
    - Outputs larger than a single stack are split into multiple stacks.
    - Setting an empty/`air` item still clears the slot as before.
- Added `can_destroy` attribute to the block clicking recipe.

## 6.5

- Added new condition type `copy_component`, `remove_component` and `copy_durability`
- Added `preserve_enchantments` attribute to the anvil crafting recipe.

## 6.4

- Added more shorthand formats.

## 6.3.6

- Added EMI integration.

## 6.3

- Added JEI/REI integration customization.
    - You can now add, hide or replace the UI elements in a Lychee's recipe.
    - You can change the width and height of the recipe UI.
    - You can replace the category icon or workstations of a recipe type.
- The `check_param`(Loot Parameter Check) condition type has been removed.
- Added `param` condition type.
- Added `move` action type to move the anchored position in the context.
- Added `set_block` action type to modify a falling block entity.
- `cycle_state_property` action: Allow cycling state property reversely.
- Improved the visual effect of the lightning channeling recipe.
- Improved the category icon of the block crushing recipe.
- Removed the catalyst tags(`lychee:item_exploding_catalysts` and `lychee:block_exploding_catalysts`).
- Fixed some tooltips in JEI / REI not showing correctly.

## 6.2

- Added LycheeBuilder to generate recipes in data generators or KubeJS.
- Now you can use bracketed item to specify an ItemStack with components, just like in the `give` command.
- Now you can use bracketed block to specify a BlockPredicate with properties or nbt, just like in the `setblock` command.
- Now you can see the comment and conditions of an anvil recipe in JEI.
- Item burning recipe now use [SizedIngredient](general-types.md#sizedingredient) instead of [Ingredient](general-types.md#ingredient).
- When you add a `place` action to break the block at the original position, it now shows a exclamation mark next to the block instead of a barrier item.

## 6.1

- Some recipes now use [SizedIngredient](general-types.md#sizedingredient) instead of [Ingredient](general-types.md#ingredient).
- Block interacting and clicking recipes now support item input with count.
- The folder name for fragments is now `lychee_fragment` instead of `lychee_fragments`.
- Added new condition type `sky_darken` to check the sky darkness.

## 6.0

- Ported to 1.21
- Kiwi is now a required dependency for Lychee.
- Now you can write recipes in YAML format.
- The key for adding contextual conditions is now `if`. The old key `contextual` still works.
- The `lychee:tag` key to assign NBT to an ItemStack has been replaced by the vanilla feature `components`.
- The `lychee:tag` key to assign visual-only NBT to an Ingredient has been replaced by an independent ingredient
  type `lychee:visual_only_components`. And it can be used in more ingredient types, not only a simple item.
- The translation key for biome tags is now `tag.biome.<namespace>.<path.replace('/', '.')>` instead
  of `biomeTag.<namespace>.<path.replace('/', '.')>`.
- The `lychee:biome_tag` in LocationPredicate is no longer supported. Instead, you should use the vanilla's new feature instead.
- JSON fragments now work with any recipe types, and you can now replace the recipe type or the conditions provided by
  loaders. The fragment id is now **namespace-sensitive**.
- The custom data in the `custom` PostAction & ContextualCondition type now should be enclosed in a `data` key.
- `hide` now is a common property in all the actions.
- Added new condition type `is_off_item_cooldown` to check if the cooldown of an item is off.
- Now you can specify an item in the `add_item_cooldown` action.
- The `break` action type has been renamed to `exit`. 
- The `hurt` action type has been removed. You should now use the [`damage`](https://minecraft.wiki/w/Commands/damage) command instead.
- The `nbt_patch` action type has been removed.