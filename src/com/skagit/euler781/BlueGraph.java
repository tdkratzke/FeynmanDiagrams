package com.skagit.euler781;

import java.util.ArrayList;
import java.util.BitSet;

public class BlueGraph {
	final private int[] _blueVector;
	final private BitSet[] _matchedNodesS;
	final private int[] _componentSizes;
	final private int _maxCycleSize;
	final private int _nRedEdgesToPlace;
	final private BitSet _connectedToPathComponents;
	private int _nUnmatchedInConnectedToPath;

	public BlueGraph(final int[] blueVector) {
		_blueVector = compress(blueVector);
		final int blueVectorLength = _blueVector.length;

		/** Compute nBlueComponents, _maxCycleSize, and _nRedEdgesToPlace. */
		int nBlueComponents = 1;
		_maxCycleSize = _blueVector.length;
		int nNodesOfInterest = _blueVector[0] - 1;
		for (int k = 1; k < blueVectorLength; ++k) {
			final int nSimilar = _blueVector[k];
			if (nSimilar > 0) {
				final int cycleLength = k + 1;
				nNodesOfInterest += nSimilar * cycleLength;
				nBlueComponents += nSimilar;
			}
		}
		if (nNodesOfInterest % 2 == 1) {
			_matchedNodesS = null;
			_componentSizes = null;
			_nRedEdgesToPlace = -1;
			_connectedToPathComponents = null;
			return;
		}
		_nRedEdgesToPlace = nNodesOfInterest / 2;

		/** Fill in _matchedNodesS and _componentSizes. */
		final int nNodesInPath = _blueVector[0] - 1;
		_matchedNodesS = new BitSet[nBlueComponents];
		_componentSizes = new int[nBlueComponents];
		for (int k = 0, k0 = 0; k < blueVectorLength; ++k) {
			final int comonentSize = k == 0 ? nNodesInPath : (k + 1);
			final int nSimilar = k == 0 ? 1 : _blueVector[k];
			for (int kSimilar = 0; kSimilar < nSimilar; ++kSimilar, ++k0) {
				_matchedNodesS[k0] = new BitSet(comonentSize);
				_componentSizes[k0] = comonentSize;
			}
		}
		/** Component 0 is always connected to the path. */
		_connectedToPathComponents = new BitSet(nBlueComponents);
		_connectedToPathComponents.set(0);
		_nUnmatchedInConnectedToPath = _componentSizes[0];
	}

	/** The real hammer; count ways to complete the red edges. */
	private long recursiveGetNRedCompletions(final int nRedEdgesPlaced) {
		if (_matchedNodesS == null) {
			return 0;
		}
		if (nRedEdgesPlaced == _nRedEdgesToPlace - 1) {
			return 1;
		}
		final int[] pair = getNodeToMatch(/* afterPair= */null);

		long nRedCompletions = 0L;

		/** Count ways to match pair into components that are connectedToPath. */
		if (_nUnmatchedInConnectedToPath > 2) {
			final int[] pairX = getNodeToMatch(/* afterPair= */pair);
			matchOrUnMatch(pair, pairX, /* match= */true);
			final long thisNRedCompletions = recursiveGetNRedCompletions(nRedEdgesPlaced + 1);
			matchOrUnMatch(pair, pairX, /* match= */false);
			nRedCompletions = (nRedCompletions + (_nUnmatchedInConnectedToPath - 1) * thisNRedCompletions)
					% FeynmanF1._Modulo;
		}

		/** Count ways to match pair into components that are not connectedToPath. */
		final int nBlueComponents = _matchedNodesS.length;
		final BitSet usedComponentSizes = new BitSet(_maxCycleSize + 1);
		for (int k0X = _connectedToPathComponents
				.nextClearBit(0); k0X < nBlueComponents; k0X = _connectedToPathComponents.nextClearBit(k0X + 1)) {
			final int nInCycle = _componentSizes[k0X];
			if (!usedComponentSizes.get(nInCycle)) {
				usedComponentSizes.set(nInCycle);
				final int[] pairX = new int[] {
						k0X, 0
				};
				matchOrUnMatch(pair, pairX, /* match= */true);
				final long thisNRedCompletions = recursiveGetNRedCompletions(nRedEdgesPlaced + 1);
				matchOrUnMatch(pair, pairX, /* match= */false);
				nRedCompletions = (nRedCompletions + thisNRedCompletions) % FeynmanF1._Modulo;
			}
		}
		return nRedCompletions;
	}

