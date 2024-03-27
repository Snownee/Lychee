package snownee.lychee.contextual;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record CheckParam(String key) implements ContextualCondition {
	@Override
	public ContextualConditionType<CheckParam> type() {
		return ContextualConditionType.CHECK_PARAM;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = getDescriptionId(inverted) + ".has";
		return Component.translatable(key, CommonProxy.white(key));
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		final var lootParamsContext = ctx.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.initBlockEntityParam();
		for (LootContextParam<?> param : lootParamsContext.params().keySet()) {
			if (key.equals(param.getName().getPath()) || key.equals(param.getName().toString())) {
				return times;
			}
		}
		return 0;
	}

	public static class Type implements ContextualConditionType<CheckParam> {
		public static final Codec<CheckParam> CODEC =
				RecordCodecBuilder.create(instance -> instance
						.group(Codec.STRING.fieldOf("key").forGetter(CheckParam::key))
						.apply(instance, CheckParam::new));

		@Override
		public @NotNull Codec<CheckParam> codec() {
			return CODEC;
		}
	}
}
