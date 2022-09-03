package com.skagit.feynman;

import java.util.Arrays;

public class FeynmanF2 extends FeynmanF {

	FeynmanF2(final int n, final int modValue, final boolean trackBlueVectorSets, final boolean dumpResults) {
		super(n, modValue, trackBlueVectorSets, dumpResults);
	}

	@Override
	public void compute() {
		final int n = _nBlueArcs - 1;
		if (_dumpResults) {
			System.out.printf("\nn[%d] (nBlueArcs[%d])", n, _nBlueArcs);
		}
		int[] counts = new int[n + 1];
		Arrays.fill(counts, 1);
		counts[0] = counts[n - 1] = 0;
		int nNodes = n;
		for (; nNodes > 2; nNodes -= 2) {
			if (_dumpResults) {
				System.out.printf("\n%d: %s", nNodes, getString(counts));
			}
			final int[] countsX = new int[nNodes - 2 + 1];
			Arrays.fill(countsX, 0);
			for (int nOpen = 1; nOpen <= nNodes; ++nOpen) {
				final int nInCycles = nNodes - nOpen;
				if (nInCycles == 1) {
					continue;
				}
				if (nOpen > 2) {
					final int nOpenX = nOpen - 2;
					countsX[nOpenX] = accumulate(countsX[nOpenX], counts[nOpen], nOpen - 1);
				}
				final int maxCycleLen = nNodes - nOpen;
				for (int targetCycle = 2; targetCycle <= maxCycleLen; ++targetCycle) {
					if (nOpen + targetCycle == nNodes - 1) {
						continue;
					}
					final int nOpenX = nOpen + targetCycle - 2;
					countsX[nOpenX] = accumulate(countsX[nOpenX], 1, counts[nOpen]);
				}
			}
			counts = countsX;
		}
		if (_dumpResults) {
			System.out.printf("\n%d: %s", nNodes, getString(counts));
		}
		_feynmanF = counts[2];
	}

	public final static void main(final String[] args) {
		for (int k = 2; k <= 26; k += 2) {
			final FeynmanF feynmanF = new FeynmanF(k, 1000000007, false, false);
			final FeynmanF2 feynmanF2 = new FeynmanF2(k, 1000000007, false, false);
			feynmanF.compute();
			feynmanF2.compute();
			final int f = feynmanF._feynmanF;
			final int f2 = feynmanF2._feynmanF;
			System.out.printf("\nk[%d] feynmanF[%d] feynmanF2[%d]", //
					k, f, f2);
			if (f != f2) {
				System.out.printf("\n\nDiscrepency!: n[%d] feynmanF[%d] feynmanF2[%d]", //
						k, f, f2);
			}
		}
	}

}
