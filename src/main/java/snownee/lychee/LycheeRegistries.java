package snownee.lychee;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.core.contextual.ContextualConditionType;
import snownee.lychee.core.post.PostActionType;

public final class LycheeRegistries {

	public static void init() {
	}

	public static final MappedRegistry<ContextualConditionType<?>> CONTEXTUAL = register("contextual", ContextualConditionType.class);

	public static final MappedRegistry<PostActionType<?>> POST_ACTION = register("post_action", PostActionType.class);

	private static <T> MappedRegistry<T> register(String name, Class<?> clazz) {
		return FabricRegistryBuilder.createSimple((Class<T>) clazz, new ResourceLocation(Lychee.ID, name)).attribute(RegistryAttribute.SYNCED).buildAndRegister();
	}

}
