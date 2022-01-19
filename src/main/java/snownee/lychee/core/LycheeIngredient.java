package snownee.lychee.core;

public interface LycheeIngredient<T> {

	boolean lychee_test(T t);

	IngredientType lychee_getType();

}
