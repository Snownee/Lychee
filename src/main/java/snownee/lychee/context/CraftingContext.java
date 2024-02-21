package snownee.lychee.context;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.mixin.CraftingMenuAccess;
import snownee.lychee.mixin.InventoryMenuAccess;
import snownee.lychee.mixin.recipes.crafting.TransientCraftingContainerAccess;
import snownee.lychee.util.Pair;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;

public record CraftingContext(
		LycheeContext context,
		CraftingContainer container,
		int matchX,
		int matchY,
		boolean mirror
) {
	public static final LoadingCache<Class<?>, Function<CraftingContainer, Pair<Vec3, Player>>>
			CONTAINER_WORLD_LOCATOR =
			CacheBuilder.newBuilder().build(new CacheLoader<>() {
				@Override
				public @NotNull Function<CraftingContainer, Pair<Vec3, Player>> load(final @NotNull Class<?> key) {
					var clazz = key.getSuperclass();
					while (clazz != CraftingContainer.class) {
						var locator = CONTAINER_WORLD_LOCATOR.getIfPresent(clazz);
						if (locator != null) {
							return locator;
						}
						clazz = clazz.getSuperclass();
					}
					return (ignored) -> null;
				}
			});

	public static final LoadingCache<Class<?>, Function<AbstractContainerMenu, Pair<Vec3, Player>>>
			MENU_WORLD_LOCATOR =
			CacheBuilder.newBuilder().build(new CacheLoader<>() {
				@Override
				public @NotNull Function<AbstractContainerMenu, Pair<Vec3, Player>> load(final @NotNull Class<?> key) {
					var clazz = key.getSuperclass();
					while (clazz != AbstractContainerMenu.class) {
						var locator = MENU_WORLD_LOCATOR.getIfPresent(clazz);
						if (locator != null) {
							return locator;
						}
						clazz = clazz.getSuperclass();
					}
					return (ignored) -> null;
				}
			});
	public static final Cache<CraftingContainer, CraftingContext> CONTEXT_CACHE =
			CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS).build();

	static {
		CONTAINER_WORLD_LOCATOR.put(TransientCraftingContainer.class, container -> {
			final var access = (TransientCraftingContainerAccess) container;
			final var menu = access.getMenu();
			try {
				return MENU_WORLD_LOCATOR.get(menu.getClass(), () -> {
					var clazz = menu.getClass().getSuperclass();
					while (clazz != AbstractContainerMenu.class) {
						var locator = MENU_WORLD_LOCATOR.getIfPresent(clazz);
						if (locator != null) {
							return locator;
						}
						clazz = clazz.getSuperclass();
					}
					return menu1 -> null;
				}).apply(menu);
			} catch (ExecutionException e) {
				return null;
			}
		});
		MENU_WORLD_LOCATOR.put(CraftingMenu.class, menu -> {
			final var access = (CraftingMenuAccess) menu;
			return Pair.of(
					access.getAccess()
							.evaluate((level, pos) -> Vec3.atCenterOf(pos), access.getPlayer().position()),
					access.getPlayer()
			);
		});
		MENU_WORLD_LOCATOR.put(InventoryMenu.class, menu -> {
			final var access = (InventoryMenuAccess) menu;
			return Pair.of(access.getOwner().position(), access.getOwner());
		});
	}

	public static CraftingContext make(
			CraftingContainer container,
			LycheeContext context,
			int matchX,
			int matchY,
			boolean mirror
	) {
		Pair<Vec3, Player> pair = null;
		try {
			pair = CONTAINER_WORLD_LOCATOR.get(container.getClass()).apply(container);
		} catch (ExecutionException ignored) {
		}
		final var craftingContext = new CraftingContext(context, container, matchX, matchY, mirror);
		final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		if (pair != null) {
			lootParamsContext.setParam(LootContextParams.ORIGIN, pair.getFirst());
			lootParamsContext.setParam(LootContextParams.THIS_ENTITY, pair.getSecond());
		}
		CONTEXT_CACHE.put(container, craftingContext);
		return craftingContext;
	}
}
