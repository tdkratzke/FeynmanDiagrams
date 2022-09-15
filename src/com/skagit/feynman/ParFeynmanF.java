package com.skagit.feynman;

import java.util.Arrays;

public class ParFeynmanF extends FeynmanF {
	final static int _NWorkers = 3;

	ParFeynmanF(final int nStar) {
		super(nStar);
	}

	@Override
	protected long[] getBravo(final long[] alpha) {
		final int n = alpha.length;
		final long[] bravo = new long[n - 2];
		Arrays.fill(bravo, 0L);
		for (int i = 0; i < _NWorkers; ++i) {
			updateBravoSlice(alpha, bravo, i);
		}
		return bravo;
	}

	private void updateBravoSlice(final long[] alpha, final long[] bravo, final int i) {
		final int n = bravo.length;
		for (int ii = i; ii < n; ii += _NWorkers) {
			/** ii == n-2 leaves exactly 1 vertex in the cycles. Can't have that. */
			if (ii == n - 2) {
				continue;
			}
			/** Update each ii for open/open red edges. */
			bravo[ii] = (bravo[ii] + alpha[ii + 2] * (ii + 2)) % _Modulo;
			/**
			 * <pre>
			 * Update each ii for open/cycle red edges.
			 * ii + 1 = (k + 1) + cycleLen - 2.
			 * k + cycleLen - 2 = ii.
			 * cycleLen = ii + 2 - k
			 * Since k < n + 2, cycleLen > ii + 2 - (n + 2) = ii - n or cycleLen >= ii - n + 1.
			 * Since k >= 0, cycleLen <= ii + 2
			 * </pre>
			 */
			for (int cycleLen = Math.max(2, ii - n + 1); cycleLen <= ii + 2L; ++cycleLen) {
				final int k = ii - cycleLen + 2;
				bravo[ii] = (bravo[ii] + alpha[k]) % _Modulo;
			}
		}
	}

}
