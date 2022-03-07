package snownee.lychee;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class LycheeTags {

	public static void init() {
	}

	public static final TagKey<Item> FIRE_IMMUNE = itemTag("fire_immune");

	public static final TagKey<Item> DISPENSER_PLACEMENT = itemTag("dispenser_placement");

	public static final TagKey<Item> EXPLOSIVES = itemTag("explosives");

	public static final TagKey<Block> EXTEND_BOX = blockTag("extend_box");

	public static TagKey<Item> itemTag(String path) {
		return tag(Registry.ITEM_REGISTRY, path);
	}

	public static TagKey<Block> blockTag(String path) {
		return tag(Registry.BLOCK_REGISTRY, path);
	}

	public static <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registryKey, String path) {
		return TagKey.create(registryKey, new ResourceLocation(Lychee.ID, path));
	}

}
