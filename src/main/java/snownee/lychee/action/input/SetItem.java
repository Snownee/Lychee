package snownee.lychee.action.input;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record SetItem(PostActionCommonProperties commonProperties, ItemStack itemStack, Reference target) implements PostAction {

	@Override
	public PostActionType<SetItem> type() {
		return PostActionTypes.SET_ITEM;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var indexes = recipe.getItemIndexes(target);
		var registryAccess = context.level().registryAccess();
		for (var index : indexes) {
			var tag = (CompoundTag) context.getItem(index).save(registryAccess);
			ItemStack stack;
			if (getPath().isEmpty()) {
				stack = this.itemStack.copy();
			} else {
				stack = ItemStack.parseOptional(
						registryAccess,
						CommonProxy.jsonToTag(new JsonPointer(getPath().get()).find(context.get(LycheeContextKey.JSON)))
				);

			}
			context.setItem(index, stack);
			if (!stack.isEmpty()) {
				((CompoundTag) context.getItem(index).saveOptional(registryAccess)).merge(tag);
			}
			context.get(LycheeContextKey.ITEM).get(index).setConsumption(0);
		}
	}

	@Override
	public Component getDisplayName() {
		return itemStack.getHoverName();
	}

	@Override
	public List<ItemStack> getOutputItems() {
		return List.of(itemStack);
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe) {
		Preconditions.checkArgument(!recipe.getItemIndexes(target).isEmpty(), "No target found for %s", target);
	}

	@Override
	public ItemStack transformRemainder(ItemStack itemStack, @Nullable ILycheeRecipe<?> recipe) {
		return itemStack();
	}

	//	@Override
	//	public JsonElement provideJsonInfo(ILycheeRecipe<?> recipe, JsonPointer pointer, JsonObject recipeObject) {
	//		setPath(pointer.toString());
	//		return CommonProxy.tagToJson(stack.save(new CompoundTag()));
	//	}

	public static class Type implements PostActionType<SetItem> {
		public static final MapCodec<SetItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(SetItem::commonProperties),
				LycheeCodecs.ITEM_STACK_MAP_CODEC.forGetter(SetItem::itemStack),
				Reference.CODEC.optionalFieldOf("target", Reference.DEFAULT).forGetter(SetItem::target)
		).apply(instance, SetItem::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, SetItem> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				SetItem::commonProperties,
				ItemStack.OPTIONAL_STREAM_CODEC,
				SetItem::itemStack,
				Reference.STREAM_CODEC,
				SetItem::target,
				SetItem::new);

		@Override
		public MapCodec<SetItem> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SetItem> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
