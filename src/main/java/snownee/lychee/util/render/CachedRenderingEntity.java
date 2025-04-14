package snownee.lychee.util.render;

import java.util.Objects;
import java.util.function.Function;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import snownee.lychee.client.gui.ILightingSettings;
import snownee.lychee.mixin.EntityAccess;

public class CachedRenderingEntity<T extends Entity> {

	protected T entity;
	protected float scale = 15;
	protected Vector3f translation = new Vector3f(0, 0, 20);
	private Function<Level, T> factory;

	protected CachedRenderingEntity(T entity) {
		setEntity(entity);
	}

	protected CachedRenderingEntity(Function<Level, T> factory) {
		this.factory = factory;
	}

	public static <T extends Entity> CachedRenderingEntity<T> of(T entity) {
		return new CachedRenderingEntity<>(entity);
	}

	public static <T extends Entity> CachedRenderingEntity<T> ofFactory(Function<Level, T> factory) {
		return new CachedRenderingEntity<>(factory);
	}

	private void ensureEntity() {
		if (entity == null) {
			entity = Objects.requireNonNull(factory.apply(Minecraft.getInstance().level));
			factory = null;
		}
	}

	public T getEntity() {
		ensureEntity();
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			entity.tickCount = player.tickCount;
		}
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
		this.factory = null;
		((EntityAccess) entity).callSetLevel(null);
	}

	public void earlySetLevel() {
		ensureEntity();
		((EntityAccess) entity).callSetLevel(Objects.requireNonNull(Minecraft.getInstance().level));
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public Vector3f getTranslation() {
		return translation;
	}

	public void render(PoseStack matrixStack, Quaternionf rotation) {
		var mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}
		ensureEntity();
		((EntityAccess) entity).callSetLevel(mc.level);
		entity.tickCount = (int) (mc.level.getGameTime() % 240000L);
		var position = mc.gameRenderer.getMainCamera().getPosition();
		entity.setPosRaw(position.x(), position.y(), position.z());

		matrixStack.pushPose();
		matrixStack.translate(translation.x, translation.y, translation.z);
		matrixStack.scale(scale, scale, scale);

		matrixStack.mulPose(rotation);
		var renderDispatcher = mc.getEntityRenderDispatcher();
		rotation.conjugate();
		renderDispatcher.overrideCameraOrientation(rotation);

		renderDispatcher.setRenderShadow(false);
		var bufferSource = mc.renderBuffers().bufferSource();
		renderDispatcher.render(
				entity,
				0.0D,
				0.0D,
				0.0D,
				mc.getTimer().getGameTimeDeltaPartialTick(true),
				1,
				matrixStack,
				bufferSource,
				15728880);
		bufferSource.endBatch();
		renderDispatcher.setRenderShadow(true);

		matrixStack.popPose();
		((EntityAccess) entity).callSetLevel(null);
		ILightingSettings.DEFAULT_3D.applyLighting();
	}

}
