package snownee.lychee.util.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import snownee.kiwi.recipe.EmptyRecipeInput;
import snownee.kiwi.recipe.SimpleRecipe;
import snownee.lychee.util.Patterns;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class CategorySettingRecipe extends SimpleRecipe<EmptyRecipeInput> implements Comparable<CategorySettingRecipe> {
	public static final Codec<Map<String, List<UIElement>>> ELEMENTS_CODEC = ExtraCodecs.strictUnboundedMap(
			ExtraCodecs.NON_EMPTY_STRING,
			ExtraCodecs.compactListCodec(UIElement.CODEC));
	public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Map<String, List<UIElement>>>> ELEMENTS_STREAM_CODEC = ByteBufCodecs.optional(
			ByteBufCodecs.map(
					HashMap::newHashMap,
					ByteBufCodecs.STRING_UTF8,
					UIElement.STREAM_CODEC.apply(ByteBufCodecs.list())));

	private final int sortOrder;
	private final Patterns category;
	private final Optional<Map<String, List<UIElement>>> elements;
	private final boolean renderDefault;

	public CategorySettingRecipe() {
		sortOrder = 0;
		category = Patterns.EMPTY;
		elements = Optional.empty();
		renderDefault = true;
	}

	public CategorySettingRecipe(int sortOrder, Patterns category, Optional<Map<String, List<UIElement>>> elements, boolean renderDefault) {
		this.sortOrder = sortOrder;
		this.category = category;
		this.elements = elements;
		this.renderDefault = renderDefault;
	}

	@Override
	public boolean matches(EmptyRecipeInput input, Level level) {
		return false;
	}

	public int sortOrder() {
		return sortOrder;
	}

	public Patterns category() {
		return category;
	}

	public Optional<Map<String, List<UIElement>>> elements() {
		return elements;
	}

	public boolean renderDefault() {
		return renderDefault;
	}

	@Override
	public int compareTo(CategorySettingRecipe o) {
		return Integer.compare(sortOrder, o.sortOrder);
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public String group() {
		return "";
	}
}
