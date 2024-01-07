package snownee.lychee;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.Kiwi;
import snownee.lychee.core.contextual.ContextualConditionType;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.util.CommonProxy;

public final class LycheeRegistries {

	public static final MappedRegistry<ContextualConditionType<?>> CONTEXTUAL = register("contextual", ContextualConditionType.class);
	public static final MappedRegistry<PostActionType<?>> POST_ACTION = register("post_action", PostActionType.class);

	public static void init() {
	}

	private static <T> MappedRegistry<T> register(String name, Class<?> clazz) {
		var registry = FabricRegistryBuilder.<T>createSimple(ResourceKey.createRegistryKey(new ResourceLocation(Lychee.ID, name))).attribute(RegistryAttribute.SYNCED).buildAndRegister();
		if (CommonProxy.hasKiwi) {
			Kiwi.registerRegistry(registry, clazz);
		}
		return registry;
	}

}
