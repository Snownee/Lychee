package snownee.lychee.compat.spectrum;

import de.dafuqs.spectrum.blocks.pedestal.PedestalBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.context.CraftingContainerLocation;
import snownee.lychee.context.CraftingContext;

public class SpectrumCompat {
	public static void init() {
		CraftingContext.CONTAINER_WORLD_LOCATOR.put(
				PedestalBlockEntity.class, container -> {
					final var blockEntity = (PedestalBlockEntity) container;
					Level level = blockEntity.getLevel();
					if (level == null) {
						return null;
					}
					return new CraftingContainerLocation(
							level,
							Vec3.atCenterOf(blockEntity.getBlockPos()),
							blockEntity.getOwnerIfOnline(level)
					);
				});
	}
}
