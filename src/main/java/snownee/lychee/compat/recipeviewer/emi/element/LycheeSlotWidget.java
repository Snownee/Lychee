package snownee.lychee.compat.recipeviewer.emi.element;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.compat.recipeviewer.SlotType;

public class LycheeSlotWidget extends SlotWidget {
	public SlotType slotType;

	public LycheeSlotWidget(EmiIngredient stack, int x, int y, SlotType slotType) {
		super(stack, x, y);
		this.slotType = slotType;
	}

	@Override
	public void drawBackground(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		if (!drawBack) {
			return;
		}
		slotType.sprite.render(draw, x, y);
	}

	@Override
	public LycheeSlotWidget catalyst(boolean catalyst) {
		super.catalyst(catalyst);
		if (catalyst) {
			slotType = SlotType.CATALYST;
		}
		return this;
	}
}
