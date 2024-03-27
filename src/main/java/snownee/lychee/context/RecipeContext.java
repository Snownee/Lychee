package snownee.lychee.context;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import snownee.lychee.util.SerializableType;
import snownee.lychee.util.context.KeyedContextValue;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.context.LycheeContextSerializer;

public record RecipeContext(ResourceLocation id) implements KeyedContextValue<RecipeContext> {

	@Override
	public LycheeContextKey<RecipeContext> key() {
		return LycheeContextKey.RECIPE_ID;
	}

	public static final class Serializer implements LycheeContextSerializer<RecipeContext>, SerializableType<RecipeContext> {
		public static final Codec<RecipeContext> CODEC = ResourceLocation.CODEC.xmap(RecipeContext::new, RecipeContext::id);

		@Override
		public @NotNull Codec<RecipeContext> codec() {
			return CODEC;
		}
	}
}
