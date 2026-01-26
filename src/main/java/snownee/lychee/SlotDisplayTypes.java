package snownee.lychee;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.compat.recipe_api.VisualOnlyComponentsIngredient;

public class SlotDisplayTypes {
	public static final SlotDisplay.Type<VisualOnlyComponentsIngredient.Display> VISUAL_ONLY = register(
			"visual_only",
			VisualOnlyComponentsIngredient.Display.CODEC,
			VisualOnlyComponentsIngredient.Display.STREAM_CODEC);

	public static <T extends SlotDisplay> SlotDisplay.Type<T> register(
			String name,
			MapCodec<T> codec,
			StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
		Identifier id = Lychee.id(name);
		return Registry.register(BuiltInRegistries.SLOT_DISPLAY, id, new SlotDisplay.Type<>(codec, streamCodec));
	}
}
