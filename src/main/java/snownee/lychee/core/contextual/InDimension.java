//package snownee.lychee.core.contextual;
//
//import com.google.gson.JsonObject;
//
//import net.minecraft.ChatFormatting;
//import net.minecraft.client.Minecraft;
//import net.minecraft.core.Registry;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.network.chat.MutableComponent;
//import net.minecraft.network.chat.TranslatableComponent;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import snownee.lychee.ContextualConditionTypes;
//import snownee.lychee.core.LycheeContext;
//import snownee.lychee.core.LycheeRecipe;
//import snownee.lychee.util.LUtil;
//
//public record InDimension(ResourceKey<Level> dimension) implements ContextualCondition {
//
//	@Override
//	public ContextualConditionType<? extends ContextualCondition> getType() {
//		return ContextualConditionTypes.IN_DIMENSION;
//	}
//
//	@Override
//	public int test(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
//		return ctx.getLevel().dimension() == dimension ? times : 0;
//	}
//
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public InteractionResult testInTooltips() {
//		return Minecraft.getInstance().level.dimension() == dimension ? InteractionResult.SUCCESS : InteractionResult.FAIL;
//	}
//
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public MutableComponent getDescription(boolean inverted) {
//		String key = makeDescriptionId(inverted);
//		MutableComponent name = LUtil.getDimensionDisplayName(dimension);
//		return new TranslatableComponent(key, name.withStyle(ChatFormatting.WHITE));
//	}
//
//	public static class Type extends ContextualConditionType<InDimension> {
//
//		@Override
//		public InDimension fromJson(JsonObject o) {
//			return new InDimension(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(o.get("dimension").getAsString())));
//		}
//
//		@Override
//		public InDimension fromNetwork(FriendlyByteBuf buf) {
//			return new InDimension(ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()));
//		}
//
//		@Override
//		public void toNetwork(InDimension condition, FriendlyByteBuf buf) {
//			buf.writeResourceLocation(condition.dimension.location());
//		}
//
//	}
//
//}
