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

    ### Custom Ingredients

    Custom ingredient created by modders:

    === "Forge"

        | Name | Description              | Type / Literal |
        | ---- | ------------------------ | -------------- |
        | type | ingredient type          | string         |
        |      | additional properties... |                |

        You can use Forge's built-in features to check item nbt or combine several ingredients to one compound ingredient: [Documentation](https://docs.minecraftforge.net/en/1.19.x/resources/server/recipes/ingredients/#forge-types)

    === "Fabric"

        | Name        | Description              | Type / Literal |
        | ----------- | ------------------------ | -------------- |
        | fabric:type | ingredient type          | string         |
        |             | additional properties... |                |

        You can use Fabric's built-in features to check item nbt or combine several ingredients to one compound ingredient: [Documentation](https://github.com/FabricMC/fabric/blob/1.20.1/fabric-recipe-api-v1/src/main/java/net/fabricmc/fabric/api/recipe/v1/ingredient/DefaultCustomIngredients.java)

## BlockPredicate

A BlockPredicate is a predicate of an in-world block. It can also be used to represent the first possible block that matches this predicate.

!!! note "Format"

    | Name   | Description                                        | Type / Literal                                        |
    | ------ | -------------------------------------------------- | ----------------------------------------------------- |
    | blocks | block resource ids ^optional^                      | string[]                                              |
    | tag    | the block tag ^optional^                           | string                                                |
    | nbt    | the block nbt ^optional^                           | string                                                |
    | state  | a map of block property names to values ^optional^ | [StatePropertiesPredicate](#statepropertiespredicate) |

    As a shortcut you can use a string to represent a simple BlockPredicate.

!!! example

    ```json
    {
        "blocks": ["tconstruct:seared_lantern"],
        "nbt": "{\"tank\":{\"FluidName\":\"thermal:tree_oil\",\"Amount\":50}}"
    }
    ```

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

Predicate applied to location. Please refer to the [Minecraft Wiki](https://minecraft.wiki/w/Predicate).

## IntBounds

Describes a inclusive range for integers.

!!! note "Format"

    | Name | Description                                            | Type / Literal |
    | ---- | ------------------------------------------------------ | -------------- |
    | min  | minimum value ^optional^{ title="default: -infinity" } | int            |
    | max  | maximum value ^optional^{ title="default: +infinity" } | int            |

    IntBounds can also be a simple `int` to represent [n, n].

## DoubleBounds

Describes a inclusive range for doubles.

!!! note "Format"

    | Name | Description                                            | Type / Literal |
    | ---- | ------------------------------------------------------ | -------------- |
    | min  | minimum value ^optional^{ title="default: -infinity" } | number         |
    | max  | maximum value ^optional^{ title="default: +infinity" } | number         |

    DoubleBounds can also be a simple `number` to represent [n, n].

## JsonPointer

JsonPointer is a string syntax for identifying a specific value within a JSON. But the only thing you need to know is you should separate the path to the target with `/`.

```json
"/path/to/the/target"
```
