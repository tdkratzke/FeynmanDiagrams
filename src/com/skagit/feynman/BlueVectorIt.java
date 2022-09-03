package com.skagit.feynman;

import java.util.Arrays;

public class BlueVectorIt {
	private int[] _next;
	private final int[] _stop;
	private final int _nBlueArcs;

	public static int[] getInitialBlueVector(final int nBlueArcs) {
		if (nBlueArcs % 2 == 0 || nBlueArcs == 1) {
			return null;
		}
		if (nBlueArcs == 3) {
			return new int[] {
					3
			};
		}
		return new int[] {
				2, (nBlueArcs - 5) / 2, 1
		};
	}

	public BlueVectorIt(final int n) {
		this(getInitialBlueVector(/* nBlueArcs= */n + 1), null);
	}

	public BlueVectorIt(final int[] start, final int[] stop) {
		_next = FeynmanF.compress(start.clone());
		_nBlueArcs = FeynmanF.computeNBlueArcs(_next);
		_stop = stop == null ? _next.clone() : FeynmanF.compress(stop.clone());
	}

	public boolean hasNext() {
		return _next != null;
	}

	public int[] next() {
		final int[] returnValue = _next.clone();
		final int pathLength = _next[0];
		final int maxCycleLength = _next.length;

		/**
		 * Figure out which cycleLength to bump. In the following loop, we will bump
		 * cycleLength + 1 if we bump anything at all. Because we might want to bump
		 * maxCycleLength + 2, we have to allow cycleLength to go all the way up to
		 * maxCycleLength + 1.
		 */
		final int[] oldNext = _next;
		_next = null;
		for (int cum = 0, cycleLength = 2; cycleLength <= maxCycleLength + 1; ++cycleLength) {
			if (cycleLength <= maxCycleLength) {
				cum += oldNext[cycleLength - 1] * cycleLength;
			}
			/**
			 * If there are cycleLength + 1, or at least two to spare, we can bump
			 * cycleLength + 1.
			 */
			if (cum == cycleLength + 1 || cum >= cycleLength + 3) {
				final int cycleLengthToBump = cycleLength + 1;
				if (cycleLengthToBump > maxCycleLength) {
					_next = new int[cycleLengthToBump];
					Arrays.fill(_next, 0);
					_next[0] = pathLength;
				} else {
					_next = oldNext;
					Arrays.fill(_next, 1, cycleLengthToBump - 1, 0);
				}
				++_next[cycleLengthToBump - 1];
				cum -= cycleLengthToBump;
				if (cum % 2 == 1) {
					++_next[2];
					cum -= 3;
				}
				_next[1] = cum / 2;
				break;
			}
		}

		if (_next == null) {
			/**
			 * Could not bump a cycleLength. Increase pathLength and then use 2-cycles and
			 * possibly one 3-cycle.
			 */
			if (pathLength == _nBlueArcs - 2) {
				_next = new int[] {
						_nBlueArcs
				};
			} else {
				final int newPathLength = pathLength + 1;
				if (newPathLength > _nBlueArcs) {
					_next = getInitialBlueVector(_nBlueArcs);
				} else {
					final int nRemaining = _nBlueArcs - newPathLength;
					if (nRemaining % 2 == 1) {
						_next = new int[] {
								newPathLength, (nRemaining - 3) / 2, 1
						};
					} else {
						_next = new int[] {
								newPathLength, nRemaining / 2
						};
					}
				}
			}
		}
		/** If _next = _stop, set _next to null. */
		final int n0 = _next.length;
		final int n1 = _stop.length;
		if (n0 == n1) {
			for (int k = 0; k < n0; ++k) {
				if (_next[k] != _stop[k]) {
					return returnValue;
				}
			}
			_next = null;
		}
		return returnValue;
	}

	@SuppressWarnings("unused")
	public static void main(final String[] args) {
		final int[] start = FeynmanF.getInitialBlueVector(9, 3);
		final int[] stop = FeynmanF.getInitialBlueVector(9, 4);
		// final BlueVectorIt it0 = new BlueVectorIt(start, stop);
		final BlueVectorIt it1 = new BlueVectorIt(/* n= */10);
		// final BlueVectorIt it2 = new BlueVectorIt(new int[] {
		// 5, 2
		// }, null);
		final BlueVectorIt it = it1;
		while (it.hasNext()) {
			System.out.printf("%s", FeynmanF.blueVectorToString(it.next()));
			if (it.hasNext()) {
				System.out.println();
			}
		}
	}

}
