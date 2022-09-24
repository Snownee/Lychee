package snownee.lychee.util;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Streams;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeConfig;
import snownee.lychee.mixin.RecipeManagerAccess;

public class LUtil {
	private static final Random RANDOM = new Random();
	private static RecipeManager recipeManager;

	public static void dropItemStack(Level pLevel, double pX, double pY, double pZ, ItemStack pStack, @Nullable Consumer<ItemEntity> extraStep) {
		while (!pStack.isEmpty()) {
			ItemEntity itementity = new ItemEntity(pLevel, pX + RANDOM.nextGaussian() * 0.1 - 0.05, pY, pZ + RANDOM.nextGaussian() * 0.1 - 0.05, pStack.split(Math.min(RANDOM.nextInt(21) + 10, pStack.getMaxStackSize())));
			itementity.setDeltaMovement(RANDOM.nextGaussian() * 0.05 - 0.025, RANDOM.nextGaussian() * 0.05 + 0.2, RANDOM.nextGaussian() * 0.05 - 0.025);
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
			return Component.translatable(key);
		} else {
			return Component.literal(capitaliseAllWords(dimension.location().getPath()));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static MutableComponent getStructureDisplayName(ResourceLocation rawName) {
		String key = "structure." + rawName.getNamespace() + "." + rawName.getPath();
		if (I18n.exists(key)) {
			return Component.translatable(key);
		} else {
			return Component.literal(capitaliseAllWords(rawName.getPath()));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static MutableComponent format(String s, Object... objects) {
		try {
			return Component.literal(MessageFormat.format(I18n.get(s), objects));
		} catch (Exception e) {
			return Component.translatable(s, objects);
		}
	}

	public static MutableComponent white(CharSequence s) {
		return Component.literal(s.toString()).withStyle(ChatFormatting.WHITE);
	}

	public static String chance(float chance) {
		return (chance < 0.01 ? "<1" : String.valueOf((int) (chance * 100))) + "%";
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

	public static <T> T getCycledItem(List<T> list, T fallback, int interval) {
		if (list.isEmpty()) {
			return fallback;
		}
		if (list.size() == 1) {
			return list.get(0);
		}
		long index = (System.currentTimeMillis() / interval) % list.size();
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
		return buf.readRegistryIdUnsafe(registry);
	}

	public static <T extends IForgeRegistryEntry<T>> void writeRegistryId(IForgeRegistry<T> registry, T entry, FriendlyByteBuf buf) {
		buf.writeRegistryIdUnsafe(registry, entry);
	}

	public static RecipeManager recipeManager() {
		if (recipeManager == null && FMLEnvironment.dist.isClient()) {
			if (LycheeConfig.debug) {
				Lychee.LOGGER.trace("Early loading recipes..");
			}
			recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
		}
		return recipeManager;
	}

	public static void setRecipeManager(RecipeManager recipeManager) {
		LUtil.recipeManager = recipeManager;
		if (LycheeConfig.debug) {
			Lychee.LOGGER.trace("Setting recipe manager..");
		}
	}

	public static Recipe<?> recipe(ResourceLocation id) {
		return recipeManager().byKey(id).orElse(null);
	}

	@SuppressWarnings("rawtypes")
	public static <T extends Recipe<?>> Collection<T> recipes(RecipeType<T> type) {
		return ((RecipeManagerAccess) recipeManager()).callByType((RecipeType) type).values();
	}

	// see Entity.getOnPos
	public static BlockPos getOnPos(Entity entity) {
		int i = Mth.floor(entity.getX());
		int j = Mth.floor(entity.getY() - 0.05); // vanilla is 0.2. carpet's height is 0.13
		int k = Mth.floor(entity.getZ());
		BlockPos blockpos = new BlockPos(i, j, k);
		if (entity.level.isEmptyBlock(blockpos)) {
			BlockPos blockpos1 = blockpos.below();
			BlockState blockstate = entity.level.getBlockState(blockpos1);
			if (blockstate.collisionExtendsVertically(entity.level, blockpos1, entity)) {
				return blockpos1;
			}
		}
		return blockpos;
	}

	public static Vec3 clampPos(Vec3 origin, BlockPos pos) {
		double x = clamp(origin.x, pos.getX());
		double y = clamp(origin.y, pos.getY());
		double z = clamp(origin.z, pos.getZ());
		if (x == origin.x && y == origin.y && z == origin.z) {
			return origin;
		}
		return new Vec3(x, y, z);
	}

	private static double clamp(double v, int target) {
		if (v < target) {
			return target;
		}
		if (v >= target + 1) {
			return target + 0.999999;
		}
		return v;
	}

	public static <T> List<T> tagElements(Registry<T> registry, TagKey<T> tag) {
		return Streams.stream(registry.getTagOrEmpty(tag)).map(Holder::value).toList();
	}

	public static boolean isSimpleIngredient(Ingredient ingredient) {
		return ingredient.isSimple();
	}

	public static BlockPos parseOffset(JsonObject o) {
		int x = GsonHelper.getAsInt(o, "offsetX", 0);
		int y = GsonHelper.getAsInt(o, "offsetY", 0);
		int z = GsonHelper.getAsInt(o, "offsetZ", 0);
		BlockPos offset = BlockPos.ZERO;
		if (x != 0 || y != 0 || z != 0) {
			offset = new BlockPos(x, y, z);
		}
		return offset;
	}
}
