package snownee.lychee.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

@Mixin(LootParams.Builder.class)
public interface LootParamsBuilderAccess {

	@Accessor
	Map<LootContextParam<?>, Object> getParams();

}
