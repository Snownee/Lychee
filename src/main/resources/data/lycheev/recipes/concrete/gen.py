template = '''
{
    "type": "lychee:item_inside",
    "item_in": {
        "item": "%s_concrete_powder"
    },
    "block_in": "water",
    "post": {
        "type": "drop_item",
        "item": "%s_concrete"
    }
}
'''

template = template.strip().replace("    ", "\t")
colors = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]

for color in colors:
    with open("%s.json" % color, "w") as f:
        f.write(template % (color, color))
