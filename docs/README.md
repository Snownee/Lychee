# Preface

Lychee is a mod that allows you to define custom interactions using JSON recipes and datapack.

You can download Lychee on CurseForge:

- [Lychee (Forge)](https://www.curseforge.com/minecraft/mc-mods/lychee)
- [Lychee (Fabric)](https://www.curseforge.com/minecraft/mc-mods/lychee-fabric)

Currently Lychee has support for the following recipe type (with full JEI/REI support!):

- Use or click on a block with item
- Item entity burning
- Item entity inside a specific block (for instance water)
- Anvil crafting
- Falling block crushing items or block
- Lightning channeling
- Random block ticking (No JEI/REI integration)
- Dripstone dripping
- Advanced shaped crafting

## Dependencies Information

### Kiwi (Optional)

1. The random block ticking recipe requires Kiwi to work.
2. Creates item breaking particles when a falling block crushing on items.

## Adding Recipes

To create a new recipe that works in every world you create, you will need a datapack loader mod, such as [OpenLoader](https://www.curseforge.com/minecraft/mc-mods/open-loader).

Or you can add recipes using [CraftTweaker](https://www.curseforge.com/minecraft/mc-mods/crafttweaker) or [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs).

## Contact

Having trouble creating recipe, or got an idea for Lychee? You can [join our Discord](http://discord.snownee.com/), or [create a new issue in Lychee's GitHub repository](https://github.com/Snownee/Lychee/issues).

## Frequently Asked Questions

### Why is my recipe not working?

1. Make sure your recipe file is a valid JSON. You can use [this site](https://jsonlint.com/) to validate your JSON.
2. Check if your recipe is shown in JEI / REI. If not, search the error output in logs for some helpful information.

### Where are the CraftTweaker/KubeJS integrations?

There are no built-in integrations for adding recipes, which means you need to add recipes in JSON format. Here are some usages in their docs: [CraftTweaker](https://docs.blamejared.com/1.19/en/vanilla/api/recipe/manager/GenericRecipesManager#addJsonRecipe) / [KubeJS 1.19.2+](https://wiki.latvian.dev/books/kubejs/page/recipes#bkmrk-custom%2Fmodded-json-r) / [KubeJS 1.18](hhttps://wiki.latvian.dev/books/kubejs-legacy/page/recipeeventjs)

There are some utility libraries from the community you may want to know:

 - [LycheeTweaker for CraftTweaker](https://github.com/ProbablyNotPetey/LycheeTweaker) by ProbablyNotPetey
 - Lychee.JS for KubeJS by Quentin765 (No links, search in the KubeJS discord)
