package snownee.lychee.contextual;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Chance(float chance) implements ContextualCondition {
	@Override
	public ContextualConditionType<Chance> type() {
		return ContextualConditionType.CHANCE;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		int n = 0;
		for (int i = 0; i < times; i++) {
			if (ctx.get(LycheeContextKey.RANDOM).nextFloat() < chance) {
				++n;
			}
		}
		return n;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = getDescriptionId(inverted);
		return Component.translatable(key, CommonProxy.white(CommonProxy.chance(chance)));
	}

	public static class Type implements ContextualConditionType<Chance> {
		public static final Codec<Chance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ExtraCodecs.validate(Codec.FLOAT, f -> {
					if (f <= 0 || f >= 1) {
						return DataResult.error(() -> "Chance must be between 0 and 1, exclusive");
					}
					return DataResult.success(f);
				}).fieldOf("chance").forGetter(Chance::chance)
		).apply(instance, Chance::new));

		@Override
		public @NotNull Codec<Chance> codec() {
			return CODEC;
		}
	}
}
