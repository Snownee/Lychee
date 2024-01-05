package snownee.lychee.contextual;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.brigadier.ParseResults;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec2;
import snownee.lychee.Lychee;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;

public record Execute(String command, MinMaxBounds.Ints bounds) implements ContextualCondition<Execute> {

	public static final MinMaxBounds.Ints DEFAULT_RANGE = MinMaxBounds.Ints.atLeast(1);
	public static final Execute DUMMY = new Execute("", DEFAULT_RANGE);

	@Override
	public ContextualConditionType<Execute> type() {
		return ContextualConditionTypes.EXECUTE;
	}

	@Override
	public int test(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		if (command.isEmpty() || ctx.level().isClientSide) {
			return 0;
		}
		final var pos = ctx.get(LootContextParams.ORIGIN);
		final var entity = ctx.getOrNull(LootContextParams.THIS_ENTITY);
		var rotation = Vec2.ZERO;
		var displayName = snownee.lychee.action.Execute.DEFAULT_NAME;
		var name = Lychee.ID;
		if (entity != null) {
			rotation = entity.getRotationVector();
			displayName = entity.getDisplayName();
			name = entity.getName().getString();
		}
		final var serverLevel = ctx.serverLevel();
		final var server = serverLevel.getServer();
		MutableInt returnValue = new MutableInt();
		final var sourceStack = new CommandSourceStack(
				CommandSource.NULL,
				pos,
				rotation,
				serverLevel,
				2,
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
		public static final Codec<Execute> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(
						Codec.STRING.fieldOf("command").forGetter(Execute::command),
						MinMaxBounds.Ints.CODEC.optionalFieldOf("value", DEFAULT_RANGE).forGetter(Execute::bounds))
				.apply(instance, Execute::new));

		@Override
		public Codec<Execute> codec() {
			return CODEC;
		}

		@Override
		public Execute fromNetwork(FriendlyByteBuf buf) {
			return DUMMY;
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, Execute condition) {}
	}
}
