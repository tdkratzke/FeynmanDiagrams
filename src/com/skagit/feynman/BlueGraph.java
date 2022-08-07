package com.skagit.feynman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;

public class BlueGraph {
	final public int[] _spec;
	final public BitSet[] _bitSets;
	final public int[] _cumSizes;

	public BlueGraph(final int[] spec) {
		_spec = spec;
		final int specLength = _spec.length;
		final int nInPath = _spec[0];

		/**
		 * <pre>
		 * k indexes _spec.
		 * id indexes matchable vertices.
		 * k0 indexes components.
		 * k1 indexes matchable vertices within a component.
		 * </pre>
		 */

		/** Compute nBlueComponents. */
		final int nBlueComponents;
		{
			int nBlueComponentsX = 1;
			for (int k = 2; k < specLength; ++k) {
				final int nSimilar = _spec[k];
				nBlueComponentsX += nSimilar;
			}
			nBlueComponents = nBlueComponentsX;
		}

		/** Fill in _bitSets and _cumSizes. */
		_bitSets = new BitSet[nBlueComponents];
		_cumSizes = new int[nBlueComponents];
		for (int k = 0, k0 = 0; k < specLength; ++k) {
			final int nMatchable = k == 0 ? (nInPath - 1) : k;
			final int nSimilar = k == 0 ? 1 : _spec[k];
			for (int kSimilar = 0; kSimilar < nSimilar; ++kSimilar, ++k0) {
				_bitSets[k0] = new BitSet(nMatchable);
				_cumSizes[k0] = (k0 == 0 ? 0 : _cumSizes[k0 - 1]) + nMatchable;
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
		final int[] pair = idToPair(_cumSizes, id);
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
			final int k1X = bitSet0.nextClearBit(k1 + 1);
			final int idX = pairToId(_cumSizes, k0, k1X);
			if (isLegalPair(id, idX)) {
				match(id, idX);
				final int thisCount = recursiveGetCount(level + 1);
				unMatch(id, idX);
				count += multiplier0 * thisCount;
			}
		}

		/** Match id with ids that are not in its component. */
		final int nBlueComponents = _bitSets.length;
		final HashSet<Integer> usedSizes = new HashSet<>();
		for (int k0A = k0 + 1; k0A < nBlueComponents; ++k0A) {
			final BitSet bitSetA = _bitSets[k0A];
			final int sizeA = getBitSetSize(k0A);
			final int cardinalityA = bitSetA.cardinality();
			if (cardinalityA == 0) {
				/**
				 * k0A is an unbroken cycle. Skip it if we've visited an unbroken cycle with the
				 * same size. Otherwise, match id to the first vertex.
				 */
				if (usedSizes.add(sizeA)) {
					final int idA = pairToId(_cumSizes, k0A, 0);
					match(id, idA);
					final int thisCount = recursiveGetCount(level + 1);
					unMatch(id, idA);
					count += thisCount;
				}
				continue;
			}
			if (cardinalityA == sizeA) {
				/** The component is full. */
				continue;
			}
			/** We're on a broken cycle. */
			final int multiplierA = sizeA - cardinalityA;
			final int kA1 = bitSetA.nextClearBit(0);
			final int idA = pairToId(_cumSizes, k0A, kA1);
			if (isLegalPair(id, idA)) {
				match(id, idA);
				final int thisCount = recursiveGetCount(level + 1);
				unMatch(id, idA);
				count += multiplierA * thisCount;
			}
		}
		return count;
	}

	/**
	 * If adding (idA,idB) leaves the graph connectable, then (idA,idB) is legal.
	 */
	private boolean isLegalPair(final int idA, final int idB) {
		final int[] pairA = idToPair(_cumSizes, idA);
		final int kA0 = pairA[0];
		final int kA1 = pairA[1];
		final BitSet bitSetA = _bitSets[kA0];
		final boolean aWasSet = bitSetA.get(kA1);
		bitSetA.set(kA1);
		final int[] pairB = idToPair(_cumSizes, idB);
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
	 * need some broken component that is not fully matched.
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
		 * _cumSizes will be {3,7}.
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

	private void match(final int idA, final int idB) {
		matchOrUnMatch(idA, idB, /* match= */true);
	}

	private void unMatch(final int idA, final int idB) {
		matchOrUnMatch(idA, idB, /* match= */false);
	}

	/** Factored out the common part of match and unMatch. */
	private void matchOrUnMatch(final int idA, final int idB, final boolean match) {
		final int[] pairA = idToPair(_cumSizes, idA);
		final int k0A = pairA[0], k1A = pairA[1];
		final BitSet bitSetA = _bitSets[k0A];
		final int[] pairB = idToPair(_cumSizes, idB);
		final int k0B = pairB[0], k1B = pairB[1];
		final BitSet bitSetB = _bitSets[k0B];
		bitSetA.set(k1A, match);
		bitSetB.set(k1B, match);
	}

	private int getSmallestUnMatchedId() {
		final int nBlueComponents = _bitSets.length;
		for (int k0 = 0; k0 < nBlueComponents; ++k0) {
			final BitSet bitSet = _bitSets[k0];
			final int k1 = bitSet.nextClearBit(0);
			final int bitSetSize = getBitSetSize(k0);
			if (k1 < bitSetSize) {
				return pairToId(_cumSizes, k0, k1);
			}
		}
		return -1;
	}

	private int getBitSetSize(final int k0) {
		return _cumSizes[k0] - (k0 == 0 ? 0 : _cumSizes[k0 - 1]);
	}

	/** Utility routine for creating a succinct string from a spec. */
	static String specToString(final int[] spec) {
		String s = String.format("[%d", spec[0]);
		final int n = spec.length;
		for (int k = 2; k < n; ++k) {
			boolean gotOne = false;
			for (int kk = k; kk < n; ++kk) {
				if (spec[kk] != 0) {
					gotOne = true;
					break;
				}
			}
			if (!gotOne) {
				break;
			}
			s += String.format("%s%d", k == 2 ? " " : ",", spec[k]);
		}
		s += "]";
		return s;
	}

	/**
	 * Utility routine for creating a spec from the string that is produced by
	 * specToString.
	 */
	static int[] stringToSpec(final String specString) {
		final String[] fields = specString.trim().split("[\\s,\\[\\]]+");
		final int nFields, fieldsArray[];
		{
			final int nFieldsX = fields.length;
			final ArrayList<Integer> fieldsList = new ArrayList<>();
			for (int k = 0; k < nFieldsX; ++k) {
				try {
					fieldsList.add(Integer.parseInt(fields[k]));
				} catch (final NumberFormatException e) {
				}
			}
			nFields = fieldsList.size();
			if (nFields < 1) {
				return null;
			}
			fieldsArray = new int[nFields];
			for (int k = 0; k < nFields; ++k) {
				fieldsArray[k] = fieldsList.get(k);
			}
		}
		int nBlueEdges = fieldsArray[0];
		for (int k = 1; k < nFields; ++k) {
			nBlueEdges += (k + 1) * fieldsArray[k];
		}
		final int[] spec = new int[nBlueEdges - 1];
		Arrays.fill(spec, 0);
		spec[0] = fieldsArray[0];
		System.arraycopy(fieldsArray, 1, spec, 2, nFields - 1);
		return spec;
	}

	public static int feynman_F(final int n) {
		final int nBlueEdges = n + 1;
		final int[] blue = new int[nBlueEdges - 1];
		Arrays.fill(blue, 0);
		System.out.printf("n[%d] (nBlueEdges[%d])", n, nBlueEdges);
		for (int globalCount = 0;;) {
			SpecIterator.nextSpec(blue);
			if (blue[0] == 0) {
				return globalCount;
			}
			final BlueGraph blueGraph = new BlueGraph(blue);
			final int thisCount = blueGraph.recursiveGetCount(/* level= */0);
			globalCount += thisCount;
			System.out.printf("\n%s, count[%d], globalCount[%d]", //
					specToString(blue), thisCount, globalCount);
		}
	}

	/* Testing individual blue configurations or testing nBlue = 9. */
	public final static void main(final String[] args) {
		final int[] individualSpecString = null; // stringToSpec("[4 0,0,0,1]");
		if (individualSpecString != null) {
			final BlueGraph blueGraph = new BlueGraph(individualSpecString);
			final int count = blueGraph.recursiveGetCount(/* level= */0);
			System.out.printf("\ncount[%d] for %s", count, specToString(individualSpecString));
		} else {
			feynman_F(8);
		}
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