package snownee.lychee.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.google.common.collect.BiMap;

import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

@Mixin(LootContextParamSets.class)
public interface LootContextParamSetsAccess {

	@Invoker
	static ContextKeySet callRegister(
			String pRegistryName,
			Consumer<ContextKeySet.Builder> pBuilderConsumer
	) {
		throw new IllegalStateException();
	}

	@Accessor("REGISTRY")
	static BiMap<Identifier, ContextKeySet> registry() {
		throw new IllegalStateException();
	}
}
