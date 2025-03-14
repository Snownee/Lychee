package snownee.lychee.datagen;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.datafixers.util.Function3;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.recipes.AnvilCraftingRecipe;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.BlockExplodingRecipe;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.recipes.RandomBlockTickingRecipe;
import snownee.lychee.recipes.ShapedCraftingRecipe;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.IngredientCollection;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionLike;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;

public abstract class LycheeRecipeBuilder<T extends LycheeRecipeBuilder<T, R>, R extends ILycheeRecipe<?>> extends ContextualBuilder<T> implements LycheeBuilder {
	protected boolean hideInRecipeViewer;
	protected boolean ghost;
	protected @Nullable String comment;
	protected String group = ILycheeRecipe.DEFAULT_GROUP;
	protected final List<PostAction> postActions = Lists.newArrayListWithExpectedSize(6);
	protected MinMaxBounds.Ints maxRepeats = MinMaxBounds.Ints.ANY;
	protected @Nullable AdvancementHolder advancement;

	public LycheeRecipeCommonProperties properties() {
		return new LycheeRecipeCommonProperties(
				hideInRecipeViewer,
				ghost,
				Optional.ofNullable(comment),
				group,
				contextualHolder(),
				postActions,
				maxRepeats);
	}

	@Contract("-> this")
	public T hideInViewer() {
		this.hideInRecipeViewer = true;
		return self();
	}

	@Contract("-> this")
	public T ghost() {
		this.ghost = true;
		return self();
	}

	@Contract("_ -> this")
	public T comment(@Nullable String comment) {
		this.comment = comment;
		return self();
	}

	@Contract("_ -> this")
	public T group(String group) {
		this.group = group;
		return self();
	}

	@Contract("_ -> this")
	public T post(PostActionLike postAction) {
		postActions.add(postAction.asAction());
		return self();
	}

	@Contract("_ -> this")
	@HideFromJS
	public T post(Collection<? extends PostActionLike> postActions) {
		postActions.forEach(this::post);
		return self();
	}

	@Contract("_ -> this")
	public T maxRepeats(MinMaxBounds.Ints maxRepeats) {
		this.maxRepeats = maxRepeats;
		return self();
	}

	@Contract("-> this")
	@CanIgnoreReturnValue
	public T noRepeat() {
		return maxRepeats(BoundsExtensions.ONE);
	}

	@Contract("_ -> this")
	public T advancement(AdvancementHolder advancement) {
		this.advancement = advancement;
		return self();
	}

	public abstract R build();

	@CanIgnoreReturnValue
	public R export(ResourceLocation id, RecipeOutput exporter) {
		R recipe = build();
		exporter.accept(id, recipe, advancement);
		return recipe;
	}

	public static abstract class Shapeless<T extends LycheeRecipeBuilder<T, R>, R extends ILycheeRecipe<?>> extends LycheeRecipeBuilder<T, R> {
		protected final List<SizedIngredient> ingredients = Lists.newArrayList();

		@Contract("_ -> this")
		public T itemIn(Collection<SizedIngredient> ingredients) {
			this.ingredients.addAll(ingredients);
			return self();
		}

		protected IngredientCollection ingredientCollection() {
			return IngredientCollection.of(ingredients);
		}
	}

	public static class SimpleShapeless<R extends ILycheeRecipe<?>> extends Shapeless<SimpleShapeless<R>, R> {
		protected final BiFunction<LycheeRecipeCommonProperties, IngredientCollection, R> constructor;

		public SimpleShapeless(BiFunction<LycheeRecipeCommonProperties, IngredientCollection, R> constructor) {
			this.constructor = constructor;
		}

		@Override
		public R build() {
			return constructor.apply(properties(), ingredientCollection());
		}
	}

	public static class BlockCrushing extends Shapeless<BlockCrushing, BlockCrushingRecipe> {
		protected BlockPredicate fallingBlock = BlockCrushingRecipe.ANVIL;
		protected BlockPredicate landingBlock = BlockPredicateExtensions.ANY;

