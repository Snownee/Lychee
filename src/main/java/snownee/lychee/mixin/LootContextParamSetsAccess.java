package snownee.lychee.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.google.common.collect.BiMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

@Mixin(LootContextParamSets.class)
public interface LootContextParamSetsAccess {

	@Invoker
	static LootContextParamSet callRegister(
			String pRegistryName,
			Consumer<LootContextParamSet.Builder> pBuilderConsumer
	) {
		throw new IllegalStateException();
	}

	@Accessor("REGISTRY")
	static BiMap<ResourceLocation, LootContextParamSet> registry() {
		throw new IllegalStateException();
	}
}
