package snownee.lychee.block_crushing;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.ItemShapelessRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.mixin.BlockPredicateAccess;
import snownee.lychee.util.RecipeMatcher;

public class BlockCrushingRecipe extends LycheeRecipe<BlockCrushingContext> implements BlockKeyRecipe<BlockCrushingRecipe> {

	public static final BlockPredicate ANVIL = BlockPredicate.Builder.block().of(BlockTags.ANVIL).build();

	protected BlockPredicate fallingBlock = ANVIL;
	protected BlockPredicate landingBlock = BlockPredicate.ANY;
	protected NonNullList<Ingredient> ingredients = NonNullList.create();

	public BlockCrushingRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(BlockCrushingContext ctx, Level pLevel) {
		if (ctx.totalItems < ingredients.size()) {
			return false;
		}
		if (!BlockPredicateHelper.fastMatch(landingBlock, ctx)) {
			return false;
		}
		if (!matchesFallingBlock(ctx.fallingBlock.getBlockState(), ctx.fallingBlock.blockData)) {
			return false;
		}
		if (ingredients.isEmpty()) {
			return true;
		}
		List<ItemEntity> itemEntities = ctx.itemEntities.stream().filter($ -> {
			// ingredient.test is not thread safe
			return ingredients.stream().anyMatch(ingredient -> ingredient.test($.getItem()));
		}).limit(ItemShapelessRecipe.MAX_INGREDIENTS).toList();
		List<ItemStack> items = itemEntities.stream().map(ItemEntity::getItem).toList();
		int[] amount = items.stream().mapToInt(ItemStack::getCount).toArray();
		Optional<RecipeMatcher<ItemStack>> match = RecipeMatcher.findMatches(items, ingredients, amount);
		if (match.isEmpty()) {
			return false;
		}
		ctx.filteredItems = itemEntities;
		ctx.setMatch(match.get());
		return true;
	}

	public boolean matchesFallingBlock(BlockState blockstate, CompoundTag nbt) {
		if (fallingBlock == BlockPredicate.ANY) {
			return true;
		}
		BlockPredicateAccess access = (BlockPredicateAccess) fallingBlock;
		if (access.getTag() != null && !blockstate.is(access.getTag())) {
			return false;
		} else if (access.getBlocks() != null && !access.getBlocks().contains(blockstate.getBlock())) {
			return false;
		} else if (!access.getProperties().matches(blockstate)) {
			return false;
		} else {
			if (access.getNbt() != NbtPredicate.ANY) {
				return nbt != null && access.getNbt().matches(nbt);
			}
			return true;
		}
	}

	@Override
	public LycheeRecipe.Serializer<?> getSerializer() {
		return RecipeSerializers.BLOCK_CRUSHING;
	}

	@Override
	public LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.BLOCK_CRUSHING;
	}

	@Override
	public BlockPredicate getBlock() {
		return fallingBlock;
	}

	public BlockPredicate getLandingBlock() {
		return landingBlock;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public List<BlockPredicate> getBlockInputs() {
		return List.of(fallingBlock, landingBlock);
	}

	@Override
	public int compareTo(BlockCrushingRecipe that) {
		int i;
		i = Integer.compare(getMaxRepeats().isAny() ? 1 : 0, that.getMaxRepeats().isAny() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(landingBlock == BlockPredicate.ANY ? 1 : 0, that.landingBlock == BlockPredicate.ANY ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = -Integer.compare(ingredients.size(), that.ingredients.size());
		if (i != 0) {
			return i;
		}
		return getId().compareTo(that.getId());
	}

	public static class Serializer extends LycheeRecipe.Serializer<BlockCrushingRecipe> {

		public Serializer() {
			super(BlockCrushingRecipe::new);
		}

		@Override
		public void fromJson(BlockCrushingRecipe pRecipe, JsonObject pSerializedRecipe) {
			if (pSerializedRecipe.has("falling_block")) {
				pRecipe.fallingBlock = BlockPredicateHelper.fromJson(pSerializedRecipe.get("falling_block"));
			}
			if (pSerializedRecipe.has("landing_block")) {
				pRecipe.landingBlock = BlockPredicateHelper.fromJson(pSerializedRecipe.get("landing_block"));
			}
			if (pSerializedRecipe.has("item_in")) {
				JsonElement itemIn = pSerializedRecipe.get("item_in");
				if (itemIn.isJsonArray()) {
					itemIn.getAsJsonArray().forEach($ -> {
						pRecipe.ingredients.add(Ingredient.fromJson($));
					});
				} else {
					pRecipe.ingredients.add(Ingredient.fromJson(itemIn));
				}
				if (!pRecipe.ghost) {
					Preconditions.checkArgument(
							pRecipe.ingredients.size() <= ItemShapelessRecipe.MAX_INGREDIENTS,
							"Ingredients cannot be more than %s",
							ItemShapelessRecipe.MAX_INGREDIENTS);
				}
			}
		}

		@Override
		public void fromNetwork(BlockCrushingRecipe pRecipe, FriendlyByteBuf pBuffer) {
			pRecipe.fallingBlock = BlockPredicateHelper.fromNetwork(pBuffer);
			pRecipe.landingBlock = BlockPredicateHelper.fromNetwork(pBuffer);
			pBuffer.readCollection(i -> pRecipe.ingredients, Ingredient::fromNetwork);
		}

		@Override
		public void toNetwork0(FriendlyByteBuf pBuffer, BlockCrushingRecipe pRecipe) {
			BlockPredicateHelper.toNetwork(pRecipe.fallingBlock, pBuffer);
			BlockPredicateHelper.toNetwork(pRecipe.landingBlock, pBuffer);
			pBuffer.writeCollection(pRecipe.ingredients, (b, i) -> i.toNetwork(b));
		}

	}

}
