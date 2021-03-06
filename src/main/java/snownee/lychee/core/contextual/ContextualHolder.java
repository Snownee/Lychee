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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionResult;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

public abstract class ContextualHolder {

	private List<ContextualCondition> conditions = Collections.EMPTY_LIST;
	@Nullable
	private BitSet secretFlags;
	@Nullable
	private List<TranslatableComponent> overrideDesc;
	private static final Component SECRET_TITLE = new TranslatableComponent("contextual.lychee.secret").withStyle(ChatFormatting.GRAY);

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
			overrideDesc.add(new TranslatableComponent(GsonHelper.getAsString(o, "description")));
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
					overrideDesc.add(new TranslatableComponent(key));
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
			for (TranslatableComponent component : overrideDesc) {
				if (component == null) {
					pBuffer.writeUtf("");
				} else {
					pBuffer.writeUtf(component.getKey());
				}
			}
		}
	}

	public int checkConditions(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		for (ContextualCondition condition : conditions) {
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

	@Environment(EnvType.CLIENT)
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
