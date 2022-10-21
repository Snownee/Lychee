package snownee.lychee;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.interaction.BlockClickingRecipe;
import snownee.lychee.interaction.BlockInteractingRecipe;

public final class Lychee implements ModInitializer {
	public static final String ID = "lychee";

	public static final Logger LOGGER = LogManager.getLogger(Lychee.ID);

	public static boolean hasKiwi;

	@Override
	public void onInitialize() {
		hasKiwi = FabricLoader.getInstance().isModLoaded("kiwi");
		RecipeTypes.init();
		LycheeTags.init();
		UseBlockCallback.EVENT.register(Lychee::useItemOn);
		AttackBlockCallback.EVENT.register(Lychee::clickItemOn);
		LycheeRegistries.init();
		ContextualConditionTypes.init();
		PostActionTypes.init();
		RecipeSerializers.init();
	}

	public static InteractionResult useItemOn(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		if (player.isSpectator()) {
			return InteractionResult.PASS;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (player.getCooldowns().isOnCooldown(stack.getItem()))
			return InteractionResult.PASS;
		LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(world);
		builder.withParameter(LootContextParams.TOOL, stack);
		Optional<BlockInteractingRecipe> result = RecipeTypes.BLOCK_INTERACTING.process(player, stack, hitResult.getBlockPos(), hitResult.getLocation(), builder);
		if (result.isPresent()) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public static InteractionResult clickItemOn(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
		if (player.isSpectator()) {
			return InteractionResult.PASS;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (player.getCooldowns().isOnCooldown(stack.getItem()))
			return InteractionResult.PASS;
		Vec3 vec = Vec3.atCenterOf(pos);
		LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(world);
		builder.withParameter(LootContextParams.TOOL, stack);
		Optional<BlockClickingRecipe> result = RecipeTypes.BLOCK_CLICKING.process(player, stack, pos, vec, builder);
		if (result.isPresent()) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

}
