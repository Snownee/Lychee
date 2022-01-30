package snownee.lychee.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface ScreenElement {

	@Environment(EnvType.CLIENT)
	void render(PoseStack ms, int x, int y);

}
