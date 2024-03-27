package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;

@Mixin(DamageSources.class)
public interface DamageSourcesAccess {
	@Invoker
	DamageSource callSource(ResourceKey<DamageType> damageTypeKey);
}
