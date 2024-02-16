package snownee.lychee.context;

import java.util.List;

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
		ItemEntity[] entities = new ItemEntity[matcher.tests.size()];
		for (int i = 0; i < matcher.inputUsed.length; i++) {
			for (int j = 0; j < matcher.inputUsed[i]; j++) {
				entities[matcher.use[i][j]] = filteredItems.get(i);
			}
		}
		context.put(LycheeContextKey.ITEM, ItemStackHolderCollection.InWorld.of(entities));
	}

	@Nullable
	public RecipeMatcher<ItemStack> getMatcher() {
		return matcher;
	}
}