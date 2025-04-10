# UI Elements

## Basic Format

| Name     | Description                                          | Type / Literal                                                                                                                                                   |
| -------- | ---------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| type     | type                                                 | string                                                                                                                                                           |
| pos      | element position ^optional^                          | number\[3]                                                                                                                                                       |
| size     | element size ^optional^{ title="default: [16, 16]" } | int\[2]                                                                                                                                                          |
| tooltip  | tooltip ^optional^                                   | [TextComponent](https://minecraft.wiki/w/Text_component_format?oldid=2725300) \| [TextComponent](https://minecraft.wiki/w/Text_component_format?oldid=2725300)[] |
| on_click | click action name to be posted ^optional^            | string                                                                                                                                                           |
| opacity  | opacity ^optional^{ title="default: 1" }             | number                                                                                                                                                           |
|          | additional properties...                             |                                                                                                                                                                  |

Note: Not every element type supports opacity.

## Sprite Element

!!! note "Format"

    | Name  | Description | Type / Literal            |
    | ----- | ----------- | ------------------------- |
    | type  | type        | `sprite`                  |
    | id    | sprite id   | string (ResourceLocation) |
    | scale | scale       | number                    |

## Text Element

!!! note "Format"

    | Name       | Description                                             | Type / Literal                                                                |
    | ---------- | ------------------------------------------------------- | ----------------------------------------------------------------------------- |
    | type       | type                                                    | `text`                                                                        |
    | text       | text                                                    | [TextComponent](https://minecraft.wiki/w/Text_component_format?oldid=2725300) |
    | dark_text  | dark theme text ^optional^                              | [TextComponent](https://minecraft.wiki/w/Text_component_format?oldid=2725300) |
    | color      | default color ^optional^{ title="default: #666666" }    | string (TextColor)                                                            |
    | dark_color | dark theme color ^optional^{ title="default: #BBBBBB" } | string (TextColor)                                                            |
    | shadow     | draw shadow ^optional^{ title="default: false" }        | boolean                                                                       |
    | centered   | center text ^optional^{ title="default: false" }        | boolean                                                                       |

## Item Element

!!! note "Format"

    | Name | Description | Type / Literal |
    | ---- | ----------- | -------------- |
    | type | type        | `item`         |

