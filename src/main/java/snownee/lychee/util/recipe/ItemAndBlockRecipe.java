package snownee.lychee.util.recipe;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public abstract class ItemAndBlockRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe<ItemAndBlockRecipe> {
	public static MapCodec<Ingredient> INPUT_CODEC = Ingredient.CODEC.fieldOf(ITEM_IN);
	public static MapCodec<BlockPredicate> BLOCK_CODEC = BlockPredicate.CODEC.fieldOf(BLOCK_IN);

	protected Ingredient input;
	protected BlockPredicate block;

	protected ItemAndBlockRecipe(LycheeRecipeCommonProperties commonProperties) {
		super(commonProperties);
	}

	public Ingredient input() {
		return input;
	}

	@Override
	public Optional<BlockPredicate> blockPredicate() {
		return Optional.ofNullable(block);
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, input);
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		final var thisEntity = context.get(LycheeContextKey.LOOT_PARAMS).get(LootContextParams.THIS_ENTITY);
		final var stack = thisEntity instanceof ItemEntity itemEntity ? itemEntity.getItem() : context.getItem(0);
		return input.test(stack) && BlockPredicateExtensions.matches(block, context);
	}

	@Override
	public int compareTo(ItemAndBlockRecipe that) {
		int i;
		i = Integer.compare(maxRepeats().isAny() ? 1 : 0, that.maxRepeats().isAny() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(block == null ? 1 : 0, that.block == null ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(
				CommonProxy.isSimpleIngredient(input) ? 1 : 0,
				CommonProxy.isSimpleIngredient(that.input) ? 1 : 0
		);
		return i;
	}
}
