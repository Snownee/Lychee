package snownee.lychee;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.NewRegistryEvent;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.contextual.ContextualConditionType;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.interaction.BlockClickingRecipe;
import snownee.lychee.interaction.BlockInteractingRecipe;

@Mod(Lychee.ID)
@Mod.EventBusSubscriber(bus = Bus.MOD)
public final class Lychee {
	public static final String ID = "lychee";

	public static final Logger LOGGER = LogManager.getLogger(Lychee.ID);

	public static boolean hasKiwi = ModList.get().isLoaded("kiwi");

	public Lychee() {
		LycheeTags.init();
		MinecraftForge.EVENT_BUS.addListener(Lychee::useItemOn);
		MinecraftForge.EVENT_BUS.addListener(Lychee::clickItemOn);
	}

	@SubscribeEvent
	public static void newRegistries(NewRegistryEvent event) {
		LycheeRegistries.init(event);
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
		RecipeTypes.init();
	}

	public static void useItemOn(PlayerInteractEvent.RightClickBlock event) {
		ItemStack stack = event.getItemStack();
		Player player = event.getPlayer();
		if (player.getCooldowns().isOnCooldown(stack.getItem())) {
			return;
		}
		LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(event.getWorld());
		builder.withParameter(LootContextParams.TOOL, stack);
		builder.withParameter(LycheeLootContextParams.DIRECTION, event.getFace());
		Optional<BlockInteractingRecipe> result = RecipeTypes.BLOCK_INTERACTING.process(player, stack, event.getPos(), event.getHitVec().getLocation(), builder);
		if (result.isPresent()) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.sidedSuccess(event.getWorld().isClientSide));
		}
	}

	public static void clickItemOn(PlayerInteractEvent.LeftClickBlock event) {
		ItemStack stack = event.getItemStack();
		Player player = event.getPlayer();
		if (player.getCooldowns().isOnCooldown(stack.getItem())) {
			return;
		}
		LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(event.getWorld());
		builder.withParameter(LootContextParams.TOOL, stack);
		builder.withParameter(LycheeLootContextParams.DIRECTION, event.getFace());
		Vec3 vec = Vec3.atCenterOf(event.getPos());
		Optional<BlockClickingRecipe> result = RecipeTypes.BLOCK_CLICKING.process(player, stack, event.getPos(), vec, builder);
		if (result.isPresent()) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.sidedSuccess(event.getWorld().isClientSide));
		}
	}

}
