package com.skagit.feynman;

import java.util.Arrays;

public class ParFeynmanF extends FeynmanF {
	final static int _NWorkers = 3;

	ParFeynmanF(final int nStar) {
		super(nStar);
	}

	@Override
	public int compute() {
		int[] alpha = new int[_nStar];
		Arrays.fill(alpha, 1);
		long oldMillis = System.currentTimeMillis();
		for (int n = _nStar; n > 2; n -= 2) {
			alpha = getBravo(alpha, _NWorkers);
			final long currentMillis = System.currentTimeMillis();
			if (_Debug && currentMillis >= oldMillis + _MillisInterval) {
				System.out.printf("\n%s %d", formatCurrentTime(), n);
				oldMillis = currentMillis;
			}
		}
		return alpha[1];
	}

	private int[] getBravo(final int[] alpha, final int nWorkers) {
		final int n = alpha.length;
		final int[] bravo = new int[n - 2];
		Arrays.fill(bravo, 0);
		for (int iWorker = 0; iWorker < nWorkers; ++iWorker) {
			updateBravoSlice(alpha, bravo, nWorkers, iWorker);
		}
		return bravo;
	}

	private void updateBravoSlice(final int[] alpha, final int[] bravo, final int nWorkers, final int i) {
		final int n = alpha.length;
		for (int ii = i; ii < n - 2; ii += nWorkers) {
			/** Update each ii for open/open red edges. */
			if (ii != n - 4) {
				bravo[ii] = bravo[ii] + alpha[ii + 2] * (ii + 2);
			}
			/**
			 * <pre>
			 * Update each ii for open/cycle red edges.
			 * k + cycleLen - 2 = ii.
			 * cycleLen = ii + 2 - k
			 * Since k <= n, cycleLen >= ii + 2 - (n - 1) = ii + 3 - n
			 * Since k >= 0, cycleLen <= ii + 2
			 * </pre>
			 */
			for (int cycleLen = Math.max(2, ii + 3 - n); cycleLen <= ii + 2; ++cycleLen) {
				if (ii + cycleLen == n - 2) {
					continue;
				}
				final int k = ii - cycleLen + 2;
				if (k == n - 2) {
					continue;
				}
				bravo[ii] = bravo[ii] + alpha[k];
			}
		}
	}

	public static void main(final String[] args) {
		System.out.printf("\n%s", formatCurrentTime());
		for (int nStar = 4; nStar <= 8; nStar += 2) {
			final FeynmanF feynmanF = new FeynmanF(nStar);
			final ParFeynmanF parFeynmanF = new ParFeynmanF(nStar);
			final int f = feynmanF.compute();
			final int parF = parFeynmanF.compute();
			System.out.printf("\n%s nStar[%d] feynmanF[%d] parFeynmanF[%d]", //
					formatCurrentTime(), nStar, f, parF);
		}
	}

}
