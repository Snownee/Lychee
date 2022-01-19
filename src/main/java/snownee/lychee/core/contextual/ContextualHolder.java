package snownee.lychee.core.contextual;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.LycheeRecipe;

public abstract class ContextualHolder {

	private List<ContextualCondition> conditions = Collections.EMPTY_LIST;
	private BitSet secretFlags;
	private static final Component HIDDEN_TITLE = new TranslatableComponent("contextual.lychee.hidden").withStyle(ChatFormatting.GRAY);

	public List<ContextualCondition> getConditions() {
		return conditions;
	}

	public void addCondition(ContextualCondition condition, boolean hide) {
		if (conditions == Collections.EMPTY_LIST) {
			conditions = Lists.newArrayList();
		}
		conditions.add(condition);
		if (hide) {
			if (secretFlags == null) {
				secretFlags = new BitSet(conditions.size());
			}
			secretFlags.set(conditions.size() - 1);
		}
	}

	public void conditionsFromNetwork(FriendlyByteBuf pBuffer) {
		int size = pBuffer.readVarInt();
		for (int i = 0; i < size; i++) {
			ContextualConditionType<?> type = pBuffer.readRegistryIdUnsafe(LycheeRegistries.CONTEXTUAL);
			addCondition(type.fromNetwork(pBuffer), false);
		}
		if (pBuffer.readBoolean()) {
			secretFlags = pBuffer.readBitSet();
		}
	}

	@SuppressWarnings("rawtypes")
	public void conditionsToNetwork(FriendlyByteBuf pBuffer) {
		pBuffer.writeVarInt(conditions.size());
		for (ContextualCondition condition : conditions) {
			ContextualConditionType type = condition.getType();
			pBuffer.writeRegistryIdUnsafe(LycheeRegistries.CONTEXTUAL, type);
			type.toNetwork(condition, pBuffer);
		}
		pBuffer.writeBoolean(secretFlags != null);
		if (secretFlags != null) {
			pBuffer.writeBitSet(secretFlags);
			//we should sync condition even though it is hidden because
			//the client-side need to know the result in tooltips for UX
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

	@OnlyIn(Dist.CLIENT)
	public void getConditonTooltips(List<Component> list, int indent) {
		int i = 0;
		for (ContextualCondition condition : getConditions()) {
			boolean hide = isSecretCondition(i);
			if (hide) {
				InteractionResult result = InteractionResult.PASS;
				if (Minecraft.getInstance().level != null) {
					result = condition.testInTooltips();
				}
				ContextualCondition.desc(list, result, indent, HIDDEN_TITLE.copy());
			} else {
				condition.appendTooltips(list, indent, false);
			}
			++i;
		}
	}

}
