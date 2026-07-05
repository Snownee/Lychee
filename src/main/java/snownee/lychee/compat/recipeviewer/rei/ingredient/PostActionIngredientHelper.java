package snownee.lychee.compat.recipeviewer.rei.ingredient;

import java.util.Objects;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.compat.recipeviewer.rei.LycheeREIPlugin;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;

public class PostActionIngredientHelper implements EntryDefinition<PostAction> {
	@Override
	public @Nullable String getContainingNamespace(EntryStack<PostAction> entry, PostAction value) {
		Identifier key = LycheeRegistries.POST_ACTION.getKey(value.type());
		return key == null ? null : CommonProxy.wrapNamespace(key.getNamespace());
	}

	@Override
	public Class<PostAction> getValueType() {
		return PostAction.class;
	}

	@Override
	public EntryType<PostAction> getType() {
		return LycheeREIPlugin.POST_ACTION;
	}

	@Override
	public EntryRenderer<PostAction> getRenderer() {
		return PostActionIngredientRenderer.INSTANCE;
	}

	@Override
	public @Nullable Identifier getIdentifier(EntryStack<PostAction> entry, PostAction value) {
		return LycheeRegistries.POST_ACTION.getKey(value.type());
	}

	@Override
	public boolean isEmpty(EntryStack<PostAction> entry, PostAction value) {
		return value == PostActionIngredientRenderer.INGREDIENT_HACK_DUMMY;
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
		return PostAction.getDisplayName(value);
	}

	@Override
	public Stream<? extends TagKey<?>> getTagsFor(EntryStack<PostAction> entry, PostAction value) {
		return Stream.of();
	}
}
