package snownee.lychee;

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import snownee.lychee.recipes.AnvilCraftingRecipe;
import snownee.lychee.recipes.BlockClickingRecipe;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.BlockCrushingRecipeType;
import snownee.lychee.recipes.BlockExplodingRecipe;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.recipes.BlockInteractingRecipeType;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.recipes.DripstoneRecipeType;
import snownee.lychee.recipes.EntityTickingRecipe;
import snownee.lychee.recipes.EntityTickingRecipeType;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.recipes.ItemExplodingRecipe;
import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.recipes.ItemInsideRecipeType;
import snownee.lychee.recipes.LightningChannelingRecipe;
import snownee.lychee.recipes.RandomBlockTickingRecipe;
import snownee.lychee.recipes.RandomBlockTickingRecipeType;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.ItemShapelessRecipeType;
import snownee.lychee.util.recipe.LycheeRecipeType;

public final class RecipeTypes {

	static {
		Objects.requireNonNull(LycheeLootContextParams.ALL);
		Objects.requireNonNull(LycheeLootContextParamSets.ALL);
	}

	public static final Set<LycheeRecipeType<? extends ILycheeRecipe<LycheeContext>>> ALL = Sets.newLinkedHashSet();
	public static final LycheeRecipeType<ItemBurningRecipe> ITEM_BURNING =
			register(new LycheeRecipeType<>("item_burning", ItemBurningRecipe.class, null));
	public static final ItemInsideRecipeType ITEM_INSIDE = register(Util.make(
			new ItemInsideRecipeType(
					"item_inside",
					ItemInsideRecipe.class,
					null
			),
			it -> it.canPreventConsumeInputs = true
	));
	public static final BlockInteractingRecipeType<BlockInteractingRecipe> BLOCK_INTERACTING = register(
			Util.make(
					new BlockInteractingRecipeType<>(
							"block_interacting",
							BlockInteractingRecipe.class,
							LycheeLootContextParamSets.BLOCK_INTERACTION),
					(it) -> {
						it.requiresClient = true;
						it.canPreventConsumeInputs = true;
					}
			));
	public static final BlockInteractingRecipeType<BlockClickingRecipe> BLOCK_CLICKING = register(
			Util.make(
					new BlockInteractingRecipeType<>(
							"block_clicking",
							BlockClickingRecipe.class,
							LycheeLootContextParamSets.BLOCK_INTERACTION),
					(it) -> {
						it.requiresClient = true;
						it.categoryId = BLOCK_INTERACTING.categoryId;
						it.canPreventConsumeInputs = true;
					}
			));
	public static final LycheeRecipeType<AnvilCraftingRecipe> ANVIL_CRAFTING =
			register(Util.make(
					new LycheeRecipeType<>("anvil_crafting", AnvilCraftingRecipe.class, null),
					it -> it.hasStandaloneCategory = false
			));
	public static final BlockCrushingRecipeType BLOCK_CRUSHING = register(new BlockCrushingRecipeType(
			"block_crushing",
			BlockCrushingRecipe.class,
			null
	));
	public static final ItemShapelessRecipeType<LightningChannelingRecipe> LIGHTNING_CHANNELING =
			register(Util.make(
					new ItemShapelessRecipeType<>("lightning_channeling", LightningChannelingRecipe.class, null),
					it -> it.canPreventConsumeInputs = true
			));
	public static final ItemShapelessRecipeType<ItemExplodingRecipe> ITEM_EXPLODING =
			register(Util.make(
					new ItemShapelessRecipeType<>("item_exploding", ItemExplodingRecipe.class, null),
					it -> it.canPreventConsumeInputs = true
			));
	public static final BlockKeyableRecipeType<BlockExplodingRecipe> BLOCK_EXPLODING =
			register(new BlockKeyableRecipeType<>(
					"block_exploding",
					BlockExplodingRecipe.class,
					LycheeLootContextParamSets.BLOCK_ONLY
			));
	public static final RandomBlockTickingRecipeType RANDOM_BLOCK_TICKING = register(Util.make(
			new RandomBlockTickingRecipeType(
					"random_block_ticking",
					RandomBlockTickingRecipe.class,
					LycheeLootContextParamSets.BLOCK_ONLY
			),
			it -> {
				it.extractChance = true;
				it.hasStandaloneCategory = false;
			}
	));
	public static final DripstoneRecipeType DRIPSTONE_DRIPPING = register(Util.make(
			new DripstoneRecipeType(
					"dripstone_dripping",
					DripstoneRecipe.class,
					LycheeLootContextParamSets.BLOCK_ONLY),
			it -> {
				it.extractChance = true;
				it.requiresClient = true;
			}
	));
	public static final EntityTickingRecipeType ENTITY_TICKING = register(new EntityTickingRecipeType(
			"entity_ticking",
			EntityTickingRecipe.class,
			null));

	public static <T extends LycheeRecipeType<? extends ILycheeRecipe<LycheeContext>>> T register(T recipeType) {
		ALL.add(recipeType);
		return Registry.register(BuiltInRegistries.RECIPE_TYPE, recipeType.id, recipeType);
	}

	public static void buildCache() {
		ALL.forEach(LycheeRecipeType::refreshCache);
		ALL.forEach(LycheeRecipeType::updateEmptyState);
	}

}
