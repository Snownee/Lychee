package snownee.lychee;

import net.minecraft.resources.ResourceLocation;
import snownee.lychee.core.post.DropItem;
import snownee.lychee.core.post.DropXp;
import snownee.lychee.core.post.Execute;
import snownee.lychee.core.post.PlaceBlock;
import snownee.lychee.core.post.PostActionType;

public class PostActionTypes {

	public static void init() {
	}

	public static final PostActionType<DropItem> DROP_ITEM = register("drop_item", new DropItem.Type());
	public static final PostActionType<DropXp> DROP_XP = register("drop_xp", new DropXp.Type());
	public static final PostActionType<Execute> EXECUTE = register("execute", new Execute.Type());
	public static final PostActionType<PlaceBlock> PLACE = register("place", new PlaceBlock.Type());

	public static <T extends PostActionType<?>> T register(String name, T t) {
		LycheeRegistries.POST_ACTION.register(t.setRegistryName(new ResourceLocation(name)));
		return t;
	}

}
