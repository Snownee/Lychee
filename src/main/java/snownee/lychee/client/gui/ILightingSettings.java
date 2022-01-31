package snownee.lychee.client.gui;

import com.mojang.blaze3d.platform.Lighting;

public interface ILightingSettings {

	void applyLighting();

	ILightingSettings DEFAULT_3D = () -> Lighting.setupFor3DItems();
	ILightingSettings DEFAULT_FLAT = () -> Lighting.setupForFlatItems();

}