	private int[] getNodeToMatch(final int[] afterPair) {
		final int startK0, startK1;
		if (afterPair == null) {
			startK0 = startK1 = 0;
		} else {
			startK0 = afterPair[0];
			startK1 = afterPair[1] + 1;
		}
		for (int k0 = _connectedToPathComponents.nextSetBit(startK0); k0 >= 0; k0 = _connectedToPathComponents
				.nextSetBit(k0 + 1)) {
			final int componentSize = _componentSizes[k0];
			if (_matchedNodesS[k0].cardinality() == componentSize) {
				continue;
			}
			final int startSearchAt = (k0 == startK0 ? startK1 : 0);
			final int k1 = _matchedNodesS[k0].nextClearBit(startSearchAt);
			if (k1 < componentSize) {
				return new int[] {
						k0, k1
				};
			}
		}
		/** To keep the compiler happy. */
		return null;
	}

	private void matchOrUnMatch(final int[] pair, final int[] pairX, final boolean match) {
		for (int iPass = 0; iPass < 2; ++iPass) {
			final int[] pairY = iPass == 0 ? pair : pairX;
			final int k0 = pairY[0], k1 = pairY[1];
			final BitSet matchedNodes = _matchedNodesS[k0];
			matchedNodes.set(k1, match ? true : false);
			_nUnmatchedInConnectedToPath += match ? -1 : 1;
			if (match) {
				/** If k0 weren't connectedToPath before, it is now. */
				if (!_connectedToPathComponents.get(k0)) {
					_connectedToPathComponents.set(k0);
					_nUnmatchedInConnectedToPath += _componentSizes[k0];
				}
			} else {
				/**
				 * If k0 == 0, it is automatically connectedToPath. Otherwise, k0 is
				 * unconnectedToPath if cardinality is now 0.
				 */
				if (k0 > 0 && matchedNodes.cardinality() == 0) {
					_connectedToPathComponents.clear(k0);
					_nUnmatchedInConnectedToPath -= _componentSizes[k0];
				}
			}
		}
	}

	public long getNRedCompletions() {
		return recursiveGetNRedCompletions(0);
	}

	static int[] compress(final int[] array) {
		final int len = array.length;
		for (int k = len - 1; k >= 0; --k) {
			if (array[k] > 0) {
				if (k == len - 1) {
					return array;
				}
				final int[] newArray = new int[k + 1];
				System.arraycopy(array, 0, newArray, 0, k + 1);
				return newArray;
			}
		}
		/** All 0s. */
		return len == 0 ? array : new int[] {};
	}

	static String blueVectorToString(final int[] blueVector) {
		String s = String.format("[%d", blueVector[0]);
		final int n = blueVector.length;
		for (int k = 1; k < n; ++k) {
			s += String.format("%s%d", k == 1 ? " " : ",", blueVector[k]);
		}
		s += "]";
		return s;
	}

	static int[] stringToBlueVector(final String s) {
		final String[] fields = s.trim().split("[\\s,\\[\\]]+");
		final int nFieldsX = fields.length;
		final ArrayList<Integer> fieldsList = new ArrayList<>();
		for (int k = 0; k < nFieldsX; ++k) {
			try {
				fieldsList.add(Integer.parseInt(fields[k]));
			} catch (final NumberFormatException e) {
			}
		}
		final int nFields = fieldsList.size();
		if (nFields < 1 || fieldsList.get(0) < 2) {
			return null;
		}
		int maxNonZeroField = 0;
		for (int k = 0; k < nFields; ++k) {
			final int field = fieldsList.get(k);
			if (field > 0) {
				maxNonZeroField = k;
			}
		}
		final int[] blueVector = new int[maxNonZeroField + 1];
		for (int k = 0; k <= maxNonZeroField; ++k) {
			blueVector[k] = fieldsList.get(k);
		}
		return blueVector;
	}

	static int computeNBlueArcs(final int[] blueVector) {
		final int len = blueVector.length;
		int nBlueArcs = blueVector[0];
		for (int k = 1; k < len; ++k) {
			nBlueArcs += (k + 1) * blueVector[k];
		}
		return nBlueArcs;
	}

}
