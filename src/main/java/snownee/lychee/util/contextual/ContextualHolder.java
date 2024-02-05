package snownee.lychee.util.contextual;

import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.TriState;
import snownee.lychee.util.codec.CompactListCodec;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class ContextualHolder implements ContextualPredicate, Iterable<ContextualCondition<?>> {
	public static final Component SECRET_COMPONENT = Component.translatable("contextual.lychee.secret").withStyle(ChatFormatting.GRAY);
	public static final Codec<ContextualHolder> CODEC = new CompactListCodec<>(ContextualConditionData.CODEC)
			.xmap(ContextualHolder::pack, ContextualHolder::unpack);

	public static final ContextualHolder EMPTY = new ContextualHolder(List.of(), null, null);

	private final List<ContextualCondition<?>> conditions;
	@Nullable
	private final BitSet secretFlags;
	@Nullable
	private final Component[] overrideDesc;

	public ContextualHolder(List<ContextualCondition<?>> conditions, @Nullable BitSet secretFlags, @Nullable Component[] overrideDesc) {
		this.conditions = Collections.unmodifiableList(conditions);
		this.secretFlags = secretFlags;
		this.overrideDesc = overrideDesc;
	}

	private static ContextualHolder pack(List<ContextualConditionData<?>> holders) {
		if (holders.isEmpty()) {
			return EMPTY;
		} else {
			List<ContextualCondition<?>> conditions = Lists.newArrayListWithExpectedSize(holders.size());
			BitSet secretFlags = null;
			Component[] overrideDesc = null;
			for (int i = 0; i < holders.size(); i++) {
				ContextualConditionData<?> holder = holders.get(i);
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

	private List<ContextualConditionData<?>> unpack() {
		List<ContextualConditionData<?>> list = Lists.newArrayListWithExpectedSize(conditions.size());
		for (int i = 0; i < conditions.size(); i++) {
			ContextualCondition<?> condition = conditions.get(i);
			boolean secret = isSecretCondition(i);
			Optional<Component> description = Optional.ofNullable(getOverridenDesc(i));
			list.add(new ContextualConditionData<>(condition, secret, description));
		}
		return list;
	}

	public List<ContextualCondition<?>> conditions() {
		return conditions;
	}

	public int showingCount() {
		return conditions().stream().mapToInt(ContextualConditionDisplay::showingCount).sum();
	}

	public void appendToTooltips(List<Component> tooltips, @Nullable Level level, @Nullable Player player, int indent) {
		if (level == null) {
			//TODO notify player that the condition is not available
			return;
		}
		int i = -1;
		for (ContextualCondition<?> condition : conditions) {
			++i;
			if (isSecretCondition(i)) {
				TriState result = condition.testForTooltips(level, player);
				ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, SECRET_COMPONENT.copy());
				continue;
			}
			Component overridenDesc = getOverridenDesc(i);
			if (overridenDesc != null) {
				TriState result = condition.testForTooltips(level, player);
				ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, overridenDesc.copy());
				continue;
			}
			condition.appendToTooltips(tooltips, level, player, indent, false);
		}
	}

	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		for (ContextualCondition<?> condition : conditions) {
			try {
				times = condition.test(recipe, ctx, times);
				if (times == 0) break;
			} catch (Throwable e) {
				Lychee.LOGGER.error("Failed to check condition {} of recipe {}", LycheeRegistries.CONTEXTUAL.getKey(condition.type()), ctx.getMatchedRecipeId(), e);
				return 0;
			}
		}
		return times;
	}

	@NotNull
	@Override
	public Iterator<ContextualCondition<?>> iterator() {
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

	public static ContextualHolder conditionsFromNetwork(FriendlyByteBuf buf) {
		int size = buf.readVarInt();
		List<ContextualCondition<?>> conditions = Lists.newArrayListWithCapacity(size);
		for (int i = 0; i < size; i++) {
			ContextualConditionType<?> type = CommonProxy.readRegistryId(LycheeRegistries.CONTEXTUAL, buf);
			conditions.add(type.fromNetwork(buf));
		}
		BitSet secretFlags = null;
		if (buf.readBoolean()) {
			secretFlags = buf.readBitSet();
		}
		Component[] overrideDesc = null;
		if (buf.readBoolean()) {
			overrideDesc = new Component[size];
			for (int i = 0; i < size; i++) {
				overrideDesc[i] = buf.readOptional(FriendlyByteBuf::readComponent).orElse(null);
			}
		}
		new ContextualHolder(conditions, secretFlags, overrideDesc);
	}

	@SuppressWarnings("rawtypes")
	public void conditionsToNetwork(FriendlyByteBuf buf) {
		buf.writeVarInt(conditions.size());
		for (ContextualCondition condition : conditions) {
			ContextualConditionType type = condition.type();
			CommonProxy.writeRegistryId(LycheeRegistries.CONTEXTUAL, type, buf);
			type.toNetwork(buf, condition);
		}
		buf.writeBoolean(secretFlags != null);
		if (secretFlags != null) {
			buf.writeBitSet(secretFlags);
			//we should sync condition even though it is secret because
			//the client-side need to know the result in tooltips for UX
		}
		buf.writeBoolean(overrideDesc != null);
		if (overrideDesc != null) {
			for (Component component : overrideDesc) {
				buf.writeOptional(Optional.ofNullable(component), FriendlyByteBuf::writeComponent);
			}
		}
	}
}
