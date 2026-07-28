package snownee.lychee.compat.kubejs;

import java.util.Objects;

import com.google.gson.JsonObject;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Recipe;
import snownee.kiwi.util.KUtil;
import snownee.lychee.datagen.LycheeBuilder;
import snownee.lychee.datagen.LycheeRecipeBuilder;

public class LycheeKubeJSUtils {
	public static JsonObject toJSON(Object obj) {
		if (obj instanceof String s) {
			Object o = KUtil.loadYaml(s, Object.class);
			return JavaOps.INSTANCE.convertTo(JsonOps.INSTANCE, o).getAsJsonObject();
		}
		if (obj instanceof LycheeRecipeBuilder<?, ?> recipeBuilder) {
			obj = recipeBuilder.build();
		}
		if (obj instanceof Recipe<?> recipe) {
			RegistryOps<Object> ops = Objects.requireNonNull(LycheeBuilder.registryOps.get());
			return ops.convertTo(JsonOps.INSTANCE, Recipe.CODEC.encodeStart(ops, recipe).getOrThrow()).getAsJsonObject();
		}
		throw new IllegalArgumentException("Unsupported type: " + obj);
	}
}
