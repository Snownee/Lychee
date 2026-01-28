package snownee.lychee.mixin;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;

import snownee.lychee.LycheeConfig;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(DataResult.class)
public interface DataResultMixin {
	@WrapOperation(
			method = {
					"error(Ljava/util/function/Supplier;Lcom/mojang/serialization/Lifecycle;)Lcom/mojang/serialization/DataResult;",
					"error(Ljava/util/function/Supplier;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lcom/mojang/serialization/DataResult;"},
			at = @At(
					value = "NEW",
					target = "(Ljava/util/function/Supplier;Ljava/util/Optional;Lcom/mojang/serialization/Lifecycle;)Lcom/mojang/serialization/DataResult$Error;"))
	private static <R> DataResult.Error<R> lychee_appendStacktrace1(
			Supplier<String> messageSupplier,
			Optional<R> partialValue,
			Lifecycle lifecycle,
			Operation<DataResult.Error<R>> original) {
		if (LycheeConfig.debug) {
			List<StackTraceElement> stackTrace = Lists.newArrayList(Thread.currentThread().getStackTrace());
			stackTrace.removeFirst(); // Thread.getStackTrace
			stackTrace.removeFirst(); // DataResult.wrapOperation$xxx
			//noinspection WhileCanBeDoWhile
			while ("error".equals(stackTrace.getFirst().getMethodName())) {
				stackTrace.removeFirst();
			}
			String message = "%s:\n-STACKTRACE-\n at %s\n".formatted(messageSupplier.get(), Joiner.on("\nat ").join(stackTrace));
			messageSupplier = () -> message;
		}
		return original.call(messageSupplier, partialValue, lifecycle);
	}
}
