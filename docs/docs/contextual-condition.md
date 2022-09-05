# Contextual Condition

Unlike Forge's recipe condition, or Fabric's resource condition, contextual condition will be tested during the crafting (or "interaction" you name it). Thus you can achieve more complex goals. For example, limit the crafting only happening in the specific biome or structure.

Contextual condition can be applied to a recipe, or a single result (aka Post Action) in a recipe.

## Basic Format

| Name        | Description                                                          | Type / Literal |
| ----------- | -------------------------------------------------------------------- | -------------- |
| type        | type                                                                 | string         |
| secret      | (optional) displays as "???" in player's tooltip                     | boolean        |
| description | (optional) overrides the default  description with a translation key | string         |
|             | additional properties...                                             |                |

!!! note

	If a condition is a secret, it is still visible to players by using some technical approach.

## Built-in Conditions

### Inverted (`not`)

Inverts another condition.

!!! note "Format"

    | Name       | Description | Type / Literal                               |
    | ---------- | ----------- | -------------------------------------------- |
    | type       | type        | "not"                                        |
    | contextual | condition   | [ContextualCondition](#contextual-condition) |

### True for Any (`or`)

Checks if any of the wrapped conditions is passed.

!!! note "Format"

    | Name       | Description | Type / Literal                                 |
    | ---------- | ----------- | ---------------------------------------------- |
    | type       | type        | "or"                                           |
    | contextual | conditions  | [ContextualCondition](#contextual-condition)[] |

### True for All (`and`)

Checks if all the wrapped conditions are passed.

!!! note "Format"

    | Name       | Description | Type / Literal                                 |
    | ---------- | ----------- | ---------------------------------------------- |
    | type       | type        | "and"                                          |
    | contextual | conditions  | [ContextualCondition](#contextual-condition)[] |

### Random Chance (`chance`)

Generates a random number between 0.0 and 1.0, and checks if it is less than a specified value.

!!! note "Format"

    | Name   | Description                      | Type / Literal |
    | ------ | -------------------------------- | -------------- |
    | type   | type                             | "chance"       |
    | chance | success rate as a number 0.0–1.0 | number         |

??? example

	```json
	{
		"type": "chance",
		"chance": 0.5
	}
	```

### Location Check (`location`)

Checks if a `location_check` predicate is passed.

!!! note "Format"

    | Name      | Description                    | Type / Literal                                          |
    | --------- | ------------------------------ | ------------------------------------------------------- |
    | type      | type                           | "location"                                              |
    | offsetX   | (optional) offsets to location | int                                                     |
    | offsetY   | (optional) offsets to location | int                                                     |
    | offsetZ   | (optional) offsets to location | int                                                     |
    | predicate | location predicate             | [LocationPredicate](general-types.md#locationpredicate) |

??? example

	Checks if player is in The End, and X position is between -100 and 100.

	```json
	{
		"type": "location",
		"predicate": {
			"dimension": "the_end",
			"position": {
				"x": {
					"min": -100,
					"max": 100
				}
			}
		}
	}
	```

Special usage: you can use `lychee:biome_tag` option to specify biome tag.

??? example

	```json
	{
		"type": "location",
		"predicate": {
			"lychee:biome_tag": "is_ocean"
		}
	}
	```

!!! note

	Fluid state predicate is not supported yet. (because I am lazy)

### Weather Check (`weather`)

Checks weather.

!!! note "Format"

    | Name    | Description | Type / Literal                 |
    | ------- | ----------- | ------------------------------ |
    | type    | type        | "weather"                      |
    | weather | weather     | "clear" \| "rain" \| "thunder" |

### Difficulty Check (`difficulty`)

Checks if world is in any of the listed difficulties.

!!! note "Format"

    | Name       | Description | Type / Literal                     |
    | ---------- | ----------- | ---------------------------------- |
    | type       | type        | "difficulty"                       |
    | difficulty | difficulty  | string \| int \| string[] \| int[] |

??? example

	Recipe or post action only works when difficulty is peaceful or easy:

	```json
	{
		"type": "difficulty",
		"difficulty": ["peaceful", 1]
	}
	```

### Time Check (`time`)

Compares the current game time (the age of the world in game ticks) against given values.

!!! note "Format"

    | Name   | Description         | Type / Literal                          |
    | ------ | ------------------- | --------------------------------------- |
    | type   | type                | "time"                                  |
    | value  | the time            | [IntBounds](general-types.md#intbounds) |
    | period | (optional) see wiki | int                                     |

    See [Minecraft Wiki](https://minecraft.fandom.com/wiki/Predicate). Only supports constant value.

??? example

	Recipe works every other second:

	```json
	{
        "type": "time",
        "value": {
            "min": 0,
            "max": 20
        },
        "period": 40
	}
	```

### Command Check (`execute`)

Executes a command and see if the range matches the return value.

!!! note "Format"

    | Name    | Description                             | Type / Literal                          |
    | ------- | --------------------------------------- | --------------------------------------- |
    | type    | type                                    | "execute"                               |
    | command | the command to run                      | string                                  |
    | value   | (optional) the range. [1,+∞) by default | [IntBounds](general-types.md#intbounds) |

### Fall Distance Check (`fall_distance`)

Checks entity fall distance. Mainly for block crushing recipes.

!!! note "Format"

    | Name  | Description | Type / Literal                                |
    | ----- | ----------- | --------------------------------------------- |
    | type  | type        | "fall_distance"                               |
    | range | the range   | [DoubleBounds](general-types.md#doublebounds) |

### Entity Health Check (`entity_health`)

Checks if entity's is in a range.

!!! note "Format"

    | Name  | Description | Type / Literal                                |
    | ----- | ----------- | --------------------------------------------- |
    | type  | type        | "entity_health"                               |
    | range | the range   | [DoubleBounds](general-types.md#doublebounds) |

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