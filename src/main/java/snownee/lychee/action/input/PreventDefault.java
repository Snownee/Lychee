package snownee.lychee.action.input;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeStreamCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record PreventDefault(PostActionCommonProperties commonProperties) implements PostAction {

	public static final PreventDefault CLIENT_DUMMY = new PreventDefault();

	public PreventDefault() {
		this(PostActionCommonProperties.EMPTY);
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
	public SlotDisplay transformRemainder(SlotDisplay itemStack, @Nullable ILycheeRecipe<?> recipe) {
		return itemStack;
	}

	public static class Type implements PostActionType<PreventDefault> {
		public static final MapCodec<PreventDefault> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(PreventDefault::commonProperties)
		).apply(instance, PreventDefault::new));

		@Override
		public MapCodec<PreventDefault> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, PreventDefault> streamCodec() {
			return LycheeStreamCodecs.uncheckedUnit(CLIENT_DUMMY);
		}
	}
}
