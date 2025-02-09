package snownee.lychee.compat.jei.input;

import java.util.function.Supplier;

import com.mojang.blaze3d.platform.InputConstants;

import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.compat.jei.category.LycheeCategory;

public class BlockClickingInputHandler implements IJeiInputHandler {
	private final ScreenRectangle screenRectangle;
	private final Supplier<BlockState> blockFactory;

	public BlockClickingInputHandler(final ScreenRectangle screenRectangle, Supplier<BlockState> blockFactory) {
		this.screenRectangle = screenRectangle;
		this.blockFactory = blockFactory;
	}

	@Override
	public ScreenRectangle getArea() {
		return screenRectangle;
	}

	@Override
	public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
		if (input.getKey().getType() != InputConstants.Type.MOUSE) {
			return false;
		}
		if (blockFactory.get().isEmpty()) {
			return false;
		}
		if (input.isSimulate()) {
			return true;
		}
		return LycheeCategory.clickBlock(blockFactory.get(), input.getKey());
	}
}
