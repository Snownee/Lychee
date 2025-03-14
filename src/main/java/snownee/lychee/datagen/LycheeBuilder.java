package snownee.lychee.datagen;

import java.util.Collection;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.mojang.serialization.JavaOps;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.action.AddItemCooldown;
import snownee.lychee.action.AnvilDamageChance;
import snownee.lychee.action.CustomAction;
import snownee.lychee.action.CycleStateProperty;
import snownee.lychee.action.Delay;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.DropXp;
import snownee.lychee.action.Execute;
import snownee.lychee.action.Exit;
import snownee.lychee.action.Explode;
import snownee.lychee.action.If;
import snownee.lychee.action.MoveTowardsFace;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.action.input.DamageItem;
import snownee.lychee.action.input.PreventDefault;
import snownee.lychee.action.input.SetItem;
import snownee.lychee.recipes.BlockClickingRecipe;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.recipes.ItemExplodingRecipe;
import snownee.lychee.recipes.LightningChannelingRecipe;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionLike;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public interface LycheeBuilder {
	ThreadLocal<RegistryOps<Object>> registryOps = new ThreadLocal<>();

	private static Ingredient parse(String s) {
		ExtraCodecs.TagOrElementLocation id = ExtraCodecs.TAG_OR_ELEMENT_ID.decode(JavaOps.INSTANCE, s).getOrThrow().getFirst();
		if (id.tag()) {
			return Ingredient.of(TagKey.create(Registries.ITEM, id.id()));
		} else {
			return Ingredient.of(BuiltInRegistries.ITEM.get(id.id()));
		}
	}

	static LycheeBuilder create(RegistryOps<?> registryOps) {
		LycheeBuilder builder = new LycheeBuilder() {};
		builder.setup(registryOps.withParent(JavaOps.INSTANCE));
		return builder;
	}

	default void setup(HolderLookup.Provider wrapperLookup) {
		setup(wrapperLookup.createSerializationContext(JavaOps.INSTANCE));
	}

	default void setup(RegistryOps<Object> ops) {
		registryOps.set(ops);
	}

	default void teardown() {
		registryOps.remove();
	}

	default LycheeRecipeBuilder.AnvilCrafting anvilCraftingRecipe(
			Ingredient left,
			@Nullable Ingredient right,
			int materialCost,
			int levelCost,
			ItemStack output) {
		return new LycheeRecipeBuilder.AnvilCrafting(left, right, materialCost, levelCost, output);
	}

	default LycheeRecipeBuilder.BlockInteracting<BlockClickingRecipe> blockClickingRecipe(
			SizedIngredient mainHand,
			@Nullable SizedIngredient offHand,
			Object block) {
		return new LycheeRecipeBuilder.BlockInteracting<>(BlockClickingRecipe::new, mainHand, offHand, block(block));
	}

	default LycheeRecipeBuilder.BlockCrushing blockCrushingRecipe() {
		return new LycheeRecipeBuilder.BlockCrushing();
	}

	default LycheeRecipeBuilder.BlockExploding blockExplodingRecipe(Object block) {
		return new LycheeRecipeBuilder.BlockExploding(block(block));
	}

	default LycheeRecipeBuilder.BlockInteracting<BlockInteractingRecipe> blockInteractingRecipe(
			SizedIngredient mainHand,
			@Nullable SizedIngredient offHand,
			Object block) {
		return new LycheeRecipeBuilder.BlockInteracting<>(BlockInteractingRecipe::new, mainHand, offHand, block(block));
	}

	default LycheeRecipeBuilder.Dripstone blockDripstoneRecipe(Object sourceBlock, Object targetBlock) {
		return new LycheeRecipeBuilder.Dripstone(block(sourceBlock), block(targetBlock));
	}

	default LycheeRecipeBuilder.ItemBurning itemBurningRecipe(SizedIngredient input) {
		return new LycheeRecipeBuilder.ItemBurning(input);
	}

	default LycheeRecipeBuilder.SimpleShapeless<ItemExplodingRecipe> itemExplodingRecipe() {
		return new LycheeRecipeBuilder.SimpleShapeless<>(ItemExplodingRecipe::new);
	}

	default LycheeRecipeBuilder.ItemInside itemInsideRecipe() {
		return new LycheeRecipeBuilder.ItemInside();
	}

	default LycheeRecipeBuilder.SimpleShapeless<LightningChannelingRecipe> lightningChannelingRecipe() {
		return new LycheeRecipeBuilder.SimpleShapeless<>(LightningChannelingRecipe::new);
	}

	default LycheeRecipeBuilder.RandomBlockTicking randomBlockTickingRecipe(Object block) {
		return new LycheeRecipeBuilder.RandomBlockTicking(block(block));
	}

	default LycheeRecipeBuilder.ShapedCrafting shapedCraftingRecipe(RecipeCategory category, ItemLike result) {
		return shapedCraftingRecipe(category, result, 1);
	}

	default LycheeRecipeBuilder.ShapedCrafting shapedCraftingRecipe(RecipeCategory category, ItemLike result, int amount) {
		return shapedCraftingRecipe(category, new ItemStack(result, amount));
	}

	default LycheeRecipeBuilder.ShapedCrafting shapedCraftingRecipe(RecipeCategory category, ItemStack result) {
		return new LycheeRecipeBuilder.ShapedCrafting(category, result);
	}

	@HideFromJS
	default ActionBuilder<DropItem> dropItem(ItemLike item) {
		return dropItem(item, 1);
	}

	default ActionBuilder<DropItem> dropItem(ItemLike item, int count) {
		return dropItem(new ItemStack(item, count));
	}

	default ActionBuilder<DropItem> dropItem(ItemStack itemStack) {
		return new ActionBuilder<>(new DropItem(PostActionCommonProperties.EMPTY, itemStack));
	}

	default ActionBuilder<PlaceBlock> place(Object block) {
		return place(block, BlockPos.ZERO);
	}

	default ActionBuilder<PlaceBlock> place(Object block, BlockPos offset) {
		return new ActionBuilder<>(new PlaceBlock(PostActionCommonProperties.EMPTY, block(block), offset));
	}

	default ActionBuilder<CycleStateProperty> cycleStateProperty(Object block, String property) {
		return cycleStateProperty(block, property, BlockPos.ZERO);
	}

	default ActionBuilder<CycleStateProperty> cycleStateProperty(Object block, String property, BlockPos offset) {
		return new ActionBuilder<>(new CycleStateProperty(PostActionCommonProperties.EMPTY, block(block), offset, property));
	}

	default ActionBuilder<MoveTowardsFace> moveTowardsFace(float factor) {
		return new ActionBuilder<>(new MoveTowardsFace(PostActionCommonProperties.EMPTY, factor));
	}

	default ActionBuilder<Execute> execute(String command) {
		return new ActionBuilder<>(new Execute(PostActionCommonProperties.EMPTY, command, true));
	}

	default ActionBuilder<Execute> executeNoRepeat(String command) {
		return new ActionBuilder<>(new Execute(PostActionCommonProperties.EMPTY, command, false));
	}

	default ActionBuilder<Exit> exit() {
		return new ActionBuilder<>(new Exit());
	}

	default ActionBuilder<If> ifAction(Collection<PostActionLike> successEntries, Collection<PostActionLike> failureEntries) {
		return new ActionBuilder<>(new If(
				PostActionCommonProperties.EMPTY,
				successEntries.stream().map(PostActionLike::asAction).toList(),
				failureEntries.stream().map(PostActionLike::asAction).toList()));
	}

	default ActionBuilder<AddItemCooldown> addItemCooldown(float seconds) {
		return addItemCooldown(seconds, null);
	}

	default ActionBuilder<AddItemCooldown> addItemCooldown(float seconds, @Nullable Item item) {
		return new ActionBuilder<>(new AddItemCooldown(PostActionCommonProperties.EMPTY, seconds, Optional.ofNullable(item)));
	}

	default ActionBuilder<PreventDefault> preventDefault() {
		return new ActionBuilder<>(new PreventDefault());
	}

	default ActionBuilder<DropXp> dropXp(int amount) {
		return new ActionBuilder<>(new DropXp(PostActionCommonProperties.EMPTY, amount));
	}

	default ActionBuilder<AnvilDamageChance> anvilDamageChance(float chance) {
		return new ActionBuilder<>(new AnvilDamageChance(PostActionCommonProperties.EMPTY, chance));
	}

	default ActionBuilder<Explode> explode(
			Explosion.BlockInteraction blockInteraction,
			BlockPos offset,
			boolean fire,
			float radius,
			float step) {
		return new ActionBuilder<>(new Explode(PostActionCommonProperties.EMPTY, blockInteraction, offset, fire, radius, step));
	}

	default ActionBuilder<CustomAction> customAction(String id, JsonObject json, boolean repeatable) {
		return new ActionBuilder<>(new CustomAction(PostActionCommonProperties.EMPTY, id, json, repeatable));
	}

	default ActionBuilder<DamageItem> damageItem(int damage, Reference target) {
		return new ActionBuilder<>(new DamageItem(PostActionCommonProperties.EMPTY, damage, target));
	}

	default ActionBuilder<SetItem> setItem(ItemStack itemStack, Reference target) {
		return new ActionBuilder<>(new SetItem(PostActionCommonProperties.EMPTY, itemStack, target));
	}

	default ActionBuilder<Delay> delay(float seconds) {
		return new ActionBuilder<>(new Delay(seconds));
	}

	@SuppressWarnings("unchecked")
	default BlockPredicate block(Object o) {
		return switch (o) {
			case BlockPredicate bp -> bp;
			case String s -> BlockPredicateExtensions.fromString(s, true).getOrThrow();
			case Block block -> BlockPredicate.Builder.block().of(block).build();
			case TagKey<?> tagKey -> BlockPredicate.Builder.block().of((TagKey<Block>) tagKey).build();
			default -> throw new IllegalArgumentException("Invalid argument: " + o);
		};
	}

	default SizedIngredient sized(Object o) {
		if (o instanceof String s && s.length() > 3) {
			char c = s.charAt(0);
			if (c >= '1' && c <= '9') {
				int i = s.indexOf("x ");
				if (i > 0) {
					int count = Integer.parseInt(s.substring(0, i));
					return sized(s.substring(i + 2), count);
				}
			}
		}
		return sized(o, 1);
	}

	@SuppressWarnings("unchecked")
	default SizedIngredient sized(Object o, int count) {
		Ingredient i = switch (o) {
			case SizedIngredient si -> si.ingredient();
			case Ingredient ing -> ing;
			case ItemLike item -> Ingredient.of(item);
			case ItemStack stack -> Ingredient.of(stack);
			case TagKey<?> tagKey -> Ingredient.of((TagKey<Item>) tagKey);
			case String s -> parse(s);
			default -> throw new IllegalArgumentException("Invalid argument: " + o);
		};
		return new SizedIngredient(i, count);
	}
}
