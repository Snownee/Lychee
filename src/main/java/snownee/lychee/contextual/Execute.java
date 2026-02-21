package snownee.lychee.contextual;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.brigadier.ParseResults;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec2;
import snownee.lychee.Lychee;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.codec.LycheeStreamCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;

public record Execute(String command, MinMaxBounds.Ints bounds) implements ContextualCondition {

	public static final MinMaxBounds.Ints DEFAULT_RANGE = MinMaxBounds.Ints.atLeast(1);
	public static final Execute DUMMY = new Execute("", DEFAULT_RANGE);

	public Execute(String command) {
		this(command, DEFAULT_RANGE);
	}

	@Override
	public ContextualConditionType<Execute> type() {
		return ContextualConditionType.EXECUTE;
	}

	@Override
	public int test(LycheeContext ctx, ActionContext actionContext, int times) {
		final var level = ctx.level();
		if (command.isEmpty() || level.isClientSide()) {
			return 0;
		}
		final var pos = actionContext.get(LootContextParams.ORIGIN);
		final var entity = actionContext.getOrNull(LootContextParams.THIS_ENTITY);
		var rotation = Vec2.ZERO;
		var displayName = snownee.lychee.action.Execute.DEFAULT_NAME;
		var name = Lychee.ID;
		if (entity != null) {
			rotation = entity.getRotationVector();
			displayName = entity.getDisplayName();
			name = entity.getName().getString();
		}
		final var serverLevel = (ServerLevel) level;
		final var server = serverLevel.getServer();
		MutableInt returnValue = new MutableInt();
		final var sourceStack = new CommandSourceStack(
				CommandSource.NULL,
				pos,
				rotation,
				serverLevel,
				LevelBasedPermissionSet.GAMEMASTER,
				name,
				displayName,
				server,
				entity
		).withCallback((success, i) -> returnValue.setValue(i));
		Commands cmds = server.getCommands();
		ParseResults<CommandSourceStack> results = cmds.getDispatcher().parse(command, sourceStack);
		cmds.performCommand(results, command);
		return bounds.matches(returnValue.intValue()) ? times : 0;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(getDescriptionId(false));
	}

	public static class Type implements ContextualConditionType<Execute> {
		public static final MapCodec<Execute> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.STRING.fieldOf("command").forGetter(Execute::command),
				MinMaxBounds.Ints.CODEC.optionalFieldOf("value", DEFAULT_RANGE).forGetter(Execute::bounds)
		).apply(instance, Execute::new));

		@Override
		public MapCodec<Execute> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, Execute> streamCodec() {
			return LycheeStreamCodecs.uncheckedUnit(DUMMY);
		}
	}
}
