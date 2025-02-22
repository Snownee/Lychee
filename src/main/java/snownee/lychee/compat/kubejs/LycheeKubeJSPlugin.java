package snownee.lychee.compat.kubejs;

import org.jetbrains.annotations.Nullable;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.KUtil;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.action.CustomAction;
import snownee.lychee.contextual.CustomCondition;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.Reference;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class LycheeKubeJSPlugin implements KubeJSPlugin {

	@Override
	public void init() {
		Lychee.LOGGER.info("LycheeKubeJSPlugin is there!");
		CommonProxy.registerCustomActionListener(this::onCustomAction);
		CommonProxy.registerCustomConditionListener(this::onCustomCondition);
		if (Platform.isPhysicalClient()) {
			ClientProxy.registerInfoBadgeClickListener(this::onInfoBadgeClicked);
		}
	}

	private boolean onCustomAction(String id, CustomAction action, ILycheeRecipe<?> recipe) {
		if (LycheeKubeJSEvents.CUSTOM_ACTION.hasListeners()) {
			return LycheeKubeJSEvents.CUSTOM_ACTION.post(ScriptType.STARTUP, id, new CustomActionKubeEvent(id, action, recipe)).override();
		}
		return false;
	}

	private boolean onCustomCondition(String id, CustomCondition condition) {
		if (LycheeKubeJSEvents.CUSTOM_CONDITION.hasListeners()) {
			return LycheeKubeJSEvents.CUSTOM_CONDITION.post(ScriptType.STARTUP, id, new CustomConditionKubeEvent(id, condition)).override();
		}
		return false;
	}

	private boolean onInfoBadgeClicked(ILycheeRecipe<?> recipe, @Nullable ResourceLocation id, int button) {
		if (LycheeKubeJSEvents.CLICKED_INFO_BADGE.hasListeners()) {
			return LycheeKubeJSEvents.CLICKED_INFO_BADGE.post(
					ScriptType.CLIENT,
					id == null ? null : id.toString(),
					new ClickedInfoBadgeKubeEvent(recipe, id, button)).override();
		}
		return false;
	}

	@Override
	public void registerEvents(EventGroupRegistry registry) {
		registry.register(LycheeKubeJSEvents.GROUP);
	}

	@Override
	public void registerBindings(BindingRegistry bindings) {
		bindings.add("Lychee", LycheeKubeJSUtils.class);
		bindings.add("LootContextParams", LootContextParams.class);
		bindings.add("LycheeLootContextParams", LycheeLootContextParams.class);
		bindings.add("LycheeReference", Reference.class);
		bindings.add("LycheeContextKey", LycheeContextKey.class);
	}

	@Override
	public void registerTypeWrappers(TypeWrapperRegistry registry) {
		registry.register(
				LootContextParam.class, o -> {
					if (o instanceof LootContextParam<?> param) {
						return param;
					}
					if (o instanceof CharSequence || o instanceof ResourceLocation) {
						return LycheeLootContextParams.ALL.get(KUtil.trimRL(o.toString()));
					}
					return null;
				});
	}
}
