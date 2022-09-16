package com.skagit.feynman;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ParFeynmanF extends FeynmanF {
	final private static int _MinNPerSlice = 100;
	final private FeynmanThreadPool _threadPool;

	ParFeynmanF(final int nStar) {
		super(nStar);
		_threadPool = new FeynmanThreadPool();
	}

	@Override
	protected void fillInBravo(final int alphaN) {
		final int bravoN = alphaN - 2;
		Future<?>[] futures = null;
		final ArrayList<Integer> notTaskedISlices = new ArrayList<>();
		int nSlices = (bravoN + (_MinNPerSlice - 1)) / _MinNPerSlice;
		int nWorkers = 0;
		if (nSlices > 1) {
			final int nFreeWorkers = _threadPool.getNFreeWorkerThreads("Standard");
			nWorkers = Math.max(0, Math.min(nSlices - 1, nFreeWorkers));
			if (nWorkers < 2) {
				nWorkers = 0;
				nSlices = 1;
			} else {
				final int finalNSlices = nSlices = nWorkers + 1;
				futures = new Future<?>[nWorkers];
				for (int i = 0; i < nWorkers; ++i) {
					final int finalI = i;
					final Runnable runnable = new Runnable() {
						@Override
						public void run() {
							/** ... to here. */
							updateBravoSlice(alphaN, finalI, finalNSlices);
						}
					};
					futures[i] = _threadPool.submitToWorkers(runnable);
					if (futures[i] == null) {
						notTaskedISlices.add(i);
					}
				}
			}
		}
		for (int iSlice = nWorkers; iSlice < nSlices; ++iSlice) {
			notTaskedISlices.add(iSlice);
		}
		for (final int iSlice : notTaskedISlices) {
			updateBravoSlice(alphaN, iSlice, nSlices);
		}
		try {
			for (int iWorker = 0; iWorker < nWorkers; ++iWorker) {
				if (futures[iWorker] != null) {
					futures[iWorker].get();
				}
			}
		} catch (final ExecutionException e) {
		} catch (final InterruptedException e) {
		}
	}

	private void updateBravoSlice(final int alphaN, final int i, final int nSlices) {
		final int bravoN = alphaN - 2;
		for (int ii = i; ii < bravoN; ii += nSlices) {
			/** ii == bravoN - 2 leaves exactly 1 vertex in the cycles. Can't have that. */
			if (ii == bravoN - 2) {
				_bravo[ii] = 0L;
				continue;
			}
			/**
			 * Update each ii for open/open red edges. Note that we initialize _bravo[ii]
			 * here.
			 */
			_bravo[ii] = (_alpha[ii + 2] * (ii + 2)) % _Modulo;
			/**
			 * <pre>
			 * Update each ii for open/cycle red edges.
			 * ii + 1 = (k + 1) + cycleLen - 2.
			 * cycleLen = ii + 2 - k
			 * Since k < bravoN + 2,
			 *   cycleLen > ii + 2 - (bravoN + 2) = ii - bravoN or
			 *   cycleLen >= ii - bravoN + 1.
			 * Since k >= 0, cycleLen <= ii + 2.
			 * Finally, note that given cycleLen, k = ii + 2 - cycleLen.
			 * </pre>
			 */
			final int loCycleLen = Math.max(2, ii - bravoN + 1);
			final int hiCycleLen = ii + 2;
			for (int cycleLen = loCycleLen; cycleLen <= hiCycleLen; ++cycleLen) {
				final int k = ii + 2 - cycleLen;
				_bravo[ii] = (_bravo[ii] + _alpha[k]) % _Modulo;
			}
		}
	}

	protected void shutDown() {
		_threadPool.shutDown();
	}

}
