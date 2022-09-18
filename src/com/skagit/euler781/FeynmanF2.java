package com.skagit.euler781;

public class FeynmanF2 extends FeynmanF1 {

	@Override
	protected void fillInBravo(final int alphaN, final long[] alpha, final long[] bravo) {
		final int bravoN = alphaN - 2;
		long cum = 0;
		for (int i = 0; i < bravoN; ++i) {
			cum = (cum + alpha[i]) % _Modulo;
			bravo[i] = (int) ((alpha[i + 2] * (i + 2L) + cum) % _Modulo);
		}
	}
}
