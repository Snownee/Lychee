package snownee.lychee.util.contextual;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;

public interface ContextualCondition extends ContextualPredicate, ContextualConditionDisplay {
	MapCodec<ContextualCondition> CODEC = LycheeRegistries.CONTEXTUAL.byNameCodec().dispatchMap(
			ContextualCondition::type,
			ContextualConditionType::codec);
	StreamCodec<RegistryFriendlyByteBuf, ContextualCondition> STREAM_CODEC = ByteBufCodecs.registry(LycheeRegistries.CONTEXTUAL.key())
			.dispatch(ContextualCondition::type, ContextualConditionType::streamCodec);

	ContextualConditionType<?> type();

	@Override
	default String getDescriptionId() {
		return CommonProxy.makeDescriptionId("contextual", LycheeRegistries.CONTEXTUAL.getKey(type()));
	}

	@Override
	default void appendToTooltips(List<Component> tooltips, Level level, @Nullable Player player, int indent, boolean inverted) {
		ContextualConditionDisplay.appendToTooltips(tooltips, testForTooltips(level, player), indent, getDescription(inverted));
	}
}
