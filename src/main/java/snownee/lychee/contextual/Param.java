package snownee.lychee.contextual;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Param(Holder<LycheeContextKey<?>> key, boolean create, String loot) implements ContextualCondition {
	public Param(Holder<LycheeContextKey<?>> key, String loot) {
		this(key, true, loot);
	}

	@Override
	public ContextualConditionType<Param> type() {
		return ContextualConditionType.PARAM;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String s = getDescriptionId(inverted) + ".has";
		String value = key.unwrapKey().orElseThrow().toString();
		if (!loot.isEmpty()) {
			value += "." + loot;
		}
		return Component.translatable(s, CommonProxy.white(value));
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (!ctx.has(key.value(), create)) {
			return 0;
		}
		if (!loot.isEmpty()) {
			boolean found = false;
			var lootParams = ctx.get(LycheeContextKey.LOOT_PARAMS);
			lootParams.initBlockEntityParam();
			for (LootContextParam<?> param : lootParams.params().keySet()) {
				if (loot.equals(param.getName().getPath()) || loot.equals(param.getName().toString())) {
					found = true;
					break;
				}
			}
			if (!found) {
				return 0;
			}
		}
		return times;
	}

	public static class Type implements ContextualConditionType<Param> {
		public static final MapCodec<Param> CODEC = RecordCodecBuilder.<Param>mapCodec(i -> i.group(
				LycheeRegistries.CONTEXT.holderByNameCodec().fieldOf("key").forGetter(Param::key),
				Codec.BOOL.optionalFieldOf("create", true).forGetter(Param::create),
				Codec.STRING.optionalFieldOf("loot", "").forGetter(Param::loot)
		).apply(i, Param::new)).validate(it -> {
			if (!it.loot.isEmpty() && it.key.value() != LycheeContextKey.LOOT_PARAMS) {
				return DataResult.error(() -> "Key must not be empty");
			}
			return DataResult.success(it);
		});
		public static final StreamCodec<RegistryFriendlyByteBuf, Param> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.holderRegistry(LycheeRegistries.CONTEXT.key()),
				Param::key,
				ByteBufCodecs.STRING_UTF8,
				Param::loot,
				Param::new);

		@Override
		public MapCodec<Param> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, Param> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
