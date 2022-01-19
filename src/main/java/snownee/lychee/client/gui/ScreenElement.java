package snownee.lychee.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ScreenElement {

	@OnlyIn(Dist.CLIENT)
	void render(PoseStack ms, int x, int y);

}
