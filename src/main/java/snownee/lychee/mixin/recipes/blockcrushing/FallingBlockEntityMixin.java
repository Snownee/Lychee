package snownee.lychee.mixin.recipes.blockcrushing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.LycheeFallingBlockEntity;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity implements LycheeFallingBlockEntity {

	@Unique
	private boolean matched;
	@Unique
	private float anvilDamageChance = -1;
	@Shadow
	private boolean cancelDrop;
	@Shadow
	private BlockState blockState;

	public FallingBlockEntityMixin(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(at = @At("HEAD"), method = "causeFallDamage")
	private void lychee_onLand(
			float pFallDistance,
			float pMultiplier,
			DamageSource pSource,
			CallbackInfoReturnable<Boolean> ci
	) {
		final var entity = (FallingBlockEntity) (Object) this;
		if (entity.level().isClientSide) {
			return;
		}
		RecipeTypes.BLOCK_CRUSHING.process(entity);
	}

	@WrapOperation(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/FallingBlock;isFree(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private boolean lychee_stopItHere(BlockState state, Operation<Boolean> original) {
		if (matched) {
			return false;
		}
		return original.call(state);
	}

	@Inject(
			method = "causeFallDamage",
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z",
					shift = At.Shift.AFTER))
	private void lychee_customDamageAnvilChance(
			float fallDistance,
			float multiplier,
			DamageSource source,
			CallbackInfoReturnable<Boolean> cir,
			@Local LocalBooleanRef bl) {
		if (bl.get() && anvilDamageChance >= 0) {
			if (random.nextFloat() < anvilDamageChance) {
				BlockState blockstate = AnvilBlock.damage(blockState);
				if (blockstate == null) {
					cancelDrop = true;
				} else {
					blockState = blockstate;
				}
			}
			bl.set(false);
		}
	}

	@Override
	public void lychee$cancelDrop() {
		cancelDrop = true;
	}

	@Override
	public void lychee$matched() {
		matched = true;
	}

	@Override
	public void lychee$anvilDamageChance(float chance) {
		anvilDamageChance = Math.max(chance, anvilDamageChance);
	}

}
