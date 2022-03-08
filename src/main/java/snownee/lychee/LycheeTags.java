package snownee.lychee;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class LycheeTags {

	public static void init() {
	}

	public static final TagKey<Item> FIRE_IMMUNE = ItemTags.create(new ResourceLocation(Lychee.ID, "fire_immune"));

	public static final TagKey<Item> DISPENSER_PLACEMENT = ItemTags.create(new ResourceLocation(Lychee.ID, "dispenser_placement"));

	public static final TagKey<Item> EXPLOSIVES = ItemTags.create(new ResourceLocation(Lychee.ID, "explosives"));

	public static final TagKey<Block> EXTEND_BOX = BlockTags.create(new ResourceLocation(Lychee.ID, "extend_box"));

}
