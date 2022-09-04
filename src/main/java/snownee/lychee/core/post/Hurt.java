package snownee.lychee.core.post;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.PostActionTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BoundsHelper;
import snownee.lychee.core.def.DoubleBoundsHelper;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

import java.util.Locale;

public class Hurt extends PostAction {

	public final MinMaxBounds.Doubles damage;
	public final SourceType source;

	public Hurt(MinMaxBounds.Doubles damage, SourceType source) {
		this.damage = damage;
		this.source = source;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.HURT;
	}

	@Override
	public boolean doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (times > 0)
			apply(recipe, ctx, times);
		return true;
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Entity entity = ctx.getParam(LootContextParams.THIS_ENTITY);
		entity.invulnerableTime = 0;
		entity.hurt(source.value, DoubleBoundsHelper.random(damage, ctx.getRandom()) * times);
	}

	@Override
	public Component getDisplayName() {
		return new TranslatableComponent(LUtil.makeDescriptionId("postAction", getType().getRegistryName()), BoundsHelper.getDescription(damage));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		//TODO this is not ideal. render bleed icon in the future
		GuiGameElement.of(Items.IRON_SWORD).render(poseStack, x, y);
	}

	public static class Type extends PostActionType<Hurt> {

		@Override
		public Hurt fromJson(JsonObject o) {
			return new Hurt(
					MinMaxBounds.Doubles.fromJson(o.get("damage")),
					SourceType.valueOf(GsonHelper.getAsString(o, "source", SourceType.GENERIC.name()).toUpperCase(Locale.ENGLISH))
			);
		}

		@Override
		public Hurt fromNetwork(FriendlyByteBuf buf) {
			return new Hurt(DoubleBoundsHelper.fromNetwork(buf), buf.readEnum(SourceType.class));
		}

		@Override
		public void toNetwork(Hurt action, FriendlyByteBuf buf) {
			DoubleBoundsHelper.toNetwork(action.damage, buf);
			buf.writeEnum(action.source);
		}

	}

	public enum SourceType {
		GENERIC(DamageSource.GENERIC),
		MAGIC(DamageSource.MAGIC),
		OUT_OF_WORLD(DamageSource.OUT_OF_WORLD),
		ANVIL(DamageSource.ANVIL),
		WITHER(DamageSource.WITHER),
		FREEZE(DamageSource.FREEZE),
		DROWN(DamageSource.DROWN),
		FALL(DamageSource.FALL),
		IN_FIRE(DamageSource.IN_FIRE),
		ON_FIRE(DamageSource.ON_FIRE),
		LAVA(DamageSource.LAVA);

		public final DamageSource value;

		SourceType(DamageSource value) {
			this.value = value;
		}
	}

}
