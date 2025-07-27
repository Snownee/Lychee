# KubeJS Integration

## Adding YAML Recipes

```js
ServerEvents.recipes((event) => {
  let yamlRecipe = (yaml) => event.custom(Lychee.toJSON(yaml))
  yamlRecipe(`
    type: 'lychee:item_burning'
    item_in:
      item: grass_block
    post:
      - type: delay
        s: 0.2
      - type: random
        entries:
          - weight: 1999
            type: drop_item
            id: coal
          - weight: 1
            type: drop_item
            id: diamond
    `).id('test:yaml_recipe')
})
```

## Adding Recipes in OOP Style

[Reference](https://github.com/Snownee/Lychee/blob/1.21-neoforge/src/main/java/snownee/lychee/datagen/LycheeBuilder.java)

```js
ServerEvents.recipes((event) => {
  let lb = LycheeBuilder.create(event.jsonOps)
  let javaRecipe = (obj) => event.custom(Lychee.toJSON(obj))
  let $Chance = Java.loadClass('snownee.lychee.contextual.Chance')
  javaRecipe(
    lb
      .itemBurningRecipe(lb.sized('3x glass'))
      .post(lb.dropItem('3x sand'))
      .condition($Chance(0.5)),
  ).id('test:java_recipe')
  lb.teardown()
})
```

## Adding JSON Recipes

Please see [KubeJS Wiki](https://wiki.latvian.dev/books/kubejs/page/recipes#bkmrk-custom%2Fmodded-json-r).

## Custom Action

First you need to add a custom action somewhere in your recipe:

!!! note "Format"

    | Name | Description       | Type / Literal |
    | ---- | ----------------- | -------------- |
    | type | type              | "custom"       |
    | id   | id                | string         |
    | data | custom properties | dictionary     |

!!! example

    ```json
    {
        "type": "custom",
        "id": "example_log_action",
        "data": {
            "custom_property": "my_value"
        }
    }
    ```

Then define the behavior of your custom action in KubeJS:

```js
// startup script. will be executed when recipe is loaded
LycheeEvents.customAction('example_log_action', (event) => {
  let msg = event.data.custom_property

  // use ProbeJS for more information about the parameters
  event.applyFunc = (recipe, ctx, times) => {
    console.log(msg)
  }
  // it is recommended to cancel the event to prevent the action from being modified by other codes
  event.cancel()
})
```

## Custom Condition

First you need to add a custom condition somewhere in your recipe:

!!! note "Format"

    | Name | Description       | Type / Literal |
    | ---- | ----------------- | -------------- |
    | type | type              | "custom"       |
    | id   | id                | string         |
    | data | custom properties | dictionary     |

!!! example

    ```json
    {
        "type": "custom",
        "id": "example_always_true_condition"
    }
    ```

Then define the behavior of your custom condition in KubeJS:

```js
// startup script. will be executed when recipe is loaded
LycheeEvents.customCondition('example_always_true_condition', (event) => {
  // use ProbeJS for more information about the parameters
  // here you need to return the repeat times that no greater than the given times, or 0 if the condition is not met
  // in this case, the condition is always met
  event.testFunc = (recipe, ctx, times) => times

  // this function is optional
  // will be called when the condition is displayed in JEI/REI/EMI on the client side
  // true    => checkmark
  // false   => cross
  // "default" => the default "-"
  event.testInTooltipsFunc = () => true

  // it is recommended to cancel the event to prevent the action from being modified by other codes
  event.cancel()
})
```

## Execute Code When Clicking the Info Badge

You can execute code when clicking the info badge in JEI/REI:

```js
// client script
LycheeEvents.clickedInfoBadge('your:recipe_id', (event) => {
  console.log(event.recipeId)
  console.log(event.button == 0) // 0 for left click, 1 for right click
})
```

## Examples

### Anvil Crafting Recipe to Repair Tools

=== "Recipe"

    ```json
    {
    	"type": "lychee:anvil_crafting",
    	"item_in": [
    		{
    			"item": "diamond_sword"
    		},
    		{
    			"item": "dirt"
    		}
    	],
    	"item_out": {
    		"id": "diamond_sword"
    	},
    	"level_cost": 1,
    	"material_cost": 1,
    	"assembling": [
    		{
    			"type": "custom",
    			"id": "repair_item",
    			"data": {
    				"durability": 1
    			}
    		}
    	],
    	"contextual": {
    		"type": "custom",
    		"id": "is_item_damaged",
    		"data": {
    			"target": "/item_in/0"
    		}
    	}
    }
    ```

=== "Startup Script"

    ```js
    function copyComponents(input, output) {
    	let components = input.getComponentsPatch();
    	if (typeof components === "function") {
    		components = components();
    	}

    	for (let entry of components.entrySet()) {
    		let type = entry.getKey();
    		let value = entry.getValue();
    		if (type == null) continue;
    		if (!value.isPresent()) continue;
    		output.set(type, value.get());
    	}
    }

    LycheeEvents.customAction('repair_item', event => {
        let durability = event.data.durability
        event.applyFunc = (recipe, ctx, times) => {
        	let input = ctx.getItem(0)
        	let material = ctx.getItem(1)
        	let output = ctx.getItem(2)
        	copyComponents(input, output)
        	let cost = 0
        	for (; cost < material.count && output.damaged; cost++) {
        		output.setDamageValue(output.damageValue - durability)
        	}
        	ctx.get(LycheeContextKey.ANVIL).materialCost = cost
        }
    })

    LycheeEvents.customCondition('is_item_damaged', event => {
        let target = LycheeReference.fromJson(event.data, "target")
        event.testFunc = (recipe, ctx, times) => {
            let indexes = recipe.getItemIndexes(target)
            return ctx.getItem(indexes.get(0)).damaged ? times : 0
        }
    })
    ```

### Anvil Crafting Recipe to Randomize the Trim on Armor

=== "Recipe"

    ```json
    {
    	"type": "lychee:anvil_crafting",
    	"item_in": [
    		{
    			"type": "neoforge:components",
    			"items": "diamond_chestplate",
    			"components": {
    				"minecraft:trim": {
    					"material": "minecraft:copper",
    					"pattern": "minecraft:eye"
    				}
    			}
    		},
    		{
    			"item": "emerald"
    		}
    	],
    	"item_out": {
    		"id": "diamond_chestplate"
    	},
    	"assembling": [
    		{
    			"type": "custom",
    			"id": "apply_random_trim"
    		}
    	],
    	"if": {
    		"type": "custom",
    		"id": "is_item_trimmed",
    		"data": {
    			"target": "/item_in/0"
    		}
    	}
    }
    ```

=== "Startup Script"

    ```js
    function copyComponents(input, output) {
    	let components = input.getComponentsPatch();
    	if (typeof components === "function") {
    		components = components();
    	}

    	for (let entry of components.entrySet()) {
    		let type = entry.getKey();
    		let value = entry.getValue();
    		if (type == null) continue;
    		if (!value.isPresent()) continue;
    		output.set(type, value.get());
    	}
    }

    let $DataComponents = Java.loadClass("net.minecraft.core.component.DataComponents");
    let $ArmorTrim = Java.loadClass("net.minecraft.world.item.armortrim.ArmorTrim");
    let $Registries = Java.loadClass("net.minecraft.core.registries.Registries");
    let $TrimMaterials = Java.loadClass("net.minecraft.world.item.armortrim.TrimMaterials");
    let $TrimPatterns = Java.loadClass("net.minecraft.world.item.armortrim.TrimPatterns");

    let trimPool = [$TrimPatterns.COAST, $TrimPatterns.SPIRE, $TrimPatterns.RIB, $TrimPatterns.SNOUT, $TrimPatterns.DUNE];

    LycheeEvents.customAction("apply_random_trim", (event) => {
      event.applyFunc = (recipe, ctx, times) => {
        let input = ctx.getItem(0);
        let output = ctx.getItem(2);
        let random = ctx.get(LycheeContextKey.RANDOM);
        let level = ctx.get(LycheeContextKey.LEVEL);
        copyComponents(input, output);
        let registryAccess = level.registryAccess();
        let material = output.get($DataComponents.TRIM).material() ?? $TrimMaterials.COPPER;
        let pattern = registryAccess
          .lookup($Registries.TRIM_PATTERN)
          .orElseThrow()
          .get(trimPool[random.nextInt(trimPool.length)])
          .orElseThrow();
        output.set($DataComponents.TRIM, new $ArmorTrim(material, pattern));
      };
    });

    LycheeEvents.customCondition("is_item_trimmed", (event) => {
      let target = LycheeReference.fromJson(event.data, "target");
      event.testFunc = (recipe, ctx, times) => {
        let indexes = recipe.getItemIndexes(target);
        let stack = ctx.getItem(indexes.getInt(0));
        return stack == null ? 0 : stack.get($DataComponents.TRIM) != null ? times : 0;
      };
    });
    ```

### Item Inside Recipe with Dynamic Time

=== "Recipe"

    ```json
    {
        "type": "lychee:item_inside",
        "time": 5,
        "block_in": "*",
        "item_in": [
            {
                "item": "yellow_dye"
            }
        ],
        "post": [
            {
                "type": "drop_item",
                "id": "red_dye"
            }
        ],
        "if": [
            {
                "type": "custom",
                "id": "neighbor_block_boost",
                "data": {
                    "booster_block": "minecraft:red_wool"
                }
            }
        ]
    }
    ```

=== "Startup Script"

    ```js
    let $DirectionPlane = Java.loadClass('net.minecraft.core.Direction$Plane')
    let $LootContextParams = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParams')

    LycheeEvents.customCondition('neighbor_block_boost', event => {
    	let booster_block = event.data.booster_block

    	event.testFunc = (recipe, ctx, times) => {
    		let params = ctx.get(LycheeContextKey.LOOT_PARAMS)
    		let item = params.get($LootContextParams.THIS_ENTITY)
    		let count = item.lychee$getCount()
    		if (count != 0) {
    			return times
    		}
    		let pos = params.get(LycheeLootContextParams.BLOCK_POS)
    		for (const direction of $DirectionPlane.HORIZONTAL) {
    			let neighbor = ctx.get(LycheeContextKey.LEVEL).getBlock(pos.relative(direction))
    			if (neighbor == booster_block) {
    				count += 1
    			}
    		}
    		item.lychee$setCount(count)
    		console.info('Neighbor block boost: ' + count)
    		return times
    	}
    })
    ```
