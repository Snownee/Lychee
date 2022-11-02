package snownee.lychee.dripstone_dripping;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.Codec;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.LUtil;

public class DripstoneRecipeMod {

	public static final Cache<Block, DripParticleHandler> particleHandlers = CacheBuilder.newBuilder().build();

	public static ParticleType<BlockParticleOption> DRIPSTONE_DRIPPING;
	public static ParticleType<BlockParticleOption> DRIPSTONE_FALLING;
	public static ParticleType<BlockParticleOption> DRIPSTONE_SPLASH;

	public static void onInitialize() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(DripstoneRecipeMod::registerParticleTypes);
	}

	public static void registerParticleTypes(RegisterEvent event) {
		if (ForgeRegistries.PARTICLE_TYPES.equals(event.getForgeRegistry())) {
			ForgeRegistries.PARTICLE_TYPES.register(new ResourceLocation(Lychee.ID, "dripstone_dripping"), DRIPSTONE_DRIPPING = new DripstoneParticleType());
			ForgeRegistries.PARTICLE_TYPES.register(new ResourceLocation(Lychee.ID, "dripstone_falling"), DRIPSTONE_FALLING = new DripstoneParticleType());
			ForgeRegistries.PARTICLE_TYPES.register(new ResourceLocation(Lychee.ID, "dripstone_splash"), DRIPSTONE_SPLASH = new DripstoneParticleType());
		}
	}

	public static boolean spawnDripParticle(Level level, BlockPos blockPos, BlockState blockState) {
		BlockState sourceBlock = DripstoneRecipe.getBlockAboveStalactite(level, blockPos, blockState);
		if (sourceBlock == null || !RecipeTypes.DRIPSTONE_DRIPPING.hasSource(sourceBlock.getBlock())) {
			return false;
		}
		FluidState sourceFluid = sourceBlock.getFluidState();
		if (sourceFluid.is(FluidTags.LAVA) || sourceFluid.is(FluidTags.WATER)) {
			return false;
		}
		DripParticleHandler handler = getParticleHandler(sourceBlock);
		if (handler == null) {
			return false;
		}
		Vec3 vec3 = blockState.getOffset(level, blockPos);
		double e = (double) blockPos.getX() + 0.5 + vec3.x;
		double f = (double) ((float) (blockPos.getY() + 1) - 0.6875f) - 0.0625;
		double g = (double) blockPos.getZ() + 0.5 + vec3.z;
		handler.addParticle(level, blockPos, sourceBlock, e, f, g);
		return true;
	}

	@SuppressWarnings("deprecation")
	public static DripParticleHandler getParticleHandler(BlockState sourceBlock) {
		Block block = sourceBlock.getBlock();
		particleHandlers.invalidateAll();
		try {
			return particleHandlers.get(block, () -> {
				if (!LUtil.isPhysicalClient()) {
					return DripParticleHandler.SIMPLE_DUMMY;
				}
				BlockState defaultState = block.defaultBlockState();
				int color = defaultState.getMapColor(Minecraft.getInstance().level, BlockPos.ZERO).col;
				return new DripParticleHandler.Simple(color, defaultState.getLightEmission() > 4);
			});
		} catch (ExecutionException e) {
			Lychee.LOGGER.catching(e);
		}
		return null;
	}

	public static class DripstoneParticleType extends ParticleType<BlockParticleOption> {

		public DripstoneParticleType() {
			super(false, BlockParticleOption.DESERIALIZER);
		}

		@Override
		public Codec<BlockParticleOption> codec() {
			return BlockParticleOption.codec(this);
		}

	}

}