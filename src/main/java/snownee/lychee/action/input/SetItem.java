package snownee.lychee.action.input;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.Displays;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record SetItem(
		PostActionCommonProperties commonProperties,
		@Nullable ItemStackTemplate itemStack,
		Reference target) implements PostAction {

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public SetItem(PostActionCommonProperties commonProperties, Optional<ItemStackTemplate> itemStack, Reference target) {
		this(commonProperties, itemStack.orElse(null), target);
	}

	@Override
	public PostActionType<SetItem> type() {
		return PostActionTypes.SET_ITEM;
	}

	@Override
	public void apply(LycheeContext context, ActionContext actionContext, int times) {
		var indexes = context.get(LycheeContextKey.RECIPE).getItemIndexes(target);
		for (var index : indexes) {
			context.setItem(index, itemStack != null ? itemStack.create() : ItemStack.EMPTY);
			context.get(LycheeContextKey.ITEM).get(index).setConsumption(0);
		}
	}

	@Override
	public Component getDisplayName() {
		if (itemStack == null) {
			return ItemStack.EMPTY.getHoverName(); //TODO
		}
		return itemStack.create().getHoverName();
	}

	@Override
	public List<SlotDisplay> getOutputItems() {
		return List.of(Displays.slot(itemStack));
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe) {
		Preconditions.checkArgument(!recipe.getItemIndexes(target).isEmpty(), "No target found for %s", target);
	}

	@Override
	public SlotDisplay transformRemainder(SlotDisplay display, @Nullable ILycheeRecipe<?> recipe) {
		return Displays.slot(itemStack());
	}

	@Override
	public boolean hidden() {
		return itemStack == null || PostAction.super.hidden();
	}

	//	@Override
	//	public JsonElement provideJsonInfo(ILycheeRecipe<?> recipe, JsonPointer pointer, JsonObject recipeObject) {
	//		setPath(pointer.toString());
	//		return CommonProxy.tagToJson(stack.save(new CompoundTag()));
	//	}

	public static class Type implements PostActionType<SetItem> {
		public static final MapCodec<SetItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(SetItem::commonProperties),
				LycheeCodecs.OPTIONAL_ITEM_STACK_TEMPLATE_MAP_CODEC.forGetter($ -> Optional.ofNullable($.itemStack)),
				Reference.CODEC.optionalFieldOf("target", Reference.DEFAULT).forGetter(SetItem::target)
		).apply(instance, SetItem::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, SetItem> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				SetItem::commonProperties,
				ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs::optional),
				$ -> Optional.ofNullable($.itemStack),
				Reference.STREAM_CODEC,
				SetItem::target,
				SetItem::new);

		@Override
		public MapCodec<SetItem> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SetItem> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
