package snownee.lychee.client.gui;

import com.mojang.blaze3d.platform.Lighting;

public interface ILightingSettings {

	void applyLighting();

	ILightingSettings DEFAULT_3D = () -> Lighting.setupFor3DItems();
	ILightingSettings DEFAULT_FLAT = () -> Lighting.setupForFlatItems();
	/* off */
	ILightingSettings DEFAULT_JEI = CustomLightingSettings.builder()
			.firstLightRotation(12.5f, 45.0f)
			.secondLightRotation(-20.0f, 50.0f)
			.build();
	/* on */

}
