# UI Elements

_Since 6.3_

## Basic Format

| Name     | Description                                          | Type / Literal                                                                                                                                                   |
| -------- | ---------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| type     | type                                                 | string                                                                                                                                                           |
| pos      | element position ^optional^                          | number[]                                                                                                                                                         |
| size     | element size ^optional^{ title="default: [16, 16]" } | int\[2]                                                                                                                                                          |
| tooltip  | tooltip ^optional^                                   | [TextComponent](https://minecraft.wiki/w/Text_component_format?oldid=2725300) \| [TextComponent](https://minecraft.wiki/w/Text_component_format?oldid=2725300)[] |
| on_input | input action name to be posted ^optional^            | string                                                                                                                                                           |
| opacity  | opacity ^optional^{ title="default: 1" }             | number                                                                                                                                                           |
|          | additional properties...                             |                                                                                                                                                                  |

Note: Not every element type supports opacity.

## Sprite Element

!!! note "Format"

    | Name  | Description | Type / Literal            |
    | ----- | ----------- | ------------------------- |
    | type  | type        | "sprite"                  |
    | id    | sprite id   | string (ResourceLocation) |
    | scale | scale       | number                    |

## Text Element

!!! note "Format"

    | Name       | Description                                             | Type / Literal                                                                |
    | ---------- | ------------------------------------------------------- | ----------------------------------------------------------------------------- |
    | type       | type                                                    | "text"                                                                        |
    | text       | text                                                    | [TextComponent](https://minecraft.wiki/w/Text_component_format?oldid=2725300) |
    | dark_text  | dark theme text ^optional^                              | [TextComponent](https://minecraft.wiki/w/Text_component_format?oldid=2725300) |
    | color      | default color ^optional^{ title="default: #666666" }    | string (TextColor)                                                            |
    | dark_color | dark theme color ^optional^{ title="default: #BBBBBB" } | string (TextColor)                                                            |
    | shadow     | draw shadow ^optional^{ title="default: false" }        | true \| false                                                                 |
    | centered   | center text ^optional^{ title="default: false" }        | true \| false                                                                 |

## Game Element Properties

Game element is a special element type that needs to be rendered in 3D space. Currently, it contains two types: `block` and `item`.

!!! note "Format"

    | Name            | Description                                           | Type / Literal |
    | --------------- | ----------------------------------------------------- | -------------- |
    |                 | properties in [Basic Format](#basic-format)           |                |
    | local_pos       | local position ^optional^{ title="default: [0,0,0]" } | number[]       |
    | rotation        | rotation ^optional^{ title="default: [0,0,0]" }       | number[]       |
    | rotation_offset | rotation offset ^optional^                            | number[]       |
    | scale           | scale ^optional^{ title="default: 1" }                | number         |
    |                 | additional properties...                              |                |

## Item Element

!!! note "Format"

    | Name       | Description                                  | Type / Literal |
    | ---------- | -------------------------------------------- | -------------- |
    | type       | type                                         | "item"         |
    | id         | the item resource id                         | string         |
    | count      | item amount ^optional^{ title="default: 1" } | int            |
    | components | item components ^optional^                   | dictionary     |

## Block Element

!!! note "Format"

    | Name  | Description              | Type / Literal                                    |
    | ----- | ------------------------ | ------------------------------------------------- |
    | type  | type                     | "block"                                           |
    | block | the block being rendered | [BlockPredicate](general-types.md#blockpredicate) |