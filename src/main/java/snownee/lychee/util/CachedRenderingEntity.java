package snownee.lychee.util;

import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import snownee.lychee.client.gui.ILightingSettings;
import snownee.lychee.mixin.EntityAccess;

public class CachedRenderingEntity<T extends Entity> {

	protected T entity;
	protected float scale = 15;
	private Function<Level, T> factory;

	protected CachedRenderingEntity(@NotNull T entity) {
		setEntity(entity);
	}

	protected CachedRenderingEntity(Function<Level, T> factory) {
		this.factory = factory;
	}

	public static <T extends Entity> CachedRenderingEntity<T> of(@NotNull T entity) {
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
		entity.tickCount = Minecraft.getInstance().player.tickCount;
		return entity;
	}

	public void setEntity(@NotNull T entity) {
		this.entity = entity;
		this.factory = null;
		((EntityAccess) entity).callSetLevel(null);
	}

	public T earlySetLevel() {
		ensureEntity();
		((EntityAccess) entity).callSetLevel(Objects.requireNonNull(Minecraft.getInstance().level));
		return entity;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public void render(PoseStack matrixStack, float x, float y, float z, Quaternionf rotation) {
		var mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		ensureEntity();
		((EntityAccess) entity).callSetLevel(mc.level);
		entity.tickCount = mc.player.tickCount;
		var position = mc.player.position();
		entity.setPosRaw(position.x(), position.y(), position.z());

		matrixStack.pushPose();
		matrixStack.translate(x, y, z);
		matrixStack.scale(scale, scale, scale);

		matrixStack.mulPose(rotation);
		var renderDispatcher = mc.getEntityRenderDispatcher();
		rotation.conjugate();
		renderDispatcher.overrideCameraOrientation(rotation);

		renderDispatcher.setRenderShadow(false);
		var bufferSource = mc.renderBuffers().bufferSource();
		renderDispatcher.render(entity, 0.0D, 0.0D, 0.0D, mc.getFrameTime(), 1, matrixStack, bufferSource, 15728880);
		bufferSource.endBatch();
		renderDispatcher.setRenderShadow(true);

		matrixStack.popPose();
		//noinspection DataFlowIssue
		((EntityAccess) entity).callSetLevel(null);
		ILightingSettings.DEFAULT_3D.applyLighting();
	}

}
