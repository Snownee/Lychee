package snownee.lychee.core.def;

import java.util.Optional;

import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.mixin.LightPredicateAccess;
import snownee.lychee.mixin.LocationPredicateAccess;
import snownee.lychee.util.LUtil;

public class LocationPredicateHelper {

	public static LocationPredicate.Builder fromNetwork(FriendlyByteBuf pBuffer) {
		LocationPredicate.Builder builder = LocationPredicate.Builder.location();
		builder.setX(DoubleBoundsHelper.fromNetwork(pBuffer));
		builder.setY(DoubleBoundsHelper.fromNetwork(pBuffer));
		builder.setZ(DoubleBoundsHelper.fromNetwork(pBuffer));
		builder.setBlock(BlockPredicateHelper.fromNetwork(pBuffer));
		//TODO fluid
		Ints ints = IntBoundsHelper.fromNetwork(pBuffer);
		if (ints != Ints.ANY) {
			builder.setLight(LightPredicate.Builder.light().setComposite(ints).build());
		}
		ResourceLocation dim = LUtil.readNullableRL(pBuffer);
		if (dim != null) {
			builder.setDimension(ResourceKey.create(Registry.DIMENSION_REGISTRY, dim));
		}
		ResourceLocation biome = LUtil.readNullableRL(pBuffer);
		if (biome != null) {
			builder.setBiome(ResourceKey.create(Registry.BIOME_REGISTRY, biome));
		}
		ResourceLocation feature = LUtil.readNullableRL(pBuffer);
		if (feature != null) {
			builder.setFeature(ResourceKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, feature));
		}
		byte smokey = pBuffer.readByte();
		if (smokey < 2) {
			builder.setSmokey(smokey == 1);
		}
		return builder;
	}

	public static void toNetwork(LocationPredicate predicate, FriendlyByteBuf pBuffer) {
		LocationPredicateAccess access = (LocationPredicateAccess) predicate;
		DoubleBoundsHelper.toNetwork(access.getX(), pBuffer);
		DoubleBoundsHelper.toNetwork(access.getY(), pBuffer);
		DoubleBoundsHelper.toNetwork(access.getZ(), pBuffer);
		BlockPredicateHelper.toNetwork(access.getBlock(), pBuffer);
		//		access.getFluid();
		Ints ints = ((LightPredicateAccess) access.getLight()).getComposite();
		IntBoundsHelper.toNetwork(ints, pBuffer);
		ResourceLocation dim = Optional.ofNullable(access.getDimension()).map(ResourceKey::location).orElse(null);
		LUtil.writeNullableRL(dim, pBuffer);
		ResourceLocation biome = Optional.ofNullable(access.getBiome()).map(ResourceKey::location).orElse(null);
		LUtil.writeNullableRL(biome, pBuffer);
		ResourceLocation feature = Optional.ofNullable(access.getFeature()).map(ResourceKey::location).orElse(null);
		LUtil.writeNullableRL(feature, pBuffer);
		Boolean smokey = access.getSmokey();
		if (smokey == null) {
			pBuffer.writeByte(2);
		} else {
			pBuffer.writeByte(smokey ? 1 : 0);
		}
	}

}
