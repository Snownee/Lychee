package snownee.lychee.compat.recipeviewer;

import snownee.lychee.Lychee;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.ui.SpriteElementRenderer;

public enum SlotType {
	NORMAL("slot"),
	CHANCE("chance_slot"),
	CATALYST("catalyst_slot");

	public final ScreenElement sprite;

	SlotType(String spriteId) {
		this.sprite = new SpriteElementRenderer(Lychee.id(spriteId)).withSize(18);
	}
}
