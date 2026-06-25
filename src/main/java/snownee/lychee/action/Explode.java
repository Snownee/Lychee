package snownee.lychee.action;

import java.util.Locale;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.util.codec.AliasOptionalFieldCodec;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;

public record Explode(
		PostActionCommonProperties commonProperties,
		boolean attributeToEntity,
		Optional<Holder<DamageType>> damageType,
		Optional<LevelBasedValue> knockbackMultiplier,
		Optional<HolderSet<Block>> immuneBlocks,
		Vec3 offset,
		LevelBasedValue radius,
		boolean createFire,
		Level.ExplosionInteraction blockInteraction,
		ParticleOptions smallParticle,
		ParticleOptions largeParticle,
		WeightedList<ExplosionParticleInfo> blockParticles,
		Holder<SoundEvent> sound) implements PostAction {

	@Override
	public PostActionType<Explode> type() {
		return PostActionTypes.EXPLODE;
	}

	@Override
	public void apply(LycheeContext context, ActionContext actionContext, int times) {
		var pos = actionContext.get(LootContextParams.ORIGIN).add(offset);
		Entity entity = actionContext.get(LootContextParams.THIS_ENTITY);
		context.level().explode(
				attributeToEntity ? entity : null,
				getDamageSource(entity, pos),
				new SimpleExplosionDamageCalculator(
						blockInteraction != Level.ExplosionInteraction.NONE,
						damageType.isPresent(),
						knockbackMultiplier.map(value -> value.calculate(times)),
						immuneBlocks),
				pos.x,
				pos.y,
				pos.z,
				Math.max(radius.calculate(times), 0.0F),
				createFire,
				blockInteraction,
				smallParticle,
				largeParticle,
				blockParticles,
				sound);
	}

	@Nullable
	private DamageSource getDamageSource(final Entity entity, final Vec3 position) {
		return damageType.map(damageTypeHolder -> attributeToEntity ?
				new DamageSource(damageTypeHolder, entity) :
				new DamageSource(damageTypeHolder, position)).orElse(null);
	}

	@Override
	public Component getName() {
		return Component.translatable(CommonProxy.makeDescriptionId("postAction", LycheeRegistries.POST_ACTION.getKey(type())) + "." +
				blockInteraction.name().toLowerCase(Locale.ENGLISH));
	}

	public static class Type implements PostActionType<Explode> {
		public static final MapCodec<Explode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(Explode::commonProperties),
						Codec.BOOL.optionalFieldOf("attribute_to_entity", true).forGetter(Explode::attributeToEntity),
						DamageType.CODEC.optionalFieldOf("damage_type").forGetter(Explode::damageType),
						LevelBasedValue.CODEC.optionalFieldOf("knockback_multiplier").forGetter(Explode::knockbackMultiplier),
						RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("immune_blocks").forGetter(Explode::immuneBlocks),
						Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(Explode::offset),
						LevelBasedValue.CODEC.optionalFieldOf("radius", LevelBasedValue.perLevel(4)).forGetter(Explode::radius),
						AliasOptionalFieldCodec.defaulted("create_fire", Codec.BOOL, false, "fire").forGetter(Explode::createFire),
						Level.ExplosionInteraction.CODEC.optionalFieldOf("block_interaction", Level.ExplosionInteraction.BLOCK)
								.forGetter(Explode::blockInteraction),
						ParticleTypes.CODEC.optionalFieldOf("small_particle", ParticleTypes.EXPLOSION).forGetter(Explode::smallParticle),
						ParticleTypes.CODEC.optionalFieldOf("large_particle", ParticleTypes.EXPLOSION_EMITTER).forGetter(Explode::largeParticle),
						WeightedList.codec(ExplosionParticleInfo.CODEC)
								.optionalFieldOf("block_particles", Level.DEFAULT_EXPLOSION_BLOCK_PARTICLES)
								.forGetter(Explode::blockParticles),
						SoundEvent.CODEC.optionalFieldOf("sound", SoundEvents.GENERIC_EXPLODE).forGetter(Explode::sound))
				.apply(instance, Explode::new));

		@Override
		public MapCodec<Explode> codec() {
			return CODEC;
		}
	}
}
