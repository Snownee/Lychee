package snownee.lychee.util.context;

import java.util.Map;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import snownee.lychee.util.DummyContainer;

public record LycheeContext(RandomSource random, Level level, Map<LycheeContextType<?>, LycheeContextValue<?>> context)
		implements DummyContainer {
	public ServerLevel serverLevel() {
		return (ServerLevel) level();
	}
}
