package snownee.lychee.action;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Explode(
		PostActionCommonProperties commonProperties,
		BlockInteraction blockInteraction,
		BlockPos offset,
		boolean fire,
		float radius,
		float step) implements PostAction {

	private void explode(
			Level level,
			@Nullable Entity source,
			Vec3 pos,
			float radius) {
		var explosion = new Explosion(level, source, pos.x, pos.y, pos.z, radius, fire, blockInteraction);
		explosion.explode();
		explosion.finalizeExplosion(true);
	}

	@Override
	public PostActionType<Explode> type() {
		return PostActionTypes.EXPLODE;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var pos = lootParamsContext.getOrNull(LootContextParams.ORIGIN).add(Vec3.atLowerCornerOf(offset));
		var boundedRadius = Math.min(radius + step * (Mth.sqrt(times) - 1), radius * 4);
		explode(context.get(LycheeContextKey.LEVEL), lootParamsContext.get(LootContextParams.THIS_ENTITY), pos, boundedRadius);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(CommonProxy.makeDescriptionId("postAction", LycheeRegistries.POST_ACTION.getKey(type())) + "." +
				blockInteraction.name().toLowerCase(Locale.ENGLISH));
	}

	public static class Type implements PostActionType<Explode> {
		public static final Codec<Explode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(Explode::commonProperties),
				Codec.STRING.comapFlatMap(
								it -> switch (it) {
									case "none", "keep" -> DataResult.success(BlockInteraction.KEEP);
									case "break", "destroy_with_decay" -> DataResult.success(BlockInteraction.DESTROY_WITH_DECAY);
									case "destroy" -> DataResult.success(BlockInteraction.DESTROY);
									default -> DataResult.error(() -> "Unexpected value: " + it);
								},
								it -> it.name().toLowerCase(Locale.ENGLISH))
						.optionalFieldOf("block_interaction", BlockInteraction.DESTROY)
						.forGetter(Explode::blockInteraction),
				RecordCodecBuilder.<BlockPos>mapCodec(posInstance -> posInstance.group(
						Codec.INT.fieldOf("offsetX").forGetter(Vec3i::getX),
						Codec.INT.fieldOf("offsetY").forGetter(Vec3i::getY),
						Codec.INT.fieldOf("offsetZ").forGetter(Vec3i::getZ)
				).apply(posInstance, BlockPos::new)).forGetter(it -> it.offset),
				Codec.BOOL.optionalFieldOf("fire", false).forGetter(Explode::fire),
				Codec.FLOAT.optionalFieldOf("radius", 4F).forGetter(Explode::radius),
				Codec.FLOAT.optionalFieldOf("radius_step", 4F).forGetter(Explode::step)
		).apply(instance, Explode::new));

		@Override
		public @NotNull Codec<Explode> codec() {
			return CODEC;
		}
	}
}
