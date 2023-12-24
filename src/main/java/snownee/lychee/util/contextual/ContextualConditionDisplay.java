package snownee.lychee.util.contextual;

import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.TriState;

public interface ContextualConditionDisplay {
	LoadingCache<ContextualConditionType<?>, String> DESCRIPTION_ID_CACHE =
			CacheBuilder.newBuilder().build(new CacheLoader<>() {
				@Override
				public @NotNull String load(@NotNull ContextualConditionType<?> key) {
					return CommonProxy.makeDescriptionId("contextual", LycheeRegistries.CONTEXTUAL.getKey(key));
				}
			});

	static void appendToTooltips(
			List<Component> tooltips,
			TriState result,
			int indent,
			MutableComponent content
	) {
		MutableComponent indentComponent = Component.literal("  ".repeat(indent));
		indentComponent.append(I18n.get("result.lychee." + result.toString().toLowerCase(Locale.ENGLISH)));
		indentComponent.append(content.withStyle(ChatFormatting.GRAY));
		tooltips.add(indentComponent);
	}

	default TriState testForTooltips(Level level, @Nullable Player player) {
		return TriState.DEFAULT;
	}

	String getDescriptionId();

	default String getDescriptionId(boolean inverted) {
		return getDescriptionId() + (inverted ? ".not" : "");
	}

	default MutableComponent getDescription(boolean inverted) {
		return Component.translatable(getDescriptionId(inverted));
	}

	void appendToTooltips(
			List<Component> tooltips,
			Level level,
			@Nullable Player player,
			int indent,
			boolean inverted
	);

	default int showingCount() {
		return 1;
	}
}
