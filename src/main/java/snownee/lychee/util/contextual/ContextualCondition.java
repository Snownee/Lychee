package snownee.lychee.util.contextual;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.serialization.Codec;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
		return DESCRIPTION_ID_CACHE.getUnchecked(type());
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
