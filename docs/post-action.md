# Post Action

Post-action is an action to be executed.

You can add post-actions to a Lychee's recipe, and they will be executed after the recipe is successfully matched.

## Basic Format

| Name | Description                                                          | Type / Literal                                                                                     |
| ---- | -------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
| type | type                                                                 | string                                                                                             |
| if   | contextual conditions ^optional^                                     | [ContextualCondition](contextual-condition.md) \| [ContextualCondition](contextual-condition.md)[] |
| hide | hide this action in JEI/REI/EMI ^optional^{ title="default: false" } | true \| false                                                                                      |
| icon | sprite icon location ^optional^                                      | string                                                                                             |
|      | additional properties...                                             |                                                                                                    |

## Basic Shorthand Format

Post-actions can be defined using a shorthand string:

===! "YAML"

	```yaml
	<command> [arg1] [arg2] ... [/option1][/option2]
	```

=== "JSON"

	```json
	"<command> [arg1] [arg2] ... [/option1][/option2]"
	```

### Available Options

- `/hide` - Hide this action in JEI/REI/EMI.
- `/<number>` - Add a chance for this action to be executed. For example, `/0.5` = 50% chance.

## Entity-related Actions

### Drop Item

Spawns an item entity on the ground.

!!! note "Shorthand Format"

    `drop <ItemStack>`

??? note "Format"

    | Name       | Description                                  | Type / Literal      |
    | ---------- | -------------------------------------------- | ------------------- |
    | type       | type                                         | "drop_item"         |
    | id         | the item resource id                         | string (Identifier) |
    | count      | item amount ^optional^{ title="default: 1" } | int                 |
    | components | item components ^optional^                   | dictionary          |

??? example

    Drops a water bottle:

    ===! "YAML"

        ```yaml
        type: drop_item
        id: potion
        components:
          potion_contents:
            potion: minecraft:water
        ```

    === "JSON"

        ```json
        {
            "type": "drop_item",
            "id": "potion",
            "components": {
                "potion_contents": {
                    "potion": "minecraft:water"
                }
            }
        }
        ```

### Drop Experience

Spawns experience orbs.

!!! note "Shorthand Format"

    `drop <amount: int>xp`

??? note "Format"

    | Name | Description | Type / Literal |
    | ---- | ----------- | -------------- |
    | type | type        | "drop_xp"      |
    | xp   | amount      | int            |

### Set Falling Block's Block

Sets the block of a falling block entity.

