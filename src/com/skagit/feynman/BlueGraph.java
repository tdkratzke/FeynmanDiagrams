package com.skagit.feynman;

import java.util.BitSet;

public class BlueGraph {
	final private int[] _blueVector;
	final private BitSet[] _matchedNodesS;
	final private int[] _sizes;
	final private int _maxCycleSize;
	final private int _nRedEdgesToPlace;
	final private BitSet _connectedToPathComponents;
	private int _nUnmatchedInConnectedToPath;

	public BlueGraph(final int[] blueVector) {
		_blueVector = blueVector;
		final int blueVectorLength = _blueVector.length;
		final int nInPath = _blueVector[0];

		/**
		 * <pre>
		 * k indexes _blueVector.
		 * k0 indexes components.
		 * k1 indexes nodes within a component.
		 * </pre>
		 */

		/** Compute nBlueComponents, _maxCycleSize, and _nRedEdgesToPlace. */
		final int nBlueComponents;
		{
			int nBlueComponentsX = 1;
			int maxCycleSize = 0;
			int nNodesOfInterest = _blueVector[0] - 1;
			for (int k = 2; k < blueVectorLength; ++k) {
				final int nSimilar = _blueVector[k];
				if (nSimilar > 0) {
					nNodesOfInterest += nSimilar * k;
					nBlueComponentsX += nSimilar;
					maxCycleSize = k;
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
			final int nMatchable = k == 0 ? (nInPath - 1) : k;
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
	private int recursiveGetCount(final int nRedEdgesPlaced) {
		if (nRedEdgesPlaced == _nRedEdgesToPlace - 1) {
			return 1;
		}
		final int[] pair = getNodeToMatch();
		final int k0 = pair[0], k1 = pair[1];

		int count = 0;

		/** Count ways to match pair into components that are connectedToPath. */
		if (_nUnmatchedInConnectedToPath > 2) {
			for (int k0X = _connectedToPathComponents.nextSetBit(0); k0X >= 0; k0X = _connectedToPathComponents
					.nextSetBit(k0X + 1)) {
				final BitSet matchedNodes = _matchedNodesS[k0X];
				final int size = _sizes[k0X];
				final int cardinality = matchedNodes.cardinality();
				if (size == cardinality) {
					continue;
				}
				final int k1X = matchedNodes.nextClearBit(k0X == k0 ? (k1 + 1) : 0);
				final int[] pairX = new int[] {
						k0X, k1X
				};
				match(pair, pairX);
				final int thisCount = recursiveGetCount(nRedEdgesPlaced + 1);
				unMatch(pair, pairX);
				if (true) {
					count += (_nUnmatchedInConnectedToPath - 1) * thisCount;
					break;
				} else {
					final int multiplier = size - cardinality - (k0 == k0X ? 1 : 0);
					count += multiplier * thisCount;
				}
			}
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
				final int thisCount = recursiveGetCount(nRedEdgesPlaced + 1);
				unMatch(pair, pairX);
				count += thisCount;
			}
		}
		return count;
	}

	private int[] getNodeToMatch() {
		for (int k0 = _connectedToPathComponents.nextSetBit(0); k0 >= 0; k0 = _connectedToPathComponents
				.nextSetBit(k0 + 1)) {
			if (_matchedNodesS[k0].cardinality() == _sizes[k0]) {
				continue;
			}
			final int k1 = _matchedNodesS[k0].nextClearBit(0);
			return new int[] {
					k0, k1
			};
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

	int getCount() {
		return recursiveGetCount(0);
	}

}
