package com.skagit.feynman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;

public class BlueGraph {
	final public int[] _spec;
	final public BitSet[] _bitSets;
	final public int[] _cumSizes;
	final private int _nRedEdgesToPlace;

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

		/** Compute and nBlueComponents. */
		final int nBlueComponents;
		{
			int nBlueComponentsX = 1;
			int nBlueVerticesOfInterest = _spec[0] - 1;
			for (int k = 2; k < specLength; ++k) {
				final int nSimilar = _spec[k];
				nBlueVerticesOfInterest += nSimilar * k;
				nBlueComponentsX += nSimilar;
			}
			nBlueComponents = nBlueComponentsX;
			_nRedEdgesToPlace = nBlueVerticesOfInterest / 2;
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
	 * Does the heavy lifting. Gets the number of ways of completing the red edges.
	 * The recursion level and the number of red edges placed is the same.
	 */
	private int recursiveGetCount(final int nRedEdgesPlaced) {
		if (nRedEdgesPlaced == _nRedEdgesToPlace) {
			/** We're done. The count is this placement and only this one. */
			return 1;
		}
		final int[] pair = getSmallestUnMatchedPair();
		final int k0 = pair[0], k1 = pair[1];
		final BitSet bitSet0 = _bitSets[k0];
		final int size0 = getBitSetSize(k0);
		final int cardinality0 = bitSet0.cardinality();

		int count = 0;

		/**
		 * <pre>
		 * Start by matching id with some vertex in its own component,recursively get the count,
		 * and multiply it by the number of unMatched elements in that component
		 * (besides id).
		 * </pre>
		 */
		final int multiplier0 = size0 - cardinality0 - 1;
		if (multiplier0 > 0) {
			final int k1X = bitSet0.nextClearBit(k1 + 1);
			final int[] pairX = new int[] {
					k0, k1X
			};
			if (isLegalPair(pair, pairX)) {
				match(pair, pairX);
				final int thisCount = recursiveGetCount(nRedEdgesPlaced + 1);
				unMatch(pair, pairX);
				count += multiplier0 * thisCount;
			}
		}

		/** Match id with ids that are not in its component. */
		final int nBlueComponents = _bitSets.length;
		final HashSet<Integer> usedSizes = new HashSet<>();
		for (int k0X = k0 + 1; k0X < nBlueComponents; ++k0X) {
			final BitSet bitSetX = _bitSets[k0X];
			final int sizeX = getBitSetSize(k0X);
			final int cardinalityX = bitSetX.cardinality();
			/**
			 * If k0X is an unopened cycle, skip it if we've visited an unopened cycle with
			 * the same size. Otherwise, match id to the first vertex and the multiplier is
			 * 1.
			 */
			if (cardinalityX == 0) {
				if (usedSizes.add(sizeX)) {
					final int[] pairX = new int[] {
							k0X, 0
					};
					match(pair, pairX);
					final int thisCount = recursiveGetCount(nRedEdgesPlaced + 1);
					unMatch(pair, pairX);
					count += thisCount;
				}
				continue;
			}
			/** We're on an open cycle. Skip it if it's full. */
			if (cardinalityX == sizeX) {
				continue;
			}
			final int multiplierX = sizeX - cardinalityX;
			final int k1X = bitSetX.nextClearBit(0);
			final int[] pairX = new int[] {
					k0X, k1X
			};
			if (isLegalPair(pair, pairX)) {
				match(pair, pairX);
				final int thisCount = recursiveGetCount(nRedEdgesPlaced + 1);
				unMatch(pair, pairX);
				count += multiplierX * thisCount;
			}
		}
		return count;
	}

	/**
	 * If adding (pair, pairX) leaves the graph connectable, then (pair, pairX) is
	 * legal.
	 */
	private boolean isLegalPair(final int[] pair, final int[] pairX) {
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

	/** idToPair is static to make debugging simpler. */
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

	private int getBitSetSize(final int k0) {
		return _cumSizes[k0] - (k0 == 0 ? 0 : _cumSizes[k0 - 1]);
	}

	/** Utility routine for creating a succinct string from a spec. */
	private static String specToString(final int[] spec) {
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

	private static void nextSpec(final int[] spec) {
		/**
		 * <pre>
		 * spec[0] is the length of the path
		 * spec[1] is the number of cycles of length 1 (which is always 0)
		 * spec[2] is the number of cycles of length 2
		 * ...
		 * The longest possible cycle is nBlueEdges-2, so spec must
		 * be allocated so that its highest index is nBlueEdgess-2 and
		 * this is the way that the calling program indicates nBlueEdges.
		 * Returns the next spec, or all 0s if there is none.
		 * The update is done in place so that the output is the
		 * same spec as the input.
		 * If spec[0] == 0, then the calling program is telling us to
		 * fill in the first spec.
		 * </pre>
		 */
		final int nBlueEdges = spec.length + 1;
		if (nBlueEdges < 3 || nBlueEdges % 2 == 0) {
			return;
		}
		final int pathLength = spec[0];
		if (pathLength == 0) {
			/** We're just starting. Return a full path with no cycles. */
			Arrays.fill(spec, 0);
			spec[0] = nBlueEdges;
			return;
		}
		final int nInCycles = nBlueEdges - pathLength;
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
		/** Must decrease pathLength. */
		final int newPathLength = pathLength - (pathLength == nBlueEdges ? 2 : 1);
		if (newPathLength < 2) {
			Arrays.fill(spec, 0);
			return;
		}
		Arrays.fill(spec, 0);
		spec[0] = newPathLength;
		int remaining = nBlueEdges - newPathLength;
		if (remaining % 2 == 1) {
			spec[3] = 1;
			remaining -= 3;
		}
		spec[2] = remaining / 2;
	}

	/** The punchline. */
	public static int feynman_F(final int n) {
		final int nBlueEdges = n + 1;
		final int[] spec = new int[nBlueEdges - 1];
		Arrays.fill(spec, 0);
		System.out.printf("n[%d] (nBlueEdges[%d])", n, nBlueEdges);
		for (int globalCount = 0;;) {
			nextSpec(spec);
			if (spec[0] == 0) {
				return globalCount;
			}
			final BlueGraph blueGraph = new BlueGraph(spec);
			final int thisCount = blueGraph.recursiveGetCount(/* nRedEdgesPlaced= */0);
			globalCount += thisCount;
			System.out.printf("\n%s, count[%d], globalCount[%d]", //
					specToString(spec), thisCount, globalCount);
		}
	}

	/* Testing individual blue configurations or testing nBlue = 9. */
	public final static void main(final String[] args) {
		final int[] individualSpecString = null; // stringToSpec("[4 0,0,0,1]");
		if (individualSpecString != null) {
			final BlueGraph blueGraph = new BlueGraph(individualSpecString);
			final int count = blueGraph.recursiveGetCount(/* nRedEdgesPlaced= */0);
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

	/** Testing idToPair. */
	public final static void main1(final String[] args) {
		final int[] cumCounts = new int[] {
				3, 7, 8
		};
		for (int id = 0; id < 10; ++id) {
			final int[] x = idToPair(cumCounts, id);
			System.out.printf("\n%d->[%d,%d]", id, x[0], x[1]);
		}
	}
}
