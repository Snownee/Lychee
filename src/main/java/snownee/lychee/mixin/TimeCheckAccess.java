package snownee.lychee.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;

@Mixin(TimeCheck.class)
public interface TimeCheckAccess {

	@Accessor
	@Nullable
	Long getPeriod();

	@Accessor
	IntRange getValue();

}