This action is not [repeatable](concepts.md#repeatability).

!!! note "Shorthand Format"

    `set_block <BlockPredicate>`

??? note "Format"

    | Name  | Description         | Type / Literal                                    |
    | ----- | ------------------- | ------------------------------------------------- |
    | type  | type                | "set_block"                                       |
    | block | the block being set | [BlockPredicate](general-types.md#blockpredicate) |

## Block-related Actions

### Place Block

Places a block in world.

This action is not [repeatable](concepts.md#repeatability).

!!! note "Shorthand Format"

    `place <BlockPredicate> [<offsetX: int> <offsetY: int> <offsetZ: int>]`

??? note "Format"

    | Name    | Description                                    | Type / Literal                                    |
    | ------- | ---------------------------------------------- | ------------------------------------------------- |
    | type    | type                                           | "place"                                           |
    | block   | the block being placed                         | [BlockPredicate](general-types.md#blockpredicate) |
    | offsetX | offsets to location ^optional^                 | int                                               |
    | offsetY | offsets to location ^optional^                 | int                                               |
    | offsetZ | offsets to location ^optional^                 | int                                               |
    | multi   | see below ^optional^{ title="default: false" } | true \| false                                     |

    The "multi" option serves the following purposes:

    1. Places the corresponding blocks if the block is a multiblock.(e.g., beds, doors, tall flowers)
    2. Triggers some special actions upon placement.(e.g., wither skull summoning, powering redstone repeater)

??? example

    Places a cauldron:

    ===! "YAML"

        ```yaml
        place cauldron
        ```

    === "JSON"

        ```json
        "place cauldron"
        ```

    ===! "YAML"

        ```yaml
        type: place
        block: cauldron
        ```

    === "JSON"

        ```json
        {
            "type": "place",
            "block": "cauldron"
        }
        ```

    Places a waterlogged oak stairs:

    ===! "YAML"

        ```yaml
        place oak_stairs[waterlogged=true]
        ```

    === "JSON"

        ```json
        "place oak_stairs[waterlogged=true]"
        ```

    ===! "YAML"

        ```yaml
        type: place
        block:
          blocks: oak_stairs
          state:
            waterlogged: 'true'
        ```

    === "JSON"

        ```json
        {
            "type": "place",
            "block": {
                "blocks": "oak_stairs",
                "state": {
                    "waterlogged": "true"
                }
            }
        }
        ```

    Destroys current block (place air):

    ===! "YAML"

        ```yaml
        place *
        ```

    === "JSON"

        ```json
        "place *"
        ```

    ===! "YAML"

        ```yaml
        type: place
        block: '*'
        ```

    === "JSON"

        ```json
        {
            "type": "place",
            "block": "*"
        }
        ```

### Cycle State Property

Cycles a property's value in a block-state.

!!! note "Format"

    | Name     | Description                                                | Type / Literal                                    |
    | -------- | ---------------------------------------------------------- | ------------------------------------------------- |
    | type     | type                                                       | "cycle_state_property"                            |
    | block    | only matched block-states will be cycled                   | [BlockPredicate](general-types.md#blockpredicate) |
    | property | the property name                                          | string                                            |
    | offsetX  | offsets to location ^optional^                             | int                                               |
    | offsetY  | offsets to location ^optional^                             | int                                               |
    | offsetZ  | offsets to location ^optional^                             | int                                               |
    | reversed | cycle in reversed order ^optional^{title="default: false"} | true \| false                                     |

## Control Flow Actions

### Prevent Default Behavior

Prevents default behavior and do nothing. The default behaviors are explained on the recipes page.

!!! note "Shorthand Format"

    `prevent_default`

??? note "Format"

    | Name | Description | Type / Literal    |
    | ---- | ----------- | ----------------- |
    | type | type        | "prevent_default" |

### Delay

Waits for several seconds, then execute the following actions.

!!! note "Shorthand Format"

    `delay <seconds: number>`

??? note "Format"

    | Name | Description | Type / Literal |
    | ---- | ----------- | -------------- |
    | type | type        | "delay"        |
    | s    | seconds     | number         |

!!! note

    After the delay, some context will lose. For example, if the player leaves the game while delaying, you can't hurt the player after this delay.

### Exit

Stops executing the following actions.

!!! note "Shorthand Format"

    `exit`

??? note "Format"

    | Name | Description | Type / Literal |
    | ---- | ----------- | -------------- |
    | type | type        | "exit"         |

### Random

Randomly selects entries from an action list to apply. Similar to loot table.

!!! note "Format"

    | Name         | Description                                          | Type / Literal                          |
    | ------------ | ---------------------------------------------------- | --------------------------------------- |
    | type         | type                                                 | "random"                                |
    | rolls        | specifies the number of rolls on the pool ^optional^ | [IntBounds](general-types.md#intbounds) |
    | entries      | a list of actions that can be applied                | WeightedPostAction[]                    |
    | empty_weight | ^optional^{ title="default: 0" }                     | int                                     |

    The format of `WeightedPostAction` is just like a normal PostAction, but you can add a `weight` entry to it to decide how often this action is chosen out of all the actions.

??? example

    ===! "YAML"

        ```yaml
        type: random
        rolls:
          min: 3
          max: 5
        entries:
        - type: drop_item
          id: gold_ingot
          if:
            type: weather
            weather: rain
        - type: drop_item
          id: ender_pearl
        - weight: 2
          type: drop_item
          id: dirt
        ```

    === "JSON"

        ```json
        {
            "type": "random",
            "rolls": {
                "min": 3,
                "max": 5
            },
            "entries": [
                {
                    "type": "drop_item",
                    "id": "gold_ingot",
                    "if": {
                        "type": "weather",
                        "weather": "rain"
                    }
                },
                {
                    "type": "drop_item",
                    "id": "ender_pearl"
                },
                {
                    "weight": 2,
                    "type": "drop_item",
                    "id": "dirt"
                }
            ]
        }
        ```

### If-Else Statement

Executes a list of actions if the contextual conditions are met or not.

!!! note "Format"

    | Name | Description                                                               | Type / Literal                                                 |
    | ---- | ------------------------------------------------------------------------- | -------------------------------------------------------------- |
    | type | type                                                                      | "if"                                                           |
    | then | a list of actions to be executed if the conditions are met ^optional^     | [PostAction](post-action.md) \| [PostAction](post-action.md)[] |
    | else | a list of actions to be executed if the conditions are not met ^optional^ | [PostAction](post-action.md) \| [PostAction](post-action.md)[] |

## Position Anchor Actions

### Move

Moves the anchored position in the context.

!!! note "Shorthand Format"

    `move <x: int> <y: int> <z: int>`

??? note "Format"

    | Name   | Description                    | Type / Literal |
    | ------ | ------------------------------ | -------------- |
    | type   | type                           | "move"         |
    | offset | the distance to move           | number\[3]     |
    | with   | block property name ^optional^ | string         |

    You can use `with` to rotate the offset according to the facing of the current block. When `with` is set, the offset will be rotated from "up".

### Move towards Face

Moves the anchored position in the context towards the direction that being interacted. Only works for interaction
recipes.

!!! note "Format"

    | Name   | Description                             | Type / Literal      |
    | ------ | --------------------------------------- | ------------------- |
    | type   | type                                    | "move_towards_face" |
    | factor | factor ^optional^{ title="default: 1" } | number              |

## Miscellaneous Actions

### Execute Command

Executes a command.

!!! note "Shorthand Format"

    `run "<command: string>"` or `execute "<command: string>"`

??? note "Format"

    | Name    | Description                                                              | Type / Literal |
    | ------- | ------------------------------------------------------------------------ | -------------- |
    | type    | type                                                                     | "execute"      |
    | command | the command to run                                                       | string         |
    | repeat  | execute commands by repetition count ^optional^{ title="default: true" } | true \| false  |

??? example

    Spawns particles:

    ===! "YAML"

        ```yaml
        run "particle minecraft:angry_villager ~ ~1 ~ 1 1 1 0 20" /hide
        ```

    === "JSON"

        ```json
        "run \"particle minecraft:angry_villager ~ ~1 ~ 1 1 1 0 20\" /hide"
        ```

    ===! "YAML"

        ```yaml
        type: execute
        command: particle minecraft:angry_villager ~ ~1 ~ 1 1 1 0 20
        hide: true
        ```

    === "JSON"

        ```json
        {
            "type": "execute",
            "command": "particle minecraft:angry_villager ~ ~1 ~ 1 1 1 0 20",
            "hide": true
        }
        ```

    For how to use `particle` command, please read the [wiki](https://minecraft.wiki/w/Commands/particle).

### Add Item Cooldown

Adds item cooldown to an item, just like the cooldown when you use an ender pearl.

!!! note "Shorthand Format"

    `add_item_cooldown <seconds: number>`

??? note "Format"

    | Name | Description                                                                   | Type / Literal      |
    | ---- | ----------------------------------------------------------------------------- | ------------------- |
    | type | type                                                                          | "add_item_cooldown" |
    | s    | seconds                                                                       | number              |
    | item | the item resource id ^optional^{ title="default: the item in player's hand" } | string (Identifier) |

### Create Explosion

Creates an explosion at where the interaction occurs.

!!! note "Format"

    | Name              | Description                                                                                   | Type / Literal                              |
    | ----------------- | --------------------------------------------------------------------------------------------- | ------------------------------------------- |
    | type              | type                                                                                          | "explode"                                   |
    | offsetX           | offsets to location ^optional^                                                                | int                                         |
    | offsetY           | offsets to location ^optional^                                                                | int                                         |
    | offsetZ           | offsets to location ^optional^                                                                | int                                         |
    | fire              | set fire. false by default ^optional^                                                         | true \| false                               |
    | block_interaction | whether break blocks or not. "destroy" by default ^optional^                                  | "keep" \| "destroy" \| "destroy_with_decay" |
    | radius            | the base radius of the explosion. 4 by default ^optional^                                     | number                                      |
    | radius_step       | the radius step according to how many times the recipe can be done. 0.5 by default ^optional^ | number                                      |

### Set Falling Anvil Damage Chance

This action can only be used in the [Block Crushing](recipe.md#block-crushing) recipe. The default damage chance depends
on the falling height.

!!! note "Format"

    | Name   | Description            | Type / Literal        |
    | ------ | ---------------------- | --------------------- |
    | type   | type                   | "anvil_damage_chance" |
    | chance | chance between 0 and 1 | number                |

### Damage Item

Consumes the item's durability.

This action is not [repeatable](concepts.md#repeatability).

!!! note "Shorthand Format"

    `damage_item`

??? note "Format"

    | Name   | Description                                           | Type / Literal                              |
    | ------ | ----------------------------------------------------- | ------------------------------------------- |
    | type   | type                                                  | "damage_item"                               |
    | damage | damage ^optional^{ title="default: 1" }               | int                                         |
    | target | target items ^optional^{ title="default: all items" } | [JsonPointer](general-types.md#jsonpointer) |

### Set Item

Replaces the inputs or the results.

This action is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name       | Description                | Type / Literal                              |
    | ---------- | -------------------------- | ------------------------------------------- |
    | type       | type                       | "set_item"                                  |
    | target     | target items ^optional^    | [JsonPointer](general-types.md#jsonpointer) |
    | id         | the item resource id       | string (Identifier)                         |
    | count      | item amount ^optional^     | int                                         |
    | components | item components ^optional^ | dictionary                                  |
