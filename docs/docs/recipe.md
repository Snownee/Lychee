# Recipes

## Basic format

=== "Forge"

	```json
	{
		"type": {{type: string}},
		"conditions": {{optional conditions: ForgeCondition[]}},
		"contextual": {{optional contextual conditions: ContextualCondition | ContextualCondition[]}},
		"post": {{optional action: PostAction | PostAction[]}},
		{{additional properties}}
	}
	```

=== "Fabric"

	```json
	{
		"type": {{type: string}},
		"fabric:conditions": {{optional conditions: FabricCondition[]}},
		"contextual": {{optional contextual conditions: ContextualCondition | ContextualCondition[]}},
		"post": {{optional action: PostAction | PostAction[]}},
		{{additional properties}}
	}
	```

## Recipe types

### Use item on a block (`lychee:block_interacting`)

Event when a player uses item on a block.

Default behavior: item is consumed.

??? note "Format"

	```json
	{
		"type": "lychee:block_interacting",
		"item_in": {{item in player's hand: Ingredient}},
		"block_in" {{block being used: BlockPredicate}}
	}
	```

### Click on a block with item (`lychee:block_clicking`)

Event when a player click on a block with item.

Default behavior: item is consumed.

??? note "Format"

	```json
	{
		"type": "lychee:block_clicking",
		"item_in": {{item in player's hand: Ingredient}},
		"block_in" {{block being clicked: BlockPredicate}}
	}
	```

### Item entity burning (`lychee:item_burning`)

Event when an item entity is burnt.

Default behavior: item is consumed.

??? note "Format"

	```json
	{
		"type": "lychee:item_burning",
		"item_in": {{burnt item: Ingredient}}
	}
	```

!!! note

	Items such as netherite or nether star can't catch fire.

	If you want to make an item fire-immune as well, you can tag it with `lychee:fire_immune`

### Item entity inside a block (`lychee:item_inside`)

Event when an item entity is inside a block. This will be tested every second.

Default behavior: item is consumed.

??? note "Format"

	```json
	{
		"type": "lychee:item_inside",
		"item_in": {{burnt item: Ingredient}}
	}
	```
