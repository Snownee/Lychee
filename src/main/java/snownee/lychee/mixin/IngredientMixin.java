package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import snownee.lychee.util.CommonProxy;

@Mixin(value = Ingredient.class, priority = 2000)
public class IngredientMixin {

	@Inject(
			method = "valueFromJson",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/crafting/ShapedRecipe;itemFromJson" +
							 "(Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/Item;"
			),
			cancellable = true
	)
	private static void lychee$itemStackFromJson(JsonObject jsonObject, CallbackInfoReturnable<Ingredient.Value> cir) {
		if (!jsonObject.has("lychee:tag")) {
			return;
		}
		CompoundTag tag = CommonProxy.jsonToTag(jsonObject.get("lychee:tag"));
		Item item = ShapedRecipe.itemFromJson(jsonObject);
		ItemStack itemStack = new ItemStack(item);
		itemStack.getOrCreateTag().merge(tag);
		cir.setReturnValue(new Ingredient.ItemValue(itemStack));
	}

}
