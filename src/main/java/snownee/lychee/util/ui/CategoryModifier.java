package snownee.lychee.util.ui;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import snownee.kiwi.recipe.EmptyRecipeInput;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.Patterns;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class CategoryModifier extends CategorySettingRecipe {

	private final Patterns recipe;

	public CategoryModifier(
			int sortOrder,
			Patterns category,
			Patterns recipe,
			Optional<Map<String, List<UIElement>>> elements,
			boolean renderDefault) {
		super(sortOrder, category, elements, renderDefault);
		this.recipe = recipe;
	}

	public Patterns recipe() {
		return recipe;
	}

	@Override
	public RecipeSerializer<? extends Recipe<EmptyRecipeInput>> getSerializer() {
		return RecipeSerializers.CATEGORY_MODIFIER;
	}

	@Override
	public RecipeType<? extends Recipe<EmptyRecipeInput>> getType() {
		return RecipeTypes.CATEGORY_MODIFIER;
	}

	public static final MapCodec<CategoryModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.optionalFieldOf("sort_order", 0).forGetter(CategoryModifier::sortOrder),
			Patterns.CODEC.fieldOf("category").forGetter(CategoryModifier::category),
			Patterns.CODEC.fieldOf("recipe").forGetter(CategoryModifier::category),
			ELEMENTS_CODEC.optionalFieldOf("elements").forGetter(CategoryModifier::elements),
			Codec.BOOL.optionalFieldOf("render_default", true).forGetter(CategoryModifier::renderDefault)
	).apply(instance, CategoryModifier::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CategoryModifier> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			CategoryModifier::sortOrder,
			Patterns.STREAM_CODEC,
			CategoryModifier::category,
			Patterns.STREAM_CODEC,
			CategoryModifier::recipe,
			ELEMENTS_STREAM_CODEC,
			CategoryModifier::elements,
			ByteBufCodecs.BOOL,
			CategoryModifier::renderDefault,
			CategoryModifier::new);
}
