package snownee.lychee.util.context;

import java.util.Objects;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.LycheeRegistries;

public interface LycheeContextSerializers {
	static void init() {
		register(LycheeContextKey.JSON, ExtraCodecs.JSON);
		register(LycheeContextKey.RECIPE_ID, ResourceKey.codec(Registries.RECIPE));
		register(LycheeContextKey.DRIPSTONE_SOURCE, BlockState.CODEC);
		register(LycheeContextKey.SMALL_EXPLOSION, Codec.BOOL);
	}

	static <T> Codec<T> register(Identifier location, Codec<T> object) {
		Registry.register(LycheeRegistries.CONTEXT_SERIALIZER, location, object);
		return object;
	}

	static <T> Codec<T> register(LycheeContextKey<T> key, Codec<T> codec) {
		return register(Objects.requireNonNull(LycheeRegistries.CONTEXT.getKey(key)), codec);
	}
}
