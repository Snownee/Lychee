package snownee.lychee.mixin.recipes.blockclicking;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import snownee.lychee.recipes.BlockClickingRecipe;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	private BlockPos destroyBlockPos;
	@Shadow
	private boolean isDestroying;

	@Inject(method = "stopDestroyBlock", at = @At("HEAD"))
	private void stopDestroyBlock(CallbackInfo ci) {
		if (!isDestroying || destroyBlockPos == null || minecraft.player == null || minecraft.level == null) {
			return;
		}
		var direction = Direction.DOWN;
		if (minecraft.hitResult instanceof BlockHitResult blockHitResult) {
			direction = blockHitResult.getDirection();
		}
		BlockClickingRecipe.invoke(minecraft.player, minecraft.level, InteractionHand.MAIN_HAND, destroyBlockPos, direction, BlockClickingRecipe.Action.ABORT);
	}
}