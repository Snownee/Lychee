package snownee.lychee.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import snownee.lychee.LycheeTags;
import snownee.lychee.core.recipe.recipe.LycheeCounter;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements LycheeCounter {

	private ResourceLocation lychee$recipeId;
	private int lychee$count;

	@Inject(at = @At("HEAD"), method = "fireImmune", cancellable = true)
	private void lychee_fireImmune(CallbackInfoReturnable<Boolean> ci) {
		if (((ItemEntity) (Object) this).getItem().is(LycheeTags.FIRE_IMMUNE)) {
			ci.setReturnValue(true);
		}
	}

	@Override
	public void lychee$setRecipeId(@Nullable ResourceLocation id) {
		lychee$recipeId = id;
	}

	@Override
	public @Nullable ResourceLocation lychee$getRecipeId() {
		return lychee$recipeId;
	}

	@Override
	public void lychee$setCount(int count) {
		lychee$count = count;
	}

	@Override
	public int lychee$getCount() {
		return lychee$count;
	}

}
