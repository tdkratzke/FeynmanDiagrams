package com.skagit.feynman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;

public class BlueGraph {
	final private int[] _spec;
	final private BitSet[] _bitSets;
	final private int[] _cumSizes;
	final private boolean[] _connectedToPath;
	final private int _nRedEdgesToPlace;

	private BlueGraph(final int[] spec) {
		_spec = spec;
		final int specLength = _spec.length;
		final int nInPath = _spec[0];

		/**
		 * <pre>
		 * k indexes _spec.
		 * id indexes matchable nodes.
		 * k0 indexes components.
		 * k1 indexes matchable nodes within a component.
		 * </pre>
		 */

		/** Compute nBlueComponents. */
		final int nBlueComponents;
		{
			int nBlueComponentsX = 1;
			int nBlueNodesOfInterest = _spec[0] - 1;
			for (int k = 2; k < specLength; ++k) {
				final int nSimilar = _spec[k];
				nBlueNodesOfInterest += nSimilar * k;
				nBlueComponentsX += nSimilar;
			}
			nBlueComponents = nBlueComponentsX;
			_nRedEdgesToPlace = nBlueNodesOfInterest / 2;
		}

		/** Fill in _bitSets, _cumSizes, and _connectedToPath. */
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
		_connectedToPath = new boolean[nBlueComponents];
	}

	/**
	 * Does the heavy lifting. Gets the number of ways of completing the red edges.
	 * The recursion level and the number of red edges placed is the same.
	 */
	private int getCount(final int nRedEdgesPlaced) {
		if (nRedEdgesPlaced == _nRedEdgesToPlace) {
			/** We're done. The count is this placement and only this one. */
			return 1;
		}
		final int[] pair = getSmallestUnMatchedPair();
		final int k0 = pair[0], k1 = pair[1];
		final BitSet bitSet = _bitSets[k0];
		final int size = getBitSetSize(k0);
		final int cardinality = bitSet.cardinality();

		int count = 0;

		/**
		 * <pre>
		 * Start by matching id with some node in its own component,recursively get the count,
		 * and multiply it by the number of unMatched elements in that component (besides id).
		 * </pre>
		 */
		if (cardinality < size) {
			final int k1X = bitSet.nextClearBit(k1 + 1);
			final int[] pairX = new int[] {
					k0, k1X
			};
			if (wouldLeaveGraphConnectable(pair, pairX)) {
				final int multiplier = size - cardinality - 1;
				match(pair, pairX);
				final int thisCount = getCount(nRedEdgesPlaced + 1);
				unMatch(pair, pairX);
				count += multiplier * thisCount;
			}
		}

		/** Match id with ids that are not in its component. */
		final int nBlueComponents = _bitSets.length;
		final HashSet<Integer> usedSizes = new HashSet<>();
		for (int k0X = k0 + 1; k0X < nBlueComponents; ++k0X) {
			final BitSet bitSetX = _bitSets[k0X];
			final int cardinalityX = bitSetX.cardinality();
			final int sizeX = getBitSetSize(k0X);
			/**
			 * If k0X is an unopened cycle, skip it if we've visited an unopened cycle with
			 * the same size. Otherwise, match id to the first node and the multiplier is 1.
			 */
			if (cardinalityX == 0) {
				if (usedSizes.add(sizeX)) {
					final int[] pairX = new int[] {
							k0X, 0
					};
					match(pair, pairX);
					final int thisCount = getCount(nRedEdgesPlaced + 1);
					unMatch(pair, pairX);
					count += thisCount;
				}
				continue;
			}
			/** We're on an open cycle. Skip it if it's full. */
			if (cardinalityX == sizeX) {
				continue;
			}
			final int k1X = bitSetX.nextClearBit(0);
			final int[] pairX = new int[] {
					k0X, k1X
			};
			if (wouldLeaveGraphConnectable(pair, pairX)) {
				final int multiplier = sizeX - cardinalityX;
				match(pair, pairX);
				final int thisCount = getCount(nRedEdgesPlaced + 1);
				unMatch(pair, pairX);
				count += multiplier * thisCount;
			}
		}
		return count;
	}

