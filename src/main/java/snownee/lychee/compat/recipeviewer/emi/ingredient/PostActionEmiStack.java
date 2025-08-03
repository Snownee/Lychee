package snownee.lychee.compat.recipeviewer.emi.ingredient;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.tooltip.EmiTextTooltipWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
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
		Chance chance = (Chance) action.conditions()
				.conditions()
				.stream()
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
		ActionRenderer.of(action).internalRender(action, draw, x, y);
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
	public List<ClientTooltipComponent> getTooltip() {
		List<ClientTooltipComponent> list = Lists.newArrayList();
		List<Component> text = getTooltipText();
		if (!text.isEmpty()) {
			list.add(new EmiTextTooltipWrapper(this, EmiPort.ordered(text.getFirst())));
		}
		list.addAll(text.stream().skip(1).map(EmiTooltipComponents::of).toList());
		String namespace = getId().getNamespace();
		EmiTooltipComponents.appendModName(list, namespace);
		list.addAll(super.getTooltip());
		return list;
	}

	@Override
	public Component getName() {
		return action.getDisplayName();
	}
}
