package snownee.lychee.mixin.fabric;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import snownee.lychee.recipes.BlockClickingRecipe;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Shadow
	@Final
	protected ServerPlayer player;
	@Shadow
	protected ServerLevel level;

	@Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
	private void handleBlockBreakAction(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
		if (action != ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
			return;
		}
		if (direction == null) {
			direction = Direction.DOWN;
		}
		BlockClickingRecipe.invoke(player, level, InteractionHand.MAIN_HAND, pos, direction, BlockClickingRecipe.Action.ABORT);
	}
}