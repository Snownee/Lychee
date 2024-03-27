package snownee.lychee.recipes;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.context.RecipeContext;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class ItemBurningRecipe extends LycheeRecipe<LycheeContext> {
	public static void invoke(ItemEntity entity) {
		final var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, entity.level());
		final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);

		lootParamsContext.setParam(LootContextParams.ORIGIN, entity.position());
		lootParamsContext.setParam(LootContextParams.THIS_ENTITY, entity);
		lootParamsContext.validate(RecipeTypes.ITEM_BURNING.contextParamSet);
		RecipeTypes.ITEM_BURNING.findFirst(context, entity.level()).ifPresent(it -> {
			context.put(LycheeContextKey.RECIPE_ID, new RecipeContext(it.id()));
			context.put(LycheeContextKey.RECIPE, it.value());
			int times = it.value().getRandomRepeats(entity.getItem().getCount(), context);
			var itemStackHolders = ItemStackHolderCollection.InWorld.of(entity);
			context.put(LycheeContextKey.ITEM, itemStackHolders);
			it.value().applyPostActions(context, times);
			itemStackHolders.postApply(true, times);
		});
	}

	protected final Ingredient input;

	protected ItemBurningRecipe(LycheeRecipeCommonProperties commonProperties, Ingredient input) {
		super(commonProperties);
		this.input = input;
		onConstructed();
	}

	public Ingredient input() {
		return input;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		ItemStack stack = ((ItemEntity) lootParamsContext.get(LootContextParams.THIS_ENTITY)).getItem();
		return input.test(stack);
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, input);
	}

	@Override
	public @NotNull RecipeSerializer<ItemBurningRecipe> getSerializer() {
		return RecipeSerializers.ITEM_BURNING;
	}

	@Override
	public @NotNull LycheeRecipeType<LycheeContext, ItemBurningRecipe> getType() {
		return RecipeTypes.ITEM_BURNING;
	}

	public static class Serializer implements LycheeRecipeSerializer<ItemBurningRecipe> {
		public static final Codec<ItemBurningRecipe> CODEC =
				RecordCodecBuilder.create(instance -> instance.group(
						LycheeRecipeCommonProperties.MAP_CODEC.forGetter(LycheeRecipe::commonProperties),
						ExtraCodecs.strictOptionalField(LycheeCodecs.OPTIONAL_INGREDIENT_CODEC, ITEM_IN, Ingredient.EMPTY)
								.forGetter(ItemBurningRecipe::input)
				).apply(instance, ItemBurningRecipe::new));

		@Override
		public @NotNull Codec<ItemBurningRecipe> codec() {
			return CODEC;
		}
	}
}
