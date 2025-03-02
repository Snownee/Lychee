# JSON Fragment

Fragment is a reusable JSON element that can be attached to your Lychee recipes.

## Define

All fragments should be defined in the `lychee_fragment` folder in a data pack, as a JSON file.

```json
{
    "value": "Here can be any type of element"
}
```

!!! note

    Fragments can be nested.

## Use Fragment

First let's assume we have such a fragment:

```json title="mymod/lychee_fragment/gems.json"
{
    "value": [
        {
            "type": "drop_item",
            "id": "diamond"
        },
        {
            "type": "drop_item",
            "id": "emerald"
        }
    ]
}
```

### Replace the Current Element

=== "Original"

    ```json
    {
        "post": {
            "@": "mymod:gems"
        }
    }
    ```

=== "Result"

    ```json
    {
        "post": [
            {
                "type": "drop_item",
                "id": "diamond"
            },
            {
                "type": "drop_item",
                "id": "emerald"
            }
        ]
    }
    ```

### [Spread](https://www.geeksforgeeks.org/javascript-spread-operator/) Elements to the Parent

=== "Original"

    ```json
    {
        "post": [
            {
                "...@": "mymod:gems"
            },
            {
                "type": "prevent_default"
            }
        ]
    }
    ```

=== "Result"

    ```json
    {
        "post": [
            {
                "type": "drop_item",
                "id": "diamond"
            },
            {
                "type": "drop_item",
                "id": "emerald"
            },
            {
                "type": "prevent_default"
            }
        ]
    }
    ```

## Use Variables

You can define variables together with the fragment path, and reference it in the fragment.

=== "Recipe"

    ```json
    {
        "comment": {
            "@": "mymod:comment",
            "name": "Fragment"
        },
        "post": [
            {
                "...@": "mymod:gems",
                "amount": 3
            },
            {
                "type": "prevent_default"
            }
        ]
    }
    ```

=== "`mymod/lychee_fragment/gems.json`"

    ```json
    {
        "value": [
            {
                "type": "drop_item",
                "id": "diamond",
                "count": "$amount"
            },
            {
                "type": "drop_item",
                "id": "emerald",
                "count": "$amount"
            }
        ]
    }
    ```

=== "`mymod/lychee_fragment/comment.json`"

    ```json
    {
        "value": "An example of ${name}"
    }
    ```
