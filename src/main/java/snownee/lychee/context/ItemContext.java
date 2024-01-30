package snownee.lychee.context;

import snownee.lychee.util.context.LycheeContextType;
import snownee.lychee.util.context.LycheeContextTypes;
import snownee.lychee.util.context.LycheeContextValue;
import snownee.lychee.util.input.ItemStackHolderCollection;

public record ItemContext(ItemStackHolderCollection items) implements LycheeContextValue<ItemContext> {

	@Override
	public LycheeContextType<ItemContext> type() {
		return LycheeContextTypes.ITEM;
	}

	public static final class Type implements LycheeContextType<ItemContext> {
	}
}
