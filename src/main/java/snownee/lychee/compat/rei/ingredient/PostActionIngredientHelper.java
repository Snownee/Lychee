package snownee.lychee.compat.rei.ingredient;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.util.LUtil;

public class PostActionIngredientHelper implements EntryDefinition<PostAction> {

	@Override
	public @Nullable String getContainingNamespace(EntryStack<PostAction> entry, PostAction value) {
		String modid = value.getType().getRegistryName().getNamespace();
		return LUtil.wrapNamespace(modid);
	}

	@Override
	public Class<PostAction> getValueType() {
		return PostAction.class;
	}

	@Override
	public EntryType<PostAction> getType() {
		return REICompat.POST_ACTION;
	}

	@Override
	public EntryRenderer<PostAction> getRenderer() {
		return PostActionIngredientRenderer.INSTANCE;
	}

	@Override
	public @Nullable ResourceLocation getIdentifier(EntryStack<PostAction> entry, PostAction value) {
		return value.getType().getRegistryName();
	}

	@Override
	public boolean isEmpty(EntryStack<PostAction> entry, PostAction value) {
		return false;
	}

	@Override
	public PostAction copy(EntryStack<PostAction> entry, PostAction value) {
		return value;
	}

	@Override
	public PostAction normalize(EntryStack<PostAction> entry, PostAction value) {
		return copy(entry, value);
	}

	@Override
	public PostAction wildcard(EntryStack<PostAction> entry, PostAction value) {
		return copy(entry, value);
	}

	@Override
	public long hash(EntryStack<PostAction> entry, PostAction value, ComparisonContext context) {
		return Objects.hashCode(value);
	}

	@Override
	public boolean equals(PostAction o1, PostAction o2, ComparisonContext context) {
		return Objects.equals(o1, o2);
	}

	@Override
	public @Nullable EntrySerializer<PostAction> getSerializer() {
		return null;
	}

	@Override
	public Component asFormattedText(EntryStack<PostAction> entry, PostAction value) {
		return value.getDisplayName();
	}

	@Override
	public Stream<? extends TagKey<?>> getTagsFor(EntryStack<PostAction> entry, PostAction value) {
		return Stream.of();
	}

}
