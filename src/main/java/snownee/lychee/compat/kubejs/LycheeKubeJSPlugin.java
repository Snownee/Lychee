package snownee.lychee.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.core.Reference;
import snownee.lychee.core.contextual.CustomCondition;
import snownee.lychee.core.post.CustomAction;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.CommonProxy;

public class LycheeKubeJSPlugin extends KubeJSPlugin {

	@Override
	public void init() {
		Lychee.LOGGER.info("LycheeKubeJSPlugin is there!");
		CommonProxy.registerCustomActionListener(this::onCustomAction);
		CommonProxy.registerCustomConditionListener(this::onCustomCondition);
		if (CommonProxy.isPhysicalClient()) {
			ClientProxy.registerInfoBadgeClickListener(this::onInfoBadgeClicked);
		}
	}

	private boolean onCustomAction(String id, CustomAction action, ILycheeRecipe<?> recipe, ILycheeRecipe.NBTPatchContext patchContext) {
		if (LycheeKubeJSEvents.CUSTOM_ACTION.hasListeners())
			return LycheeKubeJSEvents.CUSTOM_ACTION.post(ScriptType.STARTUP, id, new CustomActionEventJS(id, action, recipe, patchContext)).override();
		return false;
	}

	private boolean onCustomCondition(String id, CustomCondition condition) {
		if (LycheeKubeJSEvents.CUSTOM_CONDITION.hasListeners())
			return LycheeKubeJSEvents.CUSTOM_CONDITION.post(ScriptType.STARTUP, id, new CustomConditionEventJS(id, condition)).override();
		return false;
	}

	private boolean onInfoBadgeClicked(ILycheeRecipe<?> recipe, int button) {
		if (LycheeKubeJSEvents.CLICKED_INFO_BADGE.hasListeners())
			return LycheeKubeJSEvents.CLICKED_INFO_BADGE.post(ScriptType.CLIENT, recipe.lychee$getId(), new ClickedInfoBadgeEventJS(recipe, button)).override();
		return false;
	}

	@Override
	public void registerEvents() {
		LycheeKubeJSEvents.GROUP.register();
	}

	@Override
	public void registerBindings(BindingsEvent event) {
		event.add("InteractionResult", InteractionResult.class);
		event.add("LootContextParams", LootContextParams.class);
		event.add("LycheeLootContextParams", LycheeLootContextParams.class);
		event.add("LycheeReference", Reference.class);
	}

	@Override
	public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
		if (typeWrappers.getWrapperFactory(LootContextParam.class, null) == null) {
			typeWrappers.registerSimple(LootContextParam.class, o -> {
				if (o instanceof LootContextParam<?> param) {
					return param;
				}
				if (o instanceof CharSequence || o instanceof ResourceLocation) {
					return LycheeLootContextParams.ALL.get(LycheeLootContextParams.trimRL(o.toString()));
				}
				return null;
			});
		}
	}
}
