package snownee.lychee.util.context;

import java.util.Objects;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.context.ActionContext;

public interface LycheeContextSerializers {
	static void init() {
		register(LycheeContextKey.ACTION, ActionContext.CODEC);
		register(LycheeContextKey.JSON, ExtraCodecs.JSON);
		register(LycheeContextKey.RECIPE_ID, ResourceLocation.CODEC);
	}

	static <T> Codec<T> register(ResourceLocation location, Codec<T> object) {
		Registry.register(LycheeRegistries.CONTEXT_SERIALIZER, location, object);
		return object;
	}

	static <T> Codec<T> register(LycheeContextKey<T> key, Codec<T> codec) {
		return register(Objects.requireNonNull(LycheeRegistries.CONTEXT.getKey(key)), codec);
	}
}
