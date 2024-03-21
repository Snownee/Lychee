package snownee.lychee.compat.rei.elements;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.ScreenElement;

public class LycheeSlot extends EntryWidget {
    public LycheeSlot(Point point, SlotType type) {
        super(point);
    }

    public LycheeSlot(Rectangle bounds, SlotType type) {
        super(bounds);
    }

    @Override
    protected void drawBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.drawBackground(graphics, mouseX, mouseY, delta);
    }

    public enum SlotType {
        NORMAL(AllGuiTextures.JEI_SLOT),
        CHANCE(AllGuiTextures.JEI_CHANCE_SLOT),
        CATALYST(AllGuiTextures.JEI_CATALYST_SLOT);

        final ScreenElement element;

        SlotType(AllGuiTextures element) {
            this.element = new ScreenElementWidget(element).element;
        }
    }
}
