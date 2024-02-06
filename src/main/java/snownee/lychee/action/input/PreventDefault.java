package snownee.lychee.action.input;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public final class PreventDefault implements PostAction {

	public static final PreventDefault CLIENT_DUMMY = new PreventDefault();
	private final PostActionCommonProperties commonProperties;

	public PreventDefault(PostActionCommonProperties commonProperties) {this.commonProperties = commonProperties;}

	public PreventDefault() {
		this(new PostActionCommonProperties());
	}

	@Override
	public PostActionType<PreventDefault> type() {
		return PostActionTypes.PREVENT_DEFAULT;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		context.get(LycheeContextKey.ACTION).avoidDefault = true;
	}

	@Override
	public boolean hidden() {
		return true;
	}

	@Override
	public <T extends ILycheeRecipe<?>> void loadCatalystsInfo(@Nullable T recipe, List<IngredientInfo> ingredients) {
		if (recipe != null &&
				recipe.getType() instanceof LycheeRecipeType<?, ?> lycheeRecipeType &&
				lycheeRecipeType.canPreventConsumeInputs) {
			for (var ingredient : ingredients) {
				if (!ingredient.tooltips.isEmpty()) {
					continue;
				}
				if (!commonProperties().conditions().conditions().isEmpty()) {
					continue;
				}
				ingredient.addTooltip(((LycheeRecipeType<?, T>) lycheeRecipeType).getPreventDefaultDescription(recipe));
				ingredient.isCatalyst = true;
			}
		}
	}

	@Override
	public PostActionCommonProperties commonProperties() {return commonProperties;}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		var that = (PreventDefault) obj;
		return Objects.equals(this.commonProperties, that.commonProperties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(commonProperties);
	}

	@Override
	public String toString() {
		return "PreventDefault[" +
				"commonProperties=" + commonProperties + ']';
	}


	public static class Type implements PostActionType<PreventDefault> {
		public static final Codec<PreventDefault> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(PreventDefault::commonProperties)
		).apply(instance, PreventDefault::new));

		@Override
		public @NotNull Codec<PreventDefault> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? extends ByteBuf, PreventDefault> streamCodec() {
			return StreamCodec.of((buf, value) -> {}, (buf) -> CLIENT_DUMMY);
		}
	}
}
