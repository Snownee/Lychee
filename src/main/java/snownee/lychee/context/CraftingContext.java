package snownee.lychee.context;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.mixin.CraftingMenuAccess;
import snownee.lychee.mixin.recipes.crafting.CrafterMenuAccess;
import snownee.lychee.mixin.recipes.crafting.InventoryMenuAccess;
import snownee.lychee.mixin.recipes.crafting.TransientCraftingContainerAccess;
import snownee.lychee.util.context.LycheeContext;

public record CraftingContext(
		LycheeContext context,
		CraftingInput container,
		boolean mirror
) {
	public static final LoadingCache<Class<?>, Function<CraftingContainer, CraftingContainerLocation>>
			CONTAINER_WORLD_LOCATOR =
			CacheBuilder.newBuilder().build(new CacheLoader<>() {
				@Override
				public Function<CraftingContainer, CraftingContainerLocation> load(final Class<?> key) {
					var clazz = key.getSuperclass();
					while (clazz != null && clazz != CraftingContainer.class) {
						var locator = CONTAINER_WORLD_LOCATOR.getIfPresent(clazz);
						if (locator != null) {
							return locator;
						}
						clazz = clazz.getSuperclass();
					}
					return (ignored) -> null;
				}
			});

	public static final LoadingCache<Class<?>, Function<AbstractContainerMenu, CraftingContainerLocation>>
			MENU_WORLD_LOCATOR =
			CacheBuilder.newBuilder().build(new CacheLoader<>() {
				@Override
				public Function<AbstractContainerMenu, CraftingContainerLocation> load(final Class<?> key) {
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

	static {
		CONTAINER_WORLD_LOCATOR.put(
				TransientCraftingContainer.class, container -> {
					final var access = (TransientCraftingContainerAccess) container;
					final var menu = access.getMenu();
					try {
						return MENU_WORLD_LOCATOR.get(
								menu.getClass(), () -> {
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
		CONTAINER_WORLD_LOCATOR.put(
				BlockEntity.class, container -> {
					final var blockEntity = (BlockEntity) container;
					Level level = blockEntity.getLevel();
					if (level == null) {
						return null;
					}
					return new CraftingContainerLocation(
							level,
							Vec3.atCenterOf(blockEntity.getBlockPos()),
							null
					);
				});
		MENU_WORLD_LOCATOR.put(
				CraftingMenu.class, menu -> {
					final var access = (CraftingMenuAccess) menu;
					return new CraftingContainerLocation(
							access.getPlayer().level(),
							access.getAccess().evaluate((level, pos) -> Vec3.atCenterOf(pos), access.getPlayer().position()),
							access.getPlayer()
					);
				});
		MENU_WORLD_LOCATOR.put(
				InventoryMenu.class, menu -> {
					final var access = (InventoryMenuAccess) menu;
					return CraftingContainerLocation.of(access.getOwner());
				});
		MENU_WORLD_LOCATOR.put(
				CrafterMenu.class, menu -> {
					final var access = (CrafterMenuAccess) menu;
					return CraftingContainerLocation.of(access.getPlayer());
				}
		);
	}
}
