package com.skagit.feynman;

import java.util.Arrays;

public class NextBlueVector {

	static void nextBlueVector(final int[] blueVector) {
		/**
		 * <pre>
		 * blueVector[0] is the length of the path
		 * blueVector[1] is the number of cycles of length 1 (which is always 0)
		 * blueVector[2] is the number of cycles of length 2
		 * ...
		 * The longest possible cycle is nBlueArcs-2, so blueVector must
		 * be allocated so that its highest index is nBlueArcs-2 and
		 * this is the way that the calling program indicates nBlueArcs.
		 * Returns the next Blue Configuration, or all 0s if there is none.
		 * The update is done in place so that the output is the
		 * same blueVector as the input.
		 * If blueVector[0] == 0, then the calling program is telling us to
		 * fill in the first Blue Configuration.
		 * </pre>
		 */
		final int nBlueArcs = blueVector.length + 1;
		if (nBlueArcs < 3 || nBlueArcs % 2 == 0) {
			return;
		}
		final int pathLength = blueVector[0];
		if (pathLength == 0) {
			/** We're just starting. Return a full path with no cycles. */
			Arrays.fill(blueVector, 0);
			blueVector[0] = nBlueArcs;
			return;
		}
		final int nInCycles = nBlueArcs - pathLength;
		for (int cum = 0, k = 2; k < nInCycles; ++k) {
			cum += blueVector[k] * k;
			if (cum == k + 1 || cum > k + 2) {
				/** Can bump k+1. */
				++blueVector[k + 1];
				cum -= k + 1;
				Arrays.fill(blueVector, 1, k + 1, 0);
				if (cum % 2 == 1) {
					++blueVector[3];
					cum -= 3;
				}
				blueVector[2] = cum / 2;
				return;
			}
		}
		/** Must decrease pathLength. */
		final int newPathLength = pathLength - (pathLength == nBlueArcs ? 2 : 1);
		if (newPathLength < 2) {
			Arrays.fill(blueVector, 0);
			return;
		}
		Arrays.fill(blueVector, 0);
		blueVector[0] = newPathLength;
		int remaining = nBlueArcs - newPathLength;
		if (remaining % 2 == 1) {
			blueVector[3] = 1;
			remaining -= 3;
		}
		blueVector[2] = remaining / 2;
	}
}
