# Contextual Condition

Unlike Forge's recipe condition (sadly no docs), or Fabric's resource condition (sadly no docs x2), Contextual condition will be tested during the crafting (or "interaction" you name it). Thus you can achieve more complex goals. For example, limit the crafting only happening in the specific biome or structure.

Contextual condition can be applied to a recipe, or a single result (aka Post Action) in a recipe.

## Basic format

```json
{
	"type": {{type: string}},
	"secret": {{display as ??? in player's tooltip (optional): boolean}}
	{{additional properties}}
}
```

!!! note

	If a condition is a secret, it is still visible to players by using some technical approach.

## Built-in conditions

### Inverted (`not`)

Inverts another condition.

??? note "Format"

	```json
	{
		"type": "not",
		"contextual": {{contextual condition: ContextualCondition}}
	}
	```

### True for any (`or`)

Checks if any of the wrapped conditions is passed.

??? note "Format"

	```json
	{
		"type": "or",
		"contextual": [
			{{contextual condition: ContextualCondition}},
			{{contextual condition: ContextualCondition}},
			...
		]
	}
	```

### True for all (`or`)

Checks if all the wrapped conditions are passed.

??? note "Format"

	```json
	{
		"type": "and",
		"contextual": [
			{{contextual condition: ContextualCondition}},
			{{contextual condition: ContextualCondition}},
			...
		]
	}
	```

### Random chance (`chance`)

Generates a random number between 0.0 and 1.0, and checks if it is less than a specified value.

??? note "Format"

	```json
	{
		"type": "chance",
		"chance": {{Success rate as a number 0.0–1.0: number}}
	}
	```

??? example

	```json
	{
		"type": "chance",
		"chance": 0.5
	}
	```

### Location check (`location`)

Checks if a `location_check` predicate is passed.

??? note "Format"

	```json
	{
		"type": "location",
		"offsetX": {{optional offsets to location: int}},
		"offsetY": {{optional offsets to location: int}},
		"offsetZ": {{optional offsets to location: int}},
		"predicate": {{location predicate: object}}
	}
	```

For more details please see [Minecraft Wiki](https://minecraft.fandom.com/wiki/Predicate).

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

!!! note

	Some features of location predicate are not supported yet. (because I am lazy)

### Weather check (`weather`)

Checks weather.

??? note "Format"

	```json
	{
		"type": "weather",
		"weather": {{weather: "clear", "rain" or "thunder"}}
	}
	```

### Difficulty check (`difficulty`)

Checks if world is in any of the listed difficulties.

??? note "Format"

	```json
	{
		"type": "difficulty",
		"difficulty": {{difficulty: string or int}}
	}
	```
	
	```json
	{
		"type": "difficulty",
		"difficulty": [
			{{difficulty: string or int}},
			{{difficulty: string or int}}
		]
	}
	```

??? example

	Recipe or post action only works when difficulty is peaceful or easy:

	```json
	{
		"type": "difficulty",
		"difficulty": ["peaceful", 1]
	}
	```
