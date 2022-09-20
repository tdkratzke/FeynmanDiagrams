package com.skagit.euler.euler0781;

import java.util.Arrays;

public class Euler0781 {
	final static int _Modulo = 1000000007;

	public static long feynmanF(final int nStar) {
		int[] alpha = new int[nStar];
		Arrays.fill(alpha, 1);

		for (int alphaN = nStar; alphaN > 2; alphaN -= 2) {
			final int bravoN = alphaN - 2;
			final int[] bravo = new int[bravoN];
			int cum = 0;
			for (int i = 0; i < bravoN; ++i) {
				cum = (cum + alpha[i]) % _Modulo;
				bravo[i] = (int) ((alpha[i + 2] * (i + 2L) + cum) % _Modulo);
			}
			alpha = bravo;
		}
		return alpha[1];
	}

	public static void main(final String[] args) {
		final int nStar = 50000;
		final long millis = System.currentTimeMillis();
		System.out.printf("nStar[%d] f[%d]", nStar, feynmanF(nStar));
		System.out.printf(".\tTook %d millis.", System.currentTimeMillis() - millis);
	}
}
