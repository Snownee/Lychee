package snownee.lychee.mixin.recipes.blockclicking;

import java.util.Objects;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import snownee.lychee.recipes.BlockClickingRecipe;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
	private void continueDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (BlockClickingRecipe.isBreakProtected(Objects.requireNonNull(minecraft.player), Objects.requireNonNull(minecraft.level), pos)) {
			cir.setReturnValue(false);
		}
	}
}
