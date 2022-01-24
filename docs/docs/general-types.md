# General Types

## Ingredient

An Ingredient is a predicate of ItemStack.

??? note "Format"

    Checks if item id matches:

    | Name | Description      | Type / Literal |
    | ---- | ---------------- | -------------- |
    | item | item resource id | string         |

    Checks if item has the tag:

    | Name | Description  | Type / Literal |
    | ---- | ------------ | -------------- |
    | tag  | the item tag | string         |

    ??? note "Forge-only area"

        Custom ingredient created by modders:

        | Name | Description              | Type / Literal |
        | ---- | ------------------------ | -------------- |
        | type | ingredient type          | string         |
        |      | additional properties... |                |

        Checks for item id and nbt:

        | Name | Description      | Type / Literal   |
        | ---- | ---------------- | ---------------- |
        | type | ingredient type  | "forge:nbt"      |
        | item | item resource id | string           |
        | nbt  | the item nbt     | object \| string |

        You can combine several ingredient to one compound ingredient:

        ```
        [
          Ingredient,
          Ingredient,
          Ingredient
        ]
        ```

## BlockPredicate

An BlockPredicate is a predicate of an in-world block. It can also be used to represent the first possible block that matches this predicate.

??? note "Format"

    | Name   | Description                                        | Type / Literal                                        |
    | ------ | -------------------------------------------------- | ----------------------------------------------------- |
    | blocks | (optional) block resource ids                      | string[]                                              |
    | tag    | (optional) the block tag                           | string                                                |
    | nbt    | (optional) the block nbt                           | string                                                |
    | state  | (optional) a map of block property names to values | [StatePropertiesPredicate](#statepropertiespredicate) |

    As a shortcut you can use a string to represent a simple BlockPredicate.

## StatePropertiesPredicate

An BlockPredicate is a predicate of StateDefinition.

??? note "Format"

    | Name  | Description                       | Type / Literal           |
    | ----- | --------------------------------- | ------------------------ |
    | *key* | Block property key and value pair | string \| boolean \| int |
    | *key* | the block tag                     | object                   |
    | - min | minimum value                     | int                      |
    | - max | maximum value                     | int                      |

## LocationPredicate

Predicate applied to location. Please refer to the [Minecraft Wiki](https://minecraft.fandom.com/wiki/Predicate).
