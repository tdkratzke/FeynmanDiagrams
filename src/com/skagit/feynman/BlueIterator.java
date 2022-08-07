package com.skagit.feynman;

import java.util.Arrays;

public class BlueIterator {

	public static void nextBlue(final int[] blue) {
		/**
		 * <pre>
		 * blue[0] is the length of the path
		 * blue[1] is the number of cycles of length 1 (which is always 0)
		 * blue[2] is the number of cycles of length 2
		 * ...
		 * The longest possible cycle is nBlues-2, so blue must
		 * be allocated so that its highest index is nBlues-2 and
		 * this is the way that the calling program indicates nBlues.
		 * Returns the next blue, or all 0s if there is none.
		 * The update is done in place so that the output is the
		 * same blue as the input.
		 * If blue[0] == 0, then the calling program is telling us to
		 * fill in the first blue.
		 * </pre>
		 */
		final int nBlueEdges = blue.length + 1;
		if (nBlueEdges < 3 || nBlueEdges % 2 == 0) {
			return;
		}
		final int pathLength = blue[0];
		if (pathLength == 0) {
			/** We're just starting. Return a full path with no cycles. */
			Arrays.fill(blue, 0);
			blue[0] = nBlueEdges;
			return;
		}
		final int nInCycles = nBlueEdges - pathLength;
		for (int cum = 0, k = 2; k < nInCycles; ++k) {
			cum += blue[k] * k;
			if (cum == k + 1 || cum > k + 2) {
				/** Can bump k+1. */
				++blue[k + 1];
				cum -= k + 1;
				Arrays.fill(blue, 1, k + 1, 0);
				if (cum % 2 == 1) {
					++blue[3];
					cum -= 3;
				}
				blue[2] = cum / 2;
				return;
			}
		}
		/** Must decrease pathLength. */
		final int newPathLength = pathLength - (pathLength == nBlueEdges ? 2 : 1);
		if (newPathLength < 2) {
			Arrays.fill(blue, 0);
			return;
		}
		Arrays.fill(blue, 0);
		blue[0] = newPathLength;
		int remaining = nBlueEdges - newPathLength;
		if (remaining % 2 == 1) {
			blue[3] = 1;
			remaining -= 3;
		}
		blue[2] = remaining / 2;
	}

	public static void main(final String[] args) {
		final int[] blue = BlueGraph.stringToSpec("[10 0,1]");
		System.out.printf("\n%s", BlueGraph.specToString(blue));
	}

	public static void main0(final String[] args) {
		final int nBlueEdges = 13;
		final int[] blue = new int[nBlueEdges - 1];
		Arrays.fill(blue, 0);
		for (;;) {
			nextBlue(blue);
			if (blue[0] == 0) {
				break;
			}
			System.out.printf("\n%s", BlueGraph.specToString(blue));
		}
	}

}
