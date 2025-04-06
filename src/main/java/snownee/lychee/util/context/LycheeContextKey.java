package snownee.lychee.util.context;

import java.util.IdentityHashMap;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.AnvilContext;
import snownee.lychee.context.CraftingContext;
import snownee.lychee.context.ItemShapelessContext;
import snownee.lychee.context.LootParamsContext;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.ActionData;
import snownee.lychee.util.action.ActionMarker;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.recipe.ILycheeRecipe;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public sealed abstract class LycheeContextKey<T> permits LycheeContextKey.Required, LycheeContextKey.Optional {
	public static final LycheeContextKey.Optional<ActionMarker> MARKER = opt(
			"marker", it -> {
				var level = it.level();
				if (level.isClientSide) {
					return null;
				}
				var marker = new Marker(EntityType.MARKER, level);
				var lootParams = it.get(LycheeContextKey.LOOT_PARAMS);
				var pos = lootParams.getOrNull(LootContextParams.ORIGIN);
				if (pos != null) {
					marker.moveTo(pos);
				}
				marker.setCustomName(Component.literal(Lychee.ID));
				level.addFreshEntity(marker);
				var actionMarker = (ActionMarker) marker;
				actionMarker.lychee$setData(new ActionData(it, 0));
				return actionMarker;
			});
	public final ResourceLocation id;

	public static final LycheeContextKey.Required<Level> LEVEL = req("level");
	public static final LycheeContextKey.Required<RandomSource> RANDOM = req("random", it -> it.level().random);
	public static final LycheeContextKey.Required<LootParamsContext> LOOT_PARAMS = req(
			"loot_params",
			it -> new LootParamsContext(it, new IdentityHashMap<>()));
	public static final LycheeContextKey.Required<ActionContext> ACTION = req("action", it -> new ActionContext());

	public static final LycheeContextKey.Optional<ResourceLocation> RECIPE_ID = opt("recipe_id");
	public static final LycheeContextKey.Optional<ILycheeRecipe<?>> RECIPE = opt(
			"recipe", it -> {
				var id = it.getOrNull(LycheeContextKey.RECIPE_ID);
				if (id != null) {
					var holder = CommonProxy.recipe(id);
					if (holder != null && holder.value() instanceof ILycheeRecipe<?> lycheeRecipe) {
						return lycheeRecipe;
					}
				}
				return null;
			});

	public static final LycheeContextKey.Optional<ItemStackHolderCollection> ITEM = opt("item", it -> ItemStackHolderCollection.empty());
	@Nullable
	public final Function<LycheeContext, T> factory;
	public static final LycheeContextKey.Optional<JsonElement> JSON = opt("data");

	public static final LycheeContextKey.Optional<AnvilContext> ANVIL = opt("anvil");
	public static final LycheeContextKey.Optional<ItemShapelessContext> ITEM_SHAPELESS = opt("item_shapeless");
	public static final LycheeContextKey.Optional<CraftingContext> CRAFTING = opt("crafting");
	public static final LycheeContextKey.Optional<BlockState> DRIPSTONE_SOURCE = opt("dripstone_root");

	protected LycheeContextKey(ResourceLocation id, @Nullable Function<LycheeContext, T> factory) {
		this.id = id;
		this.factory = factory;
	}

	@Override
	public String toString() {
		return id.toString();
	}

	private static <T extends LycheeContextKey<?>> T register(T object) {
		Registry.register(LycheeRegistries.CONTEXT, object.id, object);
		return object;
	}

	public static <T> LycheeContextKey.Optional<T> opt(String name) {
		return opt(name, null);
	}

	public static <T> LycheeContextKey.Optional<T> opt(String name, @Nullable Function<LycheeContext, T> factory) {
		return register(new LycheeContextKey.Optional<>(Lychee.id(name), factory));
	}

	public static <T> LycheeContextKey.Required<T> req(String name) {
		return req(name, null);
	}

	public static <T> LycheeContextKey.Required<T> req(String name, @Nullable Function<LycheeContext, T> factory) {
		return register(new LycheeContextKey.Required<>(Lychee.id(name), factory));
	}

	public @Nullable Codec<T> codec() {
		var key = LycheeRegistries.CONTEXT.getKey(this);
		//noinspection unchecked
		return (Codec<T>) LycheeRegistries.CONTEXT_SERIALIZER.get(key);
	}

	public static final class Required<T> extends LycheeContextKey<T> {
		public Required(ResourceLocation id, @Nullable Function<LycheeContext, T> factory) {
			super(id, factory);
		}
	}

	public static final class Optional<T> extends LycheeContextKey<T> {
		public Optional(ResourceLocation id, @Nullable Function<LycheeContext, T> factory) {
			super(id, factory);
		}
	}
}
