package snownee.lychee.core.def;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate.PropertyMatcher;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.mixin.BlockPredicateAccess;
import snownee.lychee.mixin.StatePropertiesPredicateAccess;
import snownee.lychee.util.LUtil;

public class BlockPredicateHelper {

	// handle BlockPredicate.ANY by yourself!
	@SuppressWarnings("deprecation")
	public static Set<Block> getMatchedBlocks(BlockPredicate predicate) {
		BlockPredicateAccess access = (BlockPredicateAccess) predicate;
		Set<Block> blocks = Sets.newLinkedHashSet();
		if (access.getBlocks() != null) {
			blocks.addAll(access.getBlocks());
		}
		if (access.getTag() != null) {
			blocks.addAll(LUtil.tagElements(Registry.BLOCK, access.getTag()));
		}
		return blocks;
	}

	public static List<ItemStack> getMatchedItemStacks(BlockPredicate predicate) {
		return getMatchedBlocks(predicate).stream().map(Block::asItem).filter(s -> s != Items.AIR).map(Item::getDefaultInstance).toList();
	}

	public static boolean fastMatch(BlockPredicate predicate, LycheeContext context) {
		return fastMatch(predicate, context.getParam(LootContextParams.BLOCK_STATE), () -> context.getParamOrNull(LootContextParams.BLOCK_ENTITY));
	}

	public static boolean fastMatch(BlockPredicate predicate, BlockState blockstate, Supplier<BlockEntity> beGetter) {
		if (predicate == BlockPredicate.ANY) {
			return true;
		}
		BlockPredicateAccess access = (BlockPredicateAccess) predicate;
		if (access.getTag() != null && !blockstate.is(access.getTag())) {
			return false;
		} else if (access.getBlocks() != null && !access.getBlocks().contains(blockstate.getBlock())) {
			return false;
		} else if (!access.getProperties().matches(blockstate)) {
			return false;
		} else {
			if (access.getNbt() != NbtPredicate.ANY) {
				BlockEntity blockentity = beGetter.get();
				if (blockentity == null || !access.getNbt().matches(blockentity.saveWithFullMetadata())) {
					return false;
				}
			}

			return true;
		}
	}

	@SuppressWarnings("deprecation")
	public static BlockPredicate fromJson(JsonElement jsonElement) {
		if (jsonElement != null && jsonElement.isJsonPrimitive()) {
			String id = jsonElement.getAsString();
			if ("*".equals(id)) {
				return BlockPredicate.ANY;
			}
			Block block = Registry.BLOCK.get(new ResourceLocation(id));
			return new BlockPredicate(null, Set.of(block), StatePropertiesPredicate.ANY, NbtPredicate.ANY);
		}
		return BlockPredicate.fromJson(jsonElement);
	}

	public static JsonElement toJson(BlockPredicate predicate) {
		if (predicate == BlockPredicate.ANY) {
			return new JsonPrimitive("*");
		}
		return predicate.serializeToJson();
	}

	public static final NbtPredicate NBT_PREDICATE_DUMMY = new NbtPredicate(new CompoundTag());

	public static BlockPredicate fromNetwork(FriendlyByteBuf pBuffer) {
		int blockCount = pBuffer.readVarInt();
		if (blockCount == -1) {
			return BlockPredicate.ANY;
		}
		Set<Block> blocks = null;
		if (blockCount > 0) {
			blocks = Sets.newHashSet();
			for (int i = 0; i < blockCount; i++) {
				blocks.add(LUtil.readRegistryId(ForgeRegistries.BLOCKS, pBuffer));
			}
		}
		TagKey<Block> tag = null;
		ResourceLocation tagId = LUtil.readNullableRL(pBuffer);
		if (tagId != null) {
			tag = TagKey.create(Registry.BLOCK_REGISTRY, tagId);
		}
		StatePropertiesPredicate propertiesPredicate = PropertiesPredicateHelper.fromNetwork(pBuffer);
		NbtPredicate nbtPredicate = pBuffer.readBoolean() ? NBT_PREDICATE_DUMMY : NbtPredicate.ANY;
		return new BlockPredicate(tag, blocks, propertiesPredicate, nbtPredicate);
	}

	public static void toNetwork(BlockPredicate predicate, FriendlyByteBuf pBuffer) {
		if (predicate == BlockPredicate.ANY) {
			pBuffer.writeVarInt(-1);
			return;
		}
		BlockPredicateAccess access = (BlockPredicateAccess) predicate;
		Set<Block> blocks = access.getBlocks();
		if (blocks == null) {
			pBuffer.writeVarInt(0);
		} else {
			pBuffer.writeVarInt(blocks.size());
			for (Block block : blocks) {
				LUtil.writeRegistryId(ForgeRegistries.BLOCKS, block, pBuffer);
			}
		}
		ResourceLocation tagId = null;
		TagKey<Block> tag = access.getTag();
		if (tag != null)
			tagId = tag.location();
		LUtil.writeNullableRL(tagId, pBuffer);
		PropertiesPredicateHelper.toNetwork(access.getProperties(), pBuffer);
		NbtPredicate nbtPredicate = access.getNbt();
		pBuffer.writeBoolean(nbtPredicate != NbtPredicate.ANY);
	}

