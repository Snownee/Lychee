package snownee.lychee.action;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.Lychee;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Execute(PostActionCommonProperties commonProperties, String command, boolean hide, boolean repeat) implements PostAction {

	public static final Execute DUMMY = new Execute(new PostActionCommonProperties(), "", false, false);
	public static final Component DEFAULT_NAME = Component.literal(Lychee.ID);

	@Override
	public PostActionType<Execute> type() {
		return PostActionTypes.EXECUTE;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		if (command.isEmpty()) {
			return;
		}
		if (!repeat) {
			times = 1;
		}
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var pos = lootParamsContext.getOrNull(LootContextParams.ORIGIN);
		if (pos == null) {
			pos = Vec3.ZERO;
		}
		var entity = lootParamsContext.getOrNull(LootContextParams.THIS_ENTITY);
		var rotation = Vec2.ZERO;
		var displayName = DEFAULT_NAME;
		var name = Lychee.ID;
		if (entity != null) {
			rotation = entity.getRotationVector();
			displayName = entity.getDisplayName();
			name = entity.getName().getString();
		}
		var level = (ServerLevel) context.get(LycheeContextKey.LEVEL);
		var commandSourceStack = new CommandSourceStack(
				CommandSource.NULL,
				pos,
				rotation,
				level,
				2,
				name,
				displayName,
				level.getServer(),
				entity);
		for (int i = 0; i < times; i++) {
			var commands = level.getServer().getCommands();
			var parseResults = commands.getDispatcher().parse(command, commandSourceStack);
			commands.performCommand(parseResults, command);
		}
	}

	@Override
	public boolean preventSync() {
		return hide;
	}

	public static class Type implements PostActionType<Execute> {
		public static final Codec<Execute> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(Execute::commonProperties),
						Codec.STRING.fieldOf("command").forGetter(Execute::command),
						Codec.BOOL.optionalFieldOf("hide", false).forGetter(Execute::hide),
						Codec.BOOL.optionalFieldOf("repeat", true).forGetter(Execute::repeat)
				).apply(instance, Execute::new));


		@Override
		public Codec<Execute> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? extends ByteBuf, Execute> streamCodec() {
			return StreamCodec.of(
					(it, value) -> it.writeBoolean(!value.conditions().conditions().isEmpty()),
					it -> {
						if (it.readBoolean()) {
							return new Execute(new PostActionCommonProperties(), "", false, false);
						}
						return DUMMY;
					}
			);
		}
	}
}
