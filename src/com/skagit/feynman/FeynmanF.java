package com.skagit.feynman;

import java.util.Arrays;

public class FeynmanF {
	final static long _Modulo = 1000000007L;

	public static long computeFeynmanF(final int nStar) {
		long[] alpha = new long[nStar];
		Arrays.fill(alpha, 1L);
		alpha[nStar - 2] = 0L;

		for (int alphaN = nStar; alphaN > 2; alphaN -= 2) {
			final int bravoN = alphaN - 2;
			final long[] bravo = new long[bravoN];
			long cum = 0L;
			for (int i = 0; i < bravoN; ++i) {
				cum = (cum + alpha[i]) % _Modulo;
				if (i == bravoN - 2) {
					bravo[i] = 0L;
					continue;
				}
				bravo[i] = (alpha[i + 2] * (i + 2L) + cum) % _Modulo;
			}
			alpha = bravo;
		}
		return alpha[1];
	}

	public static void main(final String[] args) {
		final int nStar = 50000;
		final long f = computeFeynmanF(nStar);
		System.out.printf("nStar[%d] f[%d]", nStar, f);
	}
}
