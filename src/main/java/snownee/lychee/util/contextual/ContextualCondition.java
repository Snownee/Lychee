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

public interface ContextualCondition<T extends ContextualCondition<T>> extends RecipeCondition, ClientRecipeCondition {
	Codec<ContextualCondition<?>> CODEC = LycheeRegistries.CONTEXTUAL.byNameCodec().dispatch(
			ContextualCondition::type,
			ContextualConditionType::codec
	);

	LoadingCache<ContextualConditionType<?>, String> DESCRIPTION_ID_CACHE =
			CacheBuilder.newBuilder().build(new CacheLoader<>() {
				@Override
				public @NotNull String load(@NotNull ContextualConditionType<?> key) {
					return CommonProxy.makeDescriptionId("contextual", LycheeRegistries.CONTEXTUAL.getKey(key));
				}
			});

	ContextualConditionType<T> type();

	default String getDescriptionId() {
		return DESCRIPTION_ID_CACHE.getUnchecked(type());
	}

	default String getDescriptionId(boolean inverted) {
		return getDescriptionId() + (inverted ? ".not" : "");
	}

	default MutableComponent getDescription(boolean inverted) {
		return Component.translatable(getDescriptionId(inverted));
	}

	@Override
	default void appendToTooltips(
			List<Component> tooltips,
			Level level,
			@Nullable Player player,
			int indent,
			boolean inverted
	) {
		ClientRecipeCondition.appendToTooltips(
				tooltips,
				testForTooltips(level, player),
				indent,
				getDescription(inverted)
		);
	}

	default int showingCount() {
		return 1;
	}
}
