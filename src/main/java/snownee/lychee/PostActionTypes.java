package snownee.lychee;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoadingContext;
import snownee.lychee.core.post.AnvilDamageChance;
import snownee.lychee.core.post.DropItem;
import snownee.lychee.core.post.DropXp;
import snownee.lychee.core.post.Execute;
import snownee.lychee.core.post.PlaceBlock;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.post.RandomSelect;
import snownee.lychee.core.post.input.DamageItem;
import snownee.lychee.core.post.input.PreventDefault;

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

	public static <T extends PostActionType<?>> T register(String name, T t) {
		ModLoadingContext.get().setActiveContainer(null); // bypass Forge warning
		LycheeRegistries.POST_ACTION.register(t.setRegistryName(new ResourceLocation(name)));
		return t;
	}

}
