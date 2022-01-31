package snownee.lychee;

import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import snownee.lychee.core.contextual.ContextualConditionType;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.interaction.BlockClickingRecipe;
import snownee.lychee.interaction.BlockInteractingRecipe;

@Mod(Lychee.ID)
@Mod.EventBusSubscriber(bus = Bus.MOD)
public final class Lychee {
	public static final String ID = "lychee";

	public static final Logger LOGGER = LogManager.getLogger(Lychee.ID);

	public Lychee() {
		RecipeTypes.init();
		LycheeTags.init();
		MinecraftForge.EVENT_BUS.addListener(Lychee::useItemOn);
		MinecraftForge.EVENT_BUS.addListener(Lychee::clickItemOn);
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

	public static void useItemOn(PlayerInteractEvent.RightClickBlock event) {
		ItemStack stack = event.getItemStack();
		Optional<BlockInteractingRecipe> result = RecipeTypes.BLOCK_INTERACTING.process(event.getPlayer(), stack, event.getPos(), event.getHitVec().getLocation(), builder -> {
			builder.withParameter(LootContextParams.TOOL, stack);
		});
		if (result.isPresent()) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.sidedSuccess(event.getWorld().isClientSide));
		}
	}

	public static void clickItemOn(PlayerInteractEvent.LeftClickBlock event) {
		ItemStack stack = event.getItemStack();
		Vec3 vec = Vec3.atCenterOf(event.getPos());
		Optional<BlockClickingRecipe> result = RecipeTypes.BLOCK_CLICKING.process(event.getPlayer(), stack, event.getPos(), vec, builder -> {
			builder.withParameter(LootContextParams.TOOL, stack);
		});
		if (result.isPresent()) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.sidedSuccess(event.getWorld().isClientSide));
		}
	}

	public static void onRecipesLoaded(RecipeManager recipeManager) {
		RecipeTypes.ALL.forEach($ -> $.updateEmptyState(recipeManager));
		RecipeTypes.ALL.forEach($ -> $.buildCache(recipeManager));
	}

	public static void onRecipesReplaced(RecipeManager recipeManager, Iterable<Recipe<?>> pRecipes) {
		Set<RecipeType<?>> types = Sets.newHashSet();
		for (Recipe<?> recipe : pRecipes) {
			types.add(recipe.getType());
		}
		for (RecipeType<?> type : types) {
			if (RecipeTypes.ALL.contains(type)) {
				((LycheeRecipeType<?, ?>) type).updateEmptyState(recipeManager);
				((LycheeRecipeType<?, ?>) type).buildCache(recipeManager);
			}
		}
	}

}
