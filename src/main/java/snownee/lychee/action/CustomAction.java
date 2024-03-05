package snownee.lychee.action;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

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
		Data data,
		boolean canRepeat,
		Apply applyFunc) implements PostAction {
	public CustomAction(PostActionCommonProperties commonProperties, Data data, boolean canRepeat) {
		this(commonProperties, data, canRepeat, null);
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
	public void validate(ILycheeRecipe<?> recipe, ILycheeRecipe.NBTPatchContext patchContext) {
		CommonProxy.postCustomActionEvent(data.id, this, recipe, patchContext);
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
				Data.CODEC.forGetter(CustomAction::data),
				Codec.BOOL.fieldOf("repeatable").forGetter(CustomAction::repeatable)
		).apply(instance, CustomAction::new));

		@Override
		public Codec<CustomAction> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? extends ByteBuf, CustomAction> streamCodec() {
			throw new UnsupportedOperationException();
		}
	}
}
