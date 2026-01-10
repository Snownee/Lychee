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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.NonNullListExtensions;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.codec.LycheeCodecs;
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
	protected final boolean preserveAttributes;
	protected final boolean preserveDurability;

	public AnvilCraftingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			NonNullList<Ingredient> ingredients,
			ItemStack output,
			List<PostAction> assemblingActions,
			int levelCost,
			int materialCost,
			boolean preserveEnchantments,
			boolean preserveAttributes,
			boolean preserveDurability) {
		super(commonProperties);
		this.ingredients = ingredients;
		this.levelCost = levelCost;
		this.materialCost = materialCost;
		this.output = output;
		this.assemblingActions = assemblingActions;
		this.preserveEnchantments = preserveEnchantments;
		this.preserveAttributes = preserveAttributes;
		this.preserveDurability = preserveDurability;
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

			for (var entry : secondEnchantments.entrySet()) {
				Holder<Enchantment> enchantment = entry.getKey();
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

			EnchantmentHelper.setEnchantments(result, combinedEnchantments.toImmutable());
		}

		if (preserveAttributes) {
			ItemStack firstInput = anvilContext.input().getFirst();
			if (!firstInput.isEmpty()) {
				ItemAttributeModifiers attributes = firstInput.get(DataComponents.ATTRIBUTE_MODIFIERS);
				if (attributes != null) {
					result.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);
				}
			}
		}

		if (preserveDurability) {
			ItemStack firstInput = anvilContext.input().getFirst();
			if (firstInput.isDamageableItem() && result.isDamageableItem()) {
				float durabilityPercentage = (float)(firstInput.getMaxDamage() - firstInput.getDamageValue()) / firstInput.getMaxDamage();
				int newDamage = result.getMaxDamage() - Math.round(durabilityPercentage * result.getMaxDamage());
				result.setDamageValue(Math.max(0, Math.min(newDamage, result.getMaxDamage())));
			}
		}

		context.get(LycheeContextKey.ITEM).replace(2, result);
		final var actionContext = context.get(LycheeContextKey.ACTION);
		actionContext.reset();
		actionContext.jobs.addAll(assemblingActions.stream().map(it -> new Job(it, 1)).toList());
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

	public boolean preserveAttributes() {
		return preserveAttributes;
	}

	public boolean preserveDurability() {
		return preserveDurability;
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
						Codec.BOOL.optionalFieldOf("preserve_enchantments", true).forGetter(AnvilCraftingRecipe::preserveEnchantments),
						Codec.BOOL.optionalFieldOf("preserve_attributes", true).forGetter(AnvilCraftingRecipe::preserveAttributes),
						Codec.BOOL.optionalFieldOf("preserve_durability", true).forGetter(AnvilCraftingRecipe::preserveDurability)
				).apply(instance, AnvilCraftingRecipe::new));

		@Override
		public MapCodec<AnvilCraftingRecipe> codec() {
			return CODEC;
		}

		public static final StreamCodec<RegistryFriendlyByteBuf, AnvilCraftingRecipe> STREAM_CODEC = new StreamCodec<>() {
			@Override
			public AnvilCraftingRecipe decode(RegistryFriendlyByteBuf buf) {
				LycheeRecipeCommonProperties commonProperties = LycheeRecipeCommonProperties.STREAM_CODEC.decode(buf);
				NonNullList<Ingredient> ingredients = Ingredient.CONTENTS_STREAM_CODEC
						.apply(ByteBufCodecs.list(2))
						.map(NonNullList::copyOf, Function.identity())
						.decode(buf);
				ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
				List<PostAction> assemblingActions = PostAction.STREAM_LIST_CODEC.decode(buf);
				int levelCost = ByteBufCodecs.VAR_INT.decode(buf);
				int materialCost = ByteBufCodecs.VAR_INT.decode(buf);
				boolean preserveEnchantments = ByteBufCodecs.BOOL.decode(buf);
				boolean preserveAttributes = ByteBufCodecs.BOOL.decode(buf);
				boolean preserveDurability = ByteBufCodecs.BOOL.decode(buf);

				return new AnvilCraftingRecipe(commonProperties, ingredients, output, assemblingActions, levelCost, materialCost, preserveEnchantments, preserveAttributes, preserveDurability);
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buf, AnvilCraftingRecipe recipe) {
				LycheeRecipeCommonProperties.STREAM_CODEC.encode(buf, recipe.commonProperties());
				Ingredient.CONTENTS_STREAM_CODEC
						.apply(ByteBufCodecs.list(2))
						.map(NonNullList::copyOf, Function.identity())
						.encode(buf, recipe.getIngredients());
				ItemStack.STREAM_CODEC.encode(buf, recipe.output());
				PostAction.STREAM_LIST_CODEC.encode(buf, recipe.assemblingActions());
				ByteBufCodecs.VAR_INT.encode(buf, recipe.levelCost());
				ByteBufCodecs.VAR_INT.encode(buf, recipe.materialCost());
				ByteBufCodecs.BOOL.encode(buf, recipe.preserveEnchantments());
				ByteBufCodecs.BOOL.encode(buf, recipe.preserveAttributes());
				ByteBufCodecs.BOOL.encode(buf, recipe.preserveDurability());
			}
		};

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, AnvilCraftingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
