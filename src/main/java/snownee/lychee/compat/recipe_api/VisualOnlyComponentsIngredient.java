package snownee.lychee.compat.recipe_api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.Lychee;

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
	public Stream<Holder<Item>> items() {
		return Stream.empty();
	}

	@Override
	public SlotDisplay display() {
		return base.display(); //FIXME
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		List<ItemStack> stacks = new ArrayList<>(List.of(base.getItems()));
		stacks.replaceAll(stack -> {
			ItemStack copy = stack.copy();

			stack.applyComponents(components);

			return copy;
		});
		stacks.removeIf(stack -> !base.test(stack));
		return stacks;
	}

	@Override
	public boolean requiresTesting() {
		// TODO Fabric recipe api interface injection isn't working now
		return ((FabricIngredient) (Object) base).requiresTesting();
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	private Ingredient getBase() {
		return base;
	}

	@Nullable
	private DataComponentPatch getComponents() {
		return components;
	}

	private static class Serializer implements CustomIngredientSerializer<VisualOnlyComponentsIngredient> {
		private static final MapCodec<VisualOnlyComponentsIngredient> CODEC = createCodec(Ingredient.CODEC);
		private static final StreamCodec<RegistryFriendlyByteBuf, VisualOnlyComponentsIngredient> STREAM_CODEC = StreamCodec.composite(
				Ingredient.CONTENTS_STREAM_CODEC, VisualOnlyComponentsIngredient::getBase,
				DataComponentPatch.STREAM_CODEC, VisualOnlyComponentsIngredient::getComponents,
				VisualOnlyComponentsIngredient::new
		);

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
		public MapCodec<VisualOnlyComponentsIngredient> getCodec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, VisualOnlyComponentsIngredient> getStreamCodec() {
			return STREAM_CODEC;
		}
	}
}
