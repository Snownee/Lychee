package snownee.lychee.compat.recipe_api;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.recipe.CustomIngredient;
import snownee.kiwi.recipe.CustomIngredientSerializer;
import snownee.lychee.Lychee;

public class VisualOnlyComponentsIngredient implements CustomIngredient {
	public static final ResourceLocation ID = Lychee.id("visual_only_components");
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
		return !base.isSimple();
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
		private static final MapCodec<VisualOnlyComponentsIngredient> ALLOW_EMPTY_CODEC = createCodec(Ingredient.CODEC);
		private static final MapCodec<VisualOnlyComponentsIngredient> DISALLOW_EMPTY_CODEC = createCodec(Ingredient.CODEC_NONEMPTY);
		private static final StreamCodec<RegistryFriendlyByteBuf, VisualOnlyComponentsIngredient> PACKET_CODEC = StreamCodec.composite(
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
		public ResourceLocation getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<VisualOnlyComponentsIngredient> getCodec(boolean allowEmpty) {
			return allowEmpty ? ALLOW_EMPTY_CODEC : DISALLOW_EMPTY_CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, VisualOnlyComponentsIngredient> getPacketCodec() {
			return PACKET_CODEC;
		}
	}
}
