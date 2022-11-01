package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.block_crushing.LycheeFallingBlockEntity;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity implements LycheeFallingBlockEntity {

	public FallingBlockEntityMixin(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Shadow
	boolean cancelDrop;
	@Shadow
	BlockState blockState;
	boolean lychee$matched;
	float lychee$anvilDamageChance = -1;

	@Inject(at = @At("HEAD"), method = "causeFallDamage")
	private void lychee_causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource, CallbackInfoReturnable<Boolean> ci) {
		FallingBlockEntity entity = (FallingBlockEntity) (Object) this;
		if (entity.level.isClientSide) {
			return;
		}
		RecipeTypes.BLOCK_CRUSHING.process(entity);
	}

	@ModifyVariable(at = @At("STORE"), method = "tick", index = 9)
	private boolean lychee_modifyFlag3(boolean original) {
		if (lychee$matched) {
			return false;
		}
		return original;
	}

	@ModifyVariable(at = @At("STORE"), method = "causeFallDamage", index = 8)
	private boolean lychee_overrideDamageAnvil(boolean original) {
		if (original && lychee$anvilDamageChance >= 0) {
			if (random.nextFloat() < lychee$anvilDamageChance) {
				BlockState blockstate = AnvilBlock.damage(blockState);
				if (blockstate == null) {
					cancelDrop = true;
				} else {
					blockState = blockstate;
				}
			}
			return false;
		}
		return original;
	}

	@Override
	public void lychee$cancelDrop() {
		cancelDrop = true;
	}

	@Override
	public void lychee$matched() {
		lychee$matched = true;
	}

	@Override
	public void lychee$anvilDamageChance(float chance) {
		lychee$anvilDamageChance = Math.max(chance, lychee$anvilDamageChance);
	}

}
