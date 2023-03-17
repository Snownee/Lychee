# JSON Fragment

Fragment is a reusable JSON element that can be attached to your Lychee recipes.

## Define

All fragments should be defined in the `lychee_fragments` folder in a data pack, as a JSON file.

```json
{
  "value": "Here can be any type of element"
}
```

!!! note

	Fragments can be nested.

## Use Fragment

First let's assume we have such a fragment:

```json title="lychee_fragments/gems.json"
{
  "value": [
    {
      "type": "drop_item",
      "item": "diamond"
    },
    {
      "type": "drop_item",
      "item": "emerald"
    }
  ]
}
```

### Replace the Current Element

=== "Original"

    ```json
    {
      "post": {
        "@": "gems"
      }
    }
    ```

=== "Result"

    ```json
    {
      "post": [
        {
          "type": "drop_item",
          "item": "diamond"
        },
        {
          "type": "drop_item",
          "item": "emerald"
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
          "...@": "gems"
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
          "item": "diamond"
        },
        {
          "type": "drop_item",
          "item": "emerald"
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
        "@": "comment",
        "name": "Fragment"
      },
      "post": [
        {
          "...@": "gems",
          "amount": 3
        },
        {
          "type": "prevent_default"
        }
      ]
    }
    ```

=== "`lychee_fragments/gems.json`"

    ```json
    {
      "value": [
        {
          "type": "drop_item",
          "item": "diamond",
          "count": "$amount"
        },
        {
          "type": "drop_item",
          "item": "emerald",
          "count": "$amount"
        }
      ]
    }
    ```

=== "`lychee_fragments/comment.json`"

    ```json
    {
      "value": "An example of ${name}"
    }
    ```
