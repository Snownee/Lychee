package snownee.lychee.util.codec;

import java.util.Optional;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.action.AddItemCooldown;
import snownee.lychee.action.Delay;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.DropXp;
import snownee.lychee.action.Execute;
import snownee.lychee.action.Move;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.action.SetBlock;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;

public interface ActionParsers {
	class Place implements LycheeParser<PlaceBlock> {
		@Override
		public DataResult<PlaceBlock> parse(StringReader reader) throws CommandSyntaxException {
			DataResult<BlockPredicate> blockResult = LycheeParserUtils.readParam(
					reader,
					r -> LycheeParserUtils.readBlock(reader, false)).orElseThrow();
			if (blockResult.isError()) {
				return DataResult.error(() -> blockResult.error().orElseThrow().message());
			}
			BlockPos offset = BlockPos.ZERO;
			Optional<DataResult<BlockPos>> offsetResult = LycheeParserUtils.readParam(reader, LycheeParserUtils::readPos);
			if (offsetResult.isPresent() && offsetResult.get().isSuccess()) {
				offset = offsetResult.get().getOrThrow();
			}
			return DataResult.success(new PlaceBlock(PostActionCommonProperties.EMPTY, blockResult.getOrThrow(), offset));
		}
	}

	class SetBlockParser implements LycheeParser<SetBlock> {
		@Override
		public DataResult<SetBlock> parse(StringReader reader) throws CommandSyntaxException {
			DataResult<BlockPredicate> blockResult = LycheeParserUtils.readParam(
					reader,
					r -> LycheeParserUtils.readBlock(reader, false)).orElseThrow();
			return blockResult.map(block -> new SetBlock(PostActionCommonProperties.EMPTY, block));
		}
	}

	class MoveParser implements LycheeParser<Move> {
		@Override
		public DataResult<Move> parse(StringReader reader) throws CommandSyntaxException {
			DataResult<Vec3> vecResult = LycheeParserUtils.readParam(reader, LycheeParserUtils::readVec3).orElseThrow();
			return vecResult.map(vec -> new Move(PostActionCommonProperties.EMPTY, vec, ""));
		}
	}

	class Drop implements LycheeParser<PostAction> {
		@Override
		public DataResult<PostAction> parse(StringReader reader) throws CommandSyntaxException {
			DataResult<ItemStack> itemResult = LycheeParserUtils.readParam(
					reader,
					r -> LycheeCodecs.tryCatch(() -> ParsedItem.read(reader).itemStack())).orElseThrow();
			if (itemResult.isSuccess()) {
				return itemResult.map(item -> new DropItem(PostActionCommonProperties.EMPTY, item));
			}
			DataResult<Integer> xpResult = LycheeParserUtils.readParam(
					reader,
					r -> LycheeCodecs.tryCatch(() -> {
						int i = r.readInt();
						r.expect('x');
						r.expect('p');
						Preconditions.checkArgument(i > 0, "XP must be positive");
						return i;
					})).orElseThrow();
			if (xpResult.isSuccess()) {
				return xpResult.map(xp -> new DropXp(PostActionCommonProperties.EMPTY, xp));
			}
			return DataResult.error(() -> "Failed to parse drop action: <Item: %s; XP: %s>".formatted(
					itemResult.error().orElseThrow().message(),
					xpResult.error().orElseThrow().message()));
		}
	}

	class Run implements LycheeParser<Execute> {
		@Override
		public DataResult<Execute> parse(StringReader reader) throws CommandSyntaxException {
			DataResult<String> result = LycheeParserUtils.readParam(
					reader,
					r -> LycheeCodecs.tryCatch(reader::readQuotedString)).orElseThrow();
			return result.map(s -> new Execute(PostActionCommonProperties.EMPTY, s, false));
		}
	}

	class DelayParser implements LycheeParser<Delay> {
		@Override
		public DataResult<Delay> parse(StringReader reader) throws CommandSyntaxException {
			DataResult<Float> result = LycheeParserUtils.readParam(
					reader,
					r -> LycheeCodecs.tryCatch(() -> {
						float f = r.readFloat();
						Preconditions.checkArgument(f > 0, "Delay must be positive");
						return f;
					})).orElseThrow();
			return result.map(f -> new Delay(PostActionCommonProperties.EMPTY, f));
		}
	}

	class ItemCooldown implements LycheeParser<AddItemCooldown> {
		@Override
		public DataResult<AddItemCooldown> parse(StringReader reader) throws CommandSyntaxException {
			DataResult<Float> result = LycheeParserUtils.readParam(
					reader,
					r -> LycheeCodecs.tryCatch(() -> {
						float f = r.readFloat();
						Preconditions.checkArgument(f > 0, "Cooldown must be positive");
						return f;
					})).orElseThrow();
			return result.map(f -> new AddItemCooldown(PostActionCommonProperties.EMPTY, f, Optional.empty()));
		}
	}
}
