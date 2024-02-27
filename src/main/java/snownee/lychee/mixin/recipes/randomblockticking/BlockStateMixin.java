package snownee.lychee.mixin.recipes.randomblockticking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.base.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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

	@Inject(at = @At("HEAD"), method = "randomTick", cancellable = true)
	private void randomTick(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
		@SuppressWarnings("DataFlowIssue") var state = (BlockState) (Object) this;

		var block = (RandomlyTickable) state.getBlock();
		if (block.lychee$isTickable()) {
			final Supplier<LycheeContext> ctxSupplier = () -> {
				var context = new LycheeContext();
				context.put(LycheeContextKey.LEVEL, serverLevel);
				context.put(LycheeContextKey.RANDOM, randomSource);
				var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
				lootParamsContext.setParam(LootContextParams.BLOCK_STATE, state);
				lootParamsContext.setParam(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos));
				lootParamsContext.setParam(LycheeLootContextParams.BLOCK_POS, blockPos);
				return context;
			};
			final var result = RecipeTypes.RANDOM_BLOCK_TICKING.process(serverLevel, state, ctxSupplier);
			if (result != null && result.getFirst().get(LycheeContextKey.ACTION).avoidDefault) {
				ci.cancel();
			}
		}
	}
}
