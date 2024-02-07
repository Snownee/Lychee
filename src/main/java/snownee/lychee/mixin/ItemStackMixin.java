package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.item.ItemStack;
import snownee.lychee.util.codec.TweakedItemStackCodec;
import snownee.lychee.util.codec.TweakedItemStackMapCodec;

@Mixin(value = ItemStack.class, priority = 5000)
public class ItemStackMixin {
	@Mutable
	@Shadow
	@Final
	public static MapCodec<ItemStack> RESULT_CODEC;

	@Mutable
	@Shadow
	@Final
	public static Codec<ItemStack> ITEM_WITH_COUNT_CODEC;

	@Mutable
	@Shadow
	@Final
	public static Codec<ItemStack> SINGLE_ITEM_CODEC;

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void lychee_clinit(CallbackInfo ci) {
		ITEM_WITH_COUNT_CODEC = new TweakedItemStackCodec(ITEM_WITH_COUNT_CODEC);
		SINGLE_ITEM_CODEC = new TweakedItemStackCodec(SINGLE_ITEM_CODEC);
		RESULT_CODEC = new TweakedItemStackMapCodec(RESULT_CODEC);
	}
}
