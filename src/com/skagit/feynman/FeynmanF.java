package com.skagit.feynman;

import java.util.Arrays;

public class FeynmanF {
	final static int _Modulo = 1000000007;

	public static long computeFeynmanF(final int nStar) {
		int[] alpha = new int[nStar], bravo = new int[nStar], charlie;
		Arrays.fill(alpha, 1);
		alpha[nStar - 2] = 0;

		for (int alphaN = nStar; alphaN > 2; alphaN -= 2) {
			final int bravoN = alphaN - 2;
			int cum = 0;
			for (int i = 0; i < bravoN; ++i) {
				cum = (cum + alpha[i]) % _Modulo;
				if (i == bravoN - 2) {
					bravo[i] = 0;
					continue;
				}
				bravo[i] = (int) ((alpha[i + 2] * (i + 2L) + cum) % _Modulo);
			}
			charlie = alpha;
			alpha = bravo;
			bravo = charlie;
		}
		return alpha[1];
	}

	public static void main(final String[] args) {
		final int nStar = 50000;
		System.out.printf("nStar[%d] f[%d]", nStar, computeFeynmanF(nStar));
	}
}