		@Contract("_ -> this")
		public BlockCrushing fallingBlock(Object fallingBlock) {
			this.fallingBlock = block(fallingBlock);
			return self();
		}

		@Contract("_ -> this")
		public BlockCrushing landingBlock(Object landingBlock) {
			this.landingBlock = block(landingBlock);
			return self();
		}

		@Override
		public BlockCrushingRecipe build() {
			return new BlockCrushingRecipe(properties(), fallingBlock, landingBlock, ingredientCollection());
		}
	}

	public static class ItemInside extends Shapeless<ItemInside, ItemInsideRecipe> {
		protected BlockPredicate block = BlockPredicateExtensions.ANY;
		protected int time;

		@Contract("_ -> this")
		public ItemInside blockIn(Object block) {
			this.block = block(block);
			return self();
		}

		@Contract("_ -> this")
		public ItemInside time(int time) {
			this.time = time;
			return self();
		}

		@Override
		public ItemInsideRecipe build() {
			return new ItemInsideRecipe(properties(), block, time, ingredientCollection());
		}
	}

	public static class Dripstone extends LycheeRecipeBuilder<Dripstone, DripstoneRecipe> {
		protected final BlockPredicate sourceBlock;
		protected final BlockPredicate targetBlock;

		public Dripstone(BlockPredicate sourceBlock, BlockPredicate targetBlock) {
			this.sourceBlock = sourceBlock;
			this.targetBlock = targetBlock;
		}

		@Override
		public DripstoneRecipe build() {
			return new DripstoneRecipe(properties(), sourceBlock, targetBlock);
		}
	}

	public static class AnvilCrafting extends LycheeRecipeBuilder<AnvilCrafting, AnvilCraftingRecipe> {
		protected final NonNullList<Ingredient> ingredients;
		protected final int levelCost;
		protected final int materialCost;
		protected final ItemStack output;
		protected final List<PostAction> assemblingActions = Lists.newArrayListWithExpectedSize(6);

		public AnvilCrafting(Ingredient left, @Nullable Ingredient right, int materialCost, int levelCost, ItemStack output) {
			this.ingredients = right == null ? NonNullList.of(Ingredient.EMPTY, left) : NonNullList.of(Ingredient.EMPTY, left, right);
			this.levelCost = levelCost;
			this.materialCost = materialCost;
			this.output = output;
		}

		@Contract("_ -> this")
		public AnvilCrafting assembling(PostActionLike assemblingAction) {
			assemblingActions.add(assemblingAction.asAction());
			return self();
		}

		@Contract("_ -> this")
		@HideFromJS
		public AnvilCrafting assembling(Collection<? extends PostActionLike> assemblingActions) {
			assemblingActions.forEach(this::assembling);
			return self();
		}

		@Override
		public AnvilCraftingRecipe build() {
			return new AnvilCraftingRecipe(properties(), ingredients, output, assemblingActions, levelCost, materialCost);
		}
	}

	public static class BlockExploding extends LycheeRecipeBuilder<BlockExploding, BlockExplodingRecipe> {
		protected final BlockPredicate block;

		public BlockExploding(BlockPredicate block) {
			this.block = block;
		}

		@Override
		public BlockExplodingRecipe build() {
			return new BlockExplodingRecipe(properties(), block);
		}
	}

	public static class RandomBlockTicking extends LycheeRecipeBuilder<RandomBlockTicking, RandomBlockTickingRecipe> {
		protected final BlockPredicate block;

		public RandomBlockTicking(BlockPredicate block) {
			this.block = block;
		}

		@Override
		public RandomBlockTickingRecipe build() {
			return new RandomBlockTickingRecipe(properties(), block);
		}
	}

	public static class ItemBurning extends LycheeRecipeBuilder<ItemBurning, ItemBurningRecipe> {
		protected final SizedIngredient input;

		public ItemBurning(SizedIngredient input) {
			this.input = input;
		}

		@Override
		public ItemBurningRecipe build() {
			return new ItemBurningRecipe(properties(), input);
		}
	}

