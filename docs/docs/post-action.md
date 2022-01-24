# Post Action

Post action is an action to be executed.

Usually you can add post actions to a Lychee's recipe, and  they will be executed after the recipe is successfully matched.

## Basic format

```json
{
	"type": {{type: string}},
	"contextual": {{optional contextual conditions: ContextualCondition | ContextualCondition[]}}
	{{additional properties}}
}
```

## Built-in actions

### Drop item (`drop_item`)

Spawns an item entity on the ground.

??? note "Format"

	```json
	{
		"type": "drop_item",
		"item": {{item resource id: string}},
		"count": {{optional item amount: int}},
		"nbt": {{(Forge only) optional item nbt: object or string}}
	}
	```

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

This action is not repeatable.

??? note "Format"

	```json
	{
		"type": "place",
		"block": {{block: BlockPredicate}}
	}
	```

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

??? note "Format"

	```json
	{
		"type": "place",
		"command": {{command to run: string}},
		"hide": {{optional hide in JEI/REI: boolean}}
	}
	```

### Drop experience (`drop_xp`)

Spawns experience orbs.

??? note "Format"

	```json
	{
		"type": "drop_xp",
		"xp": {{amount: int}}
	}
	```

## Special built-in actions

These following actions will prevent the default behavior of the recipe (such as consuming input item). The default behavior is explained in the recipes page.

### Prevent default behavior (`prevent_default`)

Prevents default behavior and do nothing.

??? note "Format"

	```json
	{
		"type": "prevent_default"
	}
	```

### Damage input item (`damage_item`)

Damages input item.

This action is not repeatable.

??? note "Format"

	```json
	{
		"type": "damage_item",
		"damage": {{optional damage: int}}
	}
	```
