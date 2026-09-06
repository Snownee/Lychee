package snownee.lychee.recipes;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.NonNullListExtensions;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.codec.LycheeStreamCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class AnvilCraftingRecipe extends LycheeRecipe<LycheeContext> {
	protected final NonNullList<Ingredient> ingredients;
	protected final int levelCost;
	protected final int materialCost;
	protected final ItemStack output;
	protected final List<PostAction> assemblingActions;
	protected final boolean preserveEnchantments;

	public AnvilCraftingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			NonNullList<Ingredient> ingredients,
			ItemStack output,
			List<PostAction> assemblingActions,
			int levelCost,
			int materialCost,
			boolean preserveEnchantments) {
		super(commonProperties);
		this.ingredients = ingredients;
		this.levelCost = levelCost;
		this.materialCost = materialCost;
		this.output = output;
		this.assemblingActions = assemblingActions;
		this.preserveEnchantments = preserveEnchantments;
		onConstructed();
	}

	@Override
	public IntList getItemIndexes(final JsonPointer pointer) {
		if (pointer.size() == 1) {
			if (pointer.getString(0).equals(ITEM_OUT)) {
				return IntList.of(2);
			}
			if (pointer.getString(0).equals(ITEM_IN)) {
				return ingredients.size() == 1 ? IntList.of(0) : IntList.of(0, 1);
			}
		}
		if (pointer.size() == 2 && pointer.getString(0).equals(ITEM_IN)) {
			try {
				int i = pointer.getInt(1);
				if (i >= 0 && i < 2) {
					return IntList.of(i);
				}
			} catch (NumberFormatException ignored) {
			}
		}
		return IntList.of();
	}

	@Override
	public boolean matches(final LycheeContext context, final Level level) {
		final var anvilContext = context.get(LycheeContextKey.ANVIL);
		if (ingredients.size() == 2 && anvilContext.input().getSecond().getCount() < materialCost) {
			return false;
		}
		return ingredients.getFirst().test(anvilContext.input().getFirst()) && ingredients.getLast().test(anvilContext.input().getSecond());
	}

	@Override
	public ItemStack assemble(final LycheeContext context, final HolderLookup.Provider provider) {
		final var anvilContext = context.get(LycheeContextKey.ANVIL);
		anvilContext.setLevelCost(levelCost);
		anvilContext.setMaterialCost(materialCost);

		ItemStack result = getResultItem(provider);

		if (preserveEnchantments) {
			ItemStack firstInput = anvilContext.input().getFirst();
			ItemStack secondInput = anvilContext.input().getSecond();

			ItemEnchantments firstEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(firstInput);
			ItemEnchantments secondEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(secondInput);

			ItemEnchantments.Mutable combinedEnchantments = new ItemEnchantments.Mutable(firstEnchantments);

			outer:
			for (var entry : secondEnchantments.entrySet()) {
				Holder<Enchantment> enchantment = entry.getKey();
				for (Holder<Enchantment> secondEnchantment : combinedEnchantments.keySet()) {
					if (!Enchantment.areCompatible(enchantment, secondEnchantment)) {
						continue outer;
					}
				}

				int level = entry.getIntValue();
				int existingLevel = combinedEnchantments.getLevel(enchantment);

				if (existingLevel == level) {
					level = level + 1;
				} else {
					level = Math.max(level, existingLevel);
				}

				if (level <= enchantment.value().getMaxLevel()) {
					combinedEnchantments.set(enchantment, level);
				}
			}

			//FIXME check unsupported enchantments
			if (!combinedEnchantments.keySet().isEmpty()) {
				EnchantmentHelper.setEnchantments(result, combinedEnchantments.toImmutable());
			}
		}

		context.get(LycheeContextKey.ITEM).replace(2, result);
		final var actionContext = context.get(LycheeContextKey.ACTION);
		actionContext.reset();
		actionContext.appendActions(context, assemblingActions.stream(), 1);
		actionContext.run(context);
		return context.getItem(2);
	}

	@Override
	public ItemStack getResultItem(final HolderLookup.Provider provider) {
		return output.copy();
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public LycheeRecipeSerializer<AnvilCraftingRecipe> getSerializer() {
		return RecipeSerializers.ANVIL_CRAFTING;
	}

	@Override
	public LycheeRecipeType<AnvilCraftingRecipe> getType() {
		return RecipeTypes.ANVIL_CRAFTING;
	}

	@Override
	public Stream<PostAction> allActions() {
		return Streams.concat(postActions().stream(), assemblingActions().stream());
	}

	public int levelCost() {
		return levelCost;
	}

	public int materialCost() {
		return materialCost;
	}

	public boolean preserveEnchantments() {
		return preserveEnchantments;
	}

	public ItemStack output() {
		return output;
	}

	public List<PostAction> assemblingActions() {
		return assemblingActions;
	}

	public static class Serializer implements LycheeRecipeSerializer<AnvilCraftingRecipe> {
		public static final MapCodec<AnvilCraftingRecipe> CODEC =
				RecordCodecBuilder.mapCodec(instance -> instance.group(
						LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(AnvilCraftingRecipe::commonProperties),
						LycheeCodecs.sizeLimit(KCodecs.compactList(LycheeCodecs.NONEMPTY_INGREDIENT), 1, 2)
								.xmap(NonNullListExtensions::copyOf, Function.identity())
								.fieldOf(ITEM_IN)
								.forGetter(AnvilCraftingRecipe::getIngredients),
						LycheeCodecs.ITEM_STACK.fieldOf(ITEM_OUT).forGetter(AnvilCraftingRecipe::output),
						PostAction.LIST_CODEC.optionalFieldOf("assembling", List.of()).forGetter(AnvilCraftingRecipe::assemblingActions),
						ExtraCodecs.POSITIVE_INT.optionalFieldOf("level_cost", 1).forGetter(AnvilCraftingRecipe::levelCost),
						ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("material_cost", 1).forGetter(AnvilCraftingRecipe::materialCost),
						Codec.BOOL.optionalFieldOf("preserve_enchantments", false).forGetter(AnvilCraftingRecipe::preserveEnchantments)
				).apply(instance, AnvilCraftingRecipe::new));

		@Override
		public MapCodec<AnvilCraftingRecipe> codec() {
			return CODEC;
		}

		public static final StreamCodec<RegistryFriendlyByteBuf, AnvilCraftingRecipe> STREAM_CODEC = LycheeStreamCodecs.composite(
				LycheeRecipeCommonProperties.STREAM_CODEC,
				AnvilCraftingRecipe::commonProperties,
				Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(2)).map(NonNullListExtensions::copyOf, Function.identity()),
				AnvilCraftingRecipe::getIngredients,
				ItemStack.STREAM_CODEC,
				AnvilCraftingRecipe::output,
				PostAction.STREAM_LIST_CODEC,
				AnvilCraftingRecipe::assemblingActions,
				ByteBufCodecs.VAR_INT,
				AnvilCraftingRecipe::levelCost,
				ByteBufCodecs.VAR_INT,
				AnvilCraftingRecipe::materialCost,
				ByteBufCodecs.BOOL,
				AnvilCraftingRecipe::preserveEnchantments,
				AnvilCraftingRecipe::new);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, AnvilCraftingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
