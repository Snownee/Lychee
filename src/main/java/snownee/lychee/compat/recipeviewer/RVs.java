package snownee.lychee.compat.recipeviewer;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.joml.Quaternionf;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.CustomLightingSettings;
import snownee.lychee.client.gui.ILightingSettings;
import snownee.lychee.util.render.CachedRenderingEntity;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;

// Recipe view utils
public final class RVs {
	public static final CachedRenderingEntity<PrimedTnt> TNT_ENTITY = CachedRenderingEntity.ofFactory(EntityType.TNT::create);
	public static ILightingSettings BLOCK_LIGHTING = CustomLightingSettings.builder()
			.firstLightRotation(-45, -45)
			.secondLightRotation(15, -60)
			.build();
	public static ILightingSettings SIDE_ICON_LIGHTING = CustomLightingSettings.builder()
			.firstLightRotation(-30, -60)
			.secondLightRotation(30, -60)
			.build();
	public static ILightingSettings FUSED_TNT_LIGHTING = CustomLightingSettings.builder()
			.firstLightRotation(-120, 20)
			.secondLightRotation(200, 45)
			.build();

	public static List<IngredientInfo> generateShapelessInputs(ILycheeRecipe<LycheeContext> recipe) {
		List<IngredientInfo> ingredients;
		try {
			ingredients = recipe.sizedIngredients().stream().map(IngredientInfo::new).toList();
		} catch (Exception e) {
			ingredients = recipe.getIngredients().stream().map(IngredientInfo::new).toList();
		}
		for (PostAction action : recipe.postActions()) {
			ActionRenderer.of(action).loadCatalystsInfo(action, recipe, ingredients);
		}
		addIngredientTips(recipe, ingredients);
		return ingredients;
	}

	public static void addIngredientTips(ILycheeRecipe<LycheeContext> recipe, List<IngredientInfo> ingredients) {
		for (IngredientInfo ingredient : ingredients) {
			IngredientType type = CommonProxy.getIngredientType(ingredient.ingredient);
			if (type != IngredientType.NORMAL) {
				ingredient.addTooltip(Component.translatable("tip.lychee.ingredient." + type.name().toLowerCase(Locale.ROOT)));
			}
		}
	}

	public static MutableComponent makeTitle(ResourceLocation id) {
		var key = id.toLanguageKey("recipeType");
		var i = key.indexOf('/');
		if ("/minecraft/default".equals(key.substring(i))) {
			key = key.substring(0, i);
		}
		return Component.translatable(key);
	}

	public static List<Component> getRecipeTooltip(ILycheeRecipe<?> recipe) {
		var list = Lists.<Component>newArrayList();
		if (recipe.comment().map(it -> !Strings.isNullOrEmpty(it)).orElse(false)) {
			var comment = recipe.comment().orElseThrow();
			if (I18n.exists(comment)) {
				comment = I18n.get(comment);
			}
			Splitter.on('\n').splitToStream(comment).map(Component::literal).forEach(list::add);
		}
		var mc = Minecraft.getInstance();
		recipe.conditions().appendToTooltips(list, mc.level, mc.player, 0);
		return list;
	}

	public static <T extends BlockKeyableRecipe> Pair<BlockState, Integer> getMostUsedBlock(Collection<? extends RecipeHolder<? extends T>> recipes) {
		var blockStateCount = new Object2IntOpenHashMap<Block>();
		var blockPredicateMap = Maps.<Block, BlockPredicate>newHashMap();
		for (var object : recipes) {
			var recipe = object.value();
			for (var block : BlockPredicateExtensions.matchedBlocks(recipe.blockPredicate())) {
				if (block.defaultBlockState().isAir()) {
					continue;
				}
				blockStateCount.mergeInt(block, 1, Integer::sum);
				blockPredicateMap.putIfAbsent(block, recipe.blockPredicate());
			}
		}
		if (blockStateCount.isEmpty()) {
			return Pair.of(Blocks.AIR.defaultBlockState(), 0);
		}
		return blockStateCount.object2IntEntrySet().stream()
				.max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue))
				.map($ -> Pair.of(BlockPredicateExtensions.anyBlockState(blockPredicateMap.get($.getKey())), $.getIntValue()))
				.orElseGet(() -> Pair.of(Blocks.AIR.defaultBlockState(), 0));
	}

	public static void renderTnt(GuiGraphics graphics) {
		PrimedTnt tnt = TNT_ENTITY.getEntity();
		int fuse = 80 - tnt.tickCount % 80;
		if (fuse >= 40) {
			return;
		}
		TNT_ENTITY.earlySetLevel();
		tnt.setFuse(fuse);
		float toRad = 0.01745329251F;
		Quaternionf quaternion = new Quaternionf().rotateXYZ(200 * toRad, -20 * toRad, 0);
		FUSED_TNT_LIGHTING.applyLighting();
		TNT_ENTITY.render(graphics.pose(), quaternion);
	}

	public static ResourceLocation composeCategoryIdentifier(ResourceLocation categoryId, ResourceLocation group) {
		return ResourceLocation.fromNamespaceAndPath(
				categoryId.getNamespace(),
				"%s/%s/%s".formatted(categoryId.getPath(), group.getNamespace(), group.getPath()));
	}

	public static BlockState getIconBlock(Collection<? extends RecipeHolder<? extends BlockKeyableRecipe>> recipes) {
		var con = Minecraft.getInstance().getConnection();
		if (con == null) {
			return Blocks.AIR.defaultBlockState();
		}
		return getMostUsedBlock(recipes).getFirst();
	}
}
