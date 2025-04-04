package snownee.lychee;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.category.UIElementType;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualConditionType;

public final class LycheeRegistries {
	public static final MappedRegistry<ContextualConditionType<?>> CONTEXTUAL = register("contextual");
	public static final MappedRegistry<PostActionType<?>> POST_ACTION = register("post_action");
	public static final MappedRegistry<LycheeContextKey<?>> CONTEXT = register("context");
	public static final MappedRegistry<Codec<?>> CONTEXT_SERIALIZER = register("context_serializer");
	public static final MappedRegistry<UIElementType<?>> UI_ELEMENT = register("ui_element");

	private static <T> MappedRegistry<T> register(String id) {
		return FabricRegistryBuilder.createSimple(ResourceKey.<T>createRegistryKey(Lychee.id(id)))
				.attribute(RegistryAttribute.SYNCED)
				.buildAndRegister();
	}
}
