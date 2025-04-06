package snownee.lychee.util.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.kiwi.recipe.EmptyRecipeInput;
import snownee.kiwi.recipe.SimpleRecipe;
import snownee.kiwi.util.NotNullByDefault;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.VectorExtensions;

@NotNullByDefault
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class CategoryMetadata extends SimpleRecipe<EmptyRecipeInput> {
	public static final RecipeHolder<CategoryMetadata> EMPTY = new RecipeHolder<>(
			ResourceLocation.withDefaultNamespace("empty"),
			new CategoryMetadata());

	private final List<String> category;
	private @Nullable List<Pattern> categoryPattern;
	private final Optional<Vector2ic> size;
	private final Optional<UIElement> icon;
	private final Optional<List<Ingredient>> workstation;
	private final Optional<Map<String, List<UIElement>>> elements;
	private final boolean renderDefault;

	private CategoryMetadata() {
		category = List.of();
		size = Optional.empty();
		icon = Optional.empty();
		workstation = Optional.empty();
		elements = Optional.empty();
		renderDefault = true;
	}

	public CategoryMetadata(
			List<String> category,
			Optional<Vector2ic> size,
			Optional<UIElement> icon,
			Optional<List<Ingredient>> workstation,
			Optional<Map<String, List<UIElement>>> elements,
			boolean renderDefault) {
		this.category = category;
		this.size = size;
		this.icon = icon;
		this.workstation = workstation;
		this.elements = elements;
		this.renderDefault = renderDefault;
	}

	@Override
	public boolean matches(EmptyRecipeInput input, Level level) {
		return false;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializers.CATEGORY_METADATA;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeTypes.CATEGORY_METADATA;
	}

	public List<String> category() {
		return category;
	}

	public List<Pattern> categoryPattern() {
		if (categoryPattern == null) {
			categoryPattern = category.stream().map(it -> {
				try {
					return Pattern.compile(it);
				} catch (Exception e) {
					Lychee.LOGGER.error("Failed to compile category pattern: {}", it);
					return null;
				}
			}).filter(Objects::nonNull).toList();
		}
		return categoryPattern;
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

	public Optional<Map<String, List<UIElement>>> elements() {
		return elements;
	}

	public boolean renderDefault() {
		return renderDefault;
	}

	@NotNullByDefault
	public static class Serializer implements RecipeSerializer<CategoryMetadata> {
		public static final MapCodec<CategoryMetadata> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ExtraCodecs.nonEmptyList(KCodecs.compactList(ExtraCodecs.NON_EMPTY_STRING))
						.fieldOf("category")
						.forGetter(CategoryMetadata::category),
				VectorExtensions.CODEC2I.optionalFieldOf("size").forGetter(CategoryMetadata::size),
				UIElement.CODEC.optionalFieldOf("icon").forGetter(CategoryMetadata::icon),
				KCodecs.compactList(Ingredient.CODEC_NONEMPTY).optionalFieldOf("workstation").forGetter(CategoryMetadata::workstation),
				ExtraCodecs.strictUnboundedMap(Codec.STRING, KCodecs.compactList(UIElement.CODEC))
						.optionalFieldOf("elements")
						.forGetter(CategoryMetadata::elements),
				Codec.BOOL.optionalFieldOf("render_default", true).forGetter(CategoryMetadata::renderDefault)
		).apply(instance, CategoryMetadata::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, CategoryMetadata> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
				CategoryMetadata::category,
				ByteBufCodecs.optional(VectorExtensions.STREAM_CODEC2I),
				CategoryMetadata::size,
				ByteBufCodecs.optional(UIElement.STREAM_CODEC),
				CategoryMetadata::icon,
				ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list())),
				CategoryMetadata::workstation,
				ByteBufCodecs.optional(ByteBufCodecs.map(
						HashMap::newHashMap,
						ByteBufCodecs.STRING_UTF8,
						UIElement.STREAM_CODEC.apply(ByteBufCodecs.list()))),
				CategoryMetadata::elements,
				ByteBufCodecs.BOOL,
				CategoryMetadata::renderDefault,
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
