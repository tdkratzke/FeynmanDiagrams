package com.skagit.feynman;

public class FeynmanF2 extends FeynmanF1 {

	@Override
	protected void fillInBravo(final int alphaN, final long[] alpha, final long[] bravo) {
		final int bravoN = alphaN - 2;
		long cum = 0;
		for (int i = 0; i < bravoN; ++i) {
			cum = (cum + alpha[i]) % _Modulo;
			if (i == bravoN - 2) {
				bravo[i] = 0L;
				continue;
			}
			bravo[i] = (alpha[i + 2] * (i + 2) + cum) % _Modulo;
			bravo[i] = (bravo[i] + cum) % _Modulo;
		}
		return;
	}

}
