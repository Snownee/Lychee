# Recipes

## Basic format

=== "Forge"

    | Name       | Description                      | Type / Literal                                                                                     |
    | ---------- | -------------------------------- | -------------------------------------------------------------------------------------------------- |
    | type       | type                             | string                                                                                             |
    | conditions | (optional) conditions            | ForgeCondition[]                                                                                   |
    | contextual | (optional) contextual conditions | [ContextualCondition](contextual-condition.md) \| [ContextualCondition](contextual-condition.md)[] |
    | post       | (optional) post actions          | [PostAction](post-action.md) \| [PostAction](post-action.md)[]                                     |
    |            | additional properties...         |                                                                                                    |

=== "Fabric"

    | Name              | Description                      | Type / Literal                                                                                     |
    | ----------------- | -------------------------------- | -------------------------------------------------------------------------------------------------- |
    | type              | type                             | string                                                                                             |
    | fabric:conditions | (optional) conditions            | FabricCondition[]                                                                                  |
    | contextual        | (optional) contextual conditions | [ContextualCondition](contextual-condition.md) \| [ContextualCondition](contextual-condition.md)[] |
    | post              | (optional) post actions          | [PostAction](post-action.md) \| [PostAction](post-action.md)[]                                     |
    |                   | additional properties...         |                                                                                                    |

## Recipe types

### Use item on a block (`lychee:block_interacting`)

Event when a player uses item on a block.

Default behavior: item is consumed.

This recipe type is not [repeatable](concepts.md#repeatability).

??? note "Format"

    | Name     | Description               | Type / Literal                                    |
    | -------- | ------------------------- | ------------------------------------------------- |
    | type     | type                      | "lychee:block_interacting"                        |
    | item_in  | the item in player's hand | [Ingredient](general-types.md#ingredient)         |
    | block_in | the block being used      | [BlockPredicate](general-types.md#blockpredicate) |

??? example

	Prevent player from carving pumpkins:

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

	Stripping an oak log with iron axe, you will have 50% chance to obtain a diamond:

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

### Click on a block with item (`lychee:block_clicking`)

Event when a player click on a block with item.

Default behavior: item is consumed.

This recipe type is not [repeatable](concepts.md#repeatability).

??? note "Format"

    | Name     | Description               | Type / Literal                                    |
    | -------- | ------------------------- | ------------------------------------------------- |
    | type     | type                      | "lychee:block_clicking"                           |
    | item_in  | the item in player's hand | [Ingredient](general-types.md#ingredient)         |
    | block_in | the block being clicked   | [BlockPredicate](general-types.md#blockpredicate) |

### Item entity burning (`lychee:item_burning`)

Event when an item entity is burnt.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: item is consumed.

??? note "Format"

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

### Item entity inside a block (`lychee:item_inside`)

Event when an item entity is inside a block. This will be tested every second.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: item is consumed.

??? note "Format"

    | Name    | Description      | Type / Literal                            |
    | ------- | ---------------- | ----------------------------------------- |
    | type    | type             | "lychee:item_inside"                      |
    | item_in | the ticking item | [Ingredient](general-types.md#ingredient) |

??? example

	Drop a buck into a full water cauldron, it returns a water bucket and empty the cauldron:

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

### Anvil crafting (`lychee:anvil_crafting`)

It is not recommended to add contextual conditions or actions to the recipe, because JEI/REI does not support drawing extra things on an anvil recipe.

This recipe type is not [repeatable](concepts.md#repeatability).

Default behavior: anvil is damaged.

??? note "Format"

    | Name          | Description                                             | Type / Literal                                                                           |
    | ------------- | ------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
    | type          | type                                                    | "lychee:anvil_crafting"                                                                  |
    | item_in       | the input items (can only has left)                     | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[] |
    | item_out      | the result item                                         | ItemStack                                                                                |
    | level_cost    | player's xp level                                       | int                                                                                      |
    | material_cost | amount of items that will be cost from right input slot | int                                                                                      |

??? example

	```json
	TODO
	```
