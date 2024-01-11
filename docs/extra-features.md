# Extra Features

## `lychee:tag`

_Since: 3.11_

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

    String tag value is also supported.

## `lychee:lightning_immune`
_Since: 3.15_

This entity-type tag is used to make entity immune to lightning damage and to prevent them from starting fires when struck by lightning.

## `lychee:lightning_fire_immune`

_Since: 3.15_

This entity-type tag is used to prevent entity from starting fires when struck by lightning.
