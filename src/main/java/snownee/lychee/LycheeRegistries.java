package snownee.lychee;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.contextual.ContextualConditionType;

public final class LycheeRegistries {

	public static final MappedRegistry<ContextualConditionType<?>> CONTEXTUAL = register("contextual");
	public static final MappedRegistry<PostActionType<?>> POST_ACTION = register("post_action");

	public static void init() {}

	private static <T> MappedRegistry<T> register(String name) {
		return FabricRegistryBuilder.createSimple(ResourceKey.<T>createRegistryKey(new ResourceLocation(
				Lychee.ID,
				name
		))).attribute(RegistryAttribute.SYNCED).buildAndRegister();
	}
}
