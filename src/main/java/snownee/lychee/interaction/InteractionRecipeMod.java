package snownee.lychee.interaction;

import java.util.Optional;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;

public class InteractionRecipeMod {

	public static void onInitialize() {
		MinecraftForge.EVENT_BUS.addListener(InteractionRecipeMod::useItemOn);
		MinecraftForge.EVENT_BUS.addListener(InteractionRecipeMod::clickItemOn);
	}

	public static void useItemOn(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		if (player.isSpectator()) {
			return;
		}
		ItemStack stack = event.getItemStack();
		if (player.getCooldowns().isOnCooldown(stack.getItem()))
			return;
		LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(event.getLevel());
		builder.withParameter(LootContextParams.TOOL, stack);
		builder.withParameter(LycheeLootContextParams.DIRECTION, event.getFace());
		Optional<BlockInteractingRecipe> result = RecipeTypes.BLOCK_INTERACTING.process(player, stack, event.getPos(), event.getHitVec().getLocation(), builder);
		if (result.isPresent()) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
		}
	}

	public static void clickItemOn(PlayerInteractEvent.LeftClickBlock event) {
		Player player = event.getEntity();
		if (player.isSpectator()) {
			return;
		}
		ItemStack stack = event.getItemStack();
		if (player.getCooldowns().isOnCooldown(stack.getItem()))
			return;
		LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(event.getLevel());
		builder.withParameter(LootContextParams.TOOL, stack);
		builder.withParameter(LycheeLootContextParams.DIRECTION, event.getFace());
		Vec3 vec = Vec3.atCenterOf(event.getPos());
		Optional<BlockClickingRecipe> result = RecipeTypes.BLOCK_CLICKING.process(player, stack, event.getPos(), vec, builder);
		if (result.isPresent()) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
		}
	}

}