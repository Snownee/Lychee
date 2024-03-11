package snownee.lychee;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class LycheeTags {
	public static final TagKey<Item> FIRE_IMMUNE = itemTag("fire_immune");

	public static final TagKey<Item> DISPENSER_PLACEMENT = itemTag("dispenser_placement");

	public static final TagKey<Item> ITEM_EXPLODING_CATALYSTS = itemTag("item_exploding_catalysts");

	public static final TagKey<Item> BLOCK_EXPLODING_CATALYSTS = itemTag("block_exploding_catalysts");

	public static final TagKey<Block> EXTEND_BOX = blockTag("extend_box");

	public static final TagKey<EntityType<?>> LIGHTNING_IMMUNE = entityTag("lightning_immune");

	public static final TagKey<EntityType<?>> LIGHTING_FIRE_IMMUNE = entityTag("lightning_fire_immune");


	public static TagKey<EntityType<?>> entityTag(String path) {
		return tag(Registries.ENTITY_TYPE, path);
	}

	public static TagKey<Item> itemTag(String path) {
		return tag(Registries.ITEM, path);
	}

	public static TagKey<Block> blockTag(String path) {
		return tag(Registries.BLOCK, path);
	}

	public static <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registryKey, String path) {
		return TagKey.create(registryKey, new ResourceLocation(Lychee.ID, path));
	}

}
