package snownee.lychee.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.Recipe;
import snownee.lychee.LycheeTags;
import snownee.lychee.util.LycheeCounter;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements LycheeCounter {
	@Unique
	private @Nullable ResourceKey<Recipe<?>> recipeId;
	@Unique
	private int count;

	@Inject(at = @At("HEAD"), method = "fireImmune", cancellable = true)
	private void lychee_fireImmune(CallbackInfoReturnable<Boolean> ci) {
		if (((ItemEntity) (Object) this).getItem().is(LycheeTags.FIRE_IMMUNE)) {
			ci.setReturnValue(true);
		}
	}

	@Override
	public void lychee$setRecipeId(@Nullable ResourceKey<Recipe<?>> id) {
		recipeId = id;
	}

	@Override
	public @Nullable ResourceKey<Recipe<?>> lychee$getRecipeId() {
		return recipeId;
	}

	@Override
	public void lychee$setCount(int count) {
		this.count = count;
	}

	@Override
	public int lychee$getCount() {
		return count;
	}

}
