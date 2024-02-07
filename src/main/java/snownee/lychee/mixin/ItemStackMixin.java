package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.item.ItemStack;
import snownee.lychee.util.codec.TweakedItemStackResultCodec;

@Mixin(value = ItemStack.class, priority = 5000)
public class ItemStackMixin {
	@Final
	@Shadow
	@Mutable
	public static MapCodec<ItemStack> RESULT_CODEC;

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void lychee_clinit(CallbackInfo ci) {
		RESULT_CODEC = new TweakedItemStackResultCodec(RESULT_CODEC);
	}
}
