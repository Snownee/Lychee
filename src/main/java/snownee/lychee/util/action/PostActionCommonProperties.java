package snownee.lychee.util.action;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.util.contextual.ContextualHolder;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PostActionCommonProperties {
	public static final PostActionCommonProperties EMPTY = new PostActionCommonProperties(ContextualHolder.EMPTY, Optional.empty());

	public static final ResourceLocation HIDDEN = ResourceLocation.withDefaultNamespace("hidden");
	public static final MapCodec<Optional<ResourceLocation>> ICON_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("icon")
					.forGetter(it -> it.isEmpty() || it.get().equals(HIDDEN) ? Optional.empty() : it),
			Codec.BOOL.optionalFieldOf("hide", false).forGetter(it -> it.isPresent() && it.get().equals(HIDDEN))
	).apply(instance, (it, bl) -> bl ? Optional.of(HIDDEN) : it));

	public static final MapCodec<PostActionCommonProperties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			LycheeRecipeCommonProperties.CONTEXTUAL_CODEC.forGetter(PostActionCommonProperties::conditions),
			ICON_CODEC.forGetter(it -> Optional.ofNullable(it.icon())),
			Codec.STRING.optionalFieldOf("@path").forGetter(PostActionCommonProperties::getPath)
	).apply(instance, PostActionCommonProperties::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PostActionCommonProperties> STREAM_CODEC = StreamCodec.composite(
			ContextualHolder.STREAM_CODEC,
			PostActionCommonProperties::conditions,
			ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
			$ -> Optional.ofNullable($.icon()),
			PostActionCommonProperties::new);

	private Optional<String> path;
	private final ContextualHolder conditions;
	private final @Nullable ResourceLocation icon;

	public PostActionCommonProperties(ContextualHolder conditions, Optional<ResourceLocation> icon) {
		this(conditions, icon, Optional.empty());
	}

	public PostActionCommonProperties(ContextualHolder conditions, Optional<ResourceLocation> icon, Optional<String> path) {
		this.path = path;
		this.conditions = conditions;
		this.icon = icon.orElse(null);
	}

	public ContextualHolder conditions() {
		return conditions;
	}

	public Optional<String> getPath() {
		return path;
	}

	public void setPath(final @Nullable String path) {
		this.path = Optional.ofNullable(path);
	}

	@Nullable
	public ResourceLocation icon() {
		return icon;
	}

	public boolean hidden() {
		return HIDDEN.equals(icon);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("path", path)
				.add("icon", icon)
				.toString();
	}
}
