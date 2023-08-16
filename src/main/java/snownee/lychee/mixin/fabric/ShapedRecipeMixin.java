package snownee.lychee.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import snownee.lychee.util.CommonProxy;

@Mixin(value = ShapedRecipe.class, priority = 2000)
public class ShapedRecipeMixin {

	@Inject(method = "itemStackFromJson", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private static void lychee$itemStackFromJson(JsonObject jsonObject, CallbackInfoReturnable<ItemStack> cir, Item item, int count) {
		if (!jsonObject.has("lychee:tag")) {
			return;
		}
		CompoundTag tag = CommonProxy.jsonToTag(jsonObject.get("lychee:tag"));
		ItemStack itemStack = new ItemStack(item, count);
		itemStack.getOrCreateTag().merge(tag);
		cir.setReturnValue(itemStack);
	}
}
