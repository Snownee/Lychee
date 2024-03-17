package snownee.lychee.util.contextual;

import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.codec.CompactListCodec;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class ContextualHolder implements ContextualPredicate, Iterable<ContextualCondition> {
	public static final Component SECRET_COMPONENT = Component.translatable("contextual.lychee.secret").withStyle(
			ChatFormatting.GRAY);
	public static final Codec<ContextualHolder> CODEC = new CompactListCodec<>(ContextualConditionData.CODEC).xmap(
			ContextualHolder::pack,
			ContextualHolder::unpack);

	public static final ContextualHolder EMPTY = new ContextualHolder(List.of(), null, null);

	private final List<ContextualCondition> conditions;
	@Nullable
	private final BitSet secretFlags;
	@Nullable
	private final Component[] overrideDesc;

	public ContextualHolder(
			List<ContextualCondition> conditions,
			@Nullable BitSet secretFlags,
			@Nullable Component[] overrideDesc
	) {
		this.conditions = Collections.unmodifiableList(conditions);
		this.secretFlags = secretFlags;
		this.overrideDesc = overrideDesc;
	}

	private static ContextualHolder pack(List<ContextualConditionData<?>> holders) {
		if (holders.isEmpty()) {
			return EMPTY;
		} else {
			List<ContextualCondition> conditions = Lists.newArrayListWithExpectedSize(holders.size());
			BitSet secretFlags = null;
			Component[] overrideDesc = null;
			for (var i = 0; i < holders.size(); i++) {
				var holder = holders.get(i);
				conditions.add(holder.condition());
				if (holder.secret()) {
					if (secretFlags == null) {
						secretFlags = new BitSet(holders.size());
					}
					secretFlags.set(i);
				}
				if (holder.description().isPresent()) {
					if (overrideDesc == null) {
						overrideDesc = new Component[holders.size()];
					}
					overrideDesc[i] = holder.description().get();
				}
			}
			return new ContextualHolder(conditions, secretFlags, overrideDesc);
		}
	}

	public List<ContextualCondition> conditions() {
		return conditions;
	}

	public int showingCount() {
		return conditions().stream().mapToInt(ContextualConditionDisplay::showingCount).sum();
	}

	private List<ContextualConditionData<?>> unpack() {
		List<ContextualConditionData<?>> list = Lists.newArrayListWithExpectedSize(conditions.size());
		for (var i = 0; i < conditions.size(); i++) {
			var condition = conditions.get(i);
			var secret = isSecretCondition(i);
			list.add(new ContextualConditionData<>(condition, secret, Optional.ofNullable(getOverridenDesc(i))));
		}
		return list;
	}

	public void appendToTooltips(
			List<Component> tooltips, @Nullable Level level, @Nullable Player player, int indent
	) {
		if (level == null) {
			//TODO notify player that the condition is not available
			return;
		}
		var i = -1;
		for (var condition : conditions) {
			++i;
			if (isSecretCondition(i)) {
				var result = condition.testForTooltips(level, player);
				ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, SECRET_COMPONENT.copy());
				continue;
			}
			var overridenDesc = getOverridenDesc(i);
			if (overridenDesc != null) {
				var result = condition.testForTooltips(level, player);
				ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, overridenDesc.copy());
				continue;
			}
			condition.appendToTooltips(tooltips, level, player, indent, false);
		}
	}

	@NotNull
	@Override
	public Iterator<ContextualCondition> iterator() {
		return conditions.iterator();
	}

	private @Nullable Component getOverridenDesc(int i) {
		if (overrideDesc != null) {
			return overrideDesc[i];
		}
		return null;
	}

	public boolean isSecretCondition(int i) {
		if (secretFlags != null) {
			return secretFlags.get(i);
		}
		return false;
	}

	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		for (var condition : conditions) {
			try {
				times = condition.test(recipe, ctx, times);
				if (times == 0) {
					break;
				}
			} catch (Throwable e) {
				Lychee.LOGGER.error(
						"Failed to check condition {} of recipe {}",
						LycheeRegistries.CONTEXTUAL.getKey(condition.type()),
						ctx.get(LycheeContextKey.RECIPE_ID),
						e
				);
				return 0;
			}
		}
		return times;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ContextualHolder that = (ContextualHolder) o;
		return Objects.equal(conditions, that.conditions) &&
				Objects.equal(secretFlags, that.secretFlags) &&
				Objects.equal(overrideDesc, that.overrideDesc);
	}

	@Override
	public int hashCode() {
		return unpack().hashCode();
	}

	@Override
	public String toString() {
		return unpack().toString();
	}
}
