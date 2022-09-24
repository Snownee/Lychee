package snownee.lychee.core.contextual;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.IntBoundsHelper;
import snownee.lychee.core.def.TimeCheckHelper;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.mixin.IntRangeAccess;
import snownee.lychee.mixin.TimeCheckAccess;
import snownee.lychee.util.LUtil;

public record Time(Ints value, @Nullable Long period) implements ContextualCondition {

	@Override
	public ContextualConditionType<? extends ContextualCondition> getType() {
		return ContextualConditionTypes.TIME;
	}

	@Override
	public int test(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return test(ctx.getLevel()) ? times : 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public InteractionResult testInTooltips() {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return InteractionResult.PASS;
		}
		return LUtil.interactionResult(test(level));
	}

	public boolean test(Level level) {
		long i = level.getDayTime();
		if (period != null) {
			i %= period;
		}
		return value.matches((int) i);
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(makeDescriptionId(inverted));
	}

	public static class Type extends ContextualConditionType<Time> {

		@Override
		public Time fromJson(JsonObject o) {
			TimeCheckAccess access = (TimeCheckAccess) TimeCheckHelper.fromJson(o);
			return new Time(IntBoundsHelper.fromIntRange((IntRangeAccess) access.getValue()), access.getPeriod());
		}

		@Override
		public Time fromNetwork(FriendlyByteBuf buf) {
			Ints range = IntBoundsHelper.fromNetwork(buf);
			Long period = buf.readLong();
			if (period <= 0) {
				period = null;
			}
			return new Time(range, period);
		}

		@Override
		public void toNetwork(Time condition, FriendlyByteBuf buf) {
			IntBoundsHelper.toNetwork(condition.value, buf);
			buf.writeLong(condition.period == null ? Long.MIN_VALUE : condition.period);
		}

	}

}
