package snownee.lychee.network;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.contributor.network.CSetCosmeticPacket;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;
import snownee.lychee.Lychee;

@KiwiPacket

public record SUpdateFallingBlockPacket(
		int entityId, BlockState blockState, Optional<CompoundTag> blockData) implements CustomPacketPayload {
	public static final Type<CSetCosmeticPacket> TYPE = new Type<>(Lychee.id("update_falling_block"));

	public SUpdateFallingBlockPacket(FallingBlockEntity fbe) {
		this(fbe.getId(), fbe.blockState, Optional.ofNullable(fbe.blockData));
	}

	public void send(FallingBlockEntity entity) {
		KPacketSender.sendToTracking(this, entity);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static class Handler implements PlayPacketHandler<SUpdateFallingBlockPacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, SUpdateFallingBlockPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT,
				SUpdateFallingBlockPacket::entityId,
				ByteBufCodecs.VAR_INT.map(Block::stateById, Block::getId),
				SUpdateFallingBlockPacket::blockState,
				ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG),
				SUpdateFallingBlockPacket::blockData,
				SUpdateFallingBlockPacket::new);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SUpdateFallingBlockPacket> streamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public void handle(SUpdateFallingBlockPacket packet, PayloadContext payloadContext) {
			payloadContext.execute(() -> {
				ClientLevel level = Minecraft.getInstance().level;
				if (level == null) {
					return;
				}
				Entity entity = level.getEntity(packet.entityId());
				if (entity instanceof FallingBlockEntity fbe) {
					fbe.blockState = packet.blockState;
					fbe.blockData = packet.blockData.orElse(null);
				}
			});
		}
	}
}
