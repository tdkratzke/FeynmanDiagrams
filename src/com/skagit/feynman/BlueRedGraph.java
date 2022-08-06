package com.skagit.feynman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;

public class BlueRedGraph {
	final public int[] _blue;
	final public BitSet[] _bitSets;
	final public int[] _cumCounts;

	public BlueRedGraph(final int[] blue) {
		_blue = blue;
		final int blueLength = _blue.length;
		final int nInPath = _blue[0];

		/**
		 * <pre>
		 * k indexes _blue.
		 * id indexes matchable vertices.
		 * k0 indexes components.
		 * k1 indexes matchable vertices within a component.
		 * </pre>
		 */

		/** Compute nBlueComponents. */
		final int nBlueComponents;
		{
			int nBlueComponentsX = 0;
			for (int k = 0; k < blueLength; ++k) {
				if (k == 0) {
					++nBlueComponentsX;
				} else {
					final int nWithK = _blue[k];
					nBlueComponentsX += nWithK;
				}
			}
			nBlueComponents = nBlueComponentsX;
		}

		/** Fill in _bitSets and _cumCounts. */
		_bitSets = new BitSet[nBlueComponents];
		_cumCounts = new int[nBlueComponents];
		for (int k = 0, cum = 0, k0 = 0; k < blueLength; ++k) {
			if (k == 0) {
				final int nMatchable = nInPath - 1;
				_bitSets[0] = new BitSet(nMatchable);
				_cumCounts[0] = cum + nMatchable;
				k0 = 1;
				continue;
			}
			final int nMatchable = k;
			final int nWithK = _blue[k];
			for (int kX = 0; kX < nWithK; ++kX) {
				_bitSets[k0] = new BitSet(k);
				_cumCounts[k0] = _cumCounts[k0 - 1] + nMatchable;
				++k0;
			}
		}
	}

	/**
	 * Gets the number of ways of completing the red edges. level is a debugging
	 * parameter that indicates how deep into the recursion we are.
	 */
	private int recursiveGetCount(final int level) {
		if (isComplete()) {
			return 1;
		}
		final int id = getSmallestUnMatchedId();
		final int[] pair = idToPair(_cumCounts, id);
		final int k0 = pair[0], k1 = pair[1];
		final BitSet bitSet0 = _bitSets[k0];
		final int size0 = getBitSetSize(k0);
		final int cardinality0 = bitSet0.cardinality();

		int count = 0;

		/**
		 * <pre>
		 * Start by matching id with some element in its own component,recursively get the count
		 * and multiply it by the number of unMatched elements in that component
		 * (besides id).
		 * </pre>
		 */
		final int multiplier0 = size0 - cardinality0 - 1;
		if (multiplier0 > 0) {
			final int kX1 = bitSet0.nextClearBit(k1 + 1);
			final int idX = pairToId(_cumCounts, k0, kX1);
			if (isLegalPair(id, idX)) {
				effectMatch(id, idX, /* setUnMatched= */false);
				final int thisCount = recursiveGetCount(level + 1);
				effectMatch(id, idX, /* setUnMatched= */true);
				count += multiplier0 * thisCount;
			}
		}

		/** Match id with ids that are not in its component. */
		final int nBlueComponents = _bitSets.length;
		final HashSet<Integer> usedSizes = new HashSet<>();
		for (int kA0 = k0 + 1; kA0 < nBlueComponents; ++kA0) {
			final BitSet bitSetA = _bitSets[kA0];
			final int sizeA = getBitSetSize(kA0);
			final int cardinalityA = bitSetA.cardinality();
			if (cardinalityA == 0) {
				/** An untouched cycle. */
				if (usedSizes.add(sizeA)) {
					final int idA = pairToId(_cumCounts, kA0, 0);
					effectMatch(id, idA, /* setUnMatched= */false);
					final int thisCount = recursiveGetCount(level + 1);
					effectMatch(id, idA, /* setUnMatched= */true);
					count += thisCount;
				}
				continue;
			}
			/** A cycle that has been broken. */
			final int multiplierA = sizeA - cardinalityA;
			if (multiplierA == 0) {
				/** No room in this cycle. */
				continue;
			}
			final int kA1 = bitSetA.nextClearBit(0);
			final int idA = pairToId(_cumCounts, kA0, kA1);
			if (isLegalPair(id, idA)) {
				effectMatch(id, idA, /* setUnMatched= */false);
				final int thisCount = recursiveGetCount(level + 1);
				effectMatch(id, idA, /* setUnMatched= */true);
				count += multiplierA * thisCount;
			}
		}
		return count;
	}

