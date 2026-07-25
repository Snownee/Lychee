package snownee.lychee.datagen;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JavaOps;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.recipe.RecipeUtil;
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
import snownee.lychee.action.ExtractItem;
import snownee.lychee.action.If;
import snownee.lychee.action.InsertItem;
import snownee.lychee.action.Move;
import snownee.lychee.action.MoveTowardsFace;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.action.SetBlock;
import snownee.lychee.action.input.CopyComponent;
import snownee.lychee.action.input.CopyDurability;
import snownee.lychee.action.input.DamageItem;
import snownee.lychee.action.input.PreventDefault;
import snownee.lychee.action.input.RemoveComponent;
import snownee.lychee.action.input.SetItem;
import snownee.lychee.recipes.BlockClickingRecipe;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.recipes.LightningChannelingRecipe;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionLike;
import snownee.lychee.util.codec.ParsedItem;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.ui.BlankRecipe;

public interface LycheeBuilder {
	ThreadLocal<@Nullable RegistryOps<Object>> registryOps = new ThreadLocal<>();

	static ParsedItem parse(String s, boolean single) {
		try {
			return ParsedItem.read(new StringReader(s), single);
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	static ParsedItem parse(String s) {
		return parse(s, false);
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

	default LycheeRecipeBuilder.SimpleShapeless<BlankRecipe> blankRecipe() {
		return new LycheeRecipeBuilder.SimpleShapeless<>(BlankRecipe::new);
	}

	default LycheeRecipeBuilder.AnvilCrafting anvilCraftingRecipe(
			Ingredient left,
			@Nullable Ingredient right,
			int materialCost,
			int levelCost,
			ItemStackTemplate output) {
		return new LycheeRecipeBuilder.AnvilCrafting(left, right, materialCost, levelCost, output);
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	default LycheeRecipeBuilder.BlockInteracting<BlockClickingRecipe> blockClickingRecipe(
			Optional<SizedIngredient> mainHand,
			@Nullable Optional<SizedIngredient> offHand,
			Object block) {
		return new LycheeRecipeBuilder.BlockInteracting<>(BlockClickingRecipe::new, mainHand, offHand, block(block));
	}

	default LycheeRecipeBuilder.BlockCrushing blockCrushingRecipe() {
		return new LycheeRecipeBuilder.BlockCrushing();
	}

	default LycheeRecipeBuilder.BlockExploding blockExplodingRecipe(Object block) {
		return new LycheeRecipeBuilder.BlockExploding(block(block));
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	default LycheeRecipeBuilder.BlockInteracting<BlockInteractingRecipe> blockInteractingRecipe(
			Optional<SizedIngredient> mainHand,
			@Nullable Optional<SizedIngredient> offHand,
			Object block) {
		return new LycheeRecipeBuilder.BlockInteracting<>(BlockInteractingRecipe::new, mainHand, offHand, block(block));
	}

	default LycheeRecipeBuilder.Dripstone blockDripstoneRecipe(Object sourceBlock, Object targetBlock) {
		return new LycheeRecipeBuilder.Dripstone(block(sourceBlock), block(targetBlock));
	}

	default LycheeRecipeBuilder.ItemBurning itemBurningRecipe(SizedIngredient input) {
		return new LycheeRecipeBuilder.ItemBurning(input);
	}

	default LycheeRecipeBuilder.ItemExploding itemExplodingRecipe() {
		return new LycheeRecipeBuilder.ItemExploding();
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
		return shapedCraftingRecipe(category, new ItemStackTemplate(result.asItem(), amount));
	}

	default LycheeRecipeBuilder.ShapedCrafting shapedCraftingRecipe(RecipeCategory category, ItemStackTemplate result) {
		return new LycheeRecipeBuilder.ShapedCrafting(category, result);
	}

	default LycheeRecipeBuilder.EntityTicking shapedCraftingRecipe(EntityPredicate predicate, int interval) {
		return new LycheeRecipeBuilder.EntityTicking(predicate, interval);
	}

	//@HideFromJS TODO
	default ActionBuilder<?, DropItem> dropItem(ItemLike item) {
		return dropItem(item, 1);
	}

	default ActionBuilder<?, DropItem> dropItem(ItemLike item, int count) {
		return dropItem(new ItemStackTemplate(item.asItem(), count));
	}

	default ActionBuilder<?, DropItem> dropItem(ItemStackTemplate itemStack) {
		return new ActionBuilder<>(new DropItem(PostActionCommonProperties.EMPTY, itemStack));
	}

	default ActionBuilder<?, PlaceBlock> place(Object block) {
		return place(block, BlockPos.ZERO);
	}

	default ActionBuilder<?, PlaceBlock> place(Object block, BlockPos offset) {
		return place(block, offset, false);
	}

	default ActionBuilder<?, PlaceBlock> place(Object block, BlockPos offset, boolean multi) {
		return new ActionBuilder<>(new PlaceBlock(PostActionCommonProperties.EMPTY, block(block), offset, multi));
	}

	default ActionBuilder<?, SetBlock> setBlock(Object block) {
		return new ActionBuilder<>(new SetBlock(PostActionCommonProperties.EMPTY, block(block)));
	}

	default ActionBuilder<?, CycleStateProperty> cycleStateProperty(Object block, String property) {
		return cycleStateProperty(block, property, BlockPos.ZERO);
	}

	default ActionBuilder<?, CycleStateProperty> cycleStateProperty(Object block, String property, BlockPos offset) {
		return new ActionBuilder<>(new CycleStateProperty(PostActionCommonProperties.EMPTY, block(block), offset, property, false));
	}

	default ActionBuilder<?, CycleStateProperty> cycleStatePropertyReversed(Object block, String property) {
		return cycleStatePropertyReversed(block, property, BlockPos.ZERO);
	}

	default ActionBuilder<?, CycleStateProperty> cycleStatePropertyReversed(Object block, String property, BlockPos offset) {
		return new ActionBuilder<>(new CycleStateProperty(PostActionCommonProperties.EMPTY, block(block), offset, property, true));
	}

	default ActionBuilder<?, MoveTowardsFace> moveTowardsFace(float factor) {
		return new ActionBuilder<>(new MoveTowardsFace(PostActionCommonProperties.EMPTY, factor));
	}

	default ActionBuilder<?, Move> move(Vec3 offset) {
		return new ActionBuilder<>(new Move(PostActionCommonProperties.EMPTY, offset, ""));
	}

	default ActionBuilder<?, Move> move(Vec3 offset, String with) {
		return new ActionBuilder<>(new Move(PostActionCommonProperties.EMPTY, offset, with));
	}

	default ActionBuilder<?, Execute> execute(String command) {
		return new ActionBuilder<>(new Execute(PostActionCommonProperties.EMPTY, command, true));
	}

	default ActionBuilder<?, Execute> executeNoRepeat(String command) {
		return new ActionBuilder<>(new Execute(PostActionCommonProperties.EMPTY, command, false));
	}

	default ActionBuilder<?, Exit> exit() {
		return new ActionBuilder<>(new Exit());
	}

	default ActionBuilder<?, If> ifAction(
			Collection<? extends PostActionLike> successEntries,
			Collection<? extends PostActionLike> failureEntries) {
		return new ActionBuilder<>(If.of(
				PostActionCommonProperties.EMPTY,
				successEntries.stream().map(PostActionLike::asAction).toList(),
				failureEntries.stream().map(PostActionLike::asAction).toList()));
	}

	default ActionBuilder<?, AddItemCooldown> addItemCooldown(float seconds) {
		return addItemCooldown(seconds, null);
	}

	default ActionBuilder<?, AddItemCooldown> addItemCooldown(float seconds, @Nullable Item item) {
		return new ActionBuilder<>(new AddItemCooldown(PostActionCommonProperties.EMPTY, seconds, Optional.ofNullable(item)));
	}

	default ActionBuilder<?, PreventDefault> preventDefault() {
		return new ActionBuilder<>(new PreventDefault());
	}

	default ActionBuilder<?, DropXp> dropXp(int amount) {
		return new ActionBuilder<>(new DropXp(PostActionCommonProperties.EMPTY, amount));
	}

	default ActionBuilder<?, AnvilDamageChance> anvilDamageChance(float chance) {
		return new ActionBuilder<>(new AnvilDamageChance(PostActionCommonProperties.EMPTY, chance));
	}

	default ActionBuilder.RandomSelectBuilder randomSelect() {
		return new ActionBuilder.RandomSelectBuilder();
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	default ActionBuilder<?, Explode> explode(
			boolean attributeToEntity,
			Optional<Holder<DamageType>> damageType,
			Optional<LevelBasedValue> knockbackMultiplier,
			Optional<HolderSet<Block>> immuneBlocks,
			Vec3 offset,
			LevelBasedValue radius,
			boolean createFire,
			Level.ExplosionInteraction blockInteraction,
			ParticleOptions smallParticle,
			ParticleOptions largeParticle,
			WeightedList<ExplosionParticleInfo> blockParticles,
			Holder<SoundEvent> sound) {
		return new ActionBuilder<>(new Explode(
				PostActionCommonProperties.EMPTY,
				attributeToEntity,
				damageType,
				knockbackMultiplier,
				immuneBlocks,
				offset,
				radius,
				createFire,
				blockInteraction,
				smallParticle,
				largeParticle,
				blockParticles,
				sound));
	}

	default ActionBuilder<?, CustomAction> customAction(String id, JsonObject json, boolean repeatable, boolean preventSync) {
		return new ActionBuilder<>(new CustomAction(PostActionCommonProperties.EMPTY, id, json, repeatable, preventSync));
	}

	default ActionBuilder<?, DamageItem> damageItem(int damage, Reference target) {
		return new ActionBuilder<>(new DamageItem(PostActionCommonProperties.EMPTY, damage, target));
	}

	default ActionBuilder<?, SetItem> setItem(ItemStackTemplate itemStack, Reference target) {
		return new ActionBuilder<>(new SetItem(PostActionCommonProperties.EMPTY, itemStack, target));
	}

	default ActionBuilder<?, Delay> delay(float seconds) {
		return new ActionBuilder<>(new Delay(seconds));
	}

	default ActionBuilder<?, CopyComponent> copyComponent(
			Reference source,
			Reference target,
			@Nullable Collection<DataComponentType<?>> components) {
		return new ActionBuilder<>(new CopyComponent(
				PostActionCommonProperties.EMPTY,
				components == null ? List.of() : List.copyOf(components),
				source,
				target));
	}

	default ActionBuilder<?, RemoveComponent> removeComponent(
			Reference target,
			@Nullable Collection<DataComponentType<?>> components) {
		return new ActionBuilder<>(new RemoveComponent(
				PostActionCommonProperties.EMPTY,
				components == null ? List.of() : List.copyOf(components),
				target));
	}

	default ActionBuilder<?, CopyDurability> copyDurability(float bonus, Reference source, Reference target) {
		return new ActionBuilder<>(new CopyDurability(PostActionCommonProperties.EMPTY, bonus, source, target));
	}

	default ActionBuilder<?, InsertItem> insertItem(ItemStackTemplate itemStack) {
		return new ActionBuilder<>(new InsertItem(
				PostActionCommonProperties.EMPTY,
				Optional.of(itemStack),
				Optional.empty(),
				true));
	}

	default ActionBuilder<?, InsertItem> insertItemFrom(Reference from) {
		return new ActionBuilder<>(new InsertItem(
				PostActionCommonProperties.EMPTY,
				Optional.empty(),
				Optional.of(from),
				true));
	}

	default ActionBuilder<?, InsertItem> insertItemFrom(Reference from, boolean dropItemIfFail) {
		return new ActionBuilder<>(new InsertItem(
				PostActionCommonProperties.EMPTY,
				Optional.empty(),
				Optional.of(from),
				dropItemIfFail));
	}

	default ActionBuilder<?, ExtractItem> extractItem(ItemPredicate item, int count) {
		return new ActionBuilder<>(new ExtractItem(PostActionCommonProperties.EMPTY, item, count));
	}

	@SuppressWarnings("unchecked")
	default BlockPredicate block(Object o) {
		return switch (o) {
			case BlockPredicate bp -> bp;
			case String s -> BlockPredicateExtensions.fromString(getterOrThrow(Registries.BLOCK), s, true).getOrThrow();
			case Block block -> BlockPredicate.Builder.block().of(getterOrThrow(Registries.BLOCK), block).build();
			case TagKey<?> tagKey -> BlockPredicate.Builder.block().of(getterOrThrow(Registries.BLOCK), (TagKey<Block>) tagKey).build();
			default -> throw new IllegalArgumentException("Invalid argument: " + o);
		};
	}

	default SizedIngredient sized(Object o) {
		if (o instanceof String s) {
			return parse(s).sizedIngredient();
		}
		return sized(o, 1);
	}

	@SuppressWarnings("unchecked")
	default SizedIngredient sized(Object o, int count) {
		Ingredient i = switch (o) {
			case SizedIngredient si -> si.ingredient();
			case Ingredient ing -> ing;
			case ItemLike item -> Ingredient.of(item);
			case ItemInstance itemInstance -> Ingredient.of(itemInstance.typeHolder().value());
			case TagKey<?> tagKey -> RecipeUtil.tagIngredient(getterOrThrow(Registries.ITEM), (TagKey<Item>) tagKey);
			case String s -> parse(s).ingredient();
			default -> throw new IllegalArgumentException("Invalid argument: " + o);
		};
		return new SizedIngredient(i, count);
	}

	static <E> Optional<HolderGetter<E>> getter(ResourceKey<? extends Registry<? extends E>> registryKey) {
		return Objects.requireNonNull(registryOps.get()).getter(registryKey);
	}

	static <E> HolderGetter<E> getterOrThrow(ResourceKey<? extends Registry<? extends E>> registryKey) {
		return getter(registryKey).orElseThrow();
	}
}
