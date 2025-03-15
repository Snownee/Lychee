package snownee.lychee.util.codec;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.item.crafting.Ingredient;

public class IngredientCodecs {
	public static MapCodec<Ingredient> NON_EMPTY_MAP_CODEC = null;

	public static MapCodec<Ingredient.Value> VALUE_MAP_CODEC = null;
	public static MapCodec<Ingredient.ItemValue> ITEM_VALUE_MAP_CODEC = null;
	public static MapCodec<Ingredient.TagValue> TAG_VALUE_MAP_CODEC = null;
}
