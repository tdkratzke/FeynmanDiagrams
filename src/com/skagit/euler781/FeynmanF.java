package com.skagit.euler781;

import java.util.Arrays;

public class FeynmanF {
	final static int _Modulo = 1000000007;

	public static long computeFeynmanF(final int nStar) {
		int[] alpha = new int[nStar], bravo = new int[nStar], charlie;
		Arrays.fill(alpha, 1);

		for (int alphaN = nStar; alphaN > 2; alphaN -= 2) {
			final int bravoN = alphaN - 2;
			long cum = 0L;
			for (int i = 0; i < bravoN; ++i) {
				cum = (cum + alpha[i]) % _Modulo;
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
