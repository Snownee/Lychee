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

### Use item on a block

Event when a player uses item on a block.

Default behavior: Item is consumed.

This recipe type is not [repeatable](concepts.md#repeatability).

!!! note "Format"

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

### Click on a block with item

Event when a player click on a block with item.

Default behavior: Item is consumed.

This recipe type is not [repeatable](concepts.md#repeatability).

!!! note "Format"

    | Name     | Description               | Type / Literal                                    |
    | -------- | ------------------------- | ------------------------------------------------- |
    | type     | type                      | "lychee:block_clicking"                           |
    | item_in  | the item in player's hand | [Ingredient](general-types.md#ingredient)         |
    | block_in | the block being clicked   | [BlockPredicate](general-types.md#blockpredicate) |

### Item entity burning

Event when an item entity is burnt.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: item is consumed.

!!! note "Format"

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

### Item entity inside a block

Event when an item entity is inside a block. This will be tested every second.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: Item is consumed.

!!! note "Format"

    | Name    | Description                                     | Type / Literal                            |
    | ------- | ----------------------------------------------- | ----------------------------------------- |
    | type    | type                                            | "lychee:item_inside"                      |
    | item_in | the ticking item                                | [Ingredient](general-types.md#ingredient) |
    | time    | (optional) (Forge only) waiting time in seconds | int                                       |

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

### Anvil crafting

It is not recommended to add contextual conditions or actions to the recipe, because JEI/REI does not support drawing extra things on an anvil recipe.

This recipe type is not [repeatable](concepts.md#repeatability).

Default behavior: Anvil is damaged.

!!! note "Format"

    | Name          | Description                                             | Type / Literal                                                                           |
    | ------------- | ------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
    | type          | type                                                    | "lychee:anvil_crafting"                                                                  |
    | item_in       | the input items (the second one is optional)            | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[] |
    | item_out      | the result item                                         | ItemStack                                                                                |
    | level_cost    | player's xp level                                       | int                                                                                      |
    | material_cost | amount of items that will be cost from right input slot | int                                                                                      |

??? example

	It costs 1 apple, 8 gold ingots and 1 level to make a golden_apple. Does not damage the anvil:

	```json
	{
		"type": "lychee:anvil_crafting",
		"item_in": [
			{
				"item": "apple"
			},
			{
				"item": "gold_ingot"
			}
		],
		"item_out": {
			"item": "golden_apple"
		},
		"level_cost": 1,
		"material_cost": 8,
		"post": {
			"type": "prevent_default"
		}
	}
	```

### Block crushing

Forge only.

Event when a falling block entity lands on a block.

This recipe type is [repeatable](concepts.md#repeatability).

Default behavior: Falling block becomes block or drops item. Canceling this will make falling block disappear.

!!! note "Format"

    | Name          | Description                                              | Type / Literal                                                                           |
    | ------------- | -------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
    | type          | type                                                     | "lychee:block_crushing"                                                                  |
    | item_in       | (optional) the input items (can only has left)           | [Ingredient](general-types.md#ingredient) \| [Ingredient](general-types.md#ingredient)[] |
    | falling_block | (optional) the falling block. default are all the anvils | [BlockPredicate](general-types.md#blockpredicate)                                        |
    | landing_block | (optional) the landing block. default is any block       | [BlockPredicate](general-types.md#blockpredicate)                                        |

??? example

	Papers from sugar canes:

	```json
	{
		"type": "lychee:block_crushing",
		"item_in": [
			{
				"item": "sugar_cane"
			},
			{
				"item": "sugar_cane"
			},
			{
				"item": "sugar_cane"
			}
		],
		"post": [
			{
				"type": "drop_item",
				"item": "paper",
				"count": 3
			}
		]
	}
	```

	Making a mossy stone bricks block. It uses a location check to check the block below the current position:

	```json
	{
		"type": "lychee:block_crushing",
		"landing_block": "moss_carpet",
		"contextual": {
			"type": "location",
			"offsetY": -1,
			"predicate": {
				"block": {
					"blocks": [ "stone_bricks" ]
				}
			}
		},
		"post": [
			{
				"type": "place",
				"block": "*"
			},
			{
				"type": "place",
				"offsetY": -1,
				"block": "mossy_stone_bricks"
			}
		]
	}
	```

!!! note

	Dispenser can now place fallable blocks and items that tagged with `lychee:dispenser_placement` in front of it. Note the item should not have a special dispense behavior.

!!! note

	Normally it will process all the items that touches the falling block, but if the landing block is tagged with `lychee:extend_box` (cauldrons by default), it will collect items inside the landing block as well.
