package com.skagit.feynman;

import java.util.Arrays;

public class SpecIterator {

	public static void nextSpec(final int[] spec) {
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

	public static void main(final String[] args) {
		final int[] spec = BlueGraph.stringToSpec("[10 0,1]");
		System.out.printf("\n%s", BlueGraph.specToString(spec));
	}

	public static void main0(final String[] args) {
		final int nBlueEdges = 13;
		final int[] spec = new int[nBlueEdges - 1];
		Arrays.fill(spec, 0);
		for (;;) {
			nextSpec(spec);
			if (spec[0] == 0) {
				break;
			}
			System.out.printf("\n%s", BlueGraph.specToString(spec));
		}
	}

}
