package snownee.lychee.context;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record CraftingContainerLocation(Level level, @Nullable Vec3 position, @Nullable Player player) {
	public static CraftingContainerLocation of(Player player) {
		return new CraftingContainerLocation(player.level(), player.position(), player);
	}
}
