package snownee.lychee.util.action;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import snownee.lychee.action.RandomSelect;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface PostActionRenderer<T extends PostAction> {

	Map<PostActionType<?>, PostActionRenderer<?>> RENDERERS = Maps.newHashMap();

	static <T extends PostAction> PostActionRenderer<T> of(PostAction action) {
		return (PostActionRenderer<T>) Objects.requireNonNull(RENDERERS.get(action.type()));
	}

	static <T extends PostAction> void register(PostActionType<T> type, PostActionRenderer<T> renderer) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(renderer);
		RENDERERS.put(type, renderer);
	}

	static List<Component> getTooltipsFromRandom(RandomSelect randomSelect, PostAction child) {
		var index = randomSelect.entries.indexOf(child);
		var list = randomSelect.entries.size() == 1 && randomSelect.emptyWeight == 0 ? Lists.newArrayList(
				randomSelect.getDisplayName()) : PostActionRenderer.of(child).getBaseTooltips(child);
		if (index == -1) {
			return list; //TODO nested actions?
		}
		if (randomSelect.entries.size() > 1 || randomSelect.emptyWeight > 0) {
			var chance = CommonProxy.chance(randomSelect.entries.get(index).weight() / (float) randomSelect.totalWeight);
			if (randomSelect.rolls == BoundsExtensions.ONE) {
				list.add(Component.translatable("tip.lychee.randomChance.one", chance)
						.withStyle(ChatFormatting.YELLOW));
			} else {
				list.add(Component.translatable(
						"tip.lychee.randomChance",
						chance,
						BoundsExtensions.getDescription(randomSelect.rolls)
				).withStyle(ChatFormatting.YELLOW));
			}
		}
		var c = randomSelect.conditions().showingCount() + child.conditions().showingCount();
		if (c > 0) {
			list.add(ClientProxy.format("contextual.lychee", c).withStyle(ChatFormatting.GRAY));
		}
		var mc = Minecraft.getInstance();
		randomSelect.conditions().appendToTooltips(list, mc.level, mc.player, 0);
		child.conditions().appendToTooltips(list, mc.level, mc.player, 0);
		return list;
	}

	default void render(T action, GuiGraphics graphics, int x, int y) {
	}

	default List<Component> getBaseTooltips(T action) {
		return Lists.newArrayList(action.getDisplayName());
	}

	default List<Component> getTooltips(T action) {
		var list = getBaseTooltips(action);
		var c = action.conditions().showingCount();
		if (c > 0) {
			list.add(ClientProxy.format("contextual.lychee", c).withStyle(ChatFormatting.GRAY));
		}
		var mc = Minecraft.getInstance();
		action.conditions().appendToTooltips(list, mc.level, mc.player, 0);
		return list;
	}

	default void loadCatalystsInfo(
			T action,
			ILycheeRecipe<?> recipe,
			List<IngredientInfo> ingredients) {}
}
