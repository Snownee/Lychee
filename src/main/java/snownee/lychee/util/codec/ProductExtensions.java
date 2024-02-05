package snownee.lychee.util.codec;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.K1;

public final class ProductExtensions {

	/**
	 * There isn't a way to add products to {@link com.mojang.datafixers.Products.P8} like {@link com.mojang.datafixers.Products.P7#and(App)}
	 */
	public static <F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>
	Products.P12<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> and(
			Products.P7<F, T1, T2, T3, T4, T5, T6, T7> product,
			final App<F, T8> t8,
			final App<F, T9> t9,
			final App<F, T10> t10,
			final App<F, T11> t11,
			final App<F, T12> t12
	) {
		return new Products.P12<>(
				product.t1(),
				product.t2(),
				product.t3(),
				product.t4(),
				product.t5(),
				product.t6(),
				product.t7(),
				t8,
				t9,
				t10,
				t11,
				t12
		);
	}
}
