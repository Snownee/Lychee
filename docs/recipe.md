# Recipes

## Basic Format

Recipes should be placed in `data/<namespace>/recipe/` folder, like normal data-driven recipes.

=== "NeoForge"

    | Name                | Description                                                                            | Type / Literal                                                                                     |
    | ------------------- | -------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
    | type                | type                                                                                   | string                                                                                             |
    | neoforge:conditions | conditions ^optional^                                                                  | [NeoForgeCondition](https://docs.neoforged.net/docs/resources/server/conditional)[]                |
    | if                  | contextual conditions ^optional^                                                       | [ContextualCondition](contextual-condition.md) \| [ContextualCondition](contextual-condition.md)[] |
    | post                | post actions ^optional^                                                                | [PostAction](post-action.md) \| [PostAction](post-action.md)[]                                     |
    | comment             | language key to show in JEI/REI/EMI ^optional^                                         | string                                                                                             |
    | ghost               | only show in JEI/REI/EMI but does not take effect ^optional^{ title="default: false" } | true \| false                                                                                      |
    | hide_in_viewer      | hide in JEI/REI/EMI ^optional^{ title="default: false" }                               | true \| false                                                                                      |
    | group               | show this recipe in a new category in JEI/REI/EMI ^optional^                           | string (Identifier)                                                                                |
    | max_repeats         | max repeats for a processing. not work for a unrepeatable recipe ^optional^            | [IntBounds](general-types.md#intbounds)                                                            |
    |                     | additional properties...                                                               |                                                                                                    |

=== "Fabric"

    | Name                   | Description                                                                            | Type / Literal                                                                                     |
    | ---------------------- | -------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
    | type                   | type                                                                                   | string                                                                                             |
    | fabric:load_conditions | conditions ^optional^                                                                  | [FabricCondition](https://github.com/FabricMC/fabric/pull/1656)[]                                  |
    | if                     | contextual conditions ^optional^                                                       | [ContextualCondition](contextual-condition.md) \| [ContextualCondition](contextual-condition.md)[] |
    | post                   | post actions ^optional^                                                                | [PostAction](post-action.md) \| [PostAction](post-action.md)[]                                     |
    | comment                | language key to show in JEI/REI/EMI ^optional^                                         | string                                                                                             |
    | ghost                  | only show in JEI/REI/EMI but does not take effect ^optional^{ title="default: false" } | true \| false                                                                                      |
    | hide_in_viewer         | hide in JEI/REI/EMI ^optional^{ title="default: false" }                               | true \| false                                                                                      |
    | group                  | show this recipe in a new category in JEI/REI/EMI ^optional^                           | string (Identifier)                                                                                |
    | max_repeats            | max repeats for a processing. not work for a unrepeatable recipe ^optional^            | [IntBounds](general-types.md#intbounds)                                                            |
    |                        | additional properties...                                                               |                                                                                                    |

## Recipe Types

### Use Item on a Block

Event when a player uses item on a block.

Currently, interaction with fluid blocks is not supported.

Default behavior: Item is consumed.

This recipe type is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name     | Description               | Type / Literal                                                                                               |
    | -------- | ------------------------- | ------------------------------------------------------------------------------------------------------------ |
    | type     | type                      | "lychee:block_interacting"                                                                                   |
    | item_in  | the item in player's hand | [SizedIngredient](general-types.md#sizedingredient) \| [SizedIngredient](general-types.md#sizedingredient)[] |
    | block_in | the block being used      | [BlockPredicate](general-types.md#blockpredicate)                                                            |

    `item_in` can accept an ingredient list with 2 ingredients. The second one is the item in the off hand.

??? example

    ===! "YAML"

        ```yaml
        type: lychee:block_interacting
        item_in: shears
        block_in: pumpkin
        post: prevent_default
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:block_interacting",
            "item_in": "shears",
            "block_in": "pumpkin",
            "post": "prevent_default"
        }
        ```

    Description: Prevent player from carving pumpkins. Here the `prevent_default` means do not consume the shears.

    ===! "YAML"

        ```yaml
        type: lychee:block_interacting
        item_in: iron_axe
        block_in: oak_log
        post:
        - drop diamond /.5
        - place stripped_oak_log
        - damage_item
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:block_interacting",
            "item_in": "iron_axe",
            "block_in": "oak_log",
            "post": [
                "drop diamond /.5",
                "place stripped_oak_log",
                "damage_item"
            ]
        }
        ```

    Description: When a player uses an iron axe on an oak log, there is a 50% chance of dropping a diamond, the oak log will be stripped, and the axe will be damaged.

??? note "Note: Matches empty hand"

    Use `{}` to require interaction with empty hand.

??? note "Note: Matches any item"

    You can use a special ingredient to match any item:

    === "NeoForge"

        ===! "YAML"

            ```yaml
            type: lychee:block_interacting
            item_in:
              type: lychee:always_true
            block_in: minecraft:stone
            ```

        === "JSON"

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

        ===! "YAML"

            ```yaml
            type: lychee:block_interacting
            item_in:
              fabric:type: lychee:always_true
            block_in: minecraft:stone
            ```

        === "JSON"

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

    | Name     | Description               | Type / Literal                                                                                               |
    | -------- | ------------------------- | ------------------------------------------------------------------------------------------------------------ |
    | type     | type                      | "lychee:block_clicking"                                                                                      |
    | item_in  | the item in player's hand | [SizedIngredient](general-types.md#sizedingredient) \| [SizedIngredient](general-types.md#sizedingredient)[] |
    | block_in | the block being clicked   | [BlockPredicate](general-types.md#blockpredicate)                                                            |

    `item_in` can accept an ingredient list with 2 ingredients. The second one is the item in the off hand.

??? note "Note: Matches empty hand"

    Use `{}` to require interaction with empty hand.

??? note "Note: Matches any item"

    You can use a special ingredient to match any item:

    === "NeoForge"

        ===! "YAML"

            ```yaml
            type: lychee:block_clicking
            item_in:
              type: lychee:always_true
            block_in: minecraft:stone
            ```

        === "JSON"

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

        ===! "YAML"

            ```yaml
            type: lychee:block_clicking
            item_in:
              fabric:type: lychee:always_true
            block_in: minecraft:stone
            ```

        === "JSON"

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

    | Name    | Description    | Type / Literal                                      |
    | ------- | -------------- | --------------------------------------------------- |
    | type    | type           | "lychee:item_burning"                               |
    | item_in | the burnt item | [SizedIngredient](general-types.md#sizedingredient) |

!!! note

    Items such as netherite or nether star can't catch fire.

    Related tag: [`lychee:fire_immune`](extra-features.md#lycheefire_immune)

??? example

    ===! "YAML"

        ```yaml
        type: lychee:item_burning
        item_in: '#logs_that_burn'
        post: drop charcoal
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:item_burning",
            "item_in": "#logs_that_burn",
            "post": "drop charcoal"
        }
        ```

    Description: Logs that can be burnt will drop charcoal.

### Item Entity inside a Block

Event when an item entity is inside a block. This will be tested every second.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: Item is consumed.

!!! note "Format"

    | Name     | Description                        | Type / Literal                                                                                               |
    | -------- | ---------------------------------- | ------------------------------------------------------------------------------------------------------------ |
    | type     | type                               | "lychee:item_inside"                                                                                         |
    | item_in  | the ticking item(s)                | [SizedIngredient](general-types.md#sizedingredient) \| [SizedIngredient](general-types.md#sizedingredient)[] |
    | block_in | the block where the item(s) in     | [BlockPredicate](general-types.md#blockpredicate)                                                            |
    | time     | waiting time in seconds ^optional^ | int                                                                                                          |

??? example

    ===! "YAML"

        ```yaml
        type: lychee:item_inside
        item_in: bucket
        block_in: water_cauldron[level=3]
        post:
        - drop water_bucket
        - place cauldron
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:item_inside",
            "item_in": "bucket",
            "block_in": "water_cauldron[level=3]",
            "post": [
                "drop water_bucket",
                "place cauldron"
            ]
        }
        ```

    Description: When a water bucket is inside a full water cauldron, it will drop a water bucket and turn the cauldron into an empty one.

    ===! "YAML"

        ```yaml
        type: lychee:item_inside
        item_in: bucket
        block_in: water[level=0]
        post:
        - drop water_bucket
        - place *
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:item_inside",
            "item_in": "bucket",
            "block_in": "water[level=0]",
            "post": [
                "drop water_bucket",
                "place *"
            ]
        }
        ```

    Description: When a water bucket is inside a water source block, it will drop a water bucket and consume the water block.

!!! note

    If the block is a fluid block, the block id is not always the same as the fluid id. To see the block id, you should use Jade mod and enable the "Registry Name" option in the plugin settings.

### Anvil Crafting

It is not recommended to add actions to the recipe, because they cannot be displayed on JEI/REI/EMI at the moment.

This recipe type is not [repeatable](concepts.md#repeatability).

Default behavior: Anvil is damaged.

!!! note "Format"

    | Name          | Description                                                                              | Type / Literal                                                                           |
    | ------------- | ---------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
    | type          | type                                                                                     | "lychee:anvil_crafting"                                                                  |
    | item_in       | the input items (the second one is optional)                                             | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[] |
    | item_out      | the result item                                                                          | [ItemStack](general-types.md#itemstack)                                                  |
    | level_cost    | player's xp level ^optional^{ title="default: 1" }                                       | int && >=1                                                                               |
    | material_cost | amount of items that will be cost from right input slot ^optional^{ title="default: 1" } | int                                                                                      |
    | assembling    | actions that running before the result is displayed ^optional^                           | [PostAction](post-action.md) \| [PostAction](post-action.md)[]                           |

??? example

    ===! "YAML"

        ```yaml
        type: lychee:anvil_crafting
        item_in:
        - apple
        - gold_ingot
        item_out: golden_apple
        level_cost: 1
        material_cost: 8
        post: prevent_default
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:anvil_crafting",
            "item_in": [
                "apple",
                "gold_ingot"
            ],
            "item_out": "golden_apple",
            "level_cost": 1,
            "material_cost": 8,
            "post": "prevent_default"
        }
        ```

    Description: Use 1 apple, 8 gold ingots and 1 xp level to craft a golden apple. Here the `prevent_default` means do not damage the anvil.

### Block Crushing

Event when a falling block entity lands on a block.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: Falling block becomes block or drops item. Canceling this will make falling block disappear.

!!! note "Format"

    | Name          | Description                                                                             | Type / Literal                                                                                               |
    | ------------- | --------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
    | type          | type                                                                                    | "lychee:block_crushing"                                                                                      |
    | item_in       | the crushed items ^optional^                                                            | [SizedIngredient](general-types.md#sizedingredient) \| [SizedIngredient](general-types.md#sizedingredient)[] |
    | falling_block | the falling block. default are all the anvils ^optional^{ default: &quot;#anvil&quot; } | [BlockPredicate](general-types.md#blockpredicate)                                                            |
    | landing_block | the landing block. default is any block ^optional^                                      | [BlockPredicate](general-types.md#blockpredicate)                                                            |

??? example

    ===! "YAML"

        ```yaml
        type: lychee:block_crushing
        item_in: 3x sugar_cane
        post: drop 3x paper
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:block_crushing",
            "item_in": "3x sugar_cane",
            "post": "drop 3x paper"
        }
        ```

    Description: When an anvil falls on 3 sugar cane items, they will be crushed into 3 papers.

    ===! "YAML"

        ```yaml
        type: lychee:block_crushing
        landing_block: moss_carpet
        if:
          type: location
          offsetY: -1
          predicate:
            block:
              blocks: stone_bricks
        post:
        - place *
        - type: place
          offsetY: -1
          block: mossy_stone_bricks
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:block_crushing",
            "landing_block": "moss_carpet",
            "if": {
                "type": "location",
                "offsetY": -1,
                "predicate": {
                    "block": {
                        "blocks": "stone_bricks"
                    }
                }
            },
            "post": [
                "place *",
                {
                    "type": "place",
                    "offsetY": -1,
                    "block": "mossy_stone_bricks"
                }
            ]
        }
        ```

    Description: Making a mossy stone bricks block. It uses a location check to check the block below the current position.

!!! note

    Dispenser can now place fallable blocks and items that tagged with `lychee:dispenser_placement` in front of it. Note the item should not have a special dispense behavior.

!!! note

    Normally it will process all the items that touches the falling block, but if the landing block is tagged with `lychee:extend_box` (cauldrons by default), it will collect items inside the landing block as well.

### Lightning Channeling

Event when a lightning is channeling.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: Items are consumed. Canceling this will **not** prevent item from being damaged.

!!! note "Format"

    | Name    | Description                           | Type / Literal                                                                                               |
    | ------- | ------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
    | type    | type                                  | "lychee:lightning_channeling"                                                                                |
    | item_in | items nearby the lightning ^optional^ | [SizedIngredient](general-types.md#sizedingredient) \| [SizedIngredient](general-types.md#sizedingredient)[] |

??? example

    Make nearby stone become calcite:

    ===! "YAML"

        ```yaml
        type: lychee:lightning_channeling
        post: run "fill ~-3 ~-3 ~-3 ~3 ~3 ~3 stone replace calcite"
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:lightning_channeling",
            "post": "run \"fill ~-3 ~-3 ~-3 ~3 ~3 ~3 stone replace calcite\""
        }
        ```

    Description: When a lightning strikes, it will replace all calcite blocks with stone blocks in a 7x7x7 area.

!!! note

    Related tags: [`lychee:lightning_immune`](extra-features.md#lycheelightning_immune), [`lychee:lightning_fire_immune`](extra-features.md#lycheelightning_fire_immune)

### Item Exploding

Event when an item entity is exploded.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: Items are consumed. Canceling this will **not** prevent item from being damaged.

!!! note "Format"

    | Name                  | Description                                                                                 | Type / Literal                                                                                               |
    | --------------------- | ------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
    | type                  | type                                                                                        | "lychee:item_exploding"                                                                                      |
    | item_in               | items affected by the explosion ^optional^                                                  | [SizedIngredient](general-types.md#sizedingredient) \| [SizedIngredient](general-types.md#sizedingredient)[] |
    | display_tnt           | explosion source displayed in recipe viewers ^optional^{ title="default: &quot;tnt&quot;" } | [BlockPredicate](general-types.md#blockpredicate)                                                            |
    | allow_small_explosion | allows small explosion like wind charges ^optional^{ title="default: false" }               | true \| false                                                                                                |

### Block Exploding

Event when a block is exploded.

This recipe type is not [repeatable](concepts.md#repeatability).

Default behavior: Block drops items from loot table.

!!! note "Format"

    | Name                  | Description                                                                                 | Type / Literal                                    |
    | --------------------- | ------------------------------------------------------------------------------------------- | ------------------------------------------------- |
    | type                  | type                                                                                        | "lychee:block_exploding"                          |
    | block_in              | block destroyed by the explosion ^optional^                                                 | [BlockPredicate](general-types.md#blockpredicate) |
    | display_tnt           | explosion source displayed in recipe viewers ^optional^{ title="default: &quot;tnt&quot;" } | [BlockPredicate](general-types.md#blockpredicate) |
    | allow_small_explosion | allows small explosion like wind charges ^optional^{ title="default: false" }               | true \| false                                     |

### Entity Ticking

Event when an entity ticked.

This recipe type is not [repeatable](concepts.md#repeatability).

Default behavior: Continue matching the rest of the recipes.

!!! note "Format"

    | Name   | Description      | Type / Literal                                                                          |
    | ------ | ---------------- | --------------------------------------------------------------------------------------- |
    | type   | type             | "lychee:entity_ticking"                                                                 |
    | entity | entity predicate | [EntityPredicate](https://minecraft.wiki/w/Advancement/Conditions/entity?oldid=2600889) |

!!! note

    This recipe type does not have JEI/REI/EMI integration.

??? example

    ===! "YAML"

        ```yaml
        type: lychee:entity_ticking
        entity:
          type: falling_block
          nbt:
            BlockState:
              Name: minecraft:gravel
          movement:
            fall_distance:
              min: 5
        post: set_block iron_block
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:entity_ticking",
            "entity": {
                "type": "falling_block",
                "nbt": {
                    "BlockState": {
                        "Name": "minecraft:gravel"
                    }
                },
                "movement": {
                    "fall_distance": {
                        "min": 5
                    }
                }
            },
            "post": "set_block iron_block"
        }
        ```

    Description: When a falling block with gravel as its block state has a fall distance of 5 or more, it will be replaced with an iron block.

    ===! "YAML"

        ```yaml
        type: lychee:entity_ticking
        entity:
          type: player
          equipment:
            head:
              items: netherite_helmet
            chest:
              items: netherite_chestplate
            legs:
              items: netherite_leggings
            feet:
              items: netherite_boots
        interval: 20
        post: run "effect give @s fire_resistance 3 0 true"
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:entity_ticking",
            "entity": {
                "type": "player",
                "equipment": {
                    "head": {
                        "items": "netherite_helmet"
                    },
                    "chest": {
                        "items": "netherite_chestplate"
                    },
                    "legs": {
                        "items": "netherite_leggings"
                    },
                    "feet": {
                        "items": "netherite_boots"
                    }
                }
            },
            "interval": 20,
            "post": "run \"effect give @s fire_resistance 3 0 true\""
        }
        ```

    Description: When a player has a full set of netherite armor, they will receive fire resistance every 20 ticks.

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

    This recipe type does not have JEI/REI/EMI integration.

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

    ===! "YAML"

        ```yaml
        type: lychee:dripstone_dripping
        source_block: water
        target_block: sponge
        post: place wet_sponge
        ```

    === "JSON"

        ```json
        {
            "type": "lychee:dripstone_dripping",
            "source_block": "water",
            "target_block": "sponge",
            "post": "place wet_sponge"
        }
        ```

    Description: When water drips on a sponge, it will turn into a wet sponge.

### Advanced Shaped Crafting

This allows you to add contextual conditions and actions, and control the behaviors of the ingredients and the result.

This recipe type is not [repeatable](concepts.md#repeatability).

Default behavior: none.

!!! note "Format"

    | Name       | Description                                                    | Type / Literal                                                        |
    | ---------- | -------------------------------------------------------------- | --------------------------------------------------------------------- |
    | type       | type                                                           | "lychee:crafting"                                                     |
    | pattern    | same as vanilla                                                |                                                                       |
    | key        | same as vanilla                                                |                                                                       |
    | result     | same as vanilla                                                | [ItemStack](general-types.md#itemstack)(shorthand item not supported) |
    | group      | same as vanilla ^optional^                                     |                                                                       |
    | assembling | actions that running before the result is displayed ^optional^ | [PostAction](post-action.md) \| [PostAction](post-action.md)[]        |

    Note: You cannot use shorthand ingredients or result in this recipe type.

??? example

    ===! "YAML"

        ```yaml
        type: lychee:crafting
        pattern:
        - A
        - B
        key:
          A:
            item: pufferfish
          B:
            item: water_bucket
        result:
          id: apple
        post:
          type: set_item
          target: /key/B
          id: air
        assembling:
          type: set_item
          target: /result
          id: pufferfish_bucket
        ```

    === "JSON"

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
                "id": "apple"
            },
            "post": {
                "type": "set_item",
                "target": "/key/B",
                "id": "air"
            },
            "assembling": {
                "type": "set_item",
                "target": "/result",
                "id": "pufferfish_bucket"
            }
        }
        ```

    Description: Crafting a pufferfish and a water bucket into a pufferfish bucket. The water bucket will be consumed and the result will be shown as an apple, before you take it out.

    [Here](kubejs.md#anvil-crafting-recipe-to-repair-tools) is a more advanced example that uses KubeJS Integration.

!!! note

    No guarantee that modded crafting machines contains player and location information. You can use [`param`](contextual-condition.md#parameter-check) to require these parameters to be present when crafting.
