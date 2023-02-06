# General Types

## Ingredient

An ingredient is a predicate of ItemStack.

!!! note "Format"

    Checks if item id matches:

    | Name | Description      | Type / Literal |
    | ---- | ---------------- | -------------- |
    | item | item resource id | string         |

    Checks if item has the tag:

    | Name | Description  | Type / Literal |
    | ---- | ------------ | -------------- |
    | tag  | the item tag | string         |

    If you want to specify a modded item or tag, you need to write down the namespace at start, for example "namespace:name"

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

        ```
        {
            "type": "forge:nbt",
            "item": "ceramicbucket:ceramic_bucket",
            "nbt": {
                "BucketContent": "minecraft:milk"
            }
        }
        ```

        You can combine several ingredients to one compound ingredient:

        ```
        [
          Ingredient,
          Ingredient,
          Ingredient
        ]
        ```

## BlockPredicate

A BlockPredicate is a predicate of an in-world block. It can also be used to represent the first possible block that matches this predicate.

!!! note "Format"

    | Name   | Description                                       | Type / Literal                                        |
    | ------ | ------------------------------------------------- | ----------------------------------------------------- |
    | blocks | ^optional^block resource ids                      | string[]                                              |
    | tag    | ^optional^the block tag                           | string                                                |
    | nbt    | ^optional^the block nbt                           | string                                                |
    | state  | ^optional^a map of block property names to values | [StatePropertiesPredicate](#statepropertiespredicate) |

    As a shortcut you can use a string to represent a simple BlockPredicate.

### Special case

You can use `"*"` to represent a BlockPredicate to match all blocks.

## StatePropertiesPredicate

A BlockPredicate is a predicate of StateDefinition.

!!! note "Format"

    | Name  | Description                           | Type / Literal           |
    | ----- | ------------------------------------- | ------------------------ |
    | *key* | block property key and value pair     | string \| boolean \| int |
    | *key* | block property key with ranged number | object                   |
    | - min | minimum value                         | int                      |
    | - max | maximum value                         | int                      |

??? example

    Matches water source block:

    ```json
    {
    	"blocks": ["water"],
    	"state": {
    		"level": 0
    	}
    }
    ```

## LocationPredicate

Predicate applied to location. Please refer to the [Minecraft Wiki](https://minecraft.fandom.com/wiki/Predicate).

## IntBounds

Describes a inclusive range for integers.

!!! note "Format"

    | Name | Description                                   | Type / Literal |
    | ---- | --------------------------------------------- | -------------- |
    | min  | ^optional^minimum value. -infinity by default | int            |
    | max  | ^optional^maximum value. +infinity by default | int            |

    IntBounds can also be a simple `int` to represent [n, n].

## DoubleBounds

Describes a inclusive range for doubles.

!!! note "Format"

    | Name | Description                                   | Type / Literal |
    | ---- | --------------------------------------------- | -------------- |
    | min  | ^optional^minimum value. -infinity by default | number         |
    | max  | ^optional^maximum value. +infinity by default | number         |

    DoubleBounds can also be a simple `number` to represent [n, n].

## JsonPointer

JsonPointer is a string syntax for identifying a specific value within a JSON. But the only thing you need to know is you should separate the path to the target with `/`.

```
/path/to/the/target
```
