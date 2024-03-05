package snownee.lychee.action.input;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.core.Reference;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualHolder;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record SetItem(
		PostActionCommonProperties commonProperties,
		ContextualHolder conditions,
		ItemStack stack,
		Reference target) implements PostAction {

	@Override
	public PostActionType<SetItem> type() {
		return PostActionTypes.SET_ITEM;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var indexes = recipe.getItemIndexes(target);
		var registryAccess = context.get(LycheeContextKey.LEVEL).registryAccess();
		for (var index : indexes) {
			var tag = (CompoundTag) context.getItem(index).save(registryAccess);
			ItemStack stack;
			if (getPath().isEmpty()) {
				stack = this.stack.copy();
			} else {
				stack = ItemStack.parseOptional(
						registryAccess,
						CommonProxy.jsonToTag(new JsonPointer(getPath().get()).find(context.get(LycheeContextKey.JSON).json())));

			}
			context.setItem(index, stack);
			if (!stack.isEmpty()) {
				((CompoundTag) context.getItem(index).saveOptional(registryAccess)).merge(tag);
			}
			context.get(LycheeContextKey.ITEM).get(index).setIgnoreConsumption(true);
		}
	}

	@Override
	public Component getDisplayName() {
		return stack.getHoverName();
	}

	@Override
	public List<ItemStack> getOutputItems() {
		return List.of(stack);
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe, ILycheeRecipe.NBTPatchContext patchContext) {
		Preconditions.checkArgument(!recipe.getItemIndexes(target).isEmpty(), "No target found for %s", target);
	}

//	@Override
//	public JsonElement provideJsonInfo(ILycheeRecipe<?> recipe, JsonPointer pointer, JsonObject recipeObject) {
//		setPath(pointer.toString());
//		return CommonProxy.tagToJson(stack.save(new CompoundTag()));
//	}

	public static class Type implements PostActionType<SetItem> {
		public static final Codec<SetItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(SetItem::commonProperties),
				ContextualHolder.CODEC.fieldOf("contextual").forGetter(SetItem::conditions),
				ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(SetItem::stack),
				Reference.CODEC.fieldOf("target").forGetter(SetItem::target)
		).apply(instance, SetItem::new));

		@Override
		public Codec<SetItem> codec() {
			return CODEC;
		}
	}
}
