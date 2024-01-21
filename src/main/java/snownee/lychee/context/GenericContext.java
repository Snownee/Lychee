package snownee.lychee.context;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import snownee.lychee.util.context.LycheeContextType;
import snownee.lychee.util.context.LycheeContextTypes;
import snownee.lychee.util.context.LycheeContextValue;

public record GenericContext(Level level, RandomSource random) implements LycheeContextValue<GenericContext> {
	@Override
	public LycheeContextType<GenericContext> type() {
		return LycheeContextTypes.GENERIC;
	}

	public static final class Type implements LycheeContextType<GenericContext> {
	}
}
