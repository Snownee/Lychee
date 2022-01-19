package snownee.lychee;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;
import snownee.lychee.core.contextual.ContextualConditionType;
import snownee.lychee.core.post.PostActionType;

public final class LycheeRegistries {

	public static void init() {
	}

	public static final IForgeRegistry<ContextualConditionType<?>> CONTEXTUAL = register("contextual", ContextualConditionType.class);

	public static final IForgeRegistry<PostActionType<?>> POST_ACTION = register("post_action", PostActionType.class);

	private static <T extends IForgeRegistryEntry<T>> IForgeRegistry<T> register(String name, Class<T> clazz) {
		return new RegistryBuilder<T>().setName(new ResourceLocation(Lychee.ID, name)).setType(clazz).create();
	}

}
