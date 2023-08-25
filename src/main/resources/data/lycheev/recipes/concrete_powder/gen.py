template = '''
{
    "type": "lychee:block_crushing",
    "landing_block": "%s_concrete",
    "post": {
        "type": "place",
        "block": "%s_concrete_powder"
    }
}
'''

template = template.strip().replace("    ", "\t")
colors = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]

for color in colors:
    with open("%s.json" % color, "w") as f:
        f.write(template % (color, color))
