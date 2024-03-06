package snownee.lychee.util.context;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.JsonContext;
import snownee.lychee.context.RecipeContext;
import snownee.lychee.util.SerializableType;

public interface LycheeContextSerializer<T> extends SerializableType<T> {
	LycheeContextSerializer<ActionContext> ACTION = register(LycheeContextKey.ACTION, new ActionContext.Serializer());
	LycheeContextSerializer<JsonContext> JSON = register(LycheeContextKey.JSON, new JsonContext.Serializer());
	LycheeContextSerializer<RecipeContext> RECIPE_ID = register(LycheeContextKey.RECIPE_ID, new RecipeContext.Serializer());

	static <T extends LycheeContextSerializer<?>> T register(ResourceLocation location, T object) {
		Registry.register(LycheeRegistries.CONTEXT_SERIALIZER, location, object);
		return object;
	}

	static <T extends LycheeContextSerializer<?>, K extends LycheeContextKey<?>> T register(K key, T object) {
		return register(LycheeRegistries.CONTEXT.getKey(key), object);
	}
}
