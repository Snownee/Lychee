# Fruitful Fun

[Mod Page](https://www.curseforge.com/minecraft/mc-mods/fruit-trees)

## Recipe Types

### Hybridizing

![A bee is triggering hybridization, turning one cherry leaves into another.](https://raw.githubusercontent.com/Snownee/FruitfulFun/refs/heads/1.20.1-fabric/art/Bee%20is%20helping%20to%20crossbreed%20the%20tree.webp)

The hybridizing recipe triggers when a bee collects honey on a block. The bees will record the 4 most recently honeyed blocks, and if they contain all the blocks needed for the recipe, the recipe is matched successfully.

This recipe type is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name        | Description                        | Type / Literal            |
    | ----------- | ---------------------------------- | ------------------------- |
    | type        | type                               | "fruitfulfun:hybridizing" |
    | pollens     | the ids of the needed blocks       | string[]                  |
    | ending_step | the possible ids of the last block | string[]                  |

??? example

    ```json
    {
        "type": "fruitfulfun:hybridizing",
        "pollens": [
            "torchflower"
        ],
        "contextual": {
            "type": "fruitfulfun:bee_has_trait",
            "trait": "wither_tolerant"
        },
        "post": [
            {
                "contextual": {
                    "type": "chance",
                    "chance": 0.1
                },
                "type": "place",
                "block": "*"
            },
            {
                "type": "random",
                "entries": [
                    {
                        "type": "drop_item",
                        "weight": 1,
                        "item": "minecraft:blaze_powder"
                    },
                    {
                        "type": "drop_item",
                        "weight": 3,
                        "item": "minecraft:gunpowder"
                    },
                    {
                        "type": "drop_item",
                        "weight": 6,
                        "item": "minecraft:quartz"
                    }
                ]
            }
        ]
    }
    ```

    Description: When a bee with the trait `wither_tolerant` collects honey on a Torchflower, there is a 10% chance that the flower will be consumed. Also, there is a 10%, 30% and 60% chance of dropping blaze powder, gunpowder, and quartz respectively.

### Dragon Ritual

![An example of the dragon ritual structure.](https://raw.githubusercontent.com/Snownee/FruitfulFun/refs/heads/1.20.1-fabric/art/ritual.webp)

The structure requires candles and one or more dragon heads. The kind of supporting blocks doesn't matter. Once the structure is set up, you will only need to use a Chorus Fruit Pie to activate it. You can use a Dispenser to place the pie.

After the ritual is activated, you need to throw an item into the center of the structure. The item will be consumed when the ritual is complete. If no item is thrown, more Dragon Breath will be produced. The more Dragon Heads you use, the more Dragon Breath you will get.

This recipe type is [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name    | Description    | Type / Literal                            |
    | ------- | -------------- | ----------------------------------------- |
    | type    | type           | "fruitfulfun:dragon_ritual"               |
    | item_in | the input item | [Ingredient](general-types.md#ingredient) |

??? example

    ```json
    {
        "type": "fruitfulfun:dragon_ritual",
        "item_in": {
            "item": "glass_bottle"
        },
        "post": [
            {
                "type": "drop_item",
                "item": "dragon_breath"
            }
        ],
        "max_repeats": 1
    }
    ```

## Conditions

### Bee Trait Check

### Sky Darkness Check