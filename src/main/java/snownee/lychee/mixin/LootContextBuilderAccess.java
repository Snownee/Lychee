package snownee.lychee.mixin;

import java.util.Map;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

@Mixin(LootContext.Builder.class)
public interface LootContextBuilderAccess {

	@Accessor
	Map<LootContextParam<?>, Object> getParams();

	@Accessor
	Random getRandom();

}