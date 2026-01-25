package snownee.lychee.util.action;

import java.util.function.Function;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public record BlockBasedActionRenderer<T extends PostAction>(Function<T, BlockState> blockStateFunction) implements ActionRenderer<T> {
	public static <T extends PostAction> BlockBasedActionRenderer<T> fromPredicate(Function<T, BlockPredicate> function) {
		return new BlockBasedActionRenderer<>(function.andThen(BlockPredicateExtensions::anyBlockState));
	}

	@Override
	public void render(T action, GuiGraphics graphics, int x, int y) {
		var blockState = blockStateFunction.apply(action);
		if (blockState.isAir()) {
			GuiGameElement.of(Items.BARRIER).render(graphics, x, y);
			return;
		}
		GuiGameElement.of(blockState)
				.withRotationOffset(Vec3.ZERO)
				.rotateBlock(30, 225, 0)
				.scale(10)
				.at(x + 3, y + 3)
				.render(graphics);
	}
}
