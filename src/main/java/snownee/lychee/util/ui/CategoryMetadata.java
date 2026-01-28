package snownee.lychee.util.ui;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joml.Vector2ic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import snownee.kiwi.recipe.EmptyRecipeInput;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.Patterns;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.codec.LycheeStreamCodecs;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class CategoryMetadata extends CategorySettingRecipe {
	public static final RecipeHolder<CategoryMetadata> EMPTY = new RecipeHolder<>(
			ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("empty")),
			new CategoryMetadata());

	private final Optional<Vector2ic> size;
	private final Optional<UIElement> icon;
	private final Optional<List<Ingredient>> workstation;

	private CategoryMetadata() {
		super();
		size = Optional.empty();
		icon = Optional.empty();
		workstation = Optional.empty();
	}

	public CategoryMetadata(
			int sortOrder,
			Patterns category,
			Optional<Map<String, List<UIElement>>> elements,
			boolean renderDefault,
			Optional<Vector2ic> size,
			Optional<UIElement> icon,
			Optional<List<Ingredient>> workstation) {
		super(sortOrder, category, elements, renderDefault);
		this.size = size;
		this.icon = icon;
		this.workstation = workstation;
	}

	@Override
	public RecipeSerializer<? extends Recipe<EmptyRecipeInput>> getSerializer() {
		return RecipeSerializers.CATEGORY_METADATA;
	}

	@Override
	public RecipeType<? extends Recipe<EmptyRecipeInput>> getType() {
		return RecipeTypes.CATEGORY_METADATA;
	}

	public Optional<Vector2ic> size() {
		return size;
	}

	public Optional<UIElement> icon() {
		return icon;
	}

	public Optional<List<Ingredient>> workstation() {
		return workstation;
	}

	public static class Serializer implements RecipeSerializer<CategoryMetadata> {
		public static final MapCodec<CategoryMetadata> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.INT.optionalFieldOf("sort_order", 0).forGetter(CategoryMetadata::sortOrder),
				Patterns.CODEC.fieldOf("category").forGetter(CategoryMetadata::category),
				ELEMENTS_CODEC.optionalFieldOf("elements").forGetter(CategoryMetadata::elements),
				Codec.BOOL.optionalFieldOf("render_default", true).forGetter(CategoryMetadata::renderDefault),
				VectorExtensions.CODEC2I.optionalFieldOf("size").forGetter(CategoryMetadata::size),
				UIElement.CODEC.optionalFieldOf("icon").forGetter(CategoryMetadata::icon),
				ExtraCodecs.compactListCodec(LycheeCodecs.INGREDIENT)
						.optionalFieldOf("workstation")
						.forGetter(CategoryMetadata::workstation)
		).apply(instance, CategoryMetadata::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, CategoryMetadata> STREAM_CODEC = LycheeStreamCodecs.composite(
				ByteBufCodecs.VAR_INT,
				CategoryMetadata::sortOrder,
				Patterns.STREAM_CODEC,
				CategoryMetadata::category,
				ELEMENTS_STREAM_CODEC,
				CategoryMetadata::elements,
				ByteBufCodecs.BOOL,
				CategoryMetadata::renderDefault,
				ByteBufCodecs.optional(VectorExtensions.STREAM_CODEC2I),
				CategoryMetadata::size,
				ByteBufCodecs.optional(UIElement.STREAM_CODEC),
				CategoryMetadata::icon,
				ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list())),
				CategoryMetadata::workstation,
				CategoryMetadata::new);

		@Override
		public MapCodec<CategoryMetadata> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CategoryMetadata> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
