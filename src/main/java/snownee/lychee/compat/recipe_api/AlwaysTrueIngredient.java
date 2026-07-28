package snownee.lychee.compat.recipe_api;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.TestBlock;
import net.minecraft.world.level.block.state.properties.TestBlockMode;
import snownee.kiwi.recipe.CustomIngredient;
import snownee.kiwi.recipe.CustomIngredientSerializer;
import snownee.lychee.Lychee;
import snownee.lychee.util.Displays;

public class AlwaysTrueIngredient implements CustomIngredient {
	public static final Identifier ID = Lychee.id("always_true");
	public static final CustomIngredientSerializer<AlwaysTrueIngredient> SERIALIZER = new Serializer();

	@Override
	public boolean test(ItemStack stack) {
		return true;
	}

	@Override
	public Stream<Holder<Item>> items() {
		return Stream.empty();
	}

	@Override
	public SlotDisplay display() {
		return Displays.slot(ItemStackTemplate.fromNonEmptyStack(TestBlock.setModeOnStack(
				new ItemStack(Items.TEST_BLOCK),
				TestBlockMode.ACCEPT)));
	}

	@Override
	public boolean requiresTesting() {
		return true;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	private static class Serializer implements CustomIngredientSerializer<AlwaysTrueIngredient> {
		private static final AlwaysTrueIngredient INSTANCE = new AlwaysTrueIngredient();
		public static final MapCodec<AlwaysTrueIngredient> CODEC = MapCodec.unit(INSTANCE);
		public static final StreamCodec<RegistryFriendlyByteBuf, AlwaysTrueIngredient> STREAM_CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public Identifier getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<AlwaysTrueIngredient> getCodec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, AlwaysTrueIngredient> getStreamCodec() {
			return STREAM_CODEC;
		}
	}
}
