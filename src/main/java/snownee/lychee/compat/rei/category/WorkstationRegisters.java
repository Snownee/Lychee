package snownee.lychee.compat.rei.category;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.ItemExplodingRecipe;
import snownee.lychee.recipes.LightningChannelingRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface WorkstationRegisters {
	Map<ResourceLocation, WorkstationRegister<?>> ALL = Maps.newHashMap();

	WorkstationRegister<BlockCrushingRecipe> BLOCK_CRUSHING = register(
			RecipeTypes.BLOCK_CRUSHING,
			(registry, category, recipes) -> {
				recipes.stream()
						.<BlockPredicate>mapMulti((recipe, consumer) -> {
							var blockPredicate = recipe.value().blockPredicate();
							blockPredicate.ifPresent(consumer);
						}).distinct()
						.flatMap(it -> BlockPredicateExtensions.matchedBlocks(it).stream()).distinct()
						.forEach((block) -> {
							var item = block.asItem();
							if (!item.equals(Items.AIR)) {
								var itemStack = item.getDefaultInstance();
								registry.addWorkstations(
										category.getCategoryIdentifier(),
										EntryStacks.of(itemStack));
							}
						});
			});

	WorkstationRegister<LightningChannelingRecipe> LIGHTNING_CHANNELING = register(
			RecipeTypes.LIGHTNING_CHANNELING,
			(registry, category, recipes) -> registry.addWorkstations(category.getCategoryIdentifier(), EntryStacks.of(Items.LIGHTNING_ROD))
	);

	WorkstationRegister<ItemExplodingRecipe> ITEM_EXPLODING = register(
			RecipeTypes.ITEM_EXPLODING,
			(registry, category, recipes) -> {
				for (Item item : CommonProxy.tagElements(BuiltInRegistries.ITEM, LycheeTags.ITEM_EXPLODING_CATALYSTS)) {
					registry.addWorkstations(category.getCategoryIdentifier(), EntryStacks.of(item));
				}
			}
	);

	static <R extends ILycheeRecipe<LycheeContext>> WorkstationRegister<R> get(LycheeRecipeType<LycheeContext, R> type) {
		return (WorkstationRegister<R>) ALL.get(type.categoryId);
	}

	static <R extends ILycheeRecipe<LycheeContext>> WorkstationRegister<R> register(
			LycheeRecipeType<LycheeContext, R> type,
			WorkstationRegister<R> provider) {
		ALL.put(type.categoryId, provider);
		return provider;
	}

	@FunctionalInterface
	interface WorkstationRegister<R extends ILycheeRecipe<LycheeContext>> {
		void consume(
				CategoryRegistry registry,
				LycheeDisplayCategory<? extends LycheeDisplay<R>> category,
				Collection<RecipeHolder<R>> recipes);
	}
}
