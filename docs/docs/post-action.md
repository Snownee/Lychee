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
		"item": {{item name: string}},
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

Place a block in world.

??? note "Format"

	```json
	{
		"type": "place",
		"block": {{block: BlockPredicate}}
	}
	```

??? example

	Place a cauldron:

	```json
	{
		"type": "place",
		"block": "cauldron"
	}
	```

	Place a waterlogged oak stairs:

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

	Destroy current block (place air):

	```json
	{
		"type": "place",
		"block": "*"
	}
	```

TODO

## Special built-in actions

These following actions will prevent the default behavior of the recipe (such as consuming input item). The default behavior is explained in the recipes page.

TODO