package snownee.lychee.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import snownee.lychee.Lychee;
import snownee.lychee.core.post.CustomAction;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.LUtil;

public class LycheeKubeJSPlugin extends KubeJSPlugin {

	@Override
	public void init() {
		Lychee.LOGGER.info("LycheeKubeJSPlugin is there!");
		LUtil.registerCustomActionListener(this::onCustomAction);
		if (LUtil.isPhysicalClient()) {
			ClientProxy.registerInfoBadgeClickListener(this::onInfoBadgeClicked);
		}
	}

	private boolean onCustomAction(String id, CustomAction action, ILycheeRecipe<?> recipe, ILycheeRecipe.NBTPatchContext patchContext) {
		return LycheeKubeJSEvents.CUSTOM_ACTION.post(id, new CustomActionEventJS(id, action, recipe, patchContext));
	}

	private boolean onInfoBadgeClicked(ILycheeRecipe<?> recipe, int button) {
		return LycheeKubeJSEvents.CLICKED_INFO_BADGE.post(recipe.lychee$getId(), new ClickedInfoBadgeEventJS(recipe, button));
	}

	@Override
	public void registerEvents() {
		LycheeKubeJSEvents.GROUP.register();
	}

}
