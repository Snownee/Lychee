package snownee.lychee.util.contextual;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;

public interface ContextualCondition<T extends ContextualCondition<T>> extends RecipeCondition,
																			   ContextualConditionDisplay {
	Codec<ContextualCondition<?>> CODEC = LycheeRegistries.CONTEXTUAL.byNameCodec().dispatch(
			ContextualCondition::type,
			ContextualConditionType::codec
	);

	ContextualConditionType<T> type();

	@Override
	default String getDescriptionId() {
		return CommonProxy.makeDescriptionId("contextual", LycheeRegistries.CONTEXTUAL.getKey(type()));
	}

	@Override
	default void appendToTooltips(
			List<Component> tooltips,
			Level level,
			@Nullable Player player,
			int indent,
			boolean inverted
	) {
		ContextualConditionDisplay.appendToTooltips(
				tooltips,
				testForTooltips(level, player),
				indent,
				getDescription(inverted)
		);
	}
}
