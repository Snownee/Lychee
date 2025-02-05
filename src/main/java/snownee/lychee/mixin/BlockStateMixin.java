package snownee.lychee.mixin;

import java.util.function.Supplier;

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
import snownee.lychee.core.LycheeContext;
import snownee.lychee.dripstone_dripping.DripstoneRecipe;
import snownee.lychee.random_block_ticking.RandomlyTickable;
import snownee.lychee.util.CommonProxy;

@Mixin(BlockStateBase.class)
public class BlockStateMixin {
	/**
	 * Some implementation for {@link Block} overriding the `isRandomlyTicking`.
	 */
	@WrapOperation(
			method = "initCache",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;isRandomlyTicking(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private boolean initCache(Block block, BlockState blockState, Operation<Boolean> original) {
		return original.call(block, blockState) || ((RandomlyTickable) block).lychee$isTickable(blockState);
	}

	@Inject(at = @At("HEAD"), method = "randomTick", cancellable = true)
	private void randomTick(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
		BlockState state = (BlockState) (Object) this;

		RandomlyTickable block = (RandomlyTickable) state.getBlock();
		if (block.lychee$isTickable(state)) {
			Supplier<LycheeContext> ctxSupplier = () -> {
				var builder = new LycheeContext.Builder<>(serverLevel);
				builder.withRandom(randomSource);
				builder.withParameter(LootContextParams.BLOCK_STATE, state);
				builder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos));
				builder.withParameter(LycheeLootContextParams.BLOCK_POS, blockPos);
				return builder.create(RecipeTypes.RANDOM_BLOCK_TICKING.contextParamSet);
			};
			var result = RecipeTypes.RANDOM_BLOCK_TICKING.process(serverLevel, state, ctxSupplier);
			if (result != null && !result.getFirst().runtime.doDefault) {
				ci.cancel();
			}
		}

		if (CommonProxy.hasDFLib && DripstoneRecipe.safeTick(state, serverLevel, blockPos, randomSource)) {
			ci.cancel();
		}
	}

}
