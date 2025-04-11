# UI Customization

_Since 6.3_

These are 3 special recipe types that are used to customize the UI in JEI / REI from the server side.

Currently, modifying elements is not supported for anvil crafting and shaped crafting recipe.

- Category metadata allows you to modify the elements and properties of a category.
- Category modifier allows you to modify the elements in the specific recipes of a category.
- Blank recipe allows you to display something from scratch along with category modifiers.

The category id format is usually `<recipe type id>/<group id>`. Specifically, the block clicking recipe uses block interacting recipe's recipe type id.

If the group id is not specified, it will be set to `default`. Recipes with the same category id will be grouped together in JEI / REI.

The elements are split into many sections. You can add new elements by using any new name, or replace the existing elements by using the existing name. Replacing the existing elements with an empty array will remove that section.

## Category Metadata

!!! note "Format"

    | Name           | Description                                                 | Type / Literal                                                                                                                                                                                                           |
    | -------------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
    | type           | type                                                        | "lychee:category_metadata"                                                                                                                                                                                               |
    | sort_order     | sort order ^optional^{ title="default: 0" }                 | int                                                                                                                                                                                                                      |
    | category       | category id matcher                                         | [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html) \| [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html)[] |
    | elements       | UI elements ^optional^                                      | dictionary<string, [UIElement](#ui-element)[]>                                                                                                                                                                           |
    | render_default | render default elements ^optional^{ title="default: true" } | boolean                                                                                                                                                                                                                  |
    | size           | base panel size ^optional^                                  | int\[2]                                                                                                                                                                                                                  |
    | workstation    | workstations ^optional^                                     | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[]                                                                                                                                 |

Only one category metadata can take effect. If multiple category metadata are matched, the one with the lowest sort order will be used.

## Category Modifier

!!! note "Format"

    | Name           | Description                                                 | Type / Literal                                                                                                                                                                                                           |
    | -------------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
    | type           | type                                                        | "lychee:category_modifier"                                                                                                                                                                                               |
    | sort_order     | sort order ^optional^{ title="default: 0" }                 | int                                                                                                                                                                                                                      |
    | category       | category id matcher                                         | [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html) \| [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html)[] |
    | recipe         | recipe id matcher                                           | [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html) \| [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html)[] |
    | elements       | UI elements ^optional^                                      | dictionary<string, [UIElement](#ui-element)[]>                                                                                                                                                                           |
    | render_default | render default elements ^optional^{ title="default: true" } | boolean                                                                                                                                                                                                                  |

Multiple category modifiers can take effect on one recipe. Modifier with greater sort order will replace the previous element member if their key is the same.

## Blank Recipe

!!! note "Format"

    | Name    | Description            | Type / Literal                                                                                               |
    | ------- | ---------------------- | ------------------------------------------------------------------------------------------------------------ |
    | type    | type                   | "lychee:blank"                                                                                               |
    | item_in | ingredients ^optional^ | [SizedIngredient](general-types.md#sizedingredient) \| [SizedIngredient](general-types.md#sizedingredient)[] |

## Built-in Section Names

- `lychee:block_interacting`: `ingredient_group`, `action_group`, `info`, `consume_block_in`, `block_in`, `method`
- `lychee:item_inside`: `ingredient_group`, `action_group`, `info`, `consume_block_in`, `block_in`, `method`
- `lychee:item_burning`: `ingredient_group`, `action_group`, `info`, `consume_block_in`, `block_in`, `method`
- `lychee:block_exploding`: `action_group`, `info`, `consume_block_in`, `block_in`
- `lychee:block_crushing`: `ingredient_group`, `action_group`, `info`, `consume_block_in`, `falling_block`, `landing_block`
- `lychee:dripstone_dripping`: `action_group`, `info`, `consume_block_in`, `source_block`, `dripstone_block`, `target_block`
- `lychee:item_exploding`: `ingredient_group`, `action_group`, `info`, `block_in`, `tnt`
- `lychee:lightning_channeling`: `ingredient_group`, `action_group`, `info`, `lightning_bolt`, `lightning_rod`
- `lychee:blank`: `ingredient_group`, `action_group`