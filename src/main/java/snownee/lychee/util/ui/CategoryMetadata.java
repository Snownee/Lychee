package snownee.lychee.util.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
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

@NotNullByDefault
public class CategoryMetadata extends SimpleRecipe<EmptyRecipeInput> {
	private final List<String> category;
	private final @Nullable UIElement icon;
	private final @Nullable List<List<ItemStack>> workstations;
	private @Nullable List<Pattern> categoryPattern;

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public CategoryMetadata(List<String> category, Optional<UIElement> icon, Optional<List<List<ItemStack>>> workstations) {
		this.category = category;
		this.icon = icon.orElse(null);
		this.workstations = workstations.orElse(null);
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

	public Optional<UIElement> icon() {
		return Optional.ofNullable(icon);
	}

	public Optional<List<List<ItemStack>>> workstations() {
		return Optional.ofNullable(workstations);
	}

	@NotNullByDefault
	public static class Serializer implements RecipeSerializer<CategoryMetadata> {
		public static final MapCodec<CategoryMetadata> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ExtraCodecs.nonEmptyList(KCodecs.compactList(ExtraCodecs.NON_EMPTY_STRING))
						.fieldOf("category")
						.forGetter(CategoryMetadata::category),
				UIElement.CODEC.optionalFieldOf("icon").forGetter(CategoryMetadata::icon),
				KCodecs.compactList(ItemStack.CODEC).listOf().optionalFieldOf("workstations").forGetter(CategoryMetadata::workstations)
		).apply(instance, CategoryMetadata::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, CategoryMetadata> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
				CategoryMetadata::category,
				ByteBufCodecs.optional(UIElement.STREAM_CODEC),
				CategoryMetadata::icon,
				ByteBufCodecs.optional(ItemStack.LIST_STREAM_CODEC.apply(ByteBufCodecs.list())),
				CategoryMetadata::workstations,
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
