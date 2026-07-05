package snownee.lychee;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.compat.recipe_api.VisualOnlyComponentsIngredient;
import snownee.lychee.util.Displays;

public class SlotDisplayTypes {
	public static final DeferredRegister<SlotDisplay.Type<?>> SLOT_DISPLAYS = DeferredRegister.create(BuiltInRegistries.SLOT_DISPLAY, Lychee.ID);
	public static final SlotDisplay.Type<VisualOnlyComponentsIngredient.Display> VISUAL_ONLY = register(
			"visual_only",
			VisualOnlyComponentsIngredient.Display.CODEC,
			VisualOnlyComponentsIngredient.Display.STREAM_CODEC);

	public static final SlotDisplay.Type<Displays.WithDamage> WITH_DAMAGE = register(
			"with_damage",
			Displays.WithDamage.CODEC,
			Displays.WithDamage.STREAM_CODEC);

	public static <T extends SlotDisplay> SlotDisplay.Type<T> register(
			String name,
			MapCodec<T> codec,
			StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
		Identifier id = Lychee.id(name);
		SlotDisplay.Type<T> type = new SlotDisplay.Type<>(codec, streamCodec);
		SLOT_DISPLAYS.register(name, () -> type);
		return type;
	}
}
