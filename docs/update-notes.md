# Update Notes

## 3.14

 - Added `repeat` option to `execute` action type.

## 3.13

 - Added native REI support for Forge
 - Now you can define a tag-based `BlockPredicate` using a string prefixed with "#".

## 3.12.1

 - [`lychee:tag`](extra-features.md#lycheetag) now supports string as NBT tag value.
 - Added support for Fabric Recipe API. The "always true" ingredient for Fabric is changed a bit.

## 3.12

 - Added support for KubeJS 6.1
 - Added `empty_weight` option to `random` action type.

## 3.11

 - Added assembling actions support to anvil crafting recipes.
 - Allowed to use [`lychee:tag`](extra-features.md#lycheetag) in `Ingredient` or `ItemStack` to assign a NBT tag to the item.

## 3.10

 - Added if-else statement post action type.

## 3.9

 - Basic KubeJS integration for adding custom actions and conditions.
 - KubeJS integration for executing codes when clicking the info badge in JEI/REI.
 - Now you can specify item in the other hand for block interaction recipes.

## 3.8

 - Added JSON fragment system.

## 3.7

 - Added item NBT patching.
 - Allows to look up Lychee recipes through fluids.
 - Added anvil crafting recipe's comment and conditions display for REI (Fabric).

## 3.6

 - Added a way to separate recipes into different categories in JEI / REI.

## 3.5

 - Added a way to match any item with `Ingredient`. Mainly for the interaction recipes.
 - Fixed positioning issues in JEI / REI while rendering special items.
