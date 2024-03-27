package snownee.lychee.recipes;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.mixin.NonNullListAccess;
import snownee.lychee.util.codec.CompactListCodec;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ItemShapelessRecipeUtils;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class LightningChannelingRecipe extends LycheeRecipe<LycheeContext> {
	public static void invoke(final LightningBolt lightningBolt, final List<Entity> entities) {
		var itemEntities = entities.stream().filter(it -> it instanceof ItemEntity).map(ItemEntity.class::cast);
		var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, lightningBolt.level());
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.setParam(LootContextParams.ORIGIN, lightningBolt.position());
		lootParamsContext.setParam(LootContextParams.THIS_ENTITY, lightningBolt);
		RecipeTypes.LIGHTNING_CHANNELING.process(itemEntities, context);
	}


	protected NonNullList<Ingredient> ingredients = NonNullList.create();

	public LightningChannelingRecipe(LycheeRecipeCommonProperties commonProperties) {
		super(commonProperties);
		onConstructed();
	}

	public LightningChannelingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			final List<Ingredient> ingredients
	) {
		super(commonProperties);
		this.ingredients = NonNullListAccess.construct(ingredients, null);
		onConstructed();
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		return ItemShapelessRecipeUtils.matches(context, ingredients);
	}

	@Override
	public @NotNull RecipeSerializer<LightningChannelingRecipe> getSerializer() {
		return RecipeSerializers.LIGHTNING_CHANNELING;
	}

	@Override
	public @NotNull LycheeRecipeType<LycheeContext, LightningChannelingRecipe> getType() {
		return RecipeTypes.LIGHTNING_CHANNELING;
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	public static class Serializer implements LycheeRecipeSerializer<LightningChannelingRecipe> {
		public static final Codec<LightningChannelingRecipe> CODEC =
				RecordCodecBuilder.create(instance -> instance.group(
						LycheeRecipeCommonProperties.MAP_CODEC.forGetter(LycheeRecipe::commonProperties),
						ExtraCodecs.strictOptionalField(new CompactListCodec<>(LycheeCodecs.OPTIONAL_INGREDIENT_CODEC), ITEM_IN, List.of())
								.forGetter(it -> it.ingredients)
				).apply(instance, LightningChannelingRecipe::new));

		@Override
		public @NotNull Codec<LightningChannelingRecipe> codec() {
			return CODEC;
		}
	}
}
