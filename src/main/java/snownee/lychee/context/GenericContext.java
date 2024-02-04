package snownee.lychee.context;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import snownee.lychee.util.context.LycheeContextType;
import snownee.lychee.util.context.LycheeContextValue;

public record GenericContext(Level level, RandomSource random) implements LycheeContextValue<GenericContext> {
	public GenericContext(final Level level) {
		// TODO 这里用新的 RandomSource 的原因是？
		this(level, level.random);
	}

	@Override
	public LycheeContextType<GenericContext> type() {
		return LycheeContextType.GENERIC;
	}
}