	public static class BlockInteracting<R extends BlockInteractingRecipe> extends LycheeRecipeBuilder<BlockInteracting<R>, R> {
		protected final Function3<LycheeRecipeCommonProperties, List<SizedIngredient>, BlockPredicate, R> constructor;
		protected final List<SizedIngredient> input;
		protected final BlockPredicate blockPredicate;

		public BlockInteracting(
				Function3<LycheeRecipeCommonProperties, List<SizedIngredient>, BlockPredicate, R> constructor,
				SizedIngredient mainHand,
				@Nullable SizedIngredient offHand,
				BlockPredicate block) {
			this.constructor = constructor;
			this.input = offHand == null ? List.of(mainHand) : List.of(mainHand, offHand);
			this.blockPredicate = block;
			noRepeat();
		}

		@Override
		public R build() {
			return constructor.apply(properties(), input, blockPredicate);
		}
	}

	public static class ShapedCrafting extends LycheeRecipeBuilder<ShapedCrafting, ShapedCraftingRecipe> {
		private final RecipeCategory category;
		private final ItemStack result;
		private final List<String> rows;
		private final Map<Character, Ingredient> key;
		private boolean showNotification;
		protected final List<PostAction> assemblingActions = Lists.newArrayListWithExpectedSize(6);

		public ShapedCrafting(RecipeCategory category, ItemLike result, int amount) {
			this(category, new ItemStack(result, amount));
		}

		public ShapedCrafting(RecipeCategory category, ItemStack result) {
			this.rows = Lists.newArrayList();
			this.key = Maps.newLinkedHashMap();
			this.showNotification = true;
			this.category = category;
			this.result = result;
		}

		public ShapedCrafting define(Character key, TagKey<Item> tagKey) {
			return this.define(key, Ingredient.of(tagKey));
		}

		public ShapedCrafting define(Character key, ItemLike item) {
			return this.define(key, Ingredient.of(item));
		}

		public ShapedCrafting define(Character key, Ingredient ingredient) {
			if (this.key.containsKey(key)) {
				throw new IllegalArgumentException("Symbol '" + key + "' is already defined!");
			} else if (key == ' ') {
				throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
			} else {
				this.key.put(key, ingredient);
				return this;
			}
		}

		public ShapedCrafting pattern(String row) {
			if (!this.rows.isEmpty() && row.length() != this.rows.get(0).length()) {
				throw new IllegalArgumentException("Pattern must be the same width on every line!");
			} else {
				this.rows.add(row);
				return this;
			}
		}

		public ShapedCrafting showNotification(boolean showNotification) {
			this.showNotification = showNotification;
			return this;
		}

		@Contract("_ -> this")
		public ShapedCrafting assembling(PostActionLike assemblingAction) {
			assemblingActions.add(assemblingAction.asAction());
			return self();
		}

		@Contract("_ -> this")
		@HideFromJS
		public ShapedCrafting assembling(Collection<? extends PostActionLike> assemblingActions) {
			assemblingActions.forEach(this::assembling);
			return self();
		}

		@Override
		public ShapedCraftingRecipe build() {
			LycheeRecipeCommonProperties properties = properties();
			ShapedRecipe shapedRecipe = new ShapedRecipe(
					ILycheeRecipe.DEFAULT_GROUP.equals(properties.group()) ? "" : properties.group(),
					determineBookCategory(category),
					ShapedRecipePattern.of(this.key, this.rows),
					result,
					showNotification);
			return new ShapedCraftingRecipe(properties, shapedRecipe, assemblingActions);
		}

		static CraftingBookCategory determineBookCategory(RecipeCategory category) {
			return switch (category) {
				case BUILDING_BLOCKS -> CraftingBookCategory.BUILDING;
				case TOOLS, COMBAT -> CraftingBookCategory.EQUIPMENT;
				case REDSTONE -> CraftingBookCategory.REDSTONE;
				default -> CraftingBookCategory.MISC;
			};
		}
	}
}
