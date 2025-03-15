package snownee.lychee.util.category;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.kiwi.recipe.EmptyRecipeInput;
import snownee.kiwi.recipe.SimpleRecipe;
import snownee.kiwi.util.NotNullByDefault;
import snownee.kiwi.util.codec.KCodecs;

@NotNullByDefault
public class CategoryMetadata extends SimpleRecipe<EmptyRecipeInput> {
	private final List<ResourceLocation> categories;
	private final @Nullable UIElement icon;

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public CategoryMetadata(List<ResourceLocation> categories, Optional<UIElement> icon) {
		this.categories = categories;
		this.icon = icon.orElse(null);
	}

	@Override
	public boolean matches(EmptyRecipeInput input, Level level) {
		return false;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return null;
	}

	@Override
	public RecipeType<?> getType() {
		return null;
	}

	public List<ResourceLocation> categories() {
		return categories;
	}

	private Optional<UIElement> icon() {
		return Optional.ofNullable(icon);
	}

	@NotNullByDefault
	public static class Serializer implements RecipeSerializer<CategoryMetadata> {
		public static final MapCodec<CategoryMetadata> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				KCodecs.compactList(ResourceLocation.CODEC).fieldOf("categories").forGetter(CategoryMetadata::categories),
				UIElement.CODEC.optionalFieldOf("icon").forGetter(CategoryMetadata::icon)
		).apply(instance, CategoryMetadata::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, CategoryMetadata> STREAM_CODEC = null;

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
