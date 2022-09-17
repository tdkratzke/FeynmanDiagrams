package com.skagit.feynman;

import java.util.Arrays;

public class FeynmanF {
	final static long _Modulo = 1000000007L;

	public long compute(final int nStar) {
		long[] alpha = new long[nStar];
		Arrays.fill(alpha, 1);
		alpha[nStar - 2] = 0;

		for (int alphaN = nStar; alphaN > 2; alphaN -= 2) {
			final int bravoN = alphaN - 2;
			final long[] bravo = new long[bravoN];
			long cum = 0;
			for (int i = 0; i < bravoN; ++i) {
				cum = (cum + alpha[i]) % _Modulo;
				if (i == bravoN - 2) {
					bravo[i] = 0L;
					continue;
				}
				bravo[i] = (alpha[i + 2] * (i + 2) + cum) % _Modulo;
			}
			alpha = bravo;
		}
		return alpha[1];
	}

	public static void main(final String[] args) {
		final FeynmanF feynmanF = new FeynmanF();
		final int nStar = 50000;
		final long f = feynmanF.compute(nStar);
		System.out.printf("nStar[%d] f[%d]", nStar, f);
	}
}
