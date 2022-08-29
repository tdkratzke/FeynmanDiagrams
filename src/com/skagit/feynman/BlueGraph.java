package com.skagit.feynman;

import java.util.BitSet;

public class BlueGraph {
	final private int[] _blueVector;
	final int _modValue;
	final private BitSet[] _matchedNodesS;
	final private int[] _sizes;
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
		final int nInPath = _blueVector[0];

		/** Compute nBlueComponents, _maxCycleSize, and _nRedEdgesToPlace. */
		final int nBlueComponents;
		{
			int nBlueComponentsX = 1;
			int maxCycleSize = 0;
			int nNodesOfInterest = _blueVector[0] - 1;
			for (int k = 1; k < blueVectorLength; ++k) {
				final int nSimilar = _blueVector[k];
				if (nSimilar > 0) {
					final int cycleLength = k + 1;
					nNodesOfInterest += nSimilar * cycleLength;
					nBlueComponentsX += nSimilar;
					maxCycleSize = cycleLength;
				}
			}
			nBlueComponents = nBlueComponentsX;
			_maxCycleSize = maxCycleSize;
			_nRedEdgesToPlace = nNodesOfInterest / 2;
		}

		/** Fill in _matchedNodesS and _cumSizes. */
		_matchedNodesS = new BitSet[nBlueComponents];
		_sizes = new int[nBlueComponents];
		for (int k = 0, k0 = 0; k < blueVectorLength; ++k) {
			final int nMatchable = k == 0 ? (nInPath - 1) : (k + 1);
			final int nSimilar = k == 0 ? 1 : _blueVector[k];
			for (int kSimilar = 0; kSimilar < nSimilar; ++kSimilar, ++k0) {
				_matchedNodesS[k0] = new BitSet(nMatchable);
				_sizes[k0] = nMatchable;
			}
		}
		/** Component 0 is always connected to the path. */
		_connectedToPathComponents = new BitSet(nBlueComponents);
		_connectedToPathComponents.set(0);
		_nUnmatchedInConnectedToPath = _sizes[0];
	}

	/** The real hammer; count ways to complete the red edges. */
	private long recursiveGetCount(final int nRedEdgesPlaced) {
		if (nRedEdgesPlaced == _nRedEdgesToPlace - 1) {
			return 1L;
		}
		final int[] pair = getNodeToMatch(/* afterPair= */null);

		long count = 0L;

		/** Count ways to match pair into components that are connectedToPath. */
		if (_nUnmatchedInConnectedToPath > 2) {
			final int[] pairX = getNodeToMatch(/* afterPair= */pair);
			match(pair, pairX);
			final long thisCount = recursiveGetCount(nRedEdgesPlaced + 1);
			unMatch(pair, pairX);
			count = accumulate(count, (_nUnmatchedInConnectedToPath - 1), thisCount);
		}

		/** Count ways to match pair into components that are not connectedToPath. */
		final int nBlueComponents = _matchedNodesS.length;
		final BitSet usedSizes = new BitSet(_maxCycleSize + 1);
		for (int k0X = _connectedToPathComponents
				.nextClearBit(0); k0X < nBlueComponents; k0X = _connectedToPathComponents.nextClearBit(k0X + 1)) {
			final int nInCycle = _sizes[k0X];
			if (!usedSizes.get(nInCycle)) {
				usedSizes.set(nInCycle);
				final int[] pairX = new int[] {
						k0X, 0
				};
				match(pair, pairX);
				final long thisCount = recursiveGetCount(nRedEdgesPlaced + 1);
				unMatch(pair, pairX);
				count = accumulate(count, 1, thisCount);
			}
		}
		return count;
	}

	private long accumulate(final long count, final int multiplier, final long newCount) {
		if (_modValue <= 1) {
			return count + multiplier * newCount;
		}
		final long a = count % _modValue;
		final long b = ((multiplier % _modValue) * (newCount % _modValue)) % _modValue;
		return (a + b) % _modValue;
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
			final int size = _sizes[k0];
			if (_matchedNodesS[k0].cardinality() == size) {
				continue;
			}
			final int startSearchAt = (k0 == startK0 ? startK1 : 0);
			final int k1 = _matchedNodesS[k0].nextClearBit(startSearchAt);
			if (k1 < size) {
				return new int[] {
						k0, k1
				};
			}
		}
		/** To keep the compiler happy. */
		return null;
	}

	private void match(final int[] pair, final int[] pairX) {
		for (int iPass = 0; iPass < 2; ++iPass) {
			final int[] pairY = iPass == 0 ? pair : pairX;
			final int k0 = pairY[0], k1 = pairY[1];
			final BitSet matchedNodes = _matchedNodesS[k0];
			matchedNodes.set(k1);
			--_nUnmatchedInConnectedToPath;
			/** If k0 weren't connectedToPath before, it is now. */
			if (!_connectedToPathComponents.get(k0)) {
				_connectedToPathComponents.set(k0);
				final int size = _sizes[k0];
				_nUnmatchedInConnectedToPath += size;
			}
		}
	}

	private void unMatch(final int[] pair, final int[] pairX) {
		for (int iPass = 0; iPass < 2; ++iPass) {
			final int[] pairY = iPass == 0 ? pair : pairX;
			final int k0 = pairY[0], k1 = pairY[1];
			final BitSet matchedNodes = _matchedNodesS[k0];
			matchedNodes.clear(k1);
			++_nUnmatchedInConnectedToPath;
			/**
			 * If k0 == 0, it is automatically connectedToPath. Otherwise, k0 is
			 * unconnectedToPath if cardinality == 0.
			 */
			if (k0 > 0) {
				final int cardinality = matchedNodes.cardinality();
				if (cardinality == 0) {
					_connectedToPathComponents.clear(k0);
					final int size = _sizes[k0];
					_nUnmatchedInConnectedToPath -= size;
				}
			}
		}
	}

	public long getCount() {
		return recursiveGetCount(0);
	}

}
