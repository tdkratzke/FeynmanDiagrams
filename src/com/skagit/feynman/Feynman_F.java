package com.skagit.feynman;

import java.util.ArrayList;
import java.util.Arrays;

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
		for (int k = 2; k < n; ++k) {
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
			s += String.format("%s%d", k == 2 ? " " : ",", blueVector[k]);
		}
		s += "]";
		return s;
	}

	/** Creates a blue vector from a succinct string. */
	static int[] stringToBlueVector(final String blueVectorString) {
		final String[] fields = blueVectorString.trim().split("[\\s,\\[\\]]+");
		final int nFields, fieldsArray[];
		{
			final int nFieldsX = fields.length;
			final ArrayList<Integer> fieldsList = new ArrayList<>();
			for (int k = 0; k < nFieldsX; ++k) {
				try {
					fieldsList.add(Integer.parseInt(fields[k]));
				} catch (final NumberFormatException e) {
				}
			}
			nFields = fieldsList.size();
			if (nFields < 1) {
				return null;
			}
			fieldsArray = new int[nFields];
			for (int k = 0; k < nFields; ++k) {
				fieldsArray[k] = fieldsList.get(k);
			}
		}
		int nBlueArcs = fieldsArray[0];
		for (int k = 1; k < nFields; ++k) {
			nBlueArcs += (k + 1) * fieldsArray[k];
		}
		final int[] blueVector = new int[nBlueArcs - 1];
		Arrays.fill(blueVector, 0);
		blueVector[0] = fieldsArray[0];
		System.arraycopy(fieldsArray, 1, blueVector, 2, nFields - 1);
		return blueVector;
	}

	/** The main routine and the testing main. */
	public static int feynman_F(final int n) {
		final int nBlueArcs = n + 1;
		final int[] blueVector = new int[nBlueArcs - 1];
		Arrays.fill(blueVector, 0);
		System.out.printf("n[%d] (nBlueArcs[%d])", n, nBlueArcs);
		for (int runningTotal = 0;;) {
			NextBlueVector.nextBlueVector(blueVector);
			if (blueVector[0] == 0) {
				return runningTotal;
			}
			final BlueGraph blueGraph = new BlueGraph(blueVector);
			final int thisCount = blueGraph.getCount();
			runningTotal += thisCount;
			System.out.printf("\n%s, ThisCount[%d], RunningTotal[%d]", //
					blueVectorToString(blueVector), thisCount, runningTotal);
		}
	}

	public final static void main(final String[] args) {
		final boolean doSingleBlueVector = false;
		if (doSingleBlueVector) {
			final int[] blueVectorString = stringToBlueVector("[2 1,1]");
			final BlueGraph blueGraph = new BlueGraph(blueVectorString);
			final int count = blueGraph.getCount();
			System.out.printf("\ncount[%d] for %s", count, blueVectorToString(blueVectorString));
		} else {
			for (int n = 4; n <= 8; n += 2) {
				feynman_F(n);
				if (n < 8) {
					System.out.printf("\n\n");
				}
			}
		}
	}

	public static void main1(final String[] args) {
		final int[] blueVector = stringToBlueVector("[10 0,1]");
		System.out.printf("\n%s", blueVectorToString(blueVector));
	}

	public static void main2(final String[] args) {
		final int nBlueEdges = 13;
		final int[] blueVector = new int[nBlueEdges - 1];
		Arrays.fill(blueVector, 0);
		for (;;) {
			NextBlueVector.nextBlueVector(blueVector);
			if (blueVector[0] == 0) {
				break;
			}
			System.out.printf("\n%s", blueVectorToString(blueVector));
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
