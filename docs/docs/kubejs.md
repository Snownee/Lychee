# KubeJS Integration

KubeJS Integration is added in Lychee 3.9.

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

## Example: Repairing Tool with Anvil and Custom Item

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
      "level_cost": 1,
      "material_cost": 1,
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

## Execute Code When Clicking the Info Badge

You can execute code when clicking the info badge in JEI/REI:

```js
// client script
LycheeEvents.clickedInfoBadge('your:recipe_id', (event) => {
  console.log(event.recipe.id)
  console.log(event.button == 0) // 0 for left click, 1 for right click
})
````
