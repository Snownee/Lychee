package snownee.lychee.datagen;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.serialization.RecordBuilder;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.action.RandomSelect;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionLike;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;

@NotNullByDefault
public class ActionBuilder<T extends ActionBuilder<T, R>, R extends PostAction> extends ContextualBuilder<T> implements PostActionLike {
	protected @Nullable R action;
	protected @Nullable ResourceLocation icon;

	public ActionBuilder() {}

	public ActionBuilder(R action) {
		this.action = action;
	}

	@Contract("-> this")
	public T hide() {
		return icon(PostActionCommonProperties.HIDDEN);
	}

	@Contract("_ -> this")
	public T icon(ResourceLocation icon) {
		this.icon = icon;
		return self();
	}

	public boolean isModified() {
		return icon != null || !conditions.isEmpty();
	}

	public R build() {
		Objects.requireNonNull(action);
		if (!isModified()) {
			return action;
		}
		RegistryOps<Object> ops = Objects.requireNonNull(LycheeBuilder.registryOps.get());
		RecordBuilder<Object> builder = ops.mapBuilder();
		PostAction.MAP_CODEC.encode(action, ops, builder);
		if (icon != null) {
			PostActionCommonProperties.ICON_CODEC.encode(Optional.of(icon), ops, builder);
			icon = null;
		}
		if (!conditions.isEmpty()) {
			LycheeRecipeCommonProperties.CONTEXTUAL_CODEC.encode(contextualHolder(), ops, builder);
			conditions.clear();
		}
		//noinspection unchecked
		return action = (R) PostAction.MAP_CODEC.compressedDecode(ops, builder.build(ops.empty()).getOrThrow()).getOrThrow();
	}

	@Override
	public PostAction asAction() {
		return build();
	}

	public static class RandomSelectBuilder extends ActionBuilder<RandomSelectBuilder, RandomSelect> {
		private final List<RandomSelect.Entry> entries = Lists.newArrayList();
		private int emptyWeight;
		private MinMaxBounds.Ints rolls = BoundsExtensions.ONE;

		@Contract("_, _ -> this")
		public RandomSelectBuilder post(PostActionLike postAction, int weight) {
			Preconditions.checkArgument(weight > 0, "Weight must be positive");
			entries.add(new RandomSelect.Entry(postAction.asAction(), weight));
			return self();
		}

		@Contract("_ -> this")
		public RandomSelectBuilder emptyWeight(int emptyWeight) {
			this.emptyWeight = emptyWeight;
			return self();
		}

		@Contract("_ -> this")
		@HideFromJS
		public RandomSelectBuilder rolls(MinMaxBounds.Ints rolls) {
			this.rolls = rolls;
			return self();
		}

		@Contract("_ -> this")
		public RandomSelectBuilder rolls(int rolls) {
			return rolls(MinMaxBounds.Ints.exactly(rolls));
		}

		@Contract("_, _ -> this")
		public RandomSelectBuilder rolls(int min, int max) {
			return rolls(MinMaxBounds.Ints.between(min, max));
		}

		@Override
		public RandomSelect build() {
			action = new RandomSelect(PostActionCommonProperties.EMPTY, entries, emptyWeight, rolls);
			return super.build();
		}

		@Override
		public boolean isModified() {
			return true;
		}
	}
}
