# Contextual Conditions

Unlike Forge's recipe condition (sadly no docs), or Fabric's resource condition (sadly no docs x2), Contextual condition will be tested during the crafting (or "interaction" you name it). Thus you can achieve more complex goals. For example, limit the crafting only happening in the specific biome or structure.

## Basic structure

```json
{
	"type": {type: string}
	{additional properties}
}
```

## Built-in conditions

### `not`

Inverts another condition.

??? note "Structure"

	```json
	{
		"type": "not",
		"contextual": {contextual condition: object}
	}
	```
