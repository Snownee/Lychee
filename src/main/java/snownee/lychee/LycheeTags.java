package snownee.lychee;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class LycheeTags {

	public static void init() {
	}

	public static final Named<Item> FIRE_IMMUNE = TagFactory.ITEM.create(new ResourceLocation(Lychee.ID, "fire_immune"));

	public static final Named<Item> DISPENSER_PLACEMENT = TagFactory.ITEM.create(new ResourceLocation(Lychee.ID, "dispenser_placement"));

	public static final Named<Item> EXPLOSIVES = TagFactory.ITEM.create(new ResourceLocation(Lychee.ID, "explosives"));

	public static final Named<Block> EXTEND_BOX = TagFactory.BLOCK.create(new ResourceLocation(Lychee.ID, "extend_box"));

}
