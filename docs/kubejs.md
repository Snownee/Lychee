# KubeJS Integration

## Custom Action

First you need to add a custom action somewhere in your recipe:

!!! note "Format"

    | Name | Description          | Type / Literal |
    | ---- | -------------------- | -------------- |
    | type | type                 | "custom"       |
    | id   | id                   | string         |
    |      | custom properties... |                |

!!! example

    ```json
    {
        "type": "custom",
        "id": "example_log_action",
        "custom_property": "my_value"
    }
    ```

Then define the behavior of your custom action in KubeJS:

```js
// startup script. will be executed when recipe is loaded
LycheeEvents.customAction('example_log_action', (event) => {
    let msg = event.data.custom_property

    // use ProbeJS for more information about the parameters
    event.action.applyFunc = (recipe, ctx, times) => {
        console.log(msg)
    }
    // it is recommended to cancel the event to prevent the action from being modified by other codes
    event.cancel()
})
```

## Custom Condition

First you need to add a custom condition somewhere in your recipe:

!!! note "Format"

    | Name | Description          | Type / Literal |
    | ---- | -------------------- | -------------- |
    | type | type                 | "custom"       |
    | id   | id                   | string         |
    |      | custom properties... |                |

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
    event.condition.testFunc = (recipe, ctx, times) => times

    // this function is optional
    // will be called when the condition is displayed in JEI/REI on the client side
    // InteractionResult.SUCCESS => checkmark
    // InteractionResult.FAIL    => cross
    // InteractionResult.PASS    => the default "-"
    event.condition.testInTooltipsFunc = () => InteractionResult.SUCCESS

    // it is recommended to cancel the event to prevent the action from being modified by other codes
    event.cancel()
})
```

## Execute Code When Clicking the Info Badge

You can execute code when clicking the info badge in JEI/REI:

```js
// client script
LycheeEvents.clickedInfoBadge('your:recipe_id', (event) => {
    console.log(event.recipe.id)
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
                "item": "diamond_sword",
                "lychee:tag": {
                    "Damage": 1
                }
            },
            {
                "item": "dirt"
            }
        ],
        "item_out": {
            "item": "diamond_sword"
        },
        "assembling": [
            {
                "type": "nbt_patch",
                "op": "copy",
                "from": "/item_in/0/tag",
                "path": "/item_out/tag"
            },
            {
                "type": "custom",
                "id": "repair_item",
                "target": "/item_out",
                "durability": 1
            }
        ],
        "contextual": {
            "type": "custom",
            "id": "is_item_damaged",
            "target": "/item_in/0"
        }
    }
    ```

=== "Startup Script"

    ```js
    LycheeEvents.customAction('repair_item', event => {
        let durability = event.data.durability
        event.action.applyFunc = (recipe, ctx, times) => {
            let material = ctx.getItem(1)
            let tool = ctx.getItem(2)
            let cost = 0
            for (; cost < material.count && tool.damaged; cost++) {
                tool.setDamageValue(tool.damageValue - durability)
            }
            ctx.materialCost = cost
        }
    })

    LycheeEvents.customCondition('is_item_damaged', event => {
        let target = LycheeReference.fromJson(event.data, 'target')
        event.condition.testFunc = (recipe, ctx, times) => {
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
                "item": "diamond_chestplate",
                "lychee:tag": {
                    "Trim": {
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
            "item": "diamond_chestplate",
            "lychee:tag": {
                "Trim": {
                    "material": "minecraft:copper",
                    "pattern": "minecraft:eye"
                }
            }
        },
        "assembling": [
            {
                "type": "custom",
                "id": "apply_random_trim"
            }
        ],
        "post": [
            {
                "type": "custom",
                "id": "update_enchantment_seed"
            }
        ],
        "contextual": {
            "type": "custom",
            "id": "is_item_trimmed",
            "target": "/item_in/0"
        }
    }
    ```

=== "Startup Script"

    ```js
    let $RandomSource = Java.loadClass('net.minecraft.util.RandomSource')
    let trimPool = ['coast', 'spire', 'rib', 'snout', 'dune']

    LycheeEvents.customAction('apply_random_trim', (event) => {
        event.action.applyFunc = (recipe, ctx, times) => {
            let input = ctx.getItem(0)
            let output = ctx.getItem(2)
            let player = ctx.getParam('this_entity')
            let random = $RandomSource.create()
            //make sure the crafting result consistent
            random.setSeed(player.enchantmentSeed)
            output.setNbt(
                input.nbt.merge({
                    Trim: {
                        pattern: trimPool.armor[random.nextInt(trimPool.armor.length)]
                    }
                })
            )
        }
    })

    LycheeEvents.customAction('update_enchantment_seed', (event) => {
        event.action.applyFunc = (recipe, ctx, times) => {
            let player = ctx.getParam('this_entity')
            player.onEnchantmentPerformed(null, 0) // update seed. null == ItemStack.EMPTY
        }
    })

    LycheeEvents.customCondition('is_item_trimmed', (event) => {
        let target = LycheeReference.fromJson(event.data, 'target')
        event.condition.testFunc = (recipe, ctx, times) => {
            let indexes = recipe.getItemIndexes(target)
            let stack = ctx.getItem(indexes.getInt(0))
            return stack?.nbt?.Trim ? times : 0
        }
    })
    ```

### Transforming Item on Depot

=== "Recipe"

    ```json
    {
        "type": "lychee:block_interacting",
        "item_in": {
            "item": "create:wrench"
        },
        "block_in": "create:depot",
        "post": [
            {
                "type": "drop_item",
                "item": "minecraft:cobblestone"
            },
            {
                "type": "prevent_default"
            },
            {
                "type": "custom",
                "id": "consume_item_on_depot"
            }
        ],
        "contextual": {
            "type": "custom",
            "id": "has_item_on_depot",
            "ingredient": {
                "item": "minecraft:stone"
            }
        }
    }
    ```

=== "Startup Script"

    ```js
    let $LevelPlatformHelper = Java.loadClass('dev.latvian.mods.kubejs.platform.LevelPlatformHelper')

    LycheeEvents.customAction('consume_item_on_depot', event => {
        event.action.applyFunc = (recipe, ctx, times) => {
            let be = ctx.getParam('block_entity')
            let inv = $LevelPlatformHelper.get().getInventoryFromBlockEntity(be, 'up')
            inv.extractItem(0, 1, false)
        }
    })

    LycheeEvents.customCondition('has_item_on_depot', event => {
        let ingredient = Ingredient.of(event.data.ingredient)
        event.condition.testFunc = (recipe, ctx, times) => {
            let be = ctx.getParam('block_entity')
            let inv = $LevelPlatformHelper.get().getInventoryFromBlockEntity(be, 'up')
            return ingredient.test(inv.getStackInSlot(0)) ? times : 0
        }
    })
    ```
