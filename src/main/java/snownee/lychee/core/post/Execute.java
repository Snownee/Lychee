package snownee.lychee.core.post;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.Lychee;
import snownee.lychee.PostActionTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;

public class Execute extends PostAction {

	public static final Execute DUMMY_SHOW = new Execute("", false);
	public static final Execute DUMMY_HIDE = new Execute("", true);
	public static final Component DEFAULT_NAME = new TextComponent(Lychee.ID);

	private final String command;
	private final boolean hide;

	public Execute(String command, boolean hide) {
		this.command = command;
		this.hide = hide;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.EXECUTE;
	}

	@Override
	public boolean doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		times = checkConditions(recipe, ctx, times);
		if (times > 0) {
			apply(recipe, ctx, times);
		}
		return true;
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (command.isEmpty()) {
			return;
		}
		Vec3 pos = ctx.getParam(LootContextParams.ORIGIN);
		Entity entity = ctx.getParamOrNull(LootContextParams.THIS_ENTITY);
		Vec2 rotation = Vec2.ZERO;
		Component displayName = DEFAULT_NAME;
		String name = Lychee.ID;
		if (entity != null) {
			rotation = entity.getRotationVector();
			displayName = entity.getDisplayName();
			name = entity.getName().getString();
		}
		CommandSourceStack sourceStack = new CommandSourceStack(CommandSource.NULL, pos, rotation, ctx.getServerLevel(), 2, name, displayName, ctx.getLevel().getServer(), entity);
		for (int i = 0; i < times; i++) {
			ctx.getLevel().getServer().getCommands().performCommand(sourceStack, command);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		GuiGameElement.of(Items.COMMAND_BLOCK).render(poseStack, x, y);
	}

	@Override
	public boolean isHidden() {
		return hide;
	}

	public static class Type extends PostActionType<Execute> {

		@Override
		public Execute fromJson(JsonObject o) {
			return new Execute(GsonHelper.getAsString(o, "command"), GsonHelper.getAsBoolean(o, "hide", false));
		}

		@Override
		public Execute fromNetwork(FriendlyByteBuf buf) {
			if (buf.readBoolean()) {
				return new Execute("", buf.readBoolean());
			}
			return buf.readBoolean() ? DUMMY_HIDE : DUMMY_SHOW;
		}

		@Override
		public void toNetwork(Execute action, FriendlyByteBuf buf) {
			buf.writeBoolean(action.getConditions().isEmpty());
			buf.writeBoolean(action.hide);
		}

	}

}
