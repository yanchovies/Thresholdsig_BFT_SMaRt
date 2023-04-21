package bftsmart.benchmark;

/**
 * @author robin
 */
public enum Operation {
	PUT,
	GET;

	public static Operation[] values = values();

	public static Operation getOperation(int ordinal) {
		return values[ordinal];
	}
}
