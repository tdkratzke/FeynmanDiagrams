package com.skagit.feynman;

import java.util.Arrays;

public class NextBlueVector {

	static int[] nextBlueVector(final int[] blueVector) {
		/**
		 * <pre>
		 * blueVector[0] is the length of the path
		 * blueVector[1] is the number of cycles of length 2
		 * blueVector[2] is the number of cycles of length 3
		 * ...
		 * Leaves blueVector alone and always allocates its return value.
		 * </pre>
		 */
		final int pathLength = blueVector[0];
		final int nEntries = blueVector.length;
		int nInCycles = 0;
		int maxCycleLength = 0;
		for (int k = 1; k < nEntries; ++k) {
			final int nCycles = blueVector[k];
			if (nCycles > 0) {
				maxCycleLength = k + 1;
				nInCycles += maxCycleLength * nCycles;
			}
		}

		/**
		 * Figure out which cycleLength to bump. In the following loop, we will bump
		 * cycleLength + 1 if we bump anything at all. Because we might want to bump
		 * maxCycleLength + 2, we have to allow cycleLength to go all the way up to
		 * maxCycleLength + 1.
		 */
		final int nBlueArcs = pathLength + nInCycles;
		for (int cum = 0, cycleLength = 2; cycleLength <= maxCycleLength + 1; ++cycleLength) {
			if (cycleLength <= maxCycleLength) {
				cum += blueVector[cycleLength - 1] * cycleLength;
			}
			/**
			 * If there are cycleLength + 1, or at least two to spare, we can bump
			 * cycleLength + 1.
			 */
			if (cum == cycleLength + 1 || cum >= cycleLength + 3) {
				final int cycleLengthToBump = cycleLength + 1;
				/** We might have to allocate a new BlueVector. Otherwise, re-use the input. */
				final int[] newBlueVector;
				if (cycleLengthToBump > maxCycleLength) {
					newBlueVector = new int[cycleLengthToBump];
					Arrays.fill(newBlueVector, 0);
					newBlueVector[0] = pathLength;
				} else {
					newBlueVector = blueVector.clone();
					Arrays.fill(newBlueVector, 1, cycleLengthToBump - 1, 0);
				}
				++newBlueVector[cycleLengthToBump - 1];
				cum -= cycleLengthToBump;
				if (cum % 2 == 1) {
					++newBlueVector[2];
					cum -= 3;
				}
				newBlueVector[1] = cum / 2;
				return newBlueVector;
			}
		}

		/**
		 * Could not bump a cycleLength. Decrease pathLength and then use 2-cycles and
		 * possibly one 3-cycle.
		 */
		final int newPathLength = pathLength - (nInCycles == 0 ? 2 : 1);
		if (newPathLength < 2) {
			/** No newBlueVector. */
			return null;
		}
		final int nRemaining = nBlueArcs - newPathLength;
		if (nRemaining % 2 == 1) {
			return new int[] {
					newPathLength, (nRemaining - 3) / 2, 1
			};
		}
		return new int[] {
				newPathLength, nRemaining / 2
		};
	}

