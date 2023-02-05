package snownee.lychee;

import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import snownee.lychee.compat.ingredient_extension.AlwaysTrueIngredient;
import snownee.lychee.dripstone_dripping.DripstoneRecipeMod;
import snownee.lychee.dripstone_dripping.client.DripstoneRecipeModClient;
import snownee.lychee.interaction.InteractionRecipeMod;
import snownee.lychee.util.LUtil;

@Mod(Lychee.ID)
@Mod.EventBusSubscriber(bus = Bus.MOD)
public final class Lychee {
	public static final String ID = "lychee";

	public static final Logger LOGGER = LogManager.getLogger(Lychee.ID);

	public static boolean hasKiwi;

	public Lychee() {
		hasKiwi = LUtil.isModLoaded("kiwi");
		LycheeTags.init();
		InteractionRecipeMod.onInitialize();
		DripstoneRecipeMod.onInitialize();
		if (LUtil.isPhysicalClient()) {
			DripstoneRecipeModClient.onInitializeClient();
		}
	}

	@SubscribeEvent
	public static void newRegistries(NewRegistryEvent event) {
		LycheeRegistries.init(event);
	}

	@SubscribeEvent
	public static void register(RegisterEvent event) {
		event.register(LycheeRegistries.CONTEXTUAL.getRegistryKey(), helper -> ContextualConditionTypes.init());
		event.register(LycheeRegistries.POST_ACTION.getRegistryKey(), helper -> PostActionTypes.init());
		event.register(ForgeRegistries.RECIPE_SERIALIZERS.getRegistryKey(), helper -> {
			RecipeSerializers.init();
			CraftingHelper.register(new ResourceLocation(Lychee.ID, "always_true"), AlwaysTrueIngredient.Serializer.INSTANCE);
		});
		event.register(ForgeRegistries.RECIPE_TYPES.getRegistryKey(), helper -> RecipeTypes.init());
	}

	@SubscribeEvent
	public static void RegisterRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
		Function<Recipe<?>, RecipeBookCategories> lookup = $ -> RecipeBookCategories.UNKNOWN;
		RecipeTypes.ALL.forEach($ -> event.registerRecipeCategoryFinder($, lookup));
	}

}
