package snownee.lychee.core.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.network.KPacketTarget;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.KiwiPacket.Direction;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket(value = "level_event", dir = Direction.PLAY_TO_CLIENT)
public class SCustomLevelEventPacket extends PacketHandler {

	public static SCustomLevelEventPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		ItemStack stack = buf.readItem();
		float x = buf.readFloat();
		float y = buf.readFloat();
		float z = buf.readFloat();
		return executor.apply(() -> {
			for (int i = 0; i < 8; ++i) {
				Vec3 vec3 = new Vec3((Math.random() - 0.5D) * 0.2D, Math.random() * 0.1D + 0.1D, (Math.random() - 0.5D) * 0.2D);
				Minecraft.getInstance().level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), x, y, z, vec3.x, vec3.y + 0.05D, vec3.z);
			}
		});
	}

	public static void sendItemParticles(ItemStack stack, ServerLevel level, Vec3 pos) {
		I.send(KPacketTarget.around(level, pos, 16), buf -> {
			buf.writeItem(stack);
			buf.writeFloat((float) pos.x);
			buf.writeFloat((float) pos.y);
			buf.writeFloat((float) pos.z);
		});
	}

}
