package snownee.lychee.core.contextual;

import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.Deserializers;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.GsonContextImpl;

public interface ContextualCondition {
	Gson predicateGson = Deserializers.createConditionSerializer().create();
	GsonContextImpl gsonContext = new GsonContextImpl(predicateGson);

	static void desc(List<Component> tooltips, InteractionResult result, int indent, MutableComponent content) {
		MutableComponent indentComponent = Component.literal("  ".repeat(indent));
		indentComponent.append(I18n.get("result.lychee." + result.toString().toLowerCase(Locale.ENGLISH)));
		indentComponent.append(content.withStyle(ChatFormatting.GRAY));
		tooltips.add(indentComponent);
	}

	static ContextualCondition parse(JsonObject o) {
		ResourceLocation key = new ResourceLocation(o.get("type").getAsString());
		ContextualConditionType<?> type = LycheeRegistries.CONTEXTUAL.get(key);
		return type.fromJson(o);
	}

	ContextualConditionType<? extends ContextualCondition> getType();

	int test(ILycheeRecipe<?> recipe, LycheeContext ctx, int times);

	MutableComponent getDescription(boolean inverted);

	default String makeDescriptionId(boolean inverted) {
		String key = CommonProxy.makeDescriptionId("contextual", getType().getRegistryName());
		if (inverted) {
			key += ".not";
		}
		return key;
	}

	default void appendTooltips(List<Component> tooltips, Level level, @Nullable Player player, int indent, boolean inverted) {
		desc(tooltips, testInTooltips(level, player), indent, getDescription(inverted));
	}

	default InteractionResult testInTooltips(Level level, @Nullable Player player) {
		return InteractionResult.PASS;
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
