package snownee.lychee.context;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;

@FunctionalInterface
public interface LootParamInit {
	Map<LootContextParam<?>, LootParamInit> LOOKUP = Maps.newLinkedHashMap();

	LootParamInit ORIGIN = register(
			LootContextParams.ORIGIN,
			(level, params) -> {
				if (!params.has(LycheeLootContextParams.BLOCK_POS)) {
					return null;
				}
				return Vec3.atCenterOf(params.get(LycheeLootContextParams.BLOCK_POS));
			});

	LootParamInit BLOCK_POS = register(
			LycheeLootContextParams.BLOCK_POS,
			(level, params) -> {
				if (!params.has(LootContextParams.ORIGIN)) {
					return null;
				}
				return BlockPos.containing(params.get(LootContextParams.ORIGIN));
			});

	LootParamInit BLOCK_STATE = register(
			LootContextParams.BLOCK_STATE,
			(level, params) -> {
				BlockPos pos = params.getOrNull(LycheeLootContextParams.BLOCK_POS);
				if (pos == null) {
					return null;
				}
				return level.getBlockState(pos);
			});

	LootParamInit BLOCK_ENTITY = register(
			LootContextParams.BLOCK_ENTITY,
			(level, params) -> {
				BlockPos pos = params.getOrNull(LycheeLootContextParams.BLOCK_POS);
				if (pos == null) {
					return null;
				}
				return level.getBlockEntity(pos);
			});

	static LootParamInit register(LootContextParam<?> param, LootParamInit init) {
		LOOKUP.put(param, init);
		return init;
	}

	@Nullable
	Object init(Level level, LootParamsContext params);
}
