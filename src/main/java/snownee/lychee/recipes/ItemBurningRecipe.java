package snownee.lychee.recipes;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.NonNullListExtensions;
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
		final var lootParams = context.initLootParams(RecipeTypes.ITEM_BURNING);
		lootParams.set(LootContextParams.ORIGIN, entity.position());
		lootParams.set(LootContextParams.THIS_ENTITY, entity);
		lootParams.validate();
		RecipeTypes.ITEM_BURNING.findFirst(context, entity.level()).ifPresent(it -> {
			context.put(it);
			int times = it.value().getRandomRepeats(entity.getItem().getCount() / it.value().input.count(), context);
			var itemStackHolders = ItemStackHolderCollection.InWorld.of(entity);
			context.put(LycheeContextKey.ITEM, itemStackHolders);
			it.value().applyPostActions(context, times);
			itemStackHolders.postApply(true, times);
		});
	}

	protected final SizedIngredient input;

	public ItemBurningRecipe(LycheeRecipeCommonProperties commonProperties, SizedIngredient input) {
		super(commonProperties);
		this.input = input;
		onConstructed();
	}

	public SizedIngredient input() {
		return input;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		ItemStack stack = ((ItemEntity) lootParams.get(LootContextParams.THIS_ENTITY)).getItem();
		return input.test(stack);
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullListExtensions.copyOf(List.of(input.ingredient()));
	}

	@Override
	public List<SizedIngredient> sizedIngredients() {
		return List.of(input);
	}

	@Override
	public LycheeRecipeSerializer<ItemBurningRecipe> getSerializer() {
		return RecipeSerializers.ITEM_BURNING;
	}

	@Override
	public LycheeRecipeType<ItemBurningRecipe> getType() {
		return RecipeTypes.ITEM_BURNING;
	}

	public static class Serializer implements LycheeRecipeSerializer<ItemBurningRecipe> {
		public static final MapCodec<ItemBurningRecipe> CODEC =
				RecordCodecBuilder.mapCodec(instance -> instance.group(
						LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(LycheeRecipe::commonProperties),
						LycheeCodecs.SIZED_INGREDIENT.fieldOf(ITEM_IN).forGetter(ItemBurningRecipe::input)
				).apply(instance, ItemBurningRecipe::new));

		@Override
		public MapCodec<ItemBurningRecipe> codec() {
			return CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ItemBurningRecipe> STREAM_CODEC =
				StreamCodec.composite(
						LycheeRecipeCommonProperties.STREAM_CODEC,
						ItemBurningRecipe::commonProperties,
						SizedIngredient.STREAM_CODEC,
						ItemBurningRecipe::input,
						ItemBurningRecipe::new
				);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ItemBurningRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
