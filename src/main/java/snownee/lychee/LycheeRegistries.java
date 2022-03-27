package snownee.lychee;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import snownee.lychee.core.contextual.ContextualConditionType;
import snownee.lychee.core.post.PostActionType;

public final class LycheeRegistries {

	public static IForgeRegistry<ContextualConditionType<?>> CONTEXTUAL;

	public static IForgeRegistry<PostActionType<?>> POST_ACTION;

	@SuppressWarnings("rawtypes")
	public static void init(NewRegistryEvent event) {
		event.create(register("contextual", ContextualConditionType.class), v -> CONTEXTUAL = (IForgeRegistry) v);
		event.create(register("post_action", PostActionType.class), v -> POST_ACTION = (IForgeRegistry) v);
	}

	private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> register(String name, Class<T> clazz) {
		return new RegistryBuilder<T>().setName(new ResourceLocation(Lychee.ID, name)).setType(clazz);
	}

}
