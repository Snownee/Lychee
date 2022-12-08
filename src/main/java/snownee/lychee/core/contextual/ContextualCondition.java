package snownee.lychee.core.contextual;

import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.storage.loot.Deserializers;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.GsonContextImpl;
import snownee.lychee.util.LUtil;

public interface ContextualCondition {
	Gson predicateGson = Deserializers.createConditionSerializer().create();
	GsonContextImpl gsonContext = new GsonContextImpl(predicateGson);

	ContextualConditionType<? extends ContextualCondition> getType();

	int test(ILycheeRecipe<?> recipe, LycheeContext ctx, int times);

	MutableComponent getDescription(boolean inverted);

	default String makeDescriptionId(boolean inverted) {
		String key = LUtil.makeDescriptionId("contextual", getType().getRegistryName());
		if (inverted) {
			key += ".not";
		}
		return key;
	}

	@Environment(EnvType.CLIENT)
	default void appendTooltips(List<Component> tooltips, int indent, boolean inverted) {
		InteractionResult result = InteractionResult.PASS;
		if (Minecraft.getInstance().level != null) {
			result = testInTooltips();
		}
		desc(tooltips, result, indent, getDescription(inverted));
	}

	@Environment(EnvType.CLIENT)
	static void desc(List<Component> tooltips, InteractionResult result, int indent, MutableComponent content) {
		MutableComponent indentComponent = Component.literal("  ".repeat(indent));
		indentComponent.append(I18n.get("result.lychee." + result.toString().toLowerCase(Locale.ENGLISH)));
		indentComponent.append(content.withStyle(ChatFormatting.GRAY));
		tooltips.add(indentComponent);
	}

	@Environment(EnvType.CLIENT)
	default InteractionResult testInTooltips() {
		return InteractionResult.PASS;
	}

	static ContextualCondition parse(JsonObject o) {
		ResourceLocation key = new ResourceLocation(o.get("type").getAsString());
		ContextualConditionType<?> type = LycheeRegistries.CONTEXTUAL.get(key);
		return type.fromJson(o);
	}

	default JsonObject toJson() {
		JsonObject o = new JsonObject();
		o.addProperty("type", getType().getRegistryName().toString());
		((ContextualConditionType<ContextualCondition>) getType()).toJson(this, o);
		return o;
	}

	default int showingCount() {
		return 1;
	}

}
