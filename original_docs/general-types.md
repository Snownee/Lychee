# General Types

## Ingredient

An ingredient is a predicate of ItemStack.

!!! note "Shorthand Format"

    Checks if item id matches:

    `<item_id>`

    Checks if item has the tag:

    `#<item_tag>`

??? note "Format"

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

    === "NeoForge"

        | Name | Description              | Type / Literal |
        | ---- | ------------------------ | -------------- |
        | type | ingredient type          | string         |
        |      | additional properties... |                |

        You can use NeoForge's built-in features to check item nbt or combine several ingredients to one compound ingredient: [Documentation](https://docs.neoforged.net/docs/1.21.1/resources/server/recipes/ingredients)

    === "Fabric"

        | Name        | Description              | Type / Literal |
        | ----------- | ------------------------ | -------------- |
        | fabric:type | ingredient type          | string         |
        |             | additional properties... |                |

        You can use Fabric's built-in features to check item nbt or combine several ingredients to one compound ingredient: [Documentation](https://github.com/FabricMC/fabric/blob/1.20.6/fabric-recipe-api-v1/src/main/java/net/fabricmc/fabric/api/recipe/v1/ingredient/DefaultCustomIngredients.java)

## SizedIngredient

A SizedIngredient is an ingredient with a count.

!!! note "Shorthand Format"

    Checks if item id matches:

    `<item_id>`(single item) or `<count>x <item_id>`(multiple items)

    Checks if item has the tag:

    `#<item_tag>`(single item) or `<count>x #<item_tag>`(multiple items)

??? note "Format"

    Checks if item id matches:

    | Name  | Description                                 | Type / Literal |
    | ----- | ------------------------------------------- | -------------- |
    | item  | item resource id                            | string         |
    | count | item count ^optional^{ title="default: 1" } | int            |

    Checks if item has the tag:

    | Name  | Description                                 | Type / Literal |
    | ----- | ------------------------------------------- | -------------- |
    | tag   | the item tag                                | string         |
    | count | item count ^optional^{ title="default: 1" } | int            |

    If you want to specify a modded item or tag, you need to write down the namespace at start, for example "namespace:name"

    ### Custom Ingredients

    Custom ingredient created by modders:

    === "NeoForge"

        | Name  | Description                                 | Type / Literal |
        | ----- | ------------------------------------------- | -------------- |
        | type  | ingredient type                             | string         |
        | count | item count ^optional^{ title="default: 1" } | int            |
        |       | additional properties...                    |                |

        You can use NeoForge's built-in features to check item nbt or combine several ingredients to one compound ingredient: [Documentation](https://docs.neoforged.net/docs/1.20.4/resources/server/recipes/ingredients)

    === "Fabric"

        | Name        | Description                                 | Type / Literal |
        | ----------- | ------------------------------------------- | -------------- |
        | fabric:type | ingredient type                             | string         |
        | count       | item count ^optional^{ title="default: 1" } | int            |
        |             | additional properties...                    |                |

        You can use Fabric's built-in features to check item nbt or combine several ingredients to one compound ingredient: [Documentation](https://github.com/FabricMC/fabric/blob/1.20.6/fabric-recipe-api-v1/src/main/java/net/fabricmc/fabric/api/recipe/v1/ingredient/DefaultCustomIngredients.java)

## BlockPredicate

A BlockPredicate is a predicate of an in-world block. It can also be used to represent the first possible block that matches this predicate.

!!! note "Shorthand Format"

    Matches all blocks:

    `*`

    Checks if block id matches:

    `<block_id>` or `<block_id>[<property>=<value>, ...]` or `<block_id>{<nbt>}`

    Checks if block has the tag:

    `#<block_tag>` or `#<block_tag>[<property>=<value>, ...]` or `#<block_tag>{<nbt>}`

??? note "Format"

    A BlockPredicate can be either a simple string or a dictionary:

    | Name   | Description                                                                             | Type / Literal                                        |
    | ------ | --------------------------------------------------------------------------------------- | ----------------------------------------------------- |
    | blocks | one or more block(s) (an ID, or a tag with `#`, or an array containing IDs). ^optional^ | string \| string[]                                    |
    | nbt    | the block nbt ^optional^                                                                | string                                                |
    | state  | a map of block property names to values ^optional^                                      | [StatePropertiesPredicate](#statepropertiespredicate) |

??? example

    ```json
    "minecraft:oak_log[axis=y]"
    ```

    ```json
    "#wooden_stairs"
    ```

    ```json
    {
        "blocks": "tconstruct:seared_lantern",
        "nbt": "{\"tank\":{\"FluidName\":\"thermal:tree_oil\",\"Amount\":50}}"
    }
    ```

## StatePropertiesPredicate

A BlockPredicate is a predicate of StateDefinition.

!!! note "Format"

    | Name  | Description                           | Type / Literal |
    | ----- | ------------------------------------- | -------------- |
    | *key* | block property key and value pair     | string         |
    | *key* | block property key with ranged number | dictionary     |
    | - min | minimum value                         | string         |
    | - max | maximum value                         | string         |

??? example

    Matches water source block:

    ```json
    {
        "blocks": "water",
        "state": {
            "level": "0"
        }
    }
    ```

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

## ItemStack

An ItemStack is an item with its count and components.

!!! note "Shorthand Format"

    `<item_id>`(single item) or `<count>x <item_id>`(multiple items)

??? example

    ```json
    "3x diamond"
    ```

    ```json
    {
        "item": "diamond",
        "count": 3,
        "components": {
            "item_name": "ComponentTest"
        }
    }
    ```

    or in shorthand form:

    ```json
    {
    "item": "diamond[item_name=ComponentTest]",
    "count": 3
    }
    ```

## JsonPointer

JsonPointer is a string syntax for identifying a specific value within a JSON. But the only thing you need to know is you should separate the path to the target with `/`.

!!! example

    ```json
    "/path/to/the/target"
    ```
