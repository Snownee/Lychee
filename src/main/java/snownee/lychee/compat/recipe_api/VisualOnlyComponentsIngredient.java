package snownee.lychee.compat.recipe_api;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.kiwi.recipe.CustomIngredient;
import snownee.kiwi.recipe.CustomIngredientSerializer;
import snownee.lychee.Lychee;
import snownee.lychee.SlotDisplayTypes;

public class VisualOnlyComponentsIngredient implements CustomIngredient {
	public static final Identifier ID = Lychee.id("visual_only_components");
	public static final CustomIngredientSerializer<VisualOnlyComponentsIngredient> SERIALIZER = new Serializer();

	private final Ingredient base;
	private final DataComponentPatch components;

	public VisualOnlyComponentsIngredient(Ingredient base, DataComponentPatch components) {
		if (components.isEmpty()) {
			throw new IllegalArgumentException("ComponentIngredient must have at least one defined component");
		}

		this.base = base;
		this.components = components;
	}

	@Override
	public boolean test(ItemStack stack) {
		return base.test(stack);
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		return base.items()
				.map(holder -> new ItemStack(holder.value()))
				.map(stack -> {
					ItemStack copy = stack.copy();
					copy.applyComponents(components);
					return copy;
				})
				.filter(base::test)
				.toList();
	}

	@Override
	public boolean requiresTesting() {
		return !base.isSimple();
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	private Ingredient getBase() {
		return base;
	}

	private DataComponentPatch getComponents() {
		return components;
	}

	private static class Serializer implements CustomIngredientSerializer<VisualOnlyComponentsIngredient> {
		private static final MapCodec<VisualOnlyComponentsIngredient> CODEC = createCodec(Ingredient.CODEC);
		private static final StreamCodec<RegistryFriendlyByteBuf, VisualOnlyComponentsIngredient> STREAM_CODEC = StreamCodec.composite(
				Ingredient.CONTENTS_STREAM_CODEC, VisualOnlyComponentsIngredient::getBase,
				DataComponentPatch.STREAM_CODEC, VisualOnlyComponentsIngredient::getComponents,
				VisualOnlyComponentsIngredient::new);

		private static MapCodec<VisualOnlyComponentsIngredient> createCodec(Codec<Ingredient> ingredientCodec) {
			return RecordCodecBuilder.mapCodec(instance ->
					instance.group(
							ingredientCodec.fieldOf("base").forGetter(VisualOnlyComponentsIngredient::getBase),
							DataComponentPatch.CODEC.fieldOf("components").forGetter(VisualOnlyComponentsIngredient::getComponents)
					).apply(instance, VisualOnlyComponentsIngredient::new)
			);
		}

		@Override
		public Identifier getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<VisualOnlyComponentsIngredient> getCodec(boolean allowEmpty) {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, VisualOnlyComponentsIngredient> getPacketCodec() {
			return STREAM_CODEC;
		}
	}

	public record Display(SlotDisplay base, DataComponentPatch components) implements SlotDisplay {
		public static final MapCodec<Display> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				SlotDisplay.CODEC.fieldOf("base").forGetter(Display::base),
				DataComponentPatch.CODEC.fieldOf("components").forGetter(Display::components)
		).apply(instance, Display::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, Display> STREAM_CODEC = StreamCodec.composite(
				SlotDisplay.STREAM_CODEC, Display::base,
				DataComponentPatch.STREAM_CODEC, Display::components,
				Display::new);

		@Override
		public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> builder) {
			if (!(builder instanceof DisplayContentsFactory.ForStacks<T> stacks)) {
				return Stream.empty();
			}
			List<ItemStack> itemStacks = base.resolveForStacks(context);
			return itemStacks.isEmpty() ?
					Stream.empty() :
					itemStacks.stream().peek($ -> $.applyComponents(components)).map(stacks::forStack);
		}

		@Override
		public boolean isEnabled(FeatureFlagSet enabledFeatures) {
			return base.isEnabled(enabledFeatures);
		}

		@Override
		public SlotDisplay.Type<? extends SlotDisplay> type() {
			return SlotDisplayTypes.VISUAL_ONLY;
		}
	}
}
