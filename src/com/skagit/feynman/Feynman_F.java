package com.skagit.feynman;

import java.util.ArrayList;

/**
 * <pre>
 * This class has statics, most of which are utilitarian.
 * The main static routine is feynman_F(n) and its testing main.
 * </pre>
 */
public class Feynman_F {

	/** Creates a succinct string from a blue vector. */
	static String blueVectorToString(final int[] blueVector) {
		String s = String.format("[%d", blueVector[0]);
		final int n = blueVector.length;
		for (int k = 1; k < n; ++k) {
			boolean gotOne = false;
			for (int kk = k; kk < n; ++kk) {
				if (blueVector[kk] != 0) {
					gotOne = true;
					break;
				}
			}
			if (!gotOne) {
				break;
			}
			s += String.format("%s%d", k == 1 ? " " : ",", blueVector[k]);
		}
		s += "]";
		return s;
	}

	/** Creates a blue vector from a succinct string. */
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
		if (nFields < 1) {
			return null;
		}
		int maxCycleLength = 0;
		for (int k = 1; k < nFields; ++k) {
			final int nCycles = fieldsList.get(k);
			if (nCycles > 0) {
				final int cycleLength = k + 1;
				maxCycleLength = cycleLength;
			}
		}
		if (maxCycleLength == 0) {
			return new int[] {
					fieldsList.get(0)
			};
		}
		final int[] blueVector = new int[maxCycleLength];
		for (int k = 0; k < maxCycleLength; ++k) {
			blueVector[k] = fieldsList.get(k);
		}
		return blueVector;
	}

	/** The main routine and the testing main. */
	public static long feynman_F(final int n, final int modValue, final boolean dumpResults) {
		final int nBlueArcs = n + 1;
		int[] blueVector = new int[] {
				nBlueArcs
		};
		System.out.printf("n[%d] (nBlueArcs[%d])", n, nBlueArcs);
		long runningTotal = 0L;
		for (; blueVector != null; blueVector = NextBlueVector.nextBlueVector(blueVector)) {
			final BlueGraph blueGraph = new BlueGraph(blueVector, modValue);
			final long thisCount = blueGraph.getCount();
			runningTotal += thisCount;
			if (dumpResults) {
				System.out.printf("\n%s, ThisCount[%d], RunningTotal[%d]", //
						blueVectorToString(blueVector), thisCount, runningTotal);
			}
		}
		return runningTotal;
	}

	public final static void main(final String[] args) {
		final boolean doSingleBlueVector = false;
		if (doSingleBlueVector) {
			final int[] blueVectorString = stringToBlueVector("[2 1,1]");
			final BlueGraph blueGraph = new BlueGraph(blueVectorString);
			final long count = blueGraph.getCount();
			System.out.printf("\ncount[%d] for %s", count, blueVectorToString(blueVectorString));
		} else {
			for (int n = 20; n <= 40; n += 4) {
				final long feynmanN = feynman_F(n, /* modValue= */1000000007, /* dumpResults= */false);
				System.out.printf("\nn[%d] feynmanN[%d]", n, feynmanN);
				System.out.printf("\n\n");
			}
		}
	}
}

/**
 * <pre>
n[4] (nBlueArcs[5])
[5], ThisCount[3], RunningTotal[3]
[3 1], ThisCount[1], RunningTotal[4]
[2 0,1], ThisCount[1], RunningTotal[5]

n[6] (nBlueArcs[7])
[7], ThisCount[15], RunningTotal[15]
[5 1], ThisCount[6], RunningTotal[21]
[4 0,1], ThisCount[5], RunningTotal[26]
[3 2], ThisCount[1], RunningTotal[27]
[3 0,0,1], ThisCount[3], RunningTotal[30]
[2 1,1], ThisCount[2], RunningTotal[32]
[2 0,0,0,1], ThisCount[3], RunningTotal[35]

n[8] (nBlueArcs[9])
[9], ThisCount[105], RunningTotal[105]
[7 1], ThisCount[45], RunningTotal[150]
[6 0,1], ThisCount[35], RunningTotal[185]
[5 2], ThisCount[9], RunningTotal[194]
[5 0,0,1], ThisCount[24], RunningTotal[218]
[4 1,1], ThisCount[15], RunningTotal[233]
[4 0,0,0,1], ThisCount[21], RunningTotal[254]
[3 3], ThisCount[1], RunningTotal[255]
[3 0,2], ThisCount[5], RunningTotal[260]
[3 1,0,1], ThisCount[9], RunningTotal[269]
[3 0,0,0,0,1], ThisCount[15], RunningTotal[284]
[2 2,1], ThisCount[3], RunningTotal[287]
[2 0,1,1], ThisCount[8], RunningTotal[295]
[2 1,0,0,1], ThisCount[9], RunningTotal[304]
[2 0,0,0,0,0,1], ThisCount[15], RunningTotal[319] *
 * </pre>
 */
