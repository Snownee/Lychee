package snownee.lychee.util.action;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import snownee.lychee.util.contextual.ContextualHolder;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PostActionCommonProperties {
	public static final PostActionCommonProperties EMPTY = new PostActionCommonProperties(Optional.empty(), ContextualHolder.EMPTY, false);
	public static final MapCodec<PostActionCommonProperties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.optionalFieldOf("@path").forGetter(PostActionCommonProperties::getPath),
			LycheeRecipeCommonProperties.CONTEXTUAL_CODEC.forGetter(PostActionCommonProperties::conditions),
			LycheeRecipeCommonProperties.HIDE_CODEC.forGetter(PostActionCommonProperties::hidden)
	).apply(instance, PostActionCommonProperties::new));
	private Optional<String> path;
	private final ContextualHolder conditions;
	private final boolean hidden;

	public PostActionCommonProperties(Optional<String> path, ContextualHolder conditions, boolean hidden) {
		this.path = path;
		this.conditions = conditions;
		this.hidden = hidden;
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

	public boolean hidden() {
		return hidden;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("path", path)
				.add("hidden", hidden)
				.toString();
	}
}
