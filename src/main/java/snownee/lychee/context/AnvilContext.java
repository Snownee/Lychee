package snownee.lychee.context;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.item.ItemStack;

public class AnvilContext {
	/**
	 * Both inputs in anvil
	 */
	private final Pair<ItemStack, ItemStack> input;
	/***
	 * The name to set the item if the user specified one.
	 */
	private final String name;
	/**
	 * The base cost, set this to change it if output != null
	 */
	private int levelCost;
	/**
	 * The number of items from the right slot to be consumed during the repair.
	 * Leave as 0 to consume the entire stack.
	 */
	private int materialCost = 1;

	public AnvilContext(final Pair<ItemStack, ItemStack> input, final String name) {
		this.input = input;
		this.name = name;
	}

	public Pair<ItemStack, ItemStack> input() {
		return input;
	}

	public String name() {
		return name;
	}

	public int getLevelCost() {
		return levelCost;
	}

	public void setLevelCost(final int levelCost) {
		this.levelCost = levelCost;
	}

	public int getMaterialCost() {
		return materialCost;
	}

	public void setMaterialCost(final int materialCost) {
		this.materialCost = materialCost;
	}
}
