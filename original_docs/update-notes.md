# Update Notes

## 6.2

- Now you can use bracketed item to specify an ItemStack with components, just like in the `give` command.
- Now you can use bracketed block to specify a BlockPredicate with properties or nbt, just like in the `setblock` command.
- Now you can see the comment and conditions of an anvil recipe in JEI.

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