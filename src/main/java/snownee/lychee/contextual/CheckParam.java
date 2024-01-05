package snownee.lychee.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;

public record CheckParam(String key) implements ContextualCondition<CheckParam> {
	@Override
	public ContextualConditionType<CheckParam> type() {
		return ContextualConditionTypes.CHECK_PARAM;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = getDescriptionId(inverted) + ".has";
		return Component.translatable(key, CommonProxy.white(key));
	}

	@Override
	public int test(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		ctx.initBlockEntityParam();
		for (LootContextParam<?> param : ctx.params().keySet()) {
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
		public Codec<CheckParam> codec() {
			return CODEC;
		}
	}
}
