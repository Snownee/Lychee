package snownee.lychee.network;

import java.util.Objects;

import org.joml.Vector3fc;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.contributor.network.CSetCosmeticPacket;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;
import snownee.lychee.Lychee;

@KiwiPacket
public record SCustomLevelEventPacket(ItemStackTemplate stack, Vector3fc pos) implements CustomPacketPayload {
	public static final Type<CSetCosmeticPacket> TYPE = new CustomPacketPayload.Type<>(Lychee.id("level_event"));

	public SCustomLevelEventPacket(ItemStackTemplate stack, Vec3 pos) {
		this(stack, pos.toVector3f());
	}

	public void sendToAround(ServerLevel level) {
		KPacketSender.sendToAround(this, level, null, new Vec3(pos), 16);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static class Handler implements PlayPacketHandler<SCustomLevelEventPacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, SCustomLevelEventPacket> STREAM_CODEC = StreamCodec.composite(
				ItemStackTemplate.STREAM_CODEC,
				SCustomLevelEventPacket::stack,
				ByteBufCodecs.VECTOR3F,
				SCustomLevelEventPacket::pos,
				SCustomLevelEventPacket::new);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SCustomLevelEventPacket> streamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public void handle(SCustomLevelEventPacket packet, PayloadContext payloadContext) {
			payloadContext.execute(() -> {
				for (int i = 0; i < 8; ++i) {
					Vec3 vec3 = new Vec3((Math.random() - 0.5D) * 0.2D, Math.random() * 0.1D + 0.1D, (Math.random() - 0.5D) * 0.2D);
					Objects.requireNonNull(Minecraft.getInstance().level).addParticle(
							new ItemParticleOption(ParticleTypes.ITEM, packet.stack),
							packet.pos.x(),
							packet.pos.y(),
							packet.pos.z(),
							vec3.x,
							vec3.y + 0.05D,
							vec3.z);
				}
			});
		}
	}
}
