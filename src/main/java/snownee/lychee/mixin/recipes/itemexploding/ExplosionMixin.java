package snownee.lychee.mixin.recipes.itemexploding;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import snownee.lychee.recipes.ItemExplodingRecipe;

@Mixin(value = Explosion.class, priority = 700)
public abstract class ExplosionMixin {
	@Shadow
	@Final
	private Level level;

	@Shadow
	@Final
	private double x;

	@Shadow
	@Final
	private double y;

	@Shadow
	@Final
	private double z;

	@Shadow
	public abstract float radius();

	@Inject(at = @At("TAIL"), method = "explode()V")
	private void lychee_explode(final CallbackInfo ci, @Local List<Entity> list) {
		ItemExplodingRecipe.invoke((ServerLevel) level, x, y, z, list, radius());
	}
}
