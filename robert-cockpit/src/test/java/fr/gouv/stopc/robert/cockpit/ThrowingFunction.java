package fr.gouv.stopc.robert.cockpit;

import java.util.function.Function;

/**
 * 
 * @author plant-stopcovid
 *
 * @param <T> input type of the function
 * @param <R> output type of the function
 * @param <E> type of exception thrown by the function
 * @version 0.0.1-SNAPSHOT
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {

	/**
	 * Entry point of the FunctionInterface
	 * 
	 * @param t the input to transform
	 * @return the transformed input
	 * @throws E the exception thrown when transforming
	 * @since 0.0.1-SNAPSHOT
	 */
	R apply(T t) throws E;

	/**
	 * Rethrow the exception of the apply method in a runtime exception
	 * 
	 * @param <T> input type of the function
	 * @param <R> output type of the function
	 * @param <E> type of exception thrown by the apply method
	 * @param f   the function that throws exception
	 * @return the transformed input
	 */
	static <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> f) {
		return t -> {
			try {
				return f.apply(t);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}
}