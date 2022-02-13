# Post Action

Post action is an action to be executed.

Usually you can add post actions to a Lychee's recipe, and  they will be executed after the recipe is successfully matched.

## Basic format

| Name       | Description                      | Type / Literal                                                                                     |
| ---------- | -------------------------------- | -------------------------------------------------------------------------------------------------- |
| type       | type                             | string                                                                                             |
| contextual | (optional) contextual conditions | [ContextualCondition](contextual-condition.md) \| [ContextualCondition](contextual-condition.md)[] |
|            | additional properties...         |                                                                                                    |

## Built-in actions

### Drop item (`drop_item`)

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

### Place block (`place`)

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

### Execute command (`execute`)

Executes a command.

!!! note "Format"

    | Name    | Description                            | Type / Literal |
    | ------- | -------------------------------------- | -------------- |
    | type    | type                                   | "execute"      |
    | command | the command to run                     | string         |
    | hide    | (optional) hide this action in JEI/REI | boolean        |

### Drop experience (`drop_xp`)

Spawns experience orbs.

!!! note "Format"

    | Name | Description | Type / Literal |
    | ---- | ----------- | -------------- |
    | type | type        | "drop_xp"      |
    | xp   | amount      | int            |

## Special built-in actions

These following actions will prevent the default behavior of the recipe (such as consuming input item). The default behavior is explained in the recipes page.

### Prevent default behavior (`prevent_default`)

Prevents default behavior and do nothing.

!!! note "Format"

    | Name | Description | Type / Literal    |
    | ---- | ----------- | ----------------- |
    | type | type        | "prevent_default" |

### Damage input item (`damage_item`)

Damages input item.

This action is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name   | Description       | Type / Literal |
    | ------ | ----------------- | -------------- |
    | type   | type              | "damage_item"  |
    | damage | (optional) damage | int            |

### Set falling anvil damage chance (`damage_anvil_chance`)

This action can only be used in the [Block Crushing](recipe.md#block-crushing) recipe. The default damage chance depends on the falling height.

    | Name   | Description | Type / Literal        |
    | ------ | ----------- | --------------------- |
    | type   | type        | "damage_anvil_chance" |
    | chance | chance      | number [0,1]          |
