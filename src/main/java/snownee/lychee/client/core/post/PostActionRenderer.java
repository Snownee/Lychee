package snownee.lychee.client.core.post;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.core.def.BoundsHelper;
import snownee.lychee.core.def.IntBoundsHelper;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.post.RandomSelect;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.CommonProxy;

public interface PostActionRenderer<T extends PostAction> {

	Map<PostActionType<?>, PostActionRenderer<?>> RENDERERS = Maps.newHashMap();
	PostActionRenderer<PostAction> DEFAULT = new PostActionRenderer<>() {
	};

	static <T extends PostAction> PostActionRenderer<T> of(PostAction action) {
		return (PostActionRenderer<T>) RENDERERS.getOrDefault(action.getType(), DEFAULT);
	}

	static <T extends PostAction> void register(PostActionType<T> type, PostActionRenderer<T> renderer) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(renderer);
		RENDERERS.put(type, renderer);
	}

	static List<Component> getTooltipsFromRandom(RandomSelect randomSelect, PostAction child) {
		int index = Arrays.asList(randomSelect.entries).indexOf(child);
		List<Component> list = randomSelect.entries.length == 1 && randomSelect.emptyWeight == 0 ? Lists.newArrayList(
				randomSelect.getDisplayName()) : PostActionRenderer.of(child).getBaseTooltips(child);
		if (index == -1) {
			return list; //TODO nested actions?
		}
		if (randomSelect.entries.length > 1 || randomSelect.emptyWeight > 0) {
			String chance = CommonProxy.chance(randomSelect.weights[index] / (float) randomSelect.totalWeight);
			if (randomSelect.rolls == IntBoundsHelper.ONE) {
				list.add(Component.translatable("tip.lychee.randomChance.one", chance)
								  .withStyle(ChatFormatting.YELLOW));
			}
			else {
				list.add(Component.translatable("tip.lychee.randomChance",
												chance,
												BoundsHelper.getDescription(randomSelect.rolls))
								  .withStyle(ChatFormatting.YELLOW));
			}
		}
		int c = randomSelect.showingConditionsCount() + child.showingConditionsCount();
		if (c > 0) {
			list.add(ClientProxy.format("contextual.lychee", c).withStyle(ChatFormatting.GRAY));
		}
		Minecraft mc = Minecraft.getInstance();
		randomSelect.getConditionTooltips(list, 0, mc.level, mc.player);
		child.getConditionTooltips(list, 0, mc.level, mc.player);
		return list;
	}

	default void render(T action, GuiGraphics graphics, int x, int y) {
	}

	default List<Component> getBaseTooltips(T action) {
		return Lists.newArrayList(action.getDisplayName());
	}

	default List<Component> getTooltips(T action) {
		List<Component> list = getBaseTooltips(action);
		int c = action.showingConditionsCount();
		if (c > 0) {
			list.add(ClientProxy.format("contextual.lychee", c).withStyle(ChatFormatting.GRAY));
		}
		Minecraft mc = Minecraft.getInstance();
		action.getConditionTooltips(list, 0, mc.level, mc.player);
		return list;
	}

	default void loadCatalystsInfo(T action, ILycheeRecipe<?> recipe, List<IngredientInfo> ingredients) {
	}
}
