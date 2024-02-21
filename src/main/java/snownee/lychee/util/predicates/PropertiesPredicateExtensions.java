package snownee.lychee.util.predicates;

import java.util.Optional;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;

public class PropertiesPredicateExtensions {

	public static Optional<StatePropertiesPredicate.PropertyMatcher> findMatcher(
			StatePropertiesPredicate predicate,
			String name
	) {
		return predicate.properties()
				.stream()
				.filter($ -> $.name().equals(name))
				.findAny();
	}

	public static Optional<StatePropertiesPredicate> fromNetwork(FriendlyByteBuf buf) {
		return buf.readOptional(it -> it.readWithCodecTrusted(NbtOps.INSTANCE, StatePropertiesPredicate.CODEC));
	}

	public static void toNetwork(Optional<StatePropertiesPredicate> predicate, FriendlyByteBuf buf) {
		buf.writeOptional(
				predicate,
				(it, obj) -> it.writeWithCodec(NbtOps.INSTANCE, StatePropertiesPredicate.CODEC, obj)
		);
	}
}