	private boolean wouldLeaveGraphConnectable(final int[] pair, final int[] pairX) {
		final int k0 = pair[0], k1 = pair[1];
		final BitSet bitSet = _bitSets[k0];
		final boolean wasSet = bitSet.get(k1);
		bitSet.set(k1);
		final int k0X = pairX[0], k1X = pairX[1];
		final BitSet bitSetX = _bitSets[k0X];
		final boolean wasSetX = bitSetX.get(k1X);
		bitSetX.set(k1X);
		final boolean returnValue = isConnectable();
		if (!wasSet) {
			bitSet.clear(k1);
		}
		if (!wasSetX) {
			bitSetX.clear(k1X);
		}
		return returnValue;
	}

	/**
	 * If the graph is complete, then we say that it is connectable. Otherwise, we
	 * need some open component that is not fully matched.
	 */
	private boolean isConnectable() {
		final int nBlueComponents = _bitSets.length;
		boolean isComplete = true;
		for (int k0 = 0; k0 < nBlueComponents; ++k0) {
			final BitSet bitSet = _bitSets[k0];
			final int cardinality = bitSet.cardinality();
			final int size = getBitSetSize(k0);
			if (cardinality < size) {
				isComplete = false;
				if (0 < cardinality) {
					/** This component is started, but not complete. The graph is connectable. */
					return true;
				}
			}
		}
		/**
		 * The only way for this graph to be connectable is if it's already complete.
		 */
		return isComplete;
	}

	private void match(final int[] pair, final int[] pairX) {
		matchOrUnMatch(pair, pairX, /* match= */true);
	}

	private void unMatch(final int[] pair, final int[] pairX) {
		matchOrUnMatch(pair, pairX, /* match= */false);
	}

	/** Factored out the common part of match and unMatch. */
	private void matchOrUnMatch(final int[] pair, final int[] pairX, final boolean match) {
		final int k0 = pair[0], k1 = pair[1];
		final BitSet bitSet = _bitSets[k0];
		final int k0X = pairX[0], k1X = pairX[1];
		final BitSet bitSetX = _bitSets[k0X];
		bitSet.set(k1, match);
		bitSetX.set(k1X, match);
	}

	private int[] getSmallestUnMatchedPair() {
		final int nBlueComponents = _bitSets.length;
		for (int k0 = 0; k0 < nBlueComponents; ++k0) {
			final BitSet bitSet = _bitSets[k0];
			final int k1 = bitSet.nextClearBit(0);
			final int bitSetSize = getBitSetSize(k0);
			if (k1 < bitSetSize) {
				return new int[] {
						k0, k1
				};
			}
		}
		return null;
	}

	private static void nextSpec(final int[] spec) {
		/**
		 * <pre>
		 * spec[0] is the length of the path
		 * spec[1] is the number of cycles of length 1 (which is always 0)
		 * spec[2] is the number of cycles of length 2
		 * ...
		 * The longest possible cycle is nBlueArcs-2, so spec must
		 * be allocated so that its highest index is nBlueArcs-2 and
		 * this is the way that the calling program indicates nBlueArcs.
		 * Returns the next spec, or all 0s if there is none.
		 * The update is done in place so that the output is the
		 * same spec as the input.
		 * If spec[0] == 0, then the calling program is telling us to
		 * fill in the first spec.
		 * </pre>
		 */
		final int nBlueArcs = spec.length + 1;
		if (nBlueArcs < 3 || nBlueArcs % 2 == 0) {
			spec[0] = 0;
			return;
		}
		final int pathLength;
		if (spec[0] > 0) {
			final int pathLengthX = spec[0];
			final int nInCycles = nBlueArcs - pathLengthX;
			for (int cum = 0, k = 2; k < nInCycles; ++k) {
				cum += spec[k] * k;
				if (cum == k + 1 || cum > k + 2) {
					/** Can bump k+1. */
					++spec[k + 1];
					cum -= k + 1;
					Arrays.fill(spec, 1, k + 1, 0);
					if (cum % 2 == 1) {
						++spec[3];
						cum -= 3;
					}
					spec[2] = cum / 2;
					return;
				}
			}
			/** Must go on to the next larger pathLength. */
			pathLength = pathLengthX + 1;
		} else {
			/** This is the initial call to nextSpec. Set pathLength = 2. */
			pathLength = 2;
		}

		/**
		 * <pre>
		 * If pathLength >= nBlueArcs, there is no "next."
		 * If pathLength == nBlueArcs - 1, simply set pathLength
		 * to nBlueArcs, zero out the cycles, and that is our "next."
		 * </pre>
		 */
		Arrays.fill(spec, 0);
		if (pathLength >= nBlueArcs) {
			return;
		}
		if (pathLength == nBlueArcs - 1) {
			spec[0] = nBlueArcs;
			return;
		}
		/** Use as many 2s and up to one 3. */
		spec[0] = pathLength;
		int nInCycles = nBlueArcs - pathLength;
		if (nInCycles % 2 == 1) {
			spec[3] = 1;
			nInCycles -= 3;
		}
		spec[2] = nInCycles / 2;
	}

