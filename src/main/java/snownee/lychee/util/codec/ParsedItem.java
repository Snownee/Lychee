package snownee.lychee.util.codec;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.recipe.SizedIngredient;

public record ParsedItem(ExtraCodecs.TagOrElementLocation tagOrId, int count) {
	public static ParsedItem read(StringReader input) throws CommandSyntaxException {
		return read(input, false);
	}

	public static ParsedItem read(StringReader input, boolean single) throws CommandSyntaxException {
		int count = 1;
		int cursor = input.getCursor();
		try {
			count = input.readInt();
			input.expect('x');
			input.expect(' ');
			Preconditions.checkArgument(!single, "Count not allowed here");
			Preconditions.checkArgument(count > 0, "Count must be positive");
		} catch (CommandSyntaxException ignored) {
			input.setCursor(cursor);
		}
		boolean tag = false;
		if (input.peek() == '#') {
			input.skip();
			tag = true;
		}
		Identifier id = Identifier.read(input);
		if (!tag) {
			Preconditions.checkArgument(BuiltInRegistries.ITEM.containsKey(id), "Unknown item: %s", id);
		}
		return new ParsedItem(new ExtraCodecs.TagOrElementLocation(id, tag), count);
	}

	public String toString() {
		return count == 1 ? tagOrId.toString() : "%dx %s".formatted(count, tagOrId);
	}

	private Ingredient rawIngredient() {
		if (tagOrId.tag()) {
			return Ingredient.of(TagKey.create(Registries.ITEM, tagOrId.id()));
		} else {
			return Ingredient.of(BuiltInRegistries.ITEM.get(tagOrId.id()));
		}
	}

	public Ingredient ingredient() {
		Preconditions.checkArgument(count == 1, "Ingredient must not have count");
		return rawIngredient();
	}

	public SizedIngredient sizedIngredient() {
		return new SizedIngredient(rawIngredient(), count);
	}

	public ItemStack itemStack() {
		Preconditions.checkArgument(!tagOrId.tag(), "ItemStack must not be a tag");
		return new ItemStack(BuiltInRegistries.ITEM.getOptional(tagOrId.id()).orElseThrow(), count);
	}
}
