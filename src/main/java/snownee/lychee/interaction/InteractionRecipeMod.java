package snownee.lychee.interaction;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;

public class InteractionRecipeMod {

	public static InteractionResult useItemOn(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		if (player.isSpectator()) {
			return InteractionResult.PASS;
		}
		if (hand == InteractionHand.OFF_HAND && player.getOffhandItem().isEmpty()) {
			return InteractionResult.PASS;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (player.getCooldowns().isOnCooldown(stack.getItem()))
			return InteractionResult.PASS;
		LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(world);
		builder.withParameter(LycheeLootContextParams.DIRECTION, hitResult.getDirection());
		Optional<BlockInteractingRecipe> result = RecipeTypes.BLOCK_INTERACTING.process(player, hand, hitResult.getBlockPos(), hitResult.getLocation(), builder);
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
		builder.withParameter(LycheeLootContextParams.DIRECTION, direction);
		Optional<BlockClickingRecipe> result = RecipeTypes.BLOCK_CLICKING.process(player, hand, pos, vec, builder);
		if (result.isPresent()) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

}
