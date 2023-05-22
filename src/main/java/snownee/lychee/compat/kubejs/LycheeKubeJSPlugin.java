package snownee.lychee.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.minecraft.world.InteractionResult;
import snownee.lychee.Lychee;
import snownee.lychee.core.Reference;
import snownee.lychee.core.contextual.CustomCondition;
import snownee.lychee.core.post.CustomAction;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.LUtil;

public class LycheeKubeJSPlugin extends KubeJSPlugin {

	@Override
	public void init() {
		Lychee.LOGGER.info("LycheeKubeJSPlugin is there!");
		LUtil.registerCustomActionListener(this::onCustomAction);
		LUtil.registerCustomConditionListener(this::onCustomCondition);
		if (LUtil.isPhysicalClient()) {
			ClientProxy.registerInfoBadgeClickListener(this::onInfoBadgeClicked);
		}
	}

	private boolean onCustomAction(String id, CustomAction action, ILycheeRecipe<?> recipe, ILycheeRecipe.NBTPatchContext patchContext) {
		return LycheeKubeJSEvents.CUSTOM_ACTION.post(id, new CustomActionEventJS(id, action, recipe, patchContext));
	}

	private boolean onCustomCondition(String id, CustomCondition condition) {
		return LycheeKubeJSEvents.CUSTOM_CONDITION.post(id, new CustomConditionEventJS(id, condition));
	}

	private boolean onInfoBadgeClicked(ILycheeRecipe<?> recipe, int button) {
		return LycheeKubeJSEvents.CLICKED_INFO_BADGE.post(recipe.lychee$getId(), new ClickedInfoBadgeEventJS(recipe, button));
	}

	@Override
	public void registerEvents() {
		LycheeKubeJSEvents.GROUP.register();
	}

	@Override
	public void registerBindings(BindingsEvent event) {
		event.add("InteractionResult", InteractionResult.class);
		event.add("LycheeReference", Reference.class);
	}

}
