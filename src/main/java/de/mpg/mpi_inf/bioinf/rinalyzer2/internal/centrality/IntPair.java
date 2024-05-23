package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

public class IntPair {

	public IntPair(int aFirst, int aSecond) {
		first = aFirst;
		second = aSecond;
	}

	public int first() {
		return first;
	}

	public int second() {
		return second;
	}

	/**
	 * Checks if two {@link IntPair}s are equal. They are equal if their values are equal.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof IntPair)) {
			return false;
		}
		IntPair pair = (IntPair) o;
		return (first == pair.first() && second == pair.second());
	}

	private int first;

	private int second;
}
