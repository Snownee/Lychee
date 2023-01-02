package snownee.lychee.core.contextual;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.random_block_ticking.RandomBlockTickingRecipe;
import snownee.lychee.util.LUtil;

public class ContextualHolder {

	private List<ContextualCondition> conditions = Collections.EMPTY_LIST;
	@Nullable
	private BitSet secretFlags;
	@Nullable
	private List<Component> overrideDesc;
	private static final Component SECRET_TITLE = Component.translatable("contextual.lychee.secret").withStyle(ChatFormatting.GRAY);

	public List<ContextualCondition> getConditions() {
		return conditions;
	}

	public int showingConditionsCount() {
		return conditions.stream().mapToInt(ContextualCondition::showingCount).sum();
	}

	public void withCondition(ContextualCondition condition) {
		Objects.requireNonNull(condition);
		if (conditions == Collections.EMPTY_LIST) {
			conditions = Lists.newArrayList();
		}
		conditions.add(condition);
	}

	public void parseConditions(JsonElement element) {
		if (element == null) {
		} else if (element.isJsonObject()) {
			parse(element.getAsJsonObject());
		} else {
			JsonArray array = element.getAsJsonArray();
			for (int x = 0; x < array.size(); x++) {
				parse(array.get(x).getAsJsonObject());
			}
		}
	}

	private void parse(JsonObject o) {
		withCondition(ContextualCondition.parse(o));
		if (GsonHelper.getAsBoolean(o, "secret", false)) {
			if (secretFlags == null) {
				secretFlags = new BitSet(conditions.size());
			}
			secretFlags.set(conditions.size() - 1);
		}
		if (o.has("description")) {
			if (overrideDesc == null) {
				overrideDesc = Lists.newArrayList();
			}
			while (overrideDesc.size() + 1 < conditions.size()) {
				overrideDesc.add(null);
			}
			overrideDesc.add(Component.translatable(GsonHelper.getAsString(o, "description")));
		}
	}

	public void conditionsFromNetwork(FriendlyByteBuf pBuffer) {
		int size = pBuffer.readVarInt();
		for (int i = 0; i < size; i++) {
			ContextualConditionType<?> type = LUtil.readRegistryId(LycheeRegistries.CONTEXTUAL, pBuffer);
			withCondition(type.fromNetwork(pBuffer));
		}
		if (pBuffer.readBoolean()) {
			secretFlags = pBuffer.readBitSet();
		}
		if (pBuffer.readBoolean()) {
			overrideDesc = Lists.newArrayListWithCapacity(size);
			for (int i = 0; i < size; i++) {
				String key = pBuffer.readUtf();
				if (key.isEmpty()) {
					overrideDesc.add(null);
				} else {
					overrideDesc.add(Component.translatable(key));
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void conditionsToNetwork(FriendlyByteBuf pBuffer) {
		pBuffer.writeVarInt(conditions.size());
		for (ContextualCondition condition : conditions) {
			ContextualConditionType type = condition.getType();
			LUtil.writeRegistryId(LycheeRegistries.CONTEXTUAL, type, pBuffer);
			type.toNetwork(condition, pBuffer);
		}
		pBuffer.writeBoolean(secretFlags != null);
		if (secretFlags != null) {
			pBuffer.writeBitSet(secretFlags);
			//we should sync condition even though it is secret because
			//the client-side need to know the result in tooltips for UX
		}
		pBuffer.writeBoolean(overrideDesc != null);
		if (overrideDesc != null) {
			for (Component component : overrideDesc) {
				if (component == null || !(component.getContents() instanceof TranslatableContents translatable)) {
					pBuffer.writeUtf("");
				} else {
					pBuffer.writeUtf(translatable.getKey());
				}
			}
		}
	}

	public JsonElement rawConditionsToJson() {
		if (conditions.size() == 1) {
			return conditions.get(0).toJson();
		}
		JsonArray array = new JsonArray();
		conditions.forEach($ -> array.add($.toJson()));
		return array;
	}

	public int checkConditions(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		boolean first = true;
		for (ContextualCondition condition : conditions) {
			if (first && condition.getType() == ContextualConditionTypes.CHANCE && getClass() == RandomBlockTickingRecipe.class) {
				continue;
			}
			first = false;
			times = condition.test(recipe, ctx, times);
			if (times == 0) {
				break;
			}
		}
		return times;
	}

	public boolean isSecretCondition(int index) {
		if (secretFlags == null) {
			return false;
		} else {
			return secretFlags.get(index);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void getConditonTooltips(List<Component> list, int indent) {
		int i = 0;
		for (ContextualCondition condition : getConditions()) {
			if (isSecretCondition(i)) {
				InteractionResult result = InteractionResult.PASS;
				if (Minecraft.getInstance().level != null) {
					result = condition.testInTooltips();
				}
				ContextualCondition.desc(list, result, indent, SECRET_TITLE.copy());
			} else if (isOverridenDesc(i)) {
				InteractionResult result = InteractionResult.PASS;
				if (Minecraft.getInstance().level != null) {
					result = condition.testInTooltips();
				}
				ContextualCondition.desc(list, result, indent, overrideDesc.get(i).copy());
			} else {
				condition.appendTooltips(list, indent, false);
			}
			++i;
		}
	}

	private boolean isOverridenDesc(int i) {
		if (overrideDesc != null && overrideDesc.size() > i) {
			return overrideDesc.get(i) != null;
		}
		return false;
	}

}
