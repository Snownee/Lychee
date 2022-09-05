# Post Action

Post action is an action to be executed.

Usually you can add post actions to a Lychee's recipe, and  they will be executed after the recipe is successfully matched.

## Basic Format

| Name       | Description                      | Type / Literal                                                                                     |
| ---------- | -------------------------------- | -------------------------------------------------------------------------------------------------- |
| type       | type                             | string                                                                                             |
| contextual | (optional) contextual conditions | [ContextualCondition](contextual-condition.md) \| [ContextualCondition](contextual-condition.md)[] |
|            | additional properties...         |                                                                                                    |

## Built-in Actions

### Drop Item (`drop_item`)

Spawns an item entity on the ground.

!!! note "Format"

    | Name  | Description                      | Type / Literal   |
    | ----- | -------------------------------- | ---------------- |
    | type  | type                             | "drop_item"      |
    | item  | the item resource id             | string           |
    | count | (optional) item amount           | int              |
    | nbt   | (Forge only) (optional) item nbt | object \| string |

??? example

	Drops a water bottle:

	```json
	{
		"type": "drop_item",
		"item": "potion",
		"nbt": {
			"Potion": "minecraft:water"
		}
	}
	```

### Place Block (`place`)

Places a block in world.

This action is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name    | Description                    | Type / Literal                                    |
    | ------- | ------------------------------ | ------------------------------------------------- |
    | type    | type                           | "place"                                           |
    | block   | the block being placed         | [BlockPredicate](general-types.md#blockpredicate) |
    | offsetX | (optional) offsets to location | int                                               |
    | offsetY | (optional) offsets to location | int                                               |
    | offsetZ | (optional) offsets to location | int                                               |

??? example

	Places a cauldron:

	```json
	{
		"type": "place",
		"block": "cauldron"
	}
	```

	Places a waterlogged oak stairs:

	```json
	{
		"type": "place",
		"block": {
			"blocks": ["oak_stairs"],
			"state": {
				"waterlogged": "true"
			}
		}
	}
	```

	Destroys current block (place air):

	```json
	{
		"type": "place",
		"block": "*"
	}
	```

### Execute Command (`execute`)

Executes a command.

!!! note "Format"

    | Name    | Description                            | Type / Literal |
    | ------- | -------------------------------------- | -------------- |
    | type    | type                                   | "execute"      |
    | command | the command to run                     | string         |
    | hide    | (optional) hide this action in JEI/REI | boolean        |

### Drop Experience (`drop_xp`)

Spawns experience orbs.

!!! note "Format"

    | Name | Description | Type / Literal |
    | ---- | ----------- | -------------- |
    | type | type        | "drop_xp"      |
    | xp   | amount      | int            |

### Random (`random`)

Randomly selects entries from an actions list to apply. Similar to loot table.

!!! note "Format"

    | Name    | Description                                          | Type / Literal                          |
    | ------- | ---------------------------------------------------- | --------------------------------------- |
    | type    | type                                                 | "random"                                |
    | rolls   | (optional) specifies the number of rolls on the pool | [IntBounds](general-types.md#intbounds) |
    | entries | a list of actions that can be applied                | WeightedPostAction[]                    |

    The format of WeightedPostAction is just like a normal PostAction, but you can add a `weight` entry to it to decide how often this action is chosen out of all the actions.

??? example

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
				"item": "gold_ingot",
				"contextual": {
					"type": "weather",
					"weather": "rain"
				}
			},
			{
				"type": "drop_item",
				"item": "ender_pearl"
			},
			{
				"weight": 2,
				"type": "drop_item",
				"item": "dirt"
			}
		]
	}
	```

### Create Explosion (`explode`)

Creates explosion at where the interaction occurs.

!!! note "Format"

    | Name              | Description                                                                                   | Type / Literal                 |
    | ----------------- | --------------------------------------------------------------------------------------------- | ------------------------------ |
    | type              | type                                                                                          | "explode"                      |
    | offsetX           | (optional) offsets to location                                                                | int                            |
    | offsetY           | (optional) offsets to location                                                                | int                            |
    | offsetZ           | (optional) offsets to location                                                                | int                            |
    | fire              | (optional) set fire. false by default                                                         | boolean                        |
    | block_interaction | (optional) whether break blocks or not. "break" by default                                    | "none" \| "break" \| "destroy" |
    | radius            | (optional) the base radius of the explosion. 4 by default                                     | number                         |
    | radius_step       | (optional) the radius step according to how many times the recipe can be done. 0.5 by default | number                         |

### Hurt Entity (`hurt`)

Causes damage to the entity

!!! note "Format"

    | Name   | Description                                         | Type / Literal |
    | ------ | --------------------------------------------------- | -------------- |
    | type   | type                                                | "hurt"         |
    | damage | range of damage                                     | DoubleBounds   |
    | source | (optional) damage source type. "generic" by default | string         |
    
    Source allowed values: generic, magic, out_of_world, anvil, wither, freeze, drown, fall, in_fire, on_fire, lava

??? example

	```json
	{
		"type": "lychee:block_interacting",
		"item_in": {
			"item": "shears"
		},
		"block_in": "pumpkin",
		"contextual": {
			"type": "entity_health",
			"range": {
				"min": 2.1
			}
		},
		"post": [
			{
				"type": "prevent_default"
			},
			{
				"type": "hurt",
				"source": "generic",
				"damage": 2
			}
		]
	}
	```

## Special Built-in Actions

These following actions will prevent the default behavior of the recipe (such as consuming input item). The default behavior is explained in the recipes page.

### Prevent Default Behavior (`prevent_default`)

Prevents default behavior and do nothing.

!!! note "Format"

    | Name | Description | Type / Literal    |
    | ---- | ----------- | ----------------- |
    | type | type        | "prevent_default" |

### Damage Input Item (`damage_item`)

Damages input item.

This action is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name   | Description       | Type / Literal |
    | ------ | ----------------- | -------------- |
    | type   | type              | "damage_item"  |
    | damage | (optional) damage | int            |

### Set Falling Anvil Damage Chance (`anvil_damage_chance`)

This action can only be used in the [Block Crushing](recipe.md#block-crushing) recipe. The default damage chance depends on the falling height.

!!! note "Format"

    | Name   | Description            | Type / Literal        |
    | ------ | ---------------------- | --------------------- |
    | type   | type                   | "anvil_damage_chance" |
    | chance | chance between 0 and 1 | number                |
