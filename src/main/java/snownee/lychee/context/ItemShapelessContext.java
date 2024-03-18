package snownee.lychee.context;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.util.RecipeMatcher;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.input.ItemStackHolderCollection;

public class ItemShapelessContext {
	public final List<ItemEntity> itemEntities;
	public List<ItemEntity> filteredItems;
	private RecipeMatcher<ItemStack> matcher;
	public int totalItems;
	private final LycheeContext context;

	public ItemShapelessContext(List<ItemEntity> itemEntities, LycheeContext context) {
		this.itemEntities = itemEntities;
		totalItems = itemEntities.stream().map(ItemEntity::getItem).mapToInt(ItemStack::getCount).sum();
		this.context = context;
	}

	public void setMatcher(@Nullable RecipeMatcher<ItemStack> matcher) {
		this.matcher = matcher;
		if (matcher == null) {
			context.put(LycheeContextKey.ITEM, ItemStackHolderCollection.EMPTY);
			return;
		}
		var entities = new ItemEntity[matcher.tests.size()];
		for (var i = 0; i < matcher.inputUsed.length; i++) {
			for (var j = 0; j < matcher.inputUsed[i]; j++) {
				entities[matcher.use[i][j]] = filteredItems.get(i);
			}
		}
		context.put(LycheeContextKey.ITEM, ItemStackHolderCollection.InWorld.of(entities));
	}

	public Optional<RecipeMatcher<ItemStack>> getMatcher() {
		return Optional.ofNullable(matcher);
	}
}
