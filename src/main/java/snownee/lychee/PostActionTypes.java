package snownee.lychee;

import net.minecraft.core.Registry;
import snownee.lychee.core.post.AddItemCooldown;
import snownee.lychee.core.post.AnvilDamageChance;
import snownee.lychee.core.post.Break;
import snownee.lychee.core.post.CustomAction;
import snownee.lychee.core.post.CycleStateProperty;
import snownee.lychee.core.post.Delay;
import snownee.lychee.core.post.DropItem;
import snownee.lychee.core.post.DropXp;
import snownee.lychee.core.post.Execute;
import snownee.lychee.core.post.Explode;
import snownee.lychee.core.post.Hurt;
import snownee.lychee.core.post.MoveTowardsFace;
import snownee.lychee.core.post.PlaceBlock;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.post.RandomSelect;
import snownee.lychee.core.post.input.DamageItem;
import snownee.lychee.core.post.input.NBTPatch;
import snownee.lychee.core.post.input.PreventDefault;
import snownee.lychee.core.post.input.SetItem;

public class PostActionTypes {

	public static void init() {
	}

	public static final PostActionType<DropItem> DROP_ITEM = register("drop_item", new DropItem.Type());
	public static final PostActionType<DropXp> DROP_XP = register("drop_xp", new DropXp.Type());
	public static final PostActionType<Execute> EXECUTE = register("execute", new Execute.Type());
	public static final PostActionType<PlaceBlock> PLACE = register("place", new PlaceBlock.Type());
	public static final PostActionType<DamageItem> DAMAGE_ITEM = register("damage_item", new DamageItem.Type());
	public static final PostActionType<PreventDefault> PREVENT_DEFAULT = register("prevent_default", new PreventDefault.Type());
	public static final PostActionType<AnvilDamageChance> ANVIL_DAMAGE_CHANCE = register("anvil_damage_chance", new AnvilDamageChance.Type());
	public static final PostActionType<RandomSelect> RANDOM = register("random", new RandomSelect.Type());
	public static final PostActionType<Explode> EXPLODE = register("explode", new Explode.Type());
	public static final PostActionType<Hurt> HURT = register("hurt", new Hurt.Type());
	public static final PostActionType<Delay> DELAY = register("delay", new Delay.Type());
	public static final PostActionType<Break> BREAK = register("break", new Break.Type());
	public static final PostActionType<AddItemCooldown> ADD_ITEM_COOLDOWN = register("add_item_cooldown", new AddItemCooldown.Type());
	public static final PostActionType<CycleStateProperty> CYCLE_STATE_PROPERTY = register("cycle_state_property", new CycleStateProperty.Type());
	public static final PostActionType<MoveTowardsFace> MOVE_TOWARDS_FACE = register("move_towards_face", new MoveTowardsFace.Type());
	public static final PostActionType<SetItem> SET_ITEM = register("set_item", new SetItem.Type());
	public static final PostActionType<NBTPatch> NBT_PATCH = register("nbt_patch", new NBTPatch.Type());
	public static final PostActionType<CustomAction> CUSTOM = register("custom", new CustomAction.Type());

	public static <T extends PostActionType<?>> T register(String name, T t) {
		Registry.register(LycheeRegistries.POST_ACTION, name, t);
		return t;
	}

}
