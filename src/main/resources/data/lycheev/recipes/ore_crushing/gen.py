template = '''
{
    "type": "lychee:block_crushing",
    "landing_block": {
        "tag": "%s"
    },
    "post": [
        {
            "type": "place",
            "block": "*"
        },
        {
            "type": "drop_item",
            "item": "%s"
        },
        {
            "type": "drop_item",
            "item": "%s",
            "contextual": {
                "type": "chance",
                "chance": 0.5
            }
        }
    ]
}
'''

template = template.strip().replace("    ", "\t")

recipes = [
    ("iron_ores", "raw_iron"),
    ("copper_ores", "raw_copper"),
    ("coal_ores", "coal"),
    ("lapis_ores", "lapis_lazuli"),
    ("redstone_ores", "redstone"),
    ("diamond_ores", "diamond"),
    ("emerald_ores", "emerald"),
]

for recipe in recipes:
    with open("%s.json" % recipe[1], "w") as f:
        f.write(template % (recipe[0], recipe[1], recipe[1]))
