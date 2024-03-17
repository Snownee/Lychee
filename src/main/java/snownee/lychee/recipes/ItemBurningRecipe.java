package snownee.lychee.recipes;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.context.RecipeContext;
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
		return false;
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
						Ingredient.CODEC_NONEMPTY.fieldOf(ITEM_IN).forGetter(ItemBurningRecipe::input)
				).apply(instance, ItemBurningRecipe::new));

		@Override
		public @NotNull Codec<ItemBurningRecipe> codec() {
			return CODEC;
		}
	}
}
