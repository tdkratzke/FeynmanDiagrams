package com.skagit.feynman;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ParFeynmanF extends FeynmanF {
	final private static int _MinNPerSlice = 100;
	final private static boolean _UsePar = true;

	final private FeynmanThreadPool _threadPool;

	public ParFeynmanF() {
		_threadPool = _UsePar ? new FeynmanThreadPool() : null;
	}

	@Override
	protected void fillInBravo(final int alphaN, final long[] alpha, final long[] bravo) {
		final int bravoN = alphaN - 2;
		if (!_UsePar) {
			for (int i = 0; i < bravoN; ++i) {
				if (i == bravoN - 2) {
					bravo[i] = 0L;
					continue;
				}
				bravo[i] = (alpha[i + 2] * (i + 2)) % _Modulo;
				for (int k = 0; k <= i; ++k) {
					bravo[i] = (bravo[i] + alpha[k]) % _Modulo;
				}
			}
			return;
		}
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
							updateBravoSlice(alphaN, alpha, bravo, finalI, finalNSlices);
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
			updateBravoSlice(alphaN, alpha, bravo, iSlice, nSlices);
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

	private void updateBravoSlice(final int alphaN, final long[] alpha, final long[] bravo, final int i,
			final int nSlices) {
		final int bravoN = alphaN - 2;
		for (int ii = i; ii < bravoN; ii += nSlices) {
			/** ii == bravoN - 2 leaves exactly 1 vertex in the cycles. Can't have that. */
			if (ii == bravoN - 2) {
				bravo[ii] = 0L;
				continue;
			}
			/**
			 * Update each ii for open/open red edges. Note that we initialize _bravo[ii]
			 * here.
			 */
			bravo[ii] = (alpha[ii + 2] * (ii + 2)) % _Modulo;
			/**
			 * For small k, you can grab a cycle of appropriate size to leave ii + 1 open.
			 */
			for (int k = 0; k <= ii; ++k) {
				bravo[ii] = (bravo[ii] + alpha[k]) % _Modulo;
			}
		}
	}

	@Override
	public void shutDown() {
		if (_UsePar) {
			_threadPool.shutDown();
		}
	}

}
