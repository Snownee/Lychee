template = '''
{
    "type": "lychee:block_crushing",
    "item_in": [
        {
            "item": "%s"
        },
        {
            "item": "%s"
        },
        {
            "item": "%s"
        },
        {
            "item": "%s"
        },
        {
            "item": "%s"
        },
        {
            "item": "%s"
        },
        {
            "item": "%s"
        },
        {
            "item": "%s"
        },
        {
            "item": "%s"
        }
    ],
    "landing_block": "cauldron",
    "post": [
        {
            "type": "drop_item",
            "item": "%s"
        },
        {
            "type": "anvil_damage_chance",
            "chance": 0
        }
    ]
}
'''

template = template.strip().replace("    ", "\t")
recipes = [
    ("coal", "coal_block"),
    ("iron_ingot", "iron_block"),
    ("gold_ingot", "gold_block"),
    ("diamond", "diamond_block"),
    ("emerald", "emerald_block"),
    ("redstone", "redstone_block"),
    ("lapis_lazuli", "lapis_block"),
    ("copper_ingot", "copper_block"),
    ("netherite_ingot", "netherite_block"),
    ("raw_iron", "raw_iron_block"),
    ("raw_gold", "raw_gold_block"),
    ("raw_copper", "raw_copper_block"),
    ("slime_ball", "slime_block"),
    ("bone_meal", "bone_block"),
    ("ice", "packed_ice"),
    ("packed_ice", "blue_ice"),
    ("melon_slice", "melon"),
    ("prismarine_shard", "prismarine_bricks"),
    ("nether_wart", "nether_wart_block"),
    ("wheat", "hay_block"),
    ("disc_fragment_5", "music_disc_5")
]

for recipe in recipes:
    with open("%s.data" % recipe[1], "w") as f:
        f.write(template % (
            recipe[0], recipe[0], recipe[0], recipe[0], recipe[0], recipe[0], recipe[0], recipe[0], recipe[0],
            recipe[1]))

template = '''
{
    "type": "lychee:block_crushing",
    "item_in": [
        {
            "item": "%s"
        },
        {
            "item": "%s"
        },
        {
            "item": "%s"
        },
        {
            "item": "%s"
        }
    ],
    "landing_block": "cauldron",
    "post": [
        {
            "type": "drop_item",
            "item": "%s",
            "count": %s
        },
        {
            "type": "anvil_damage_chance",
            "chance": 0
        }
    ]
}
'''

template = template.strip().replace("    ", "\t")
recipes = [
    ("stone", "stone_bricks", 4),
    ("cobbled_deepslate", "polished_deepslate", 4),
    ("granite", "polished_granite", 4),
    ("diorite", "polished_diorite", 4),
    ("andesite", "polished_andesite", 4),
    ("pointed_dripstone", "dripstone_block", 1),
    ("sand", "sandstone", 1),
    ("red_sand", "red_sandstone", 1),
    ("amethyst_shard", "amethyst_block", 1),
    ("copper_block", "cut_copper", 4),
    ("exposed_copper", "exposed_cut_copper", 4),
    ("weathered_copper", "weathered_cut_copper", 4),
    ("oxidized_copper", "oxidized_cut_copper", 4),
    ("waxed_copper_block", "waxed_cut_copper", 4),
    ("waxed_exposed_copper", "waxed_exposed_cut_copper", 4),
    ("waxed_weathered_copper", "waxed_weathered_cut_copper", 4),
    ("waxed_oxidized_copper", "waxed_oxidized_cut_copper", 4),
    ("oak_log", "oak_wood", 3),
    ("spruce_log", "spruce_wood", 3),
    ("birch_log", "birch_wood", 3),
    ("jungle_log", "jungle_wood", 3),
    ("acacia_log", "acacia_wood", 3),
    ("dark_oak_log", "dark_oak_wood", 3),
    ("mangrove_log", "mangrove_wood", 3),
    ("stripped_oak_log", "stripped_oak_wood", 3),
    ("stripped_spruce_log", "stripped_spruce_wood", 3),
    ("stripped_birch_log", "stripped_birch_wood", 3),
    ("stripped_jungle_log", "stripped_jungle_wood", 3),
    ("stripped_acacia_log", "stripped_acacia_wood", 3),
    ("stripped_dark_oak_log", "stripped_dark_oak_wood", 3),
    ("stripped_mangrove_log", "stripped_mangrove_wood", 3),
    ("crimson_stem", "crimson_hyphae", 3),
    ("warped_stem", "warped_hyphae", 3),
    ("stripped_crimson_stem", "stripped_crimson_hyphae", 3),
    ("stripped_warped_stem", "stripped_warped_hyphae", 3),
    ("sandstone", "cut_sandstone", 4),
    ("brick", "bricks", 4),
    ("popped_chorus_fruit", "purpur_block", 4),
    ("snowball", "snow_block", 1),
    ("clay_ball", "clay", 1),
    ("basalt", "polished_basalt", 4),
    ("glowstone_dust", "glowstone", 1),
    ("nether_brick", "nether_bricks", 4),
    ("packed_mud", "mud_bricks", 4),
    ("magma_cream", "magma_block", 1),
    ("polished_deepslate", "deepslate_bricks", 4),
    ("deepslate_bricks", "deepslate_tiles", 4),
    ("end_stone", "end_stone_bricks", 4),
    ("quartz", "quartz_block", 1),
    ("prismarine_shard", "prismarine", 1),
    ("red_sandstone", "cut_red_sandstone", 4),
    ("blackstone", "polished_blackstone", 4),
    ("polished_blackstone", "polished_blackstone_bricks", 4),
    ("honeycomb", "honeycomb_block", 1),
    ("honey_bottle", "honey_block", 1),
]

for recipe in recipes:
    with open("%s.data" % recipe[1], "w") as f:
        f.write(template % (recipe[0], recipe[0], recipe[0], recipe[0], recipe[1], recipe[2]))
