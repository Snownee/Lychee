# Post Action

Post-action is an action to be executed.

Usually, you can add post actions to a Lychee's recipe, and they will be executed after the recipe is successfully matched.

## Basic Format

| Name       | Description                      | Type / Literal                                                                                     |
| ---------- | -------------------------------- | -------------------------------------------------------------------------------------------------- |
| type       | type                             | string                                                                                             |
| contextual | contextual conditions ^optional^ | [ContextualCondition](contextual-condition.md) \| [ContextualCondition](contextual-condition.md)[] |
|            | additional properties...         |                                                                                                    |

## Built-in Actions

### Drop Item

Spawns an item entity on the ground.

!!! note "Format"

    | Name  | Description                      | Type / Literal   |
    | ----- | -------------------------------- | ---------------- |
    | type  | type                             | "drop_item"      |
    | item  | the item resource id             | string           |
    | count | item amount ^optional^           | int              |
    | nbt   | (Forge only) item nbt ^optional^ | object \| string |

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

### Place Block

Places a block in world.

This action is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name    | Description                    | Type / Literal                                    |
    | ------- | ------------------------------ | ------------------------------------------------- |
    | type    | type                           | "place"                                           |
    | block   | the block being placed         | [BlockPredicate](general-types.md#blockpredicate) |
    | offsetX | offsets to location ^optional^ | int                                               |
    | offsetY | offsets to location ^optional^ | int                                               |
    | offsetZ | offsets to location ^optional^ | int                                               |

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

### Execute Command

Executes a command.

!!! note "Format"

    | Name    | Description                                                   | Type / Literal |
    | ------- | ------------------------------------------------------------- | -------------- |
    | type    | type                                                          | "execute"      |
    | command | the command to run                                            | string         |
    | hide    | hide this action in JEI/REI. [^optional^](# "default: false") | boolean        |

??? example

	Spawns particles:

	```json
	{
		"type": "execute",
		"command": "particle minecraft:angry_villager ~ ~1 ~ 1 1 1 0 20",
		"hide": true
	}
	```

	For how to use `particle` command, please read the [wiki](https://minecraft.fandom.com/wiki/Commands/particle).

### Drop Experience

Spawns experience orbs.

!!! note "Format"

    | Name | Description | Type / Literal |
    | ---- | ----------- | -------------- |
    | type | type        | "drop_xp"      |
    | xp   | amount      | int            |

### Random

Randomly selects entries from an action list to apply. Similar to loot table.

!!! note "Format"

    | Name    | Description                                          | Type / Literal                          |
    | ------- | ---------------------------------------------------- | --------------------------------------- |
    | type    | type                                                 | "random"                                |
    | rolls   | specifies the number of rolls on the pool ^optional^ | [IntBounds](general-types.md#intbounds) |
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

### Create Explosion

Creates an explosion at where the interaction occurs.

!!! note "Format"

    | Name              | Description                                                                                   | Type / Literal                 |
    | ----------------- | --------------------------------------------------------------------------------------------- | ------------------------------ |
    | type              | type                                                                                          | "explode"                      |
    | offsetX           | offsets to location ^optional^                                                                | int                            |
    | offsetY           | offsets to location ^optional^                                                                | int                            |
    | offsetZ           | offsets to location ^optional^                                                                | int                            |
    | fire              | set fire. false by default ^optional^                                                         | boolean                        |
    | block_interaction | whether break blocks or not. "break" by default ^optional^                                    | "none" \| "break" \| "destroy" |
    | radius            | the base radius of the explosion. 4 by default ^optional^                                     | number                         |
    | radius_step       | the radius step according to how many times the recipe can be done. 0.5 by default ^optional^ | number                         |

### Hurt Entity

Causes damage to the entity.

!!! note "Format"

    | Name   | Description                                         | Type / Literal |
    | ------ | --------------------------------------------------- | -------------- |
    | type   | type                                                | "hurt"         |
    | damage | range of damage                                     | DoubleBounds   |
    | source | damage source type. "generic" by default ^optional^ | string         |
    
    Allowed values for `source`: generic, magic, out_of_world, anvil, wither, freeze, drown, fall, in_fire, on_fire, lava

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

### Set Falling Anvil Damage Chance

This action can only be used in the [Block Crushing](recipe.md#block-crushing) recipe. The default damage chance depends on the falling height.

!!! note "Format"

    | Name   | Description            | Type / Literal        |
    | ------ | ---------------------- | --------------------- |
    | type   | type                   | "anvil_damage_chance" |
    | chance | chance between 0 and 1 | number                |

### Add Item Cooldown

Adds item cooldown to the item in player's hand, just like when you use ender pearl.

This action only works for interaction recipes.

!!! note "Format"

    | Name | Description | Type / Literal      |
    | ---- | ----------- | ------------------- |
    | type | type        | "add_item_cooldown" |
    | s    | seconds     | number              |

### Move towards Face

*Not implemented for Fabric 1.18.2*

Moves the anchored position in the context towards the direction that being interacted. Only works for interaction recipes.

!!! note "Format"

    | Name   | Description                     | Type / Literal      |
    | ------ | ------------------------------- | ------------------- |
    | type   | type                            | "move_towards_face" |
    | factor | factor. 1 by default ^optional^ | number              |

### Delay

*Since: 3.2*

Waits for several seconds, then execute the following actions.

!!! note "Format"

    | Name | Description | Type / Literal |
    | ---- | ----------- | -------------- |
    | type | type        | "delay"        |
    | s    | seconds     | number         |

!!! note

	After the delay, some context will lose. For example, if the player leaves the game while delaying, you can't hurt the player after this delay.

### Break

*Since: 3.2*

Stops executing the following actions.

!!! note "Format"

    | Name | Description | Type / Literal |
    | ---- | ----------- | -------------- |
    | type | type        | "break"        |

### Cycle State Property

*Since: 3.2*

Cycles a property's value in a block-state.

!!! note "Format"

    | Name     | Description                              | Type / Literal                                    |
    | -------- | ---------------------------------------- | ------------------------------------------------- |
    | type     | type                                     | "cycle_state_property"                            |
    | block    | only matched block-states will be cycled | [BlockPredicate](general-types.md#blockpredicate) |
    | property | the property name                        | string                                            |
    | offsetX  | offsets to location ^optional^           | int                                               |
    | offsetY  | offsets to location ^optional^           | int                                               |
    | offsetZ  | offsets to location ^optional^           | int                                               |

## Special Built-in Actions

These following actions will prevent the default behavior of the recipe (such as consuming input item). The default behaviors are explained on the recipes page.

### Prevent Default Behavior

Prevents default behavior and do nothing.

!!! note "Format"

    | Name | Description | Type / Literal    |
    | ---- | ----------- | ----------------- |
    | type | type        | "prevent_default" |

### Damage Item

Consumes the item's durability.

This action is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name   | Description                        | Type / Literal                              |
    | ------ | ---------------------------------- | ------------------------------------------- |
    | type   | type                               | "damage_item"                               |
    | damage | damage ^optional^                  | int                                         |
    | target | (optional, since 3.4) target items | [JsonPointer](general-types.md#jsonpointer) |

### Set Item

Replaces the inputs or the results.

This action is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name   | Description                      | Type / Literal                              |
    | ------ | -------------------------------- | ------------------------------------------- |
    | type   | type                             | "set_item"                                  |
    | target | target items ^optional^          | [JsonPointer](general-types.md#jsonpointer) |
    | item   | the item resource id             | string                                      |
    | count  | item amount ^optional^           | int                                         |
    | nbt    | (Forge only) item nbt ^optional^ | object \| string                            |