	public static void main(final String[] args) {
		if (false) {
			final int[] blueVector = FeynmanF.stringToBlueVector("[5 2]");
			System.out.printf("\n%s", FeynmanF.blueVectorToString(nextBlueVector(blueVector)));
			System.exit(33);
		} else {
			final int nBlueArcs = 9;
			int[] blueVector = new int[] {
					nBlueArcs
			};
			for (; blueVector != null; blueVector = nextBlueVector(blueVector)) {
				System.out.printf("\n%s", FeynmanF.blueVectorToString(blueVector));
			}
		}
	}

}
/**
 * <pre>
 *
[21]
[19 1]
[18 0,1]
[17 2]
[17 0,0,1]
[16 1,1]
[16 0,0,0,1]
[15 3]
[15 0,2]
[15 1,0,1]
[15 0,0,0,0,1]
[14 2,1]
[14 0,1,1]
[14 1,0,0,1]
[14 0,0,0,0,0,1]
[13 4]
[13 1,2]
[13 2,0,1]
[13 0,0,2]
[13 0,1,0,1]
[13 1,0,0,0,1]
[13 0,0,0,0,0,0,1]
[12 3,1]
[12 0,3]
[12 1,1,1]
[12 2,0,0,1]
[12 0,0,1,1]
[12 0,1,0,0,1]
[12 1,0,0,0,0,1]
[12 0,0,0,0,0,0,0,1]
[11 5]
[11 2,2]
[11 3,0,1]
[11 0,2,1]
[11 1,0,2]
[11 1,1,0,1]
[11 0,0,0,2]
[11 2,0,0,0,1]
[11 0,0,1,0,1]
[11 0,1,0,0,0,1]
[11 1,0,0,0,0,0,1]
[11 0,0,0,0,0,0,0,0,1]
[10 4,1]
[10 1,3]
[10 2,1,1]
[10 0,1,2]
[10 3,0,0,1]
[10 0,2,0,1]
[10 1,0,1,1]
[10 1,1,0,0,1]
[10 0,0,0,1,1]
[10 2,0,0,0,0,1]
[10 0,0,1,0,0,1]
[10 0,1,0,0,0,0,1]
[10 1,0,0,0,0,0,0,1]
[10 0,0,0,0,0,0,0,0,0,1]
[9 6]
[9 3,2]
[9 0,4]
[9 4,0,1]
[9 1,2,1]
[9 2,0,2]
[9 0,0,3]
[9 2,1,0,1]
[9 0,1,1,1]
[9 1,0,0,2]
[9 3,0,0,0,1]
[9 0,2,0,0,1]
[9 1,0,1,0,1]
[9 0,0,0,0,2]
[9 1,1,0,0,0,1]
[9 0,0,0,1,0,1]
[9 2,0,0,0,0,0,1]
[9 0,0,1,0,0,0,1]
[9 0,1,0,0,0,0,0,1]
[9 1,0,0,0,0,0,0,0,1]
[9 0,0,0,0,0,0,0,0,0,0,1]
[8 5,1]
[8 2,3]
[8 3,1,1]
[8 0,3,1]
[8 1,1,2]
[8 4,0,0,1]
[8 1,2,0,1]
[8 2,0,1,1]
[8 0,0,2,1]
[8 0,1,0,2]
[8 2,1,0,0,1]
[8 0,1,1,0,1]
[8 1,0,0,1,1]
[8 3,0,0,0,0,1]
[8 0,2,0,0,0,1]
[8 1,0,1,0,0,1]
[8 0,0,0,0,1,1]
[8 1,1,0,0,0,0,1]
[8 0,0,0,1,0,0,1]
[8 2,0,0,0,0,0,0,1]
[8 0,0,1,0,0,0,0,1]
[8 0,1,0,0,0,0,0,0,1]
[8 1,0,0,0,0,0,0,0,0,1]
[8 0,0,0,0,0,0,0,0,0,0,0,1]
[7 7]
[7 4,2]
[7 1,4]
[7 5,0,1]
[7 2,2,1]
[7 3,0,2]
[7 0,2,2]
[7 1,0,3]
[7 3,1,0,1]
[7 0,3,0,1]
[7 1,1,1,1]
[7 2,0,0,2]
[7 0,0,1,2]
[7 4,0,0,0,1]
[7 1,2,0,0,1]
[7 2,0,1,0,1]
[7 0,0,2,0,1]
[7 0,1,0,1,1]
[7 1,0,0,0,2]
[7 2,1,0,0,0,1]
[7 0,1,1,0,0,1]
[7 1,0,0,1,0,1]
[7 0,0,0,0,0,2]
[7 3,0,0,0,0,0,1]
[7 0,2,0,0,0,0,1]
[7 1,0,1,0,0,0,1]
[7 0,0,0,0,1,0,1]
[7 1,1,0,0,0,0,0,1]
[7 0,0,0,1,0,0,0,1]
[7 2,0,0,0,0,0,0,0,1]
[7 0,0,1,0,0,0,0,0,1]
[7 0,1,0,0,0,0,0,0,0,1]
[7 1,0,0,0,0,0,0,0,0,0,1]
[7 0,0,0,0,0,0,0,0,0,0,0,0,1]
[6 6,1]
[6 3,3]
[6 0,5]
[6 4,1,1]
[6 1,3,1]
[6 2,1,2]
[6 0,1,3]
[6 5,0,0,1]
[6 2,2,0,1]
[6 3,0,1,1]
[6 0,2,1,1]
[6 1,0,2,1]
[6 1,1,0,2]
[6 0,0,0,3]
[6 3,1,0,0,1]
[6 0,3,0,0,1]
[6 1,1,1,0,1]
[6 2,0,0,1,1]
[6 0,0,1,1,1]
[6 0,1,0,0,2]
[6 4,0,0,0,0,1]
[6 1,2,0,0,0,1]
[6 2,0,1,0,0,1]
[6 0,0,2,0,0,1]
[6 0,1,0,1,0,1]
[6 1,0,0,0,1,1]
[6 2,1,0,0,0,0,1]
[6 0,1,1,0,0,0,1]
[6 1,0,0,1,0,0,1]
[6 0,0,0,0,0,1,1]
[6 3,0,0,0,0,0,0,1]
[6 0,2,0,0,0,0,0,1]
[6 1,0,1,0,0,0,0,1]
[6 0,0,0,0,1,0,0,1]
[6 1,1,0,0,0,0,0,0,1]
[6 0,0,0,1,0,0,0,0,1]
[6 2,0,0,0,0,0,0,0,0,1]
[6 0,0,1,0,0,0,0,0,0,1]
[6 0,1,0,0,0,0,0,0,0,0,1]
[6 1,0,0,0,0,0,0,0,0,0,0,1]
[6 0,0,0,0,0,0,0,0,0,0,0,0,0,1]
[5 8]
[5 5,2]
[5 2,4]
[5 6,0,1]
[5 3,2,1]
[5 0,4,1]
[5 4,0,2]
[5 1,2,2]
[5 2,0,3]
[5 0,0,4]
[5 4,1,0,1]
[5 1,3,0,1]
[5 2,1,1,1]
[5 0,1,2,1]
[5 3,0,0,2]
[5 0,2,0,2]
[5 1,0,1,2]
[5 5,0,0,0,1]
[5 2,2,0,0,1]
[5 3,0,1,0,1]
[5 0,2,1,0,1]
[5 1,0,2,0,1]
[5 1,1,0,1,1]
[5 0,0,0,2,1]
[5 2,0,0,0,2]
[5 0,0,1,0,2]
[5 3,1,0,0,0,1]
[5 0,3,0,0,0,1]
[5 1,1,1,0,0,1]
[5 2,0,0,1,0,1]
[5 0,0,1,1,0,1]
[5 0,1,0,0,1,1]
[5 1,0,0,0,0,2]
[5 4,0,0,0,0,0,1]
[5 1,2,0,0,0,0,1]
[5 2,0,1,0,0,0,1]
[5 0,0,2,0,0,0,1]
[5 0,1,0,1,0,0,1]
[5 1,0,0,0,1,0,1]
[5 0,0,0,0,0,0,2]
[5 2,1,0,0,0,0,0,1]
[5 0,1,1,0,0,0,0,1]
[5 1,0,0,1,0,0,0,1]
[5 0,0,0,0,0,1,0,1]
[5 3,0,0,0,0,0,0,0,1]
[5 0,2,0,0,0,0,0,0,1]
[5 1,0,1,0,0,0,0,0,1]
[5 0,0,0,0,1,0,0,0,1]
[5 1,1,0,0,0,0,0,0,0,1]
[5 0,0,0,1,0,0,0,0,0,1]
[5 2,0,0,0,0,0,0,0,0,0,1]
[5 0,0,1,0,0,0,0,0,0,0,1]
[5 0,1,0,0,0,0,0,0,0,0,0,1]
[5 1,0,0,0,0,0,0,0,0,0,0,0,1]
[5 0,0,0,0,0,0,0,0,0,0,0,0,0,0,1]
[4 7,1]
[4 4,3]
[4 1,5]
[4 5,1,1]
[4 2,3,1]
[4 3,1,2]
[4 0,3,2]
[4 1,1,3]
[4 6,0,0,1]
[4 3,2,0,1]
[4 0,4,0,1]
[4 4,0,1,1]
[4 1,2,1,1]
[4 2,0,2,1]
[4 0,0,3,1]
[4 2,1,0,2]
[4 0,1,1,2]
[4 1,0,0,3]
[4 4,1,0,0,1]
[4 1,3,0,0,1]
[4 2,1,1,0,1]
[4 0,1,2,0,1]
[4 3,0,0,1,1]
[4 0,2,0,1,1]
[4 1,0,1,1,1]
[4 1,1,0,0,2]
[4 0,0,0,1,2]
[4 5,0,0,0,0,1]
[4 2,2,0,0,0,1]
[4 3,0,1,0,0,1]
[4 0,2,1,0,0,1]
[4 1,0,2,0,0,1]
[4 1,1,0,1,0,1]
[4 0,0,0,2,0,1]
[4 2,0,0,0,1,1]
[4 0,0,1,0,1,1]
[4 0,1,0,0,0,2]
[4 3,1,0,0,0,0,1]
[4 0,3,0,0,0,0,1]
[4 1,1,1,0,0,0,1]
[4 2,0,0,1,0,0,1]
[4 0,0,1,1,0,0,1]
[4 0,1,0,0,1,0,1]
[4 1,0,0,0,0,1,1]
[4 4,0,0,0,0,0,0,1]
[4 1,2,0,0,0,0,0,1]
[4 2,0,1,0,0,0,0,1]
[4 0,0,2,0,0,0,0,1]
[4 0,1,0,1,0,0,0,1]
[4 1,0,0,0,1,0,0,1]
[4 0,0,0,0,0,0,1,1]
[4 2,1,0,0,0,0,0,0,1]
[4 0,1,1,0,0,0,0,0,1]
[4 1,0,0,1,0,0,0,0,1]
[4 0,0,0,0,0,1,0,0,1]
[4 3,0,0,0,0,0,0,0,0,1]
[4 0,2,0,0,0,0,0,0,0,1]
[4 1,0,1,0,0,0,0,0,0,1]
[4 0,0,0,0,1,0,0,0,0,1]
[4 1,1,0,0,0,0,0,0,0,0,1]
[4 0,0,0,1,0,0,0,0,0,0,1]
[4 2,0,0,0,0,0,0,0,0,0,0,1]
[4 0,0,1,0,0,0,0,0,0,0,0,1]
[4 0,1,0,0,0,0,0,0,0,0,0,0,1]
[4 1,0,0,0,0,0,0,0,0,0,0,0,0,1]
[4 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1]
[3 9]
[3 6,2]
[3 3,4]
[3 0,6]
[3 7,0,1]
[3 4,2,1]
[3 1,4,1]
[3 5,0,2]
[3 2,2,2]
[3 3,0,3]
[3 0,2,3]
[3 1,0,4]
[3 5,1,0,1]
[3 2,3,0,1]
[3 3,1,1,1]
[3 0,3,1,1]
[3 1,1,2,1]
[3 4,0,0,2]
[3 1,2,0,2]
[3 2,0,1,2]
[3 0,0,2,2]
[3 0,1,0,3]
[3 6,0,0,0,1]
[3 3,2,0,0,1]
[3 0,4,0,0,1]
[3 4,0,1,0,1]
[3 1,2,1,0,1]
[3 2,0,2,0,1]
[3 0,0,3,0,1]
[3 2,1,0,1,1]
[3 0,1,1,1,1]
[3 1,0,0,2,1]
[3 3,0,0,0,2]
[3 0,2,0,0,2]
[3 1,0,1,0,2]
[3 0,0,0,0,3]
[3 4,1,0,0,0,1]
[3 1,3,0,0,0,1]
[3 2,1,1,0,0,1]
[3 0,1,2,0,0,1]
[3 3,0,0,1,0,1]
[3 0,2,0,1,0,1]
[3 1,0,1,1,0,1]
[3 1,1,0,0,1,1]
[3 0,0,0,1,1,1]
[3 2,0,0,0,0,2]
[3 0,0,1,0,0,2]
[3 5,0,0,0,0,0,1]
[3 2,2,0,0,0,0,1]
[3 3,0,1,0,0,0,1]
[3 0,2,1,0,0,0,1]
[3 1,0,2,0,0,0,1]
[3 1,1,0,1,0,0,1]
[3 0,0,0,2,0,0,1]
[3 2,0,0,0,1,0,1]
[3 0,0,1,0,1,0,1]
[3 0,1,0,0,0,1,1]
[3 1,0,0,0,0,0,2]
[3 3,1,0,0,0,0,0,1]
[3 0,3,0,0,0,0,0,1]
[3 1,1,1,0,0,0,0,1]
[3 2,0,0,1,0,0,0,1]
[3 0,0,1,1,0,0,0,1]
[3 0,1,0,0,1,0,0,1]
[3 1,0,0,0,0,1,0,1]
[3 0,0,0,0,0,0,0,2]
[3 4,0,0,0,0,0,0,0,1]
[3 1,2,0,0,0,0,0,0,1]
[3 2,0,1,0,0,0,0,0,1]
[3 0,0,2,0,0,0,0,0,1]
[3 0,1,0,1,0,0,0,0,1]
[3 1,0,0,0,1,0,0,0,1]
[3 0,0,0,0,0,0,1,0,1]
[3 2,1,0,0,0,0,0,0,0,1]
[3 0,1,1,0,0,0,0,0,0,1]
[3 1,0,0,1,0,0,0,0,0,1]
[3 0,0,0,0,0,1,0,0,0,1]
[3 3,0,0,0,0,0,0,0,0,0,1]
[3 0,2,0,0,0,0,0,0,0,0,1]
[3 1,0,1,0,0,0,0,0,0,0,1]
[3 0,0,0,0,1,0,0,0,0,0,1]
[3 1,1,0,0,0,0,0,0,0,0,0,1]
[3 0,0,0,1,0,0,0,0,0,0,0,1]
[3 2,0,0,0,0,0,0,0,0,0,0,0,1]
[3 0,0,1,0,0,0,0,0,0,0,0,0,1]
[3 0,1,0,0,0,0,0,0,0,0,0,0,0,1]
[3 1,0,0,0,0,0,0,0,0,0,0,0,0,0,1]
[3 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1]
[2 8,1]
[2 5,3]
[2 2,5]
[2 6,1,1]
[2 3,3,1]
[2 0,5,1]
[2 4,1,2]
[2 1,3,2]
[2 2,1,3]
[2 0,1,4]
[2 7,0,0,1]
[2 4,2,0,1]
[2 1,4,0,1]
[2 5,0,1,1]
[2 2,2,1,1]
[2 3,0,2,1]
[2 0,2,2,1]
[2 1,0,3,1]
[2 3,1,0,2]
[2 0,3,0,2]
[2 1,1,1,2]
[2 2,0,0,3]
[2 0,0,1,3]
[2 5,1,0,0,1]
[2 2,3,0,0,1]
[2 3,1,1,0,1]
[2 0,3,1,0,1]
[2 1,1,2,0,1]
[2 4,0,0,1,1]
[2 1,2,0,1,1]
[2 2,0,1,1,1]
[2 0,0,2,1,1]
[2 0,1,0,2,1]
[2 2,1,0,0,2]
[2 0,1,1,0,2]
[2 1,0,0,1,2]
[2 6,0,0,0,0,1]
[2 3,2,0,0,0,1]
[2 0,4,0,0,0,1]
[2 4,0,1,0,0,1]
[2 1,2,1,0,0,1]
[2 2,0,2,0,0,1]
[2 0,0,3,0,0,1]
[2 2,1,0,1,0,1]
[2 0,1,1,1,0,1]
[2 1,0,0,2,0,1]
[2 3,0,0,0,1,1]
[2 0,2,0,0,1,1]
[2 1,0,1,0,1,1]
[2 0,0,0,0,2,1]
[2 1,1,0,0,0,2]
[2 0,0,0,1,0,2]
[2 4,1,0,0,0,0,1]
[2 1,3,0,0,0,0,1]
[2 2,1,1,0,0,0,1]
[2 0,1,2,0,0,0,1]
[2 3,0,0,1,0,0,1]
[2 0,2,0,1,0,0,1]
[2 1,0,1,1,0,0,1]
[2 1,1,0,0,1,0,1]
[2 0,0,0,1,1,0,1]
[2 2,0,0,0,0,1,1]
[2 0,0,1,0,0,1,1]
[2 0,1,0,0,0,0,2]
[2 5,0,0,0,0,0,0,1]
[2 2,2,0,0,0,0,0,1]
[2 3,0,1,0,0,0,0,1]
[2 0,2,1,0,0,0,0,1]
[2 1,0,2,0,0,0,0,1]
[2 1,1,0,1,0,0,0,1]
[2 0,0,0,2,0,0,0,1]
[2 2,0,0,0,1,0,0,1]
[2 0,0,1,0,1,0,0,1]
[2 0,1,0,0,0,1,0,1]
[2 1,0,0,0,0,0,1,1]
[2 3,1,0,0,0,0,0,0,1]
[2 0,3,0,0,0,0,0,0,1]
[2 1,1,1,0,0,0,0,0,1]
[2 2,0,0,1,0,0,0,0,1]
[2 0,0,1,1,0,0,0,0,1]
[2 0,1,0,0,1,0,0,0,1]
[2 1,0,0,0,0,1,0,0,1]
[2 0,0,0,0,0,0,0,1,1]
[2 4,0,0,0,0,0,0,0,0,1]
[2 1,2,0,0,0,0,0,0,0,1]
[2 2,0,1,0,0,0,0,0,0,1]
[2 0,0,2,0,0,0,0,0,0,1]
[2 0,1,0,1,0,0,0,0,0,1]
[2 1,0,0,0,1,0,0,0,0,1]
[2 0,0,0,0,0,0,1,0,0,1]
[2 2,1,0,0,0,0,0,0,0,0,1]
[2 0,1,1,0,0,0,0,0,0,0,1]
[2 1,0,0,1,0,0,0,0,0,0,1]
[2 0,0,0,0,0,1,0,0,0,0,1]
[2 3,0,0,0,0,0,0,0,0,0,0,1]
[2 0,2,0,0,0,0,0,0,0,0,0,1]
[2 1,0,1,0,0,0,0,0,0,0,0,1]
[2 0,0,0,0,1,0,0,0,0,0,0,1]
[2 1,1,0,0,0,0,0,0,0,0,0,0,1]
[2 0,0,0,1,0,0,0,0,0,0,0,0,1]
[2 2,0,0,0,0,0,0,0,0,0,0,0,0,1]
[2 0,0,1,0,0,0,0,0,0,0,0,0,0,1]
[2 0,1,0,0,0,0,0,0,0,0,0,0,0,0,1]
[2 1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1]
[2 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1]
 * </pre>
 */
