package snownee.lychee.core.post;

import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.PostActionTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.mixin.ItemEntityAccess;
import snownee.lychee.util.LUtil;

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
	public void doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		times = checkConditions(recipe, ctx, times);
		if (times > 0) {
			apply(recipe, ctx, times);
		}
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Vec3 pos = ctx.getParam(LootContextParams.ORIGIN);
		ItemStack stack = this.stack.copy();
		stack.setCount(stack.getCount() * times);
		LUtil.dropItemStack(ctx.getLevel(), pos.x, pos.y, pos.z, stack, $ -> {
			((ItemEntityAccess) $).setHealth(20);
		});
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		GuiGameElement.of(stack).render(poseStack, x, y);
	}

	public static class Type extends PostActionType<DropItem> {

		@Override
		public DropItem fromJson(JsonObject o) {
			return new DropItem(ShapedRecipe.itemStackFromJson(o));
		}

		@Override
		public DropItem fromNetwork(FriendlyByteBuf buf) {
			return new DropItem(buf.readItem());
		}

		@Override
		public void toNetwork(DropItem condition, FriendlyByteBuf buf) {
			buf.writeItem(condition.stack);
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

}
