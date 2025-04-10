# UI Customization

_Since 6.3_

TODO

The category id format is `<recipe type id>/<group id>`. If the group id is not specified, it will be set to `default`. Recipes with the same category id will be grouped together in JEI / REI.

## Category Metadata

!!! note "Format"

    | Name           | Description                                                 | Type / Literal                                                                                                                                                                                                           |
    | -------------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
    | type           | type                                                        | `lychee:category_metadata`                                                                                                                                                                                               |
    | sort_order     | sort order ^optional^{ title="default: 0" }                 | int                                                                                                                                                                                                                      |
    | category       | category id matcher                                         | [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html) \| [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html)[] |
    | elements       | UI elements ^optional^                                      | dictionary<string, [UIElement](#ui-element)[]>                                                                                                                                                                           |
    | render_default | render default elements ^optional^{ title="default: true" } | boolean                                                                                                                                                                                                                  |
    | size           | base panel size ^optional^                                  | int\[2]                                                                                                                                                                                                                  |
    | workstation    | workstations ^optional^                                     | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[]                                                                                                                                 |

## Category Modifier

!!! note "Format"

    | Name           | Description                                                 | Type / Literal                                                                                                                                                                                                           |
    | -------------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
    | type           | type                                                        | `lychee:category_modifier`                                                                                                                                                                                               |
    | sort_order     | sort order ^optional^{ title="default: 0" }                 | int                                                                                                                                                                                                                      |
    | category       | category id matcher                                         | [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html) \| [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html)[] |
    | recipe         | recipe id matcher                                           | [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html) \| [RegExPattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html)[] |
    | elements       | UI elements ^optional^                                      | dictionary<string, [UIElement](#ui-element)[]>                                                                                                                                                                           |
    | render_default | render default elements ^optional^{ title="default: true" } | boolean                                                                                                                                                                                                                  |

## Blank Recipe

!!! note "Format"

    | Name    | Description            | Type / Literal                                                                                               |
    | ------- | ---------------------- | ------------------------------------------------------------------------------------------------------------ |
    | type    | type                   | `lychee:blank`                                                                                               |
    | item_in | ingredients ^optional^ | [SizedIngredient](general-types.md#sizedingredient) \| [SizedIngredient](general-types.md#sizedingredient)[] |
