package snownee.lychee.mixin.recipes.randomblockticking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.RandomlyTickable;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;

@Mixin(BlockStateBase.class)
public class BlockStateMixin {

	@WrapOperation(
			method = "initCache", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/Block;isRandomlyTicking(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private boolean initCache(Block block, BlockState blockState, Operation<Boolean> original) {
		return original.call(block, blockState) || ((RandomlyTickable) block).lychee$isTickable(blockState);
	}

	@Inject(at = @At("HEAD"), method = "randomTick", cancellable = true)
	private void randomTick(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
		@SuppressWarnings("DataFlowIssue") var blockState = (BlockState) (Object) this;

		var block = (RandomlyTickable) blockState.getBlock();
		if (block.lychee$isTickable(blockState)) {
			var context = new LycheeContext();
			context.put(LycheeContextKey.LEVEL, serverLevel);
			context.put(LycheeContextKey.RANDOM, randomSource);
			var lootParams = context.initLootParams(RecipeTypes.RANDOM_BLOCK_TICKING);
			lootParams.set(LootContextParams.BLOCK_STATE, blockState);
			lootParams.set(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos));
			lootParams.set(LycheeLootContextParams.BLOCK_POS, blockPos);
			final var recipe = RecipeTypes.RANDOM_BLOCK_TICKING.process(serverLevel, blockState, context);
			if (recipe != null && context.get(LycheeContextKey.ACTION).avoidDefault) {
				ci.cancel();
			}
		}
	}
}
