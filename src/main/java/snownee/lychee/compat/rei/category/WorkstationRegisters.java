package snownee.lychee.compat.rei.category;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface WorkstationRegisters {
	Map<ResourceLocation, WorkstationRegister<?>> ALL = Maps.newHashMap();

	WorkstationRegister<BlockCrushingRecipe> BLOCK_CRUSHING = register(
			RecipeTypes.BLOCK_CRUSHING.categoryId,
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
										EntryStack.of(VanillaEntryTypes.ITEM, itemStack));
							}
						});
			});

	static <R extends ILycheeRecipe<LycheeContext>> WorkstationRegister<R> get(LycheeRecipeType<LycheeContext, R> type) {
		return (WorkstationRegister<R>) ALL.get(type.categoryId);
	}

	static <R extends ILycheeRecipe<LycheeContext>> WorkstationRegister<R> register(
			ResourceLocation type,
			WorkstationRegister<R> provider) {
		ALL.put(type, provider);
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
