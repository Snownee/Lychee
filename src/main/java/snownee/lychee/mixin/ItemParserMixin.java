package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.StringReader;

import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import snownee.lychee.util.codec.LycheeCodecs;

@Mixin(ItemParser.class)
public class ItemParserMixin {
	@Inject(method = "validateComponents", at = @At("HEAD"), cancellable = true)
	private static void lychee_validateComponents(StringReader reader, Holder<Item> item, DataComponentPatch components, CallbackInfo ci) {
		if (LycheeCodecs.skipComponentsValidation.get() != null) {
			ci.cancel();
		}
	}
}
