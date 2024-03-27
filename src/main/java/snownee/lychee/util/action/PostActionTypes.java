package snownee.lychee.util.action;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.action.AddItemCooldown;
import snownee.lychee.action.AnvilDamageChance;
import snownee.lychee.action.Break;
import snownee.lychee.action.CustomAction;
import snownee.lychee.action.CycleStateProperty;
import snownee.lychee.action.Delay;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.DropXp;
import snownee.lychee.action.Execute;
import snownee.lychee.action.Explode;
import snownee.lychee.action.Hurt;
import snownee.lychee.action.If;
import snownee.lychee.action.MoveTowardsFace;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.action.RandomSelect;
import snownee.lychee.action.input.DamageItem;
import snownee.lychee.action.input.PreventDefault;
import snownee.lychee.action.input.SetItem;

public class PostActionTypes {
	public static final PostActionType<DropItem> DROP_ITEM = register("drop_item", new DropItem.Type());
	public static final PostActionType<DropXp> DROP_XP = register("drop_xp", new DropXp.Type());
	public static final PostActionType<Execute> EXECUTE = register("execute", new Execute.Type());
	public static final PostActionType<PlaceBlock> PLACE = register("place", new PlaceBlock.Type());
	public static final PostActionType<DamageItem> DAMAGE_ITEM = register("damage_item", new DamageItem.Type());
	public static final PostActionType<PreventDefault> PREVENT_DEFAULT = register(
			"prevent_default",
			new PreventDefault.Type()
	);
	public static final PostActionType<AnvilDamageChance> ANVIL_DAMAGE_CHANCE = register(
			"anvil_damage_chance",
			new AnvilDamageChance.Type()
	);
	public static final PostActionType<RandomSelect> RANDOM = register("random", new RandomSelect.Type());
	public static final PostActionType<Explode> EXPLODE = register("explode", new Explode.Type());
	public static final PostActionType<Hurt> HURT = register("hurt", new Hurt.Type());
	public static final PostActionType<Delay> DELAY = register("delay", new Delay.Type());
	public static final PostActionType<Break> BREAK = register("break", new Break.Type());
	public static final PostActionType<AddItemCooldown> ADD_ITEM_COOLDOWN = register(
			"add_item_cooldown",
			new AddItemCooldown.Type()
	);
	public static final PostActionType<CycleStateProperty> CYCLE_STATE_PROPERTY = register(
			"cycle_state_property",
			new CycleStateProperty.Type()
	);
	public static final PostActionType<MoveTowardsFace> MOVE_TOWARDS_FACE = register(
			"move_towards_face",
			new MoveTowardsFace.Type()
	);
	public static final PostActionType<SetItem> SET_ITEM = register("set_item", new SetItem.Type());
	public static final PostActionType<CustomAction> CUSTOM = register("custom", new CustomAction.Type());
	public static final PostActionType<If> IF = register("if", new If.Type());

	public static <T extends PostActionType<?>> T register(String name, T t) {
		Registry.register(LycheeRegistries.POST_ACTION, new ResourceLocation(name), t);
		return t;
	}
}
