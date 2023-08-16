package snownee.lychee.mixin.forge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import snownee.lychee.util.CommonProxy;

@Mixin(value = ShapedRecipe.class, priority = 2000)
public class ShapedRecipeMixin {

	@Inject(method = "itemStackFromJson", at = @At("HEAD"), cancellable = true)
	private static void lychee$itemStackFromJson(JsonObject jsonObject, CallbackInfoReturnable<ItemStack> cir) {
		if (!jsonObject.has("lychee:tag")) {
			return;
		}
		CompoundTag tag = CommonProxy.jsonToTag(jsonObject.get("lychee:tag"));
		ItemStack itemStack = CraftingHelper.getItemStack(jsonObject, true, false);
		itemStack.getOrCreateTag().merge(tag);
		cir.setReturnValue(itemStack);
	}
}
