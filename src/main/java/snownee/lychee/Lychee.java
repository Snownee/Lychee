package snownee.lychee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import snownee.lychee.core.contextual.ContextualConditionType;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.item_inside.ItemInsideRecipe;

@Mod(Lychee.ID)
@Mod.EventBusSubscriber(bus = Bus.MOD)
public final class Lychee {
	public static final String ID = "lychee";

	public static final Logger LOGGER = LogManager.getLogger(Lychee.ID);

	public Lychee() {
		RecipeTypes.init();
		LycheeLootContextParamSets.init();
		LycheeTags.init();
	}

	@SubscribeEvent
	public static void newRegistries(RegistryEvent.NewRegistry event) {
		LycheeRegistries.init();
	}

	@SubscribeEvent
	public static void registerContextual(RegistryEvent.Register<ContextualConditionType<?>> event) {
		ContextualConditionTypes.init();
	}

	@SubscribeEvent
	public static void registerPostAction(RegistryEvent.Register<PostActionType<?>> event) {
		PostActionTypes.init();
	}

	@SubscribeEvent
	public static void registerRegisterSerializer(RegistryEvent.Register<RecipeSerializer<?>> event) {
		RecipeSerializers.init();
	}

	public static void onRecipesLoaded(RecipeManager recipeManager) {
		RecipeTypes.ALL.forEach($ -> $.updateEmptyState(recipeManager));
		ItemInsideRecipe.buildCache(RecipeTypes.ITEM_INSIDE.recipes(recipeManager));
	}

}
