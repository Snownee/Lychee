# Extra Features

## `lychee:tag`

*Since 3.11*

You can use `lychee:tag` in `Ingredient` or `ItemStack` to assign a NBT tag to the item. When used in `Ingredient`, it is expected that this is only for better presentation in JEI / REI.

!!! example

    ```json
    {
      "item": "diamond_sword",
      "lychee:tag": {
        "display": {
          "Name": "{\"text\":\"Lychee\"}"
        }
      }
    }
    ```
