id_template = '''
{
    "type": "lychee:block_crushing",
    "landing_block": "%s",
    "post": {
        "type": "place",
        "block": "%s"
    }
}
'''

tag_template = '''
{
    "type": "lychee:block_crushing",
    "landing_block": {
        "tag": "%s"
    },
    "post": {
        "type": "place",
        "block": "%s"
    }
}
'''

id_template = id_template.strip().replace("    ", "\t")
tag_template = tag_template.strip().replace("    ", "\t")
recipes = [
    # ("stone", "cobblestone"),
    # ("deepslate", "cobbled_deepslate"),
    ("c:cobblestone", "gravel"),
    ("c:yellow_sandstones", "sand"),
    ("c:red_sandstones", "red_sand"),
]

for recipe in recipes:
    with open("%s.json" % recipe[1], "w") as f:
        f.write(tag_template % recipe)
