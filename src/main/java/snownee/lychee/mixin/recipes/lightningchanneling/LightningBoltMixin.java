package snownee.lychee.mixin.recipes.lightningchanneling;

import java.util.Collection;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import snownee.lychee.recipes.LightningChannelingRecipe;

@Mixin(LightningBolt.class)
public class LightningBoltMixin {

	@Shadow
	private int life;

	@ModifyArg(at = @At(value = "INVOKE", target = "Ljava/util/Set;addAll(Ljava/util/Collection;)Z"), method = "tick")
	private <E> Collection<? extends E> lychee_tick(final Collection<? extends E> entities) {
		if (!entities.isEmpty() || life == 0) {
			LightningChannelingRecipe.invoke((LightningBolt) (Object) this, (List<Entity>) entities);
		}
		return entities;
	}
}
