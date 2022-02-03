package snownee.lychee.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.server.ServerLifecycleHooks;
import snownee.lychee.Lychee;

public class LUtil {
	private static final Random RANDOM = new Random();

	public static void dropItemStack(Level pLevel, double pX, double pY, double pZ, ItemStack pStack, @Nullable Consumer<ItemEntity> extraStep) {
		while (!pStack.isEmpty()) {
			ItemEntity itementity = new ItemEntity(pLevel, pX, pY, pZ, pStack.split(Math.min(RANDOM.nextInt(21) + 10, pStack.getMaxStackSize())));
			itementity.setDeltaMovement(RANDOM.nextGaussian() * 0.05, RANDOM.nextGaussian() * 0.05 + 0.2, RANDOM.nextGaussian() * 0.05F);
			if (extraStep != null) {
				extraStep.accept(itementity);
			}
			pLevel.addFreshEntity(itementity);
		}
	}

	public static String makeDescriptionId(String pType, @Nullable ResourceLocation pId) {
		return pId == null ? pType + ".unregistered_sadface" : pType + "." + wrapNamespace(pId.getNamespace()) + "." + pId.getPath().replace('/', '.');
	}

	public static String wrapNamespace(String modid) {
		return "minecraft".equals(modid) ? Lychee.ID : modid;
	}

	@OnlyIn(Dist.CLIENT)
	public static MutableComponent getDimensionDisplayName(ResourceKey<Level> dimension) {
		String key = Util.makeDescriptionId("dimension", dimension.location());
		if (I18n.exists(key)) {
			return new TranslatableComponent(key);
		} else {
			return new TextComponent(capitaliseAllWords(dimension.location().getPath()));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static MutableComponent format(String s, Object... objects) {
		return new TextComponent(MessageFormat.format(I18n.get(s), objects));
	}

	public static MutableComponent white(CharSequence s) {
		return new TextComponent(s.toString()).withStyle(ChatFormatting.WHITE);
	}

	public static String capitaliseAllWords(String str) {
		int sz = str.length();
		StringBuilder buffer = new StringBuilder(sz);
		boolean space = true;
		for (int i = 0; i < sz; i++) {
			char ch = str.charAt(i);
			if (Character.isWhitespace(ch)) {
				buffer.append(ch);
				space = true;
			} else if (space) {
				buffer.append(Character.toTitleCase(ch));
				space = false;
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	public static <T> T getCycledItem(List<T> list, T fallback) {
		if (list.isEmpty()) {
			return fallback;
		}
		long index = (System.currentTimeMillis() / 1000) % list.size();
		return list.get(Math.toIntExact(index));
	}

	public static ResourceLocation readNullableRL(FriendlyByteBuf buf) {
		String string = buf.readUtf();
		if (string.isEmpty()) {
			return null;
		} else {
			return new ResourceLocation(string);
		}
	}

	public static void writeNullableRL(ResourceLocation rl, FriendlyByteBuf buf) {
		if (rl == null) {
			buf.writeUtf("");
		} else {
			buf.writeUtf(rl.toString());
		}
	}

	public static InteractionResult interactionResult(Boolean bool) {
		if (bool == null) {
			return InteractionResult.PASS;
		}
		return bool ? InteractionResult.SUCCESS : InteractionResult.FAIL;
	}

	public static <T extends IForgeRegistryEntry<T>> T readRegistryId(IForgeRegistry<T> registry, FriendlyByteBuf buf) {
		return LUtil.readRegistryId(registry, buf);
	}

	public static <T extends IForgeRegistryEntry<T>> void writeRegistryId(IForgeRegistry<T> registry, T entry, FriendlyByteBuf buf) {
		LUtil.writeRegistryId(registry, entry, buf);
	}

	public static RecipeManager recipeManager() {
		return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
	}

	public static Recipe<?> recipe(ResourceLocation id) {
		return recipeManager().byKey(id).orElse(null);
	}

}
