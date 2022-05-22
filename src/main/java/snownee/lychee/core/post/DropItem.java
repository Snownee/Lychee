package snownee.lychee.core.post;

import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.LycheeTags;
import snownee.lychee.PostActionTypes;
import snownee.lychee.RecipeTypes;
import snownee.lychee.block_exploding.BlockExplodingContext;
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
	public boolean doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
		return true;
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Vec3 pos = ctx.getParam(LootContextParams.ORIGIN);
		if (recipe.getType() == RecipeTypes.BLOCK_CRUSHING) {
			BlockState landingBlock = ctx.getParam(LootContextParams.BLOCK_STATE);
			if (landingBlock.is(LycheeTags.EXTEND_BOX)) {
				pos = Vec3.atCenterOf(ctx.getParam(LycheeLootContextParams.BLOCK_POS));
			}
		}
		ItemStack stack = this.stack.copy();
		stack.setCount(stack.getCount() * times);
		if (ctx.getClass() == BlockExplodingContext.class) {
			((BlockExplodingContext) ctx).items.add(stack);
		} else {
			LUtil.dropItemStack(ctx.getLevel(), pos.x, pos.y, pos.z, stack, $ -> {
				((ItemEntityAccess) $).setHealth(20);
			});
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public List<Component> getBaseTooltips() {
		return stack.getTooltipLines(null, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
	}

	@Override
	@Environment(EnvType.CLIENT)
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
		public void toNetwork(DropItem action, FriendlyByteBuf buf) {
			buf.writeItem(action.stack);
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
