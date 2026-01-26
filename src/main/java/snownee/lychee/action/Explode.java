package snownee.lychee.action;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Explode(
		PostActionCommonProperties commonProperties,
		Level.ExplosionInteraction explosionInteraction,
		BlockPos offset,
		boolean fire,
		float radius,
		float step) implements PostAction {

	@Override
	public PostActionType<Explode> type() {
		return PostActionTypes.EXPLODE;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		var pos = lootParams.get(LootContextParams.ORIGIN).add(Vec3.atLowerCornerOf(offset));
		var boundedRadius = Math.min(radius + step * (Mth.sqrt(times) - 1), radius * 4);
		context.level().explode(
				lootParams.get(LootContextParams.THIS_ENTITY),
				null, //FIXME check ExplodeEffect.java
				null,
				pos.x,
				pos.y,
				pos.z,
				boundedRadius,
				fire,
				explosionInteraction,
				ParticleTypes.EXPLOSION, //FIXME check ExplodeEffect.java
				ParticleTypes.EXPLOSION_EMITTER,
				Level.DEFAULT_EXPLOSION_BLOCK_PARTICLES,
				SoundEvents.GENERIC_EXPLODE);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(CommonProxy.makeDescriptionId("postAction", LycheeRegistries.POST_ACTION.getKey(type())) + "." +
				explosionInteraction.name().toLowerCase(Locale.ENGLISH));
	}

	public static class Type implements PostActionType<Explode> {
		public static final MapCodec<Explode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(Explode::commonProperties),
				Level.ExplosionInteraction.CODEC.optionalFieldOf("block_interaction", Level.ExplosionInteraction.BLOCK)
						.forGetter(Explode::explosionInteraction),
				LycheeCodecs.OFFSET.forGetter(Explode::offset),
				Codec.BOOL.optionalFieldOf("fire", false).forGetter(Explode::fire),
				ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("radius", 4F).forGetter(Explode::radius),
				Codec.FLOAT.optionalFieldOf("radius_step", 4F).forGetter(Explode::step)
		).apply(instance, Explode::new));

		@Override
		public MapCodec<Explode> codec() {
			return CODEC;
		}
	}
}