	/**
	 * If adding (idA,idB) leaves the graph connectable, then (idA,idB) is legal.
	 */
	private boolean isLegalPair(final int idA, final int idB) {
		final int[] pairA = idToPair(_cumCounts, idA);
		final int kA0 = pairA[0];
		final int kA1 = pairA[1];
		final BitSet bitSetA = _bitSets[kA0];
		final boolean aWasSet = bitSetA.get(kA1);
		bitSetA.set(kA1);
		final int[] pairB = idToPair(_cumCounts, idB);
		final int kB0 = pairB[0];
		final int kB1 = pairB[1];
		final BitSet bitSetB = _bitSets[kB0];
		final boolean bWasSet = bitSetB.get(kB1);
		bitSetB.set(kB1);
		final boolean returnValue = isConnectable();
		if (!aWasSet) {
			bitSetA.clear(kA1);
		}
		if (!bWasSet) {
			bitSetB.clear(kB1);
		}
		return returnValue;
	}

	private boolean isComplete() {
		final int nBlueComponents = _bitSets.length;
		for (int k0 = 0; k0 < nBlueComponents; ++k0) {
			final BitSet bitSet = _bitSets[k0];
			final int cardinality = bitSet.cardinality();
			final int size = getBitSetSize(k0);
			if (cardinality < size) {
				return false;
			}
		}
		return true;
	}

	/**
	 * If the graph is completed, then we say that it is connectable. Otherwise, we
	 * need some component that has at least one matched matchable vertex but not
	 * all of them.
	 */
	private boolean isConnectable() {
		final int nBlueComponents = _bitSets.length;
		for (int k0 = 0; k0 < nBlueComponents; ++k0) {
			final BitSet bitSet = _bitSets[k0];
			final int cardinality = bitSet.cardinality();
			final int size = getBitSetSize(k0);
			if (0 < cardinality && cardinality < size) {
				return true;
			}
		}
		return isComplete();
	}

	/** idToPair and pairToId are static to make debugging simpler. */
	private static int[] idToPair(final int[] cumCounts, final int id) {
		/**
		 * <pre>
		 * If there are 2 blue components with sizes 3 and 4, then
		 * _cumCounts will be {3,7}.
		 * id = 0 -> {0,0}
		 * id = 3 ->{1,0}
		 * etc.
		 * </pre>
		 */
		final int binSearch = Arrays.binarySearch(cumCounts, id);
		if (binSearch >= 0) {
			return new int[] {
					binSearch + 1, 0
			};
		}
		final int k1 = -binSearch - 1;
		return new int[] {
				k1, id - (k1 == 0 ? 0 : cumCounts[k1 - 1])
		};
	}

	private static int pairToId(final int[] cumCounts, final int k0, final int k1) {
		return (k0 == 0 ? 0 : cumCounts[k0 - 1]) + k1;
	}

	private void effectMatch(final int idA, final int idB, final boolean setUnMatched) {
		final int[] pairA = idToPair(_cumCounts, idA);
		final int kA0 = pairA[0], kA1 = pairA[1];
		final BitSet bitSetA = _bitSets[kA0];
		final int[] pairB = idToPair(_cumCounts, idB);
		final int kB0 = pairB[0], kB1 = pairB[1];
		final BitSet bitSetB = _bitSets[kB0];
		bitSetA.set(kA1, !setUnMatched);
		bitSetB.set(kB1, !setUnMatched);
	}

