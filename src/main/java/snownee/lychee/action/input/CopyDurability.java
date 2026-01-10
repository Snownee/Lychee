package snownee.lychee.action.input;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record CopyDurability(
		PostActionCommonProperties commonProperties,
		float bonus,
		Reference source,
		Reference target) implements PostAction {
	public static final Reference DEFAULT_SOURCE = Reference.create("/%s/0".formatted(ILycheeRecipe.ITEM_IN));
	public static final Reference DEFAULT_TARGET = new Reference.Pointer(ILycheeRecipe.ITEM_OUT_POINTER);

	@Override
	public boolean hidden() {
		return true;
	}

	@Override
	public PostActionType<?> type() {
		return PostActionTypes.COPY_DURABILITY;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		IntList targetIndexes = Objects.requireNonNull(recipe).getItemIndexes(target);
		ItemStackHolderCollection itemHolders = context.get(LycheeContextKey.ITEM);
		ItemStack sourceItem = itemHolders.get(recipe.getItemIndexes(source).getFirst()).get();
		if (!sourceItem.isDamageableItem()) {
			return;
		}
		float durabilityPercentage = (float) (sourceItem.getMaxDamage() - sourceItem.getDamageValue()) / sourceItem.getMaxDamage();
		if (bonus != 0F) {
			durabilityPercentage *= 1 + bonus;
		}
		for (int targetIndex : targetIndexes) {
			ItemStack targetItem = itemHolders.get(targetIndex).get();
			if (targetItem.isDamageableItem()) {
				int newDamage = targetItem.getMaxDamage() - Math.round(durabilityPercentage * targetItem.getMaxDamage());
				targetItem.setDamageValue(Mth.clamp(newDamage, 0, targetItem.getMaxDamage()));
			}
		}
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe) {
		var sourceIndexes = recipe.getItemIndexes(source);
		if (sourceIndexes.isEmpty()) {
			throw new IllegalStateException("No source item for CopyDurability action: " + source);
		}
		if (sourceIndexes.size() > 1) {
			throw new IllegalStateException("Multiple source items for CopyDurability action: " + source);
		}
		var targetIndexes = recipe.getItemIndexes(target);
		if (targetIndexes.isEmpty()) {
			throw new IllegalStateException("No target item for CopyDurability action: " + target);
		}
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	public static class Type implements PostActionType<CopyDurability> {
		public static final MapCodec<CopyDurability> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(CopyDurability::commonProperties),
						Codec.floatRange(-1, 999).optionalFieldOf("bonus", 0F).forGetter(CopyDurability::bonus),
						Reference.CODEC.optionalFieldOf("source", DEFAULT_SOURCE).forGetter(CopyDurability::source),
						Reference.CODEC.optionalFieldOf("target", DEFAULT_TARGET).forGetter(CopyDurability::target))
				.apply(instance, CopyDurability::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, CopyDurability> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				CopyDurability::commonProperties,
				ByteBufCodecs.FLOAT,
				CopyDurability::bonus,
				Reference.STREAM_CODEC,
				CopyDurability::source,
				Reference.STREAM_CODEC,
				CopyDurability::target,
				CopyDurability::new);

		@Override
		public MapCodec<CopyDurability> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CopyDurability> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