	public static BlockState anyBlockState(BlockPredicate predicate) {
		return getShowcaseBlockStates(predicate).stream().findFirst().orElse(Blocks.AIR.defaultBlockState());
	}

	public static Set<Property<?>> ITERABLE_PROPERTIES = Sets.newConcurrentHashSet();
	private static final Cache<BlockPredicate, List<BlockState>> CACHE = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

	static {
		ITERABLE_PROPERTIES.addAll(List.of(BlockStateProperties.AGE_1, BlockStateProperties.AGE_2, BlockStateProperties.AGE_3, BlockStateProperties.AGE_5, BlockStateProperties.AGE_7, BlockStateProperties.CANDLES, BlockStateProperties.BITES, BlockStateProperties.POWER, BlockStateProperties.POWERED, BlockStateProperties.LIT, BlockStateProperties.BERRIES, BlockStateProperties.OPEN, BlockStateProperties.DELAY, BlockStateProperties.DISTANCE, BlockStateProperties.LAYERS, BlockStateProperties.PICKLES, BlockStateProperties.LEVEL, BlockStateProperties.LEVEL_HONEY, BlockStateProperties.LEVEL_CAULDRON, BlockStateProperties.DRIPSTONE_THICKNESS));
	}

	@SuppressWarnings("rawtypes")
	public static List<BlockState> getShowcaseBlockStates(BlockPredicate predicate) {
		try {
			return CACHE.get(predicate, () -> {
				Set<Block> blocks = getMatchedBlocks(predicate);
				if (blocks.isEmpty()) {
					return Collections.EMPTY_LIST;
				}
				List<BlockState> states = Lists.newArrayList();
				BlockPredicateAccess access = (BlockPredicateAccess) predicate;
				StatePropertiesPredicate propertiesPredicate = access.getProperties();

				for (Block block : blocks) {
					BlockState state = block.defaultBlockState();
					Multimap<Property<?>, Comparable> propertyMap = ArrayListMultimap.create();
					for (Property<? extends Comparable> property : block.getStateDefinition().getProperties()) {
						String name = property.getName();
						PropertyMatcher matcher = PropertiesPredicateHelper.findMatcher(propertiesPredicate, name);
						if (matcher != null) {
							for (Comparable object : property.getPossibleValues()) {
								if (matcher.match(block.getStateDefinition(), state.setValue((Property) property, object))) {
									propertyMap.put(property, object);
								}
							}
						} else if (ITERABLE_PROPERTIES.contains(property)) {
							propertyMap.putAll(property, property.getPossibleValues());
						}
					}
					Stream<BlockState> stream = Stream.of(state);
					for (Entry<Property<?>, Collection<Comparable>> e : propertyMap.asMap().entrySet()) {
						stream = stream.flatMap($ -> {
							return e.getValue().stream().map(v -> $.setValue((Property) e.getKey(), v));
						});
					}

					states.addAll(stream.toList());
				}
				return states;
			});
		} catch (ExecutionException e) {
			return Collections.EMPTY_LIST;
		}
	}

	@SuppressWarnings("rawtypes")
	public static List<Component> getTooltips(BlockState state, BlockPredicate predicate) {
		if (predicate == BlockPredicate.ANY) {
			return List.of(Component.translatable("tip.lychee.anyBlock"));
		}
		List<Component> list = Lists.newArrayList(state.getBlock().getName());
		BlockPredicateAccess access = (BlockPredicateAccess) predicate;
		List<PropertyMatcher> matchers = ((StatePropertiesPredicateAccess) access.getProperties()).getProperties();
		for (PropertyMatcher matcher : matchers) {
			MutableComponent name = Component.literal(matcher.getName() + "=").withStyle(ChatFormatting.GRAY);
			JsonElement json = matcher.toJson();
			if (json.isJsonPrimitive()) {
				name.append(Component.literal(json.getAsString()).withStyle(ChatFormatting.WHITE));
			} else {
				JsonObject object = json.getAsJsonObject();
				StateDefinition<Block, BlockState> definition = state.getBlock().getStateDefinition();
				String min, max;
				Property property = definition.getProperty(matcher.getName());
				List<Comparable> sorted = null;
				if (!object.has("min") || !object.has("max")) {
					sorted = property.getPossibleValues().stream().sorted().toList();
				}
				if (object.has("min")) {
					min = object.get("min").getAsString();
				} else {
					min = property.getName(sorted.get(0));
				}
				if (object.has("max")) {
					max = object.get("max").getAsString();
				} else {
					max = property.getName(sorted.get(sorted.size() - 1));
				}
				name.append(Component.literal(min).withStyle(ChatFormatting.WHITE));
				name.append(Component.literal("~").withStyle(ChatFormatting.GRAY));
				name.append(Component.literal(max).withStyle(ChatFormatting.WHITE));
			}
			list.add(name);
		}
		if (access.getNbt() != NbtPredicate.ANY) {
			list.add(Component.translatable("tip.lychee.nbtPredicate").withStyle(ChatFormatting.GRAY));
		}
		return list;
	}

}
