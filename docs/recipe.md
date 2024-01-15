# Recipes

!!! info

    Try [**Fruitful Generator**](https://fruitful-generator.github.io/) !
    
    It's a convenient tool for creating Lychee recipes.

## Basic Format

Recipes should be placed in `data/<namespace>/recipes/` folder, like normal data-driven recipes.

=== "Forge"

    | Name           | Description                                                                 | Type / Literal                                                                                     |
    | -------------- | --------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
    | type           | type                                                                        | string                                                                                             |
    | conditions     | conditions ^optional^                                                       | [ForgeCondition](https://docs.minecraftforge.net/en/1.19.x/resources/server/conditional/)[]        |
    | contextual     | contextual conditions ^optional^                                            | [ContextualCondition](contextual-condition.md) \| [ContextualCondition](contextual-condition.md)[] |
    | post           | post actions ^optional^                                                     | [PostAction](post-action.md) \| [PostAction](post-action.md)[]                                     |
    | comment        | language key to show in JEI/REI ^optional^                                  | string                                                                                             |
    | ghost          | only show in JEI/REI but does not take effect ^optional^                    | true \| false                                                                                      |
    | hide_in_viewer | hide in JEI/REI ^optional^                                                  | true \| false                                                                                      |
    | group          | show this recipe in a new category in JEI/REI ^optional^                    | string (ResourceLocation)                                                                          |
    | max_repeats    | max repeats for a processing. not work for a unrepeatable recipe ^optional^ | int                                                                                                |
    |                | additional properties...                                                    |                                                                                                    |

=== "Fabric"

    | Name                   | Description                                                                 | Type / Literal                                                                                     |
    | ---------------------- | --------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
    | type                   | type                                                                        | string                                                                                             |
    | fabric:load_conditions | conditions ^optional^                                                       | [FabricCondition](https://github.com/FabricMC/fabric/pull/1656)[]                                  |
    | contextual             | contextual conditions ^optional^                                            | [ContextualCondition](contextual-condition.md) \| [ContextualCondition](contextual-condition.md)[] |
    | post                   | post actions ^optional^                                                     | [PostAction](post-action.md) \| [PostAction](post-action.md)[]                                     |
    | comment                | language key to show in JEI/REI ^optional^                                  | string                                                                                             |
    | ghost                  | only show in JEI/REI but does not take effect ^optional^                    | true \| false                                                                                      |
    | hide_in_viewer         | hide in JEI/REI ^optional^                                                  | true \| false                                                                                      |
    | group                  | show this recipe in a new category in JEI/REI ^optional^                    | string (ResourceLocation)                                                                          |
    | max_repeats            | max repeats for a processing. not work for a unrepeatable recipe ^optional^ | int                                                                                                |
    |                        | additional properties...                                                    |                                                                                                    |

## Recipe Types

### Use Item on a Block

Event when a player uses item on a block.

Currently, interaction with fluid blocks is not supported.

Default behavior: Item is consumed.

This recipe type is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name     | Description               | Type / Literal                                                                           |
    | -------- | ------------------------- | ---------------------------------------------------------------------------------------- |
    | type     | type                      | "lychee:block_interacting"                                                               |
    | item_in  | the item in player's hand | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[] |
    | block_in | the block being used      | [BlockPredicate](general-types.md#blockpredicate)                                        |

    `item_in` can accept an ingredient list with 2 ingredients. The second one is the item in the off hand.

??? example

    Prevent player from carving pumpkins. Here the `prevent_default` means do not consume the shears.

    ```json
    {
        "type": "lychee:block_interacting",
        "item_in": {
            "item": "shears"
        },
        "block_in": "pumpkin",
        "post": {
            "type": "prevent_default"
        }
    }
    ```

    Stripping an oak log with an iron axe, you will have 50% chance to obtain a diamond:

    ```json
    {
        "type": "lychee:block_interacting",
        "item_in": {
            "item": "iron_axe"
        },
        "block_in": "oak_log",
        "post": [
            {
                "type": "drop_item",
                "item": "diamond",
                "contextual": {
                    "type": "chance",
                    "chance": 0.5
                }
            },
            {
                "type": "place",
                "block": "stripped_oak_log"
            },
            {
                "type": "damage_item"
            }
        ]
    }
    ```

??? note "Note: Matches empty hand"

    Use `"item": "air"` to require interaction with empty hand.

??? note "Note: Matches any item"

    You can use a special ingredient to match any item:

    === "Forge"

        ```json
        {
            "type": "lychee:block_interacting",
            "item_in": {
                "type": "lychee:always_true"
            },
            "block_in": "minecraft:stone"
        }
        ```

    === "Fabric"

        ```json
        {
            "type": "lychee:block_interacting",
            "item_in": {
                "fabric:type": "lychee:always_true"
            },
            "block_in": "minecraft:stone"
        }
        ```

!!! warning

    It is not recommended to use recipe contextual conditions in it, because some contextual conditions are not supported to run on the client side.

### Click on a Block with Item

Event when a player click on a block with item.

Currently, interaction with fluid blocks is not supported.

Default behavior: Item is consumed.

This recipe type is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name     | Description               | Type / Literal                                                                           |
    | -------- | ------------------------- | ---------------------------------------------------------------------------------------- |
    | type     | type                      | "lychee:block_clicking"                                                                  |
    | item_in  | the item in player's hand | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[] |
    | block_in | the block being clicked   | [BlockPredicate](general-types.md#blockpredicate)                                        |

    `item_in` can accept an ingredient list with 2 ingredients. The second one is the item in the off hand.

??? note "Note: Matches empty hand"

    Use `"item": "air"` to require interaction with empty hand.

??? note "Note: Matches any item"

    You can use a special ingredient to match any item:

    === "Forge"

        ```json
        {
            "type": "lychee:block_clicking",
            "item_in": {
                "type": "lychee:always_true"
            },
            "block_in": "minecraft:stone"
        }
        ```

    === "Fabric"

        ```json
        {
            "type": "lychee:block_clicking",
            "item_in": {
                "fabric:type": "lychee:always_true"
            },
            "block_in": "minecraft:stone"
        }
        ```

!!! warning

    It is not recommended to use recipe contextual conditions in it, because some contextual conditions are not supported to run on the client side.

### Item Entity Burning

Event when an item entity is burnt.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: none.

!!! note "Format"

    | Name    | Description    | Type / Literal                            |
    | ------- | -------------- | ----------------------------------------- |
    | type    | type           | "lychee:item_burning"                     |
    | item_in | the burnt item | [Ingredient](general-types.md#ingredient) |

!!! note

    Items such as netherite or nether star can't catch fire.

    If you want to make an item fire-immune as well, you can tag it with `lychee:fire_immune`

??? example

    Burning logs produces charcoal:

    ```json
    {
        "type": "lychee:item_burning",
        "item_in": {
            "tag": "logs_that_burn"
        },
        "post": {
            "type": "drop_item",
            "item": "charcoal"
        }
    }
    ```

### Item Entity inside a Block

Event when an item entity is inside a block. This will be tested every second.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: Item is consumed.

!!! note "Format"

    | Name     | Description                        | Type / Literal                                                                           |
    | -------- | ---------------------------------- | ---------------------------------------------------------------------------------------- |
    | type     | type                               | "lychee:item_inside"                                                                     |
    | item_in  | the ticking item(s)                | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[] |
    | block_in | the block where the item(s) in     | [BlockPredicate](general-types.md#blockpredicate)                                        |
    | time     | waiting time in seconds ^optional^ | int                                                                                      |

??? example

    Drop a bucket into a full water cauldron, it returns a water bucket and empty the cauldron:

    ```json
    {
        "type": "lychee:item_inside",
        "item_in": {
            "item": "bucket"
        },
        "block_in": {
            "blocks": ["water_cauldron"],
            "state": {
                "level": 3
            }
        },
        "post": [
            {
                "type": "drop_item",
                "item": "water_bucket"
            },
            {
                "type": "place",
                "block": "cauldron"
            }
        ]
    }
    ```

    Drop a bucket to pick up a water source block:

    ```json
    {
        "type": "lychee:item_inside",
        "item_in": {
            "item": "bucket"
        },
        "block_in": {
            "blocks": ["water"],
            "state": {
                "level": 0
            }
        },
        "post": [
            {
                "type": "drop_item",
                "item": "water_bucket"
            },
            {
                "type": "place",
                "block": "*"
            }
        ]
    }
    ```

!!! note

    If the block is a fluid block, the block id is not always the same as the fluid id. To see the block id, you should use Jade mod and enable the "Registry Name" option in the plugin settings.

### Anvil Crafting

It is not recommended to add contextual conditions or actions to the recipe, because JEI/REI does not support drawing extra things on an anvil recipe.

This recipe type is not [repeatable](concepts.md#repeatability).

Default behavior: Anvil is damaged.

!!! note "Format"

    | Name          | Description                                                    | Type / Literal                                                                           |
    | ------------- | -------------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
    | type          | type                                                           | "lychee:anvil_crafting"                                                                  |
    | item_in       | the input items (the second one is optional)                   | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[] |
    | item_out      | the result item                                                | ItemStack                                                                                |
    | level_cost    | player's xp level                                              | int && >=1                                                                               |
    | material_cost | amount of items that will be cost from right input slot        | int                                                                                      |
    | assembling    | actions that running before the result is displayed ^optional^ | [PostAction](post-action.md) \| [PostAction](post-action.md)[]                           |

??? example

    It costs 1 apple, 8 gold ingots and 1 level to make a golden_apple. Does not damage the anvil:

    ```json
    {
        "type": "lychee:anvil_crafting",
        "item_in": [
            {
                "item": "apple"
            },
            {
                "item": "gold_ingot"
            }
        ],
        "item_out": {
            "item": "golden_apple"
        },
        "level_cost": 1,
        "material_cost": 8,
        "post": {
            "type": "prevent_default"
        }
    }
    ```

### Block Crushing

Event when a falling block entity lands on a block.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: Falling block becomes block or drops item. Canceling this will make falling block disappear.

!!! note "Format"

    | Name          | Description                                              | Type / Literal                                                                           |
    | ------------- | -------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
    | type          | type                                                     | "lychee:block_crushing"                                                                  |
    | item_in       | the crushed items ^optional^                             | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[] |
    | falling_block | the falling block. default are all the anvils ^optional^ | [BlockPredicate](general-types.md#blockpredicate)                                        |
    | landing_block | the landing block. default is any block ^optional^       | [BlockPredicate](general-types.md#blockpredicate)                                        |

??? example

    Papers from sugar canes:

    ```json
    {
        "type": "lychee:block_crushing",
        "item_in": [
            {
                "item": "sugar_cane"
            },
            {
                "item": "sugar_cane"
            },
            {
                "item": "sugar_cane"
            }
        ],
        "post": [
            {
                "type": "drop_item",
                "item": "paper",
                "count": 3
            }
        ]
    }
    ```

    Making a mossy stone bricks block. It uses a location check to check the block below the current position:

    ```json
    {
        "type": "lychee:block_crushing",
        "landing_block": "moss_carpet",
        "contextual": {
            "type": "location",
            "offsetY": -1,
            "predicate": {
                "block": {
                    "blocks": [ "stone_bricks" ]
                }
            }
        },
        "post": [
            {
                "type": "place",
                "block": "*"
            },
            {
                "type": "place",
                "offsetY": -1,
                "block": "mossy_stone_bricks"
            }
        ]
    }
    ```

!!! note

    Dispenser can now place fallable blocks and items that tagged with `lychee:dispenser_placement` in front of it. Note the item should not have a special dispense behavior.

!!! note

    Normally it will process all the items that touches the falling block, but if the landing block is tagged with `lychee:extend_box` (cauldrons by default), it will collect items inside the landing block as well.

### Lightning Channeling

Event when a lightning is channeling.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: Items are consumed. Canceling this will **not** prevent item from being damaged.

!!! note "Format"

    | Name    | Description                           | Type / Literal                                                                           |
    | ------- | ------------------------------------- | ---------------------------------------------------------------------------------------- |
    | type    | type                                  | "lychee:lightning_channeling"                                                            |
    | item_in | items nearby the lightning ^optional^ | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[] |

??? example

    Make nearby stone become calcite:

    ```json
    {
        "type": "lychee:lightning_channeling",
        "post": [
            {
                "type": "execute",
                "command": "fill ~-3 ~-3 ~-3 ~3 ~3 ~3 stone replace calcite"
            }
        ]
    }
    ```

### Item Exploding

Event when an item entity is exploded.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: Items are consumed. Canceling this will **not** prevent item from being damaged.

!!! note "Format"

    | Name    | Description                                | Type / Literal                                                                           |
    | ------- | ------------------------------------------ | ---------------------------------------------------------------------------------------- |
    | type    | type                                       | "lychee:item_exploding"                                                                  |
    | item_in | items affected by the explosion ^optional^ | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[] |

!!! note

    You can tag items with `lychee:item_exploding_catalysts` to let them be shown in JEI / REI.

### Block Exploding

Event when a block is exploded.

This recipe type is not [repeatable](concepts.md#repeatability).

Default behavior: Block drops items from loot table.

!!! note "Format"

    | Name     | Description                                 | Type / Literal                                    |
    | -------- | ------------------------------------------- | ------------------------------------------------- |
    | type     | type                                        | "lychee:block_exploding"                          |
    | block_in | block destroyed by the explosion ^optional^ | [BlockPredicate](general-types.md#blockpredicate) |

!!! note

    You can tag items with `lychee:block_exploding_catalysts` to let them be shown as catalysts in JEI / REI.

### Random Block Ticking

Event when a block is randomly ticked. [(Wiki about random tick)](https://minecraft.wiki/w/Tick#Random_tick)

This recipe type is not [repeatable](concepts.md#repeatability).

Default behavior: Do the default ticking behavior.

!!! note "Format"

    | Name     | Description | Type / Literal                                    |
    | -------- | ----------- | ------------------------------------------------- |
    | type     | type        | "lychee:random_block_ticking"                     |
    | block_in | the block   | [BlockPredicate](general-types.md#blockpredicate) |

!!! note

    This recipe type does not have JEI / REI integration.

### Dripstone Dripping

Event when a block is randomly ticked.

This recipe type is not [repeatable](concepts.md#repeatability).

Default behavior: Do the default ticking behavior.

!!! note "Format"

    | Name         | Description                                    | Type / Literal                                    |
    | ------------ | ---------------------------------------------- | ------------------------------------------------- |
    | type         | type                                           | "lychee:dripstone_dripping"                       |
    | source_block | block that two blocks above the root dripstone | [BlockPredicate](general-types.md#blockpredicate) |
    | target_block | block that being dripped                       | [BlockPredicate](general-types.md#blockpredicate) |

??? example

    ```json
    {
        "type": "lychee:dripstone_dripping",
        "source_block": "water",
        "target_block": "sponge",
        "post": [
            {
                "type": "place",
                "block": "wet_sponge"
            }
        ]
    }
    ```

### Advanced Shaped Crafting

This allows you to add contextual conditions and actions, and control the behaviors of the ingredients and the result.

This recipe type is not [repeatable](concepts.md#repeatability).

Default behavior: none.

!!! note "Format"

    | Name       | Description                                                    | Type / Literal                                                 |
    | ---------- | -------------------------------------------------------------- | -------------------------------------------------------------- |
    | type       | type                                                           | "lychee:crafting"                                              |
    | pattern    | same as vanilla                                                |                                                                |
    | key        | same as vanilla                                                |                                                                |
    | result     | same as vanilla                                                |                                                                |
    | group      | same as vanilla ^optional^                                     |                                                                |
    | assembling | actions that running before the result is displayed ^optional^ | [PostAction](post-action.md) \| [PostAction](post-action.md)[] |

??? example

    With the uses of the [`set_item`](post-action.md#set-item-set_item) action, you can customize the remainders and dynamically change the crafting result.

    ```json
    {
        "type": "lychee:crafting",
        "pattern": [
            "A",
            "B"
        ],
        "key": {
            "A": {
                "item": "pufferfish"
            },
            "B": {
                "item": "water_bucket"
            }
        },
        "result": {
            "item": "apple"
        },
        "post": [
            {
                "type": "set_item",
                "target": "/key/B",
                "item": "air"
            }
        ],
        "assembling": [
            {
                "type": "set_item",
                "target": "/result",
                "item": "pufferfish_bucket"
            }
        ]
    }
    ```

    [Here](kubejs.md#example-repairing-tool-with-anvil-and-custom-item) is a more advanced example that uses KubeJS Integration.

!!! note

    No guarantee that modded crafting machines contains player and location information. You can use [`check_param`](contextual-condition.md#check-parameter-check_param) to require these parameters to be present when crafting.
