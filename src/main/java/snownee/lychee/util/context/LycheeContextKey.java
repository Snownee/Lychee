package snownee.lychee.util.context;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.AnvilContext;
import snownee.lychee.context.CraftingContext;
import snownee.lychee.context.ItemShapelessContext;
import snownee.lychee.context.JsonContext;
import snownee.lychee.context.LootParamsContext;
import snownee.lychee.util.input.ItemStackHolderCollection;

public interface LycheeContextKey<T> {
	LycheeContextKey<RandomSource> RANDOM = register("random");
	LycheeContextKey<Level> LEVEL = register("level");
	LycheeContextKey<LootParamsContext> LOOT_PARAMS = register("loot_params");

	LycheeContextKey<ItemStackHolderCollection> ITEM = register("item");
	LycheeContextKey<ActionContext> ACTION = register("action");
	LycheeContextKey<JsonContext> JSON = register("json");

	LycheeContextKey<AnvilContext> ANVIL = register("anvil");

	LycheeContextKey<ItemShapelessContext> ITEM_SHAPELESS = register("item_shapeless");
	LycheeContextKey<FallingBlockEntity> FALLING_BLOCK_ENTITY = register("falling_block_entity");

	LycheeContextKey<CraftingContext> CRAFTING = register("crafting");

	static <T extends LycheeContextKey<?>> T register(ResourceLocation location, T object) {
		Registry.register(LycheeRegistries.CONTEXT, location, object);
		return object;
	}

	static <T> LycheeContextKey<T> register(String name) {
		return register(Lychee.id(name));
	}

	static <T> LycheeContextKey<T> register(ResourceLocation location) {
		return register(location, new LycheeContextKey<T>() {
			@Override
			public String toString() {
				return location.toString();
			}
		});
	}
}
