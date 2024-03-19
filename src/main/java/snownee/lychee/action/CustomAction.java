package snownee.lychee.action;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record CustomAction(
		PostActionCommonProperties commonProperties,
		String id,
		JsonObject data,
		boolean canRepeat,
		Apply applyFunc
) implements PostAction {

	public CustomAction(
			PostActionCommonProperties commonProperties,
			String id,
			JsonObject json,
			boolean canRepeat
	) {
		this(commonProperties, id, json, canRepeat, null);
	}

	@Override
	public PostActionType<CustomAction> type() {
		return PostActionTypes.CUSTOM;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		if (applyFunc != null) {
			applyFunc.apply(recipe, context, times);
		}
	}

	@Override
	public boolean preventSync() {
		return true;
	}

	@Override
	public boolean repeatable() {
		return canRepeat;
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe) {
		CommonProxy.postCustomActionEvent(id, this, recipe);
	}

	@Override
	public PostActionCommonProperties commonProperties() {return commonProperties;}

	public boolean canRepeat() {return canRepeat;}

	public Apply applyFunc() {return applyFunc;}

	@Override
	public String id() {
		return id;
	}

	public JsonObject data() {
		return data;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CustomAction that = (CustomAction) o;
		return canRepeat == that.canRepeat && Objects.equal(
				commonProperties,
				that.commonProperties
		) && Objects.equal(id, that.id) && Objects.equal(data, that.data) &&
				Objects.equal(applyFunc, that.applyFunc);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(commonProperties, id, data, canRepeat, applyFunc);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("commonProperties", commonProperties)
				.add("id", id)
				.add("data", data)
				.add("canRepeat", canRepeat)
				.add("applyFunc", applyFunc)
				.toString();
	}

	@FunctionalInterface
	public interface Apply {
		void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times);
	}

	public record Data(String id, JsonObject data) {
		public static final MapCodec<Data> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(Data::id),
				ExtraCodecs.JSON.comapFlatMap(it -> {
					try {
						return DataResult.success(it.getAsJsonObject());
					} catch (Exception e) {
						return DataResult.error(e::getMessage);
					}
				}, Function.identity()).fieldOf("data").forGetter(Data::data)
		).apply(instance, Data::new));
	}

	public static class Type implements PostActionType<CustomAction> {
		public static final Codec<CustomAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(CustomAction::commonProperties),
				Codec.STRING.fieldOf("id").forGetter(CustomAction::id),
				ExtraCodecs.strictOptionalField(ExtraCodecs.JSON.comapFlatMap(it -> {
					try {
						return DataResult.success(it.getAsJsonObject());
					} catch (Exception e) {
						return DataResult.error(e::getMessage);
					}
				}, Function.identity()), "data", new JsonObject()).forGetter(CustomAction::data),
				ExtraCodecs.strictOptionalField(Codec.BOOL, "repeatable", true).forGetter(CustomAction::repeatable)
		).apply(instance, CustomAction::new));

		@Override
		public @NotNull Codec<CustomAction> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? extends ByteBuf, CustomAction> streamCodec() {
			throw new UnsupportedOperationException();
		}
	}
}
