package snownee.lychee.core.network;

import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.contributor.network.CSetCosmeticPacket;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;
import snownee.lychee.Lychee;

@KiwiPacket
public record SCustomLevelEventPacket(ItemStack stack, Vector3f pos) implements CustomPacketPayload {
	public static final Type<CSetCosmeticPacket> TYPE = new CustomPacketPayload.Type<>(Lychee.id("level_event"));

	public static SCustomLevelEventPacket I;

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static class Handler extends PlayPacketHandler<SCustomLevelEventPacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, SCustomLevelEventPacket> STREAM_CODEC = StreamCodec.composite(
				ItemStack.OPTIONAL_STREAM_CODEC,
				SCustomLevelEventPacket::stack,
				ByteBufCodecs.VECTOR3F,
				SCustomLevelEventPacket::pos,
				SCustomLevelEventPacket::new);

		@Override
		public SCustomLevelEventPacket read(RegistryFriendlyByteBuf buf) {
			return streamCodec().decode(buf);
		}

		@Override
		public void write(
				SCustomLevelEventPacket packet, RegistryFriendlyByteBuf buf) {
			streamCodec().encode(buf, packet);
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SCustomLevelEventPacket> streamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public void handle(SCustomLevelEventPacket packet, PayloadContext context) {
			for (int i = 0; i < 8; ++i) {
				Vec3 vec3 = new Vec3((Math.random() - 0.5D) * 0.2D, Math.random() * 0.1D + 0.1D, (Math.random() - 0.5D) * 0.2D);
				Minecraft.getInstance().level.addParticle(
						new ItemParticleOption(ParticleTypes.ITEM, packet.stack),
						packet.pos.x,
						packet.pos.y,
						packet.pos.z,
						vec3.x,
						vec3.y + 0.05D,
						vec3.z);
			}
		}
	}
}
