package snownee.lychee.action;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.LycheeTags;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.mixin.ItemEntityAccess;
import snownee.lychee.recipes.block_crushing.BlockCrushingRecipe;
import snownee.lychee.recipes.block_exploding.BlockExplodingContext;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.json.JsonPointer;

public class DropItem extends PostAction {

	public final ItemStack stack;

	public DropItem(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.DROP_ITEM;
	}

	@Override
	public void doApply(LycheeRecipe recipe, LycheeRecipeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(LycheeRecipe recipe, LycheeRecipeContext ctx, int times) {
		Vec3 pos = ctx.getParam(LootContextParams.ORIGIN);
		if (recipe instanceof BlockCrushingRecipe) {
			BlockState landingBlock = ctx.getParam(LootContextParams.BLOCK_STATE);
			if (landingBlock.is(LycheeTags.EXTEND_BOX)) {
				pos = Vec3.atCenterOf(ctx.getParam(LycheeLootContextParams.BLOCK_POS));
			}
		}
		ItemStack stack;
		if (path == null) {
			stack = this.stack.copy();
		} else {
			stack = ItemStack.of(CommonProxy.jsonToTag(new JsonPointer(path).find(ctx.json)));
		}
		stack.setCount(stack.getCount() * times);
		if (ctx.getClass() == BlockExplodingContext.class) {
			ctx.itemHolders.stacksNeedHandle.add(stack);
		} else {
			CommonProxy.dropItemStack(ctx.getLevel(), pos.x, pos.y, pos.z, stack, $ -> {
				((ItemEntityAccess) $).setHealth(80);
			});
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
	public JsonElement provideJsonInfo(LycheeRecipe recipe, JsonPointer pointer, JsonObject recipeObject) {
		path = pointer.toString();
		return CommonProxy.tagToJson(stack.save(new CompoundTag()));
	}

	public static class Type extends PostActionType<DropItem> {

		@Override
		public DropItem fromJson(JsonObject o) {
			return new DropItem(ShapedRecipe.itemStackFromJson(o));
		}

		@Override
		public void toJson(DropItem action, JsonObject o) {
			CommonProxy.itemstackToJson(action.stack, o);
		}

		@Override
		public DropItem fromNetwork(FriendlyByteBuf buf) {
			return new DropItem(buf.readItem());
		}

		@Override
		public void toNetwork(DropItem action, FriendlyByteBuf buf) {
			buf.writeItem(action.stack);
		}

	}
}
