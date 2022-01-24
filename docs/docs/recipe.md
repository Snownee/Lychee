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
    | fabric:conditions | (optional) conditions            | FabricCondition[][]                                                                                |
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

Default behavior: item is consumed.

??? note "Format"

    | Name    | Description    | Type / Literal                            |
    | ------- | -------------- | ----------------------------------------- |
    | type    | type           | "lychee:item_burning"                     |
    | item_in | the burnt item | [Ingredient](general-types.md#ingredient) |

!!! note

	Items such as netherite or nether star can't catch fire.

	If you want to make an item fire-immune as well, you can tag it with `lychee:fire_immune`

### Item entity inside a block (`lychee:item_inside`)

Event when an item entity is inside a block. This will be tested every second.

Default behavior: item is consumed.

??? note "Format"

    | Name    | Description      | Type / Literal                            |
    | ------- | ---------------- | ----------------------------------------- |
    | type    | type             | "lychee:item_inside"                      |
    | item_in | the ticking item | [Ingredient](general-types.md#ingredient) |
