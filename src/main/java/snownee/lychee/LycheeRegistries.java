package snownee.lychee;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import snownee.lychee.core.contextual.ContextualConditionType;
import snownee.lychee.core.post.PostActionType;

public final class LycheeRegistries {

	public static MappedRegistry<ContextualConditionType<?>> CONTEXTUAL;

	public static MappedRegistry<PostActionType<?>> POST_ACTION;

	@SuppressWarnings("rawtypes")
	public static void init(NewRegistryEvent event) {
		event.create(register("contextual"), v -> CONTEXTUAL = new MappedRegistry<>((IForgeRegistry) v));
		event.create(register("post_action"), v -> POST_ACTION = new MappedRegistry<>((IForgeRegistry) v));
	}

	private static <T> RegistryBuilder<T> register(String name) {
		return new RegistryBuilder<T>().setName(new ResourceLocation(Lychee.ID, name));
	}

	public record MappedRegistry<T>(IForgeRegistry<T> registry) { //TODO 1.20: move to Kiwi

		public ResourceKey<Registry<T>> key() {
			return registry.getRegistryKey();
		}

		public void register(ResourceLocation id, T t) {
			registry.register(id, t);
		}

		public T get(ResourceLocation key) {
			return registry.getValue(key);
		}

		public ResourceLocation getKey(T t) {
			return registry.getKey(t);
		}
	}
}
