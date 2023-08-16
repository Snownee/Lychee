package snownee.lychee.core.post;

import java.util.Locale;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BoundsHelper;
import snownee.lychee.core.def.DoubleBoundsHelper;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.CommonProxy;

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
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Entity entity = ctx.getParam(LootContextParams.THIS_ENTITY);
		entity.invulnerableTime = 0;
		entity.hurt(source.value, DoubleBoundsHelper.random(damage, ctx.getRandom()) * times);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(CommonProxy.makeDescriptionId("postAction", getType().getRegistryName()), BoundsHelper.getDescription(damage));
	}

	public static class Type extends PostActionType<Hurt> {

		@Override
		public Hurt fromJson(JsonObject o) {
			return new Hurt(MinMaxBounds.Doubles.fromJson(o.get("damage")), SourceType.parse(GsonHelper.getAsString(o, "source", SourceType.GENERIC.name())));
		}

		@Override
		public void toJson(Hurt action, JsonObject o) {
			o.add("damage", action.damage.serializeToJson());
			if (action.source != SourceType.GENERIC) {
				o.addProperty("source", action.source.name().toLowerCase(Locale.ENGLISH));
			}
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

		public static SourceType parse(String s) {
			try {
				return valueOf(s.toUpperCase(Locale.ENGLISH));
			} catch (Throwable e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

}
