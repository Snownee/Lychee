package snownee.lychee.item_inside;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParamSets;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.LycheeRecipe;
import snownee.lychee.core.def.BlockPredicateHelper;

public class ItemInsideRecipe extends LycheeRecipe<LycheeContext> {

	protected Ingredient input;
	protected BlockPredicate block;

	public ItemInsideRecipe(ResourceLocation id) {
		super(id);
	}

	public Ingredient getInput() {
		return input;
	}

	public BlockPredicate getBlock() {
		return block;
	}

	@Override
	public boolean matches(LycheeContext ctx, Level pLevel) {
		ItemStack stack = ((ItemEntity) ctx.getParam(LootContextParams.THIS_ENTITY)).getItem();
		return checkConditions(this, ctx, 1) > 0 && input.test(stack) && BlockPredicateHelper.fastMatch(block, ctx);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializers.ITEM_INSIDE;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeTypes.ITEM_INSIDE;
	}

	public static class Serializer extends LycheeRecipe.Serializer<ItemInsideRecipe> {

		public Serializer() {
			super(ItemInsideRecipe::new);
		}

		@Override
		public void fromJson(ItemInsideRecipe pRecipe, JsonObject pSerializedRecipe) {
			pRecipe.input = Ingredient.fromJson(pSerializedRecipe.get("item_in"));
			pRecipe.block = BlockPredicateHelper.fromJson(pSerializedRecipe.get("block_in"));
		}

		@Override
		public void fromNetwork(ItemInsideRecipe pRecipe, FriendlyByteBuf pBuffer) {
			pRecipe.input = Ingredient.fromNetwork(pBuffer);
			pRecipe.block = BlockPredicateHelper.fromNetwork(pBuffer);
		}

		@Override
		public void toNetwork(FriendlyByteBuf pBuffer, ItemInsideRecipe pRecipe) {
			super.toNetwork(pBuffer, pRecipe);
			pRecipe.input.toNetwork(pBuffer);
			BlockPredicateHelper.toNetwork(pRecipe.block, pBuffer);
		}

	}

	private static final Set<Block> POSSIBLE_BLOCKS = Sets.newIdentityHashSet();

	public static void on(ItemEntity entity) {
		BlockPos pos = entity.blockPosition();
		BlockState blockstate = entity.level.getBlockState(pos);
		if (!POSSIBLE_BLOCKS.contains(blockstate.getBlock())) {
			return;
		}
		LootContext.Builder builder = new LootContext.Builder((ServerLevel) entity.level);
		builder.withParameter(LootContextParams.ORIGIN, entity.position());
		builder.withParameter(LootContextParams.THIS_ENTITY, entity);
		builder.withParameter(LootContextParams.BLOCK_STATE, blockstate);
		builder.withParameter(LycheeLootContextParams.BLOCK_POS, pos);
		if (blockstate.hasBlockEntity()) {
			builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, entity.level.getBlockEntity(pos));
		}
		LycheeContext ctx = new LycheeContext(builder.create(LycheeLootContextParamSets.ITEM_INSIDE));
		entity.level.getRecipeManager().getRecipeFor(RecipeTypes.ITEM_INSIDE, ctx, entity.level).ifPresent($ -> {
			int times = $.willBatchRun ? entity.getItem().getCount() : 1;
			entity.getItem().shrink(times);
			$.applyPostActions(ctx, times);
		});
	}

	public static void buildCache(Collection<ItemInsideRecipe> recipes) {
		POSSIBLE_BLOCKS.clear();
		recipes.stream().flatMap($ -> BlockPredicateHelper.getMatchedBlocks($.block).stream()).forEach(POSSIBLE_BLOCKS::add);
	}

}
