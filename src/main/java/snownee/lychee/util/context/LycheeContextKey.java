package snownee.lychee.util.context;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.AnvilContext;
import snownee.lychee.context.CraftingContext;
import snownee.lychee.context.ItemShapelessContext;
import snownee.lychee.context.JsonContext;
import snownee.lychee.context.LootParamsContext;
import snownee.lychee.context.RecipeContext;
import snownee.lychee.util.action.ActionMarker;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface LycheeContextKey<T> {
	LycheeContextKey<RandomSource> RANDOM = register("random");
	LycheeContextKey<Level> LEVEL = register("level");
	LycheeContextKey<LootParamsContext> LOOT_PARAMS = register("loot_params");
	LycheeContextKey<RecipeContext> RECIPE_ID = register("recipe_id");
	LycheeContextKey<ILycheeRecipe<?>> RECIPE = register("recipe");

	LycheeContextKey<ItemStackHolderCollection> ITEM = register("item");
	LycheeContextKey<ActionContext> ACTION = register("action");
	LycheeContextKey<ActionMarker> MARKER = register("marker");
	LycheeContextKey<JsonContext> JSON = register("data");

	LycheeContextKey<AnvilContext> ANVIL = register("anvil");

	LycheeContextKey<ItemShapelessContext> ITEM_SHAPELESS = register("item_shapeless");
	LycheeContextKey<FallingBlockEntity> FALLING_BLOCK_ENTITY = register("falling_block_entity");

	LycheeContextKey<CraftingContext> CRAFTING = register("crafting");

	LycheeContextKey<BlockState> DRIPSTONE_SOURCE = register("dripstone_root");

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