	private int getBitSetSize(final int k0) {
		return _cumSizes[k0] - (k0 == 0 ? 0 : _cumSizes[k0 - 1]);
	}

	/** Utility routine for creating a succinct string from a spec. */
	private static String specToString(final int[] spec) {
		String s = String.format("[%d", spec[0]);
		final int n = spec.length;
		int maxN = 0;
		for (int k = 2; k < n; ++k) {
			if (spec[k] > 0) {
				maxN = k;
			}
		}
		for (int k = 2; k <= maxN; ++k) {
			s += String.format("%s%d", k == 2 ? " " : ",", spec[k]);
		}
		s += "]";
		return s;
	}

	/**
	 * Utility routine for creating a spec from the string that is produced by
	 * specToString.
	 */
	private static int[] stringToSpec(final String specString) {
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
		int nBlueArcs = fieldsArray[0];
		for (int k = 1; k < nFields; ++k) {
			nBlueArcs += (k + 1) * fieldsArray[k];
		}
		final int[] spec = new int[nBlueArcs - 1];
		Arrays.fill(spec, 0);
		spec[0] = fieldsArray[0];
		System.arraycopy(fieldsArray, 1, spec, 2, nFields - 1);
		return spec;
	}

	/** The punchline. */
	public static int feynman_F(final int n) {
		final int nBlueArcs = n + 1;
		System.out.printf("n[%d] (nBlueArcs[%d])", n, nBlueArcs);
		final int[] spec = new int[nBlueArcs - 1];
		/** To indicate that we are just starting through the set of specs: */
		spec[0] = 0;
		for (int globalCount = 0;;) {
			nextSpec(spec);
			if (spec[0] == 0) {
				return globalCount;
			}
			final BlueGraph blueGraph = new BlueGraph(spec);
			final int thisCount = blueGraph.getCount(/* nRedEdgesPlaced= */0);
			globalCount += thisCount;
			System.out.printf("\n%s, #Red Completions[%d], Running Total[%d]", //
					specToString(spec), thisCount, globalCount);
		}
	}

	/* Testing individual blue configurations or feynman_F for n = whatever. */
	public final static void main(final String[] args) {
		final int[] specString = null; // stringToSpec("[4 0,0,0,1]");
		if (specString != null) {
			final BlueGraph blueGraph = new BlueGraph(specString);
			final int count = blueGraph.getCount(/* nRedEdgesPlaced= */0);
			System.out.printf("\ncount[%d] for %s", count, specToString(specString));
		} else {
			feynman_F(6);
		}
	}

	/** Testing stringToSpec. */
	public static void main1(final String[] args) {
		final int[] spec = stringToSpec("[10 0,1]");
		System.out.printf("\n%s", specToString(spec));
	}

	/** Testing nextSpec. */
	public static void main2(final String[] args) {
		final int nBlueArcs = 9;
		final int[] spec = new int[nBlueArcs - 1];
		Arrays.fill(spec, 0);
		for (long nSpecs = 0;; ++nSpecs) {
			nextSpec(spec);
			if (spec[0] == 0) {
				break;
			}
			if (nSpecs % 10000000L == 0) {
				System.out.printf("\n%10d.\t%s", nSpecs, specToString(spec));
			}
		}
	}

	/** Utility routine that is tested but turned out not to be used. */
	private static int[] idToPair(final int[] cumCounts, final int id) {
		/**
		 * <pre>
		 * If there are 2 blue components with sizes 3 and 4, then
		 * _cumSizes will be {3,7}.
		 * id = 0 -> {0,0}
		 * id = 3 -> {1,0}
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

	/** Testing idToPair. */
	public final static void main3(final String[] args) {
		final int[] cumCounts = new int[] {
				3, 7, 8
		};
		for (int id = 0; id < 10; ++id) {
			final int[] x = idToPair(cumCounts, id);
			System.out.printf("\n%d->[%d,%d]", id, x[0], x[1]);
		}
	}

}
