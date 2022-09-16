package com.skagit.feynman;

public class ParFeynmanF extends FeynmanF {
	final static int _NWorkers = 3;

	ParFeynmanF(final int nStar) {
		super(nStar);
	}

	@Override
	protected void fillInBravo(final int alphaN) {
		for (int i = 0; i < _NWorkers; ++i) {
			updateBravoSlice(alphaN, i);
		}
	}

	private void updateBravoSlice(final int alphaN, final int i) {
		final int bravoN = alphaN - 2;
		for (int ii = i; ii < bravoN; ii += _NWorkers) {
			/** ii == bravoN - 2 leaves exactly 1 vertex in the cycles. Can't have that. */
			if (ii == bravoN - 2) {
				_bravo[ii] = 0L;
				continue;
			}
			/**
			 * Update each ii for open/open red edges. Note that we're initializing
			 * _bravo[ii] here.
			 */
			_bravo[ii] = (_alpha[ii + 2] * (ii + 2)) % _Modulo;
			/**
			 * <pre>
			 * Update each ii for open/cycle red edges.
			 * ii + 1 = (k + 1) + cycleLen - 2.
			 * k + cycleLen - 2 = ii.
			 * cycleLen = ii + 2 - k
			 * Since k < bravoN + 2,
			 *   cycleLen > ii + 2 - (bravoN + 2) = ii - n or cycleLen >= ii - n + 1.
			 * Since k >= 0, cycleLen <= ii + 2
			 * </pre>
			 */
			for (int cycleLen = Math.max(2, ii - bravoN + 1); cycleLen <= ii + 2L; ++cycleLen) {
				final int k = ii - cycleLen + 2;
				_bravo[ii] = (_bravo[ii] + _alpha[k]) % _Modulo;
			}
		}
	}

}
