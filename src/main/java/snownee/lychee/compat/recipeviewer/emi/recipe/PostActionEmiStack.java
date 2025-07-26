package snownee.lychee.compat.recipeviewer.emi.recipe;

import java.util.List;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.contextual.Chance;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.contextual.ContextualConditionType;

public class PostActionEmiStack extends EmiStack {
	private final PostAction action;

	public PostActionEmiStack(PostAction action) {
		this.action = action;
		Chance chance = (Chance) action.conditions().conditions().stream()
				.filter($ -> $.type() == ContextualConditionType.CHANCE)
				.findFirst()
				.orElse(null);
		if (chance != null) {
			setChance(chance.chance());
		}
	}

	@Override
	public EmiStack copy() {
		return new PostActionEmiStack(action);
	}

	@Override
	public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
		ActionRenderer.of(action).render(action, draw, x, y);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public DataComponentPatch getComponentChanges() {
		return DataComponentPatch.EMPTY;
	}

	@Override
	public Object getKey() {
		return action;
	}

	@Override
	public ResourceLocation getId() {
		return LycheeRegistries.POST_ACTION.getKey(action.type());
	}

	@Override
	public List<Component> getTooltipText() {
		return ActionRenderer.of(action).getTooltips(action, Minecraft.getInstance().player);
	}

	@Override
	public Component getName() {
		return action.getDisplayName();
	}
}
