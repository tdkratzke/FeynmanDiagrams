package com.skagit.feynman;

import java.util.BitSet;

public class BlueGraph {
	final private int _modValue;
	final private int[] _blueVector;
	final private BitSet[] _matchedNodesS;
	final private int[] _componentSizes;
	final private int _maxCycleSize;
	final private int _nRedEdgesToPlace;
	final private BitSet _connectedToPathComponents;
	private int _nUnmatchedInConnectedToPath;

	public BlueGraph(final int[] blueVector) {
		this(blueVector, -1);
	}

	public BlueGraph(final int[] blueVector, final int modValue) {
		_blueVector = blueVector;
		_modValue = modValue;
		final int blueVectorLength = _blueVector.length;

		/** Compute nBlueComponents, _maxCycleSize, and _nRedEdgesToPlace. */
		int nBlueComponents = 1;
		int maxCycleSize = 0;
		int nNodesOfInterest = _blueVector[0] - 1;
		for (int k = 1; k < blueVectorLength; ++k) {
			final int nSimilar = _blueVector[k];
			if (nSimilar > 0) {
				final int cycleLength = k + 1;
				nNodesOfInterest += nSimilar * cycleLength;
				nBlueComponents += nSimilar;
				maxCycleSize = cycleLength;
			}
		}
		if (nNodesOfInterest % 2 == 1) {
			_matchedNodesS = null;
			_componentSizes = null;
			_maxCycleSize = -1;
			_nRedEdgesToPlace = -1;
			_connectedToPathComponents = null;
			return;
		}
		_maxCycleSize = maxCycleSize;
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
	private int recursiveGetNRedCompletions(final int nRedEdgesPlaced) {
		if (_matchedNodesS == null) {
			return 0;
		}
		if (nRedEdgesPlaced == _nRedEdgesToPlace - 1) {
			return 1;
		}
		final int[] pair = getNodeToMatch(/* afterPair= */null);

		int nRedCompletions = 0;

		/** Count ways to match pair into components that are connectedToPath. */
		if (_nUnmatchedInConnectedToPath > 2) {
			final int[] pairX = getNodeToMatch(/* afterPair= */pair);
			matchOrUnMatch(pair, pairX, /* match= */true);
			final int thisNRedCompletions = recursiveGetNRedCompletions(nRedEdgesPlaced + 1);
			matchOrUnMatch(pair, pairX, /* match= */false);
			nRedCompletions = FeynmanF.accumulate(nRedCompletions, (_nUnmatchedInConnectedToPath - 1),
					thisNRedCompletions, _modValue);
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
				final int thisNRedCompletions = recursiveGetNRedCompletions(nRedEdgesPlaced + 1);
				matchOrUnMatch(pair, pairX, /* match= */false);
				nRedCompletions = FeynmanF.accumulate(nRedCompletions, 1, thisNRedCompletions, _modValue);
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

	public int getNRedCompletions() {
		return recursiveGetNRedCompletions(0);
	}

}
