package snownee.lychee.core.post.input;

import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.PostActionTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.Reference;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

public class SetItem extends PostAction {

	public final ItemStack stack;
	public final Reference target;

	public SetItem(ItemStack stack, Reference target) {
		this.stack = stack;
		this.target = target;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.SET_ITEM;
	}

	@Override
	public void doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		IntList indexes = recipe.getItemIndexes(target);
		for (var index : indexes) {
			CompoundTag tag = ctx.itemHolders.get(index).get().getTag();
			ctx.itemHolders.replace(index, stack.copy());
			if (tag != null) {
				ctx.itemHolders.get(index).get().getOrCreateTag().merge(tag);
			}
			ctx.itemHolders.ignoreConsumptionFlags.set(index);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Component> getBaseTooltips() {
		return stack.getTooltipLines(null, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		GuiGameElement.of(stack).render(poseStack, x, y);
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
	public boolean canRepeat() {
		return false;
	}

	@Override
	public boolean validate(LycheeRecipe<?> recipe) {
		return !recipe.getItemIndexes(target).isEmpty();
	}

	public static class Type extends PostActionType<SetItem> {

		@Override
		public SetItem fromJson(JsonObject o) {
			return new SetItem(ShapedRecipe.itemStackFromJson(o), Reference.fromJson(o, "target"));
		}

		@Override
		public void toJson(SetItem action, JsonObject o) {
			LUtil.itemstackToJson(action.stack, o);
			Reference.toJson(action.target, o, "target");
		}

		@Override
		public SetItem fromNetwork(FriendlyByteBuf buf) {
			return new SetItem(buf.readItem(), Reference.fromNetwork(buf));
		}

		@Override
		public void toNetwork(SetItem action, FriendlyByteBuf buf) {
			buf.writeItem(action.stack);
			Reference.toNetwork(action.target, buf);
		}

	}

}
