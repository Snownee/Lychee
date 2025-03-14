package snownee.lychee.compat.rv;

import snownee.lychee.Lychee;
import snownee.lychee.category.SpriteElement;
import snownee.lychee.category.SpriteElementRenderer;
import snownee.lychee.client.gui.ScreenElement;

public enum SlotType {
	NORMAL("slot"),
	CHANCE("chance_slot"),
	CATALYST("catalyst_slot");

	public final ScreenElement sprite;

	SlotType(String spriteId) {
		this.sprite = new SpriteElementRenderer(new SpriteElement(Lychee.id(spriteId)), 0, 0, 0, 18, 18, 1);
	}
}
