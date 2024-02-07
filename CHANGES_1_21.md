# Changes in 1.20.4 & 1.21

- Kiwi is now a required dependency for Lychee.
- The key to assign NBT to an ItemStack is now renamed to `lychee:nbt` instead of `lychee:tag`. And it can now be used
  in more circumstances, such as in stonecutting recipes.
- The translation key for biome tags is now `biome.#<namespace>.<path.replace('/', '.')>` instead
  of `biomeTag.<namespace>.<path.replace('/', '.')>`.
- The `lychee:biome_tag` in LocationPredicate is no longer supported. Instead, you should use `"biome": "#<tag>"`, and
  it is only supported in Lychee's JSON.
- `structure` and `dimension` predicates in LocationPredicate now support using tags too, just like `biome`.
- JSON fragments now work with any recipe types, and you can now replace the recipe type or the conditions provided by
  loaders. The fragment id is now **namespace-sensitive**.
