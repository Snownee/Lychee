package snownee.lychee.core;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class LycheeContext extends EmptyContainer {

	public LootContext context;

	public LycheeContext(LootContext context) {
		this.context = context;
	}

	public Random getRandom() {
		return context.getRandom();
	}

	public ServerLevel getLevel() {
		return context.getLevel();
	}

	public <T> T getParam(LootContextParam<T> pParam) {
		return context.getParam(pParam);
	}

	@Nullable
	public <T> T getParamOrNull(LootContextParam<T> pParam) {
		return context.getParamOrNull(pParam);
	}

}
