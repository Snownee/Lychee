package snownee.lychee.core.post;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

public abstract class PostAction extends ContextualHolder {

	public abstract PostActionType<?> getType();

	/**
	 * @return false if prevent default behavior
	 */
	public boolean doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		for (int i = 0; i < times; i++) {
			apply(recipe, ctx, times);
		}
		return true;
	}

	protected abstract void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times);

	public List<ItemStack> getOutputItems() {
		return List.of();
	}

	public static void parseActions(JsonElement element, Consumer<PostAction> consumer) {
		if (element == null) {
		} else if (element.isJsonObject()) {
			consumer.accept(parse(element.getAsJsonObject()));
		} else {
			JsonArray array = element.getAsJsonArray();
			for (int x = 0; x < array.size(); x++) {
				consumer.accept(parse(array.get(x).getAsJsonObject()));
			}
		}
	}

	public static PostAction parse(JsonObject o) {
		ResourceLocation key = new ResourceLocation(o.get("type").getAsString());
		PostActionType<?> type = LycheeRegistries.POST_ACTION.getValue(key);
		PostAction action = type.fromJson(o);
		action.parseConditions(o.get("contextual"));
		return action;
	}

	public Component getDisplayName() {
		return Component.translatable(LUtil.makeDescriptionId("postAction", getType().getRegistryName()));
	}

	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		AllGuiTextures.JEI_QUESTION_MARK.render(poseStack, x + 2, y + 1);
	}

	@OnlyIn(Dist.CLIENT)
	public List<Component> getBaseTooltips() {
		return Lists.newArrayList(getDisplayName());
	}

	@OnlyIn(Dist.CLIENT)
	public List<Component> getTooltips() {
		List<Component> list = getBaseTooltips();
		int c = showingConditionsCount();
		if (c > 0) {
			list.add(LUtil.format("contextual.lychee", c).withStyle(ChatFormatting.GRAY));
		}
		getConditonTooltips(list, 0);
		return list;
	}

	public boolean isHidden() {
		return false;
	}

	@Override
	public String toString() {
		return "";
	}

	public boolean canRepeat() {
		return true;
	}

}
