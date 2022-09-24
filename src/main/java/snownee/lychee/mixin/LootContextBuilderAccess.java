package snownee.lychee.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

@Mixin(LootContext.Builder.class)
public interface LootContextBuilderAccess {

	@Accessor
	Map<LootContextParam<?>, Object> getParams();

	@Accessor
	RandomSource getRandom();

}
