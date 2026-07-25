package snownee.lychee.mixin.recipes.sculkspreading;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.SculkVeinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LootContextKeys;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;

@Mixin(SculkVeinBlock.class)
public abstract class SculkVeinBlockMixin extends MultifaceSpreadeableBlock implements SculkBehaviour {
	@Shadow
	@Final
	private MultifaceSpreader veinSpreader;

	public SculkVeinBlockMixin(Properties properties) {
		super(properties);
	}

	@Inject(method = "attemptUseCharge", at = @At("HEAD"), cancellable = true)
	public void attemptUseCharge(
			SculkSpreader.ChargeCursor cursor,
			LevelAccessor level,
			BlockPos originPos,
			RandomSource random,
			SculkSpreader spreader,
			boolean spreadVeins,
			CallbackInfoReturnable<Integer> cir) {
		if (spreader.isWorldGeneration() || !(level instanceof ServerLevel serverLevel)) {
			return;
		}
		BlockState state = level.getBlockState(cursor.getPos());
		for (Direction support : Direction.allShuffled(random)) {
			if (!hasFace(state, support)) {
				continue;
			}
			BlockPos supportPos = cursor.getPos().relative(support);
			BlockState supportState = level.getBlockState(supportPos);
			if (supportState.isAir()) {
				continue;
			}
			var context = new LycheeContext();
			context.put(LycheeContextKey.LEVEL, serverLevel);
			context.put(LycheeContextKey.RANDOM, random);
			var lootParams = context.initLootParams(RecipeTypes.SCULK_SPREADING);
			lootParams.set(LootContextParams.BLOCK_STATE, supportState);
			lootParams.set(LootContextParams.ORIGIN, Vec3.atCenterOf(supportPos));
			lootParams.set(LootContextKeys.BLOCK_POS, supportPos);
			var result = RecipeTypes.SCULK_SPREADING.process(serverLevel, supportState, context);
			if (result == null) {
				continue;
			}
			if (!result.getSecond().avoidDefault) {
				BlockState newBlock = level.getBlockState(supportPos);
				Block.pushEntitiesUp(supportState, newBlock, level, supportPos);
				level.playSound(null, supportPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
				veinSpreader.spreadAll(newBlock, level, supportPos, spreader.isWorldGeneration());

				for (Direction veinBlocks : DIRECTIONS) {
					if (veinBlocks != support.getOpposite()) {
						BlockPos veinPos = supportPos.relative(veinBlocks);
						BlockState possibleVeinBlock = level.getBlockState(veinPos);
						if (possibleVeinBlock.is(this)) {
							onDischarged(level, possibleVeinBlock, veinPos, random);
						}
					}
				}
			}
			int charge = result.getFirst().value().charge(random);
			cir.setReturnValue(cursor.getCharge() - charge);
			return;
		}
	}
}