	private int getSmallestUnMatchedId() {
		final int nBlueComponents = _bitSets.length;
		for (int k0 = 0; k0 < nBlueComponents; ++k0) {
			final BitSet bitSet = _bitSets[k0];
			final int k1 = bitSet.nextClearBit(0);
			final int bitSetSize = getBitSetSize(k0);
			if (k1 < bitSetSize) {
				return pairToId(_cumCounts, k0, k1);
			}
		}
		return -1;
	}

	private int getBitSetSize(final int k0) {
		return _cumCounts[k0] - (k0 == 0 ? 0 : _cumCounts[k0 - 1]);
	}

	/** Utility routine for creating a succinct string from a blue vector. */
	static String blueToString(final int[] blue) {
		String s = String.format("[%d", blue[0]);
		final int n = blue.length;
		for (int k = 2; k < n; ++k) {
			boolean gotOne = false;
			for (int kk = k; kk < n; ++kk) {
				if (blue[kk] != 0) {
					gotOne = true;
					break;
				}
			}
			if (!gotOne) {
				break;
			}
			s += String.format("%s%d", k == 2 ? " " : ",", blue[k]);
		}
		s += "]";
		return s;
	}

	/**
	 * Utility routine for creating a blue from the string that is produced by
	 * blueToString.
	 */
	static int[] stringToBlue(final String blueString) {
		final String[] fields = blueString.trim().split("[\\s,\\[\\]]+");
		final int nFields, intArray[];
		{
			final int nFieldsX = fields.length;
			final ArrayList<Integer> intsList = new ArrayList<>();
			for (int k = 0; k < nFieldsX; ++k) {
				try {
					intsList.add(Integer.parseInt(fields[k]));
				} catch (final NumberFormatException e) {
				}
			}
			nFields = intsList.size();
			if (nFields < 1) {
				return null;
			}
			intArray = new int[nFields];
			for (int k = 0; k < nFields; ++k) {
				intArray[k] = intsList.get(k);
			}
		}
		int nBlueEdges = intArray[0];
		for (int k = 1; k < nFields; ++k) {
			nBlueEdges += (k + 1) * intArray[k];
		}
		final int[] blue = new int[nBlueEdges - 1];
		Arrays.fill(blue, 0);
		blue[0] = intArray[0];
		System.arraycopy(intArray, 1, blue, 2, nFields - 1);
		return blue;
	}

	public static int getCount(final int nBlueEdges) {
		final int[] blue = new int[nBlueEdges - 1];

		Arrays.fill(blue, 0);
		for (int globalCount = 0;;) {
			BlueIterator.nextBlue(blue);
			if (blue[0] == 0) {
				return globalCount;
			}
			final BlueRedGraph blueRedGraph = new BlueRedGraph(blue);
			final int thisCount = blueRedGraph.recursiveGetCount(/* level= */0);
			globalCount += thisCount;
			System.out.printf("\n%s, count[%d], globalCount[%d]", //
					blueToString(blue), thisCount, globalCount);
		}

	}

	/* Testing nBlue = 9. */
	public final static void main1(final String[] args) {
		getCount(9);
	}

	/* Testing individual blue configurations. */
	public final static void main(final String[] args) {
		final int[] blue = stringToBlue("[6 0,1]");
		final BlueRedGraph blueRedGraph = new BlueRedGraph(blue);
		final int count = blueRedGraph.recursiveGetCount(/* level= */0);
		System.out.printf("\ncount[%d] for %s", count, blueToString(blue));
	}

	/** Testing idToPair. */
	public final static void main0(final String[] args) {
		final int[] cumCounts = new int[] {
				3, 7, 8
		};
		for (int id = 0; id < 10; ++id) {
			final int[] x = idToPair(cumCounts, id);
			System.out.printf("\n%d->[%d,%d]", id, x[0], x[1]);
		}
	}
}
