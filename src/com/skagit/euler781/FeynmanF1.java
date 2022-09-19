package com.skagit.euler781;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import com.skagit.util.GetStrings;

public class FeynmanF1 {

	final static long _Modulo = 1000000007;
	final static boolean _Debug = false;
	final static File _DebugFile = new File("Feynman.txt");
	final static long _MillisInterval = 10000L;
	final static int _LoN = 2, _HiN = 26;
	final static boolean _Run0 = true;
	final static boolean _Run1 = true;
	final static boolean _Run2 = true;

	public long compute(final int nStar) {
		final long[] alpha = new long[nStar];
		final long[] bravo = new long[nStar];
		Arrays.fill(alpha, 1);
		alpha[nStar - 2] = 0;
		try (PrintStream debugPs = new PrintStream(_DebugFile)) {
			return hammer(nStar, alpha, bravo, debugPs);
		} catch (final IOException e) {
		}
		return hammer(nStar, alpha, bravo, /* debugPrintStream= */null);
	}

	private long hammer(final int nStar, long[] alpha, long bravo[], final PrintStream debugPs) {
		final String startTimeString = GetStrings.getCurrentTimeString();
		long oldMillis = System.currentTimeMillis();
		int oldN = nStar;
		for (int alphaN = nStar; alphaN > 2; alphaN -= 2) {
			if (_Debug) {
				final long millis = System.currentTimeMillis();
				if ((millis >= oldMillis + _MillisInterval) || alphaN >= nStar - 10 || alphaN < 100) {
					final double secs = (millis - oldMillis) / 1000.0;
					final int nDone = oldN - alphaN;
					final double avg = secs > 0d ? (nDone / secs) : 0d;
					oldN = alphaN;
					final String s = String.format("%s n[%d], %d done in %.3f seconds (avg=%.3f/sec).\n", //
							GetStrings.getCurrentTimeString(), alphaN, nDone, secs, avg);
					System.out.print(s);
					if (debugPs != null) {
						debugPs.print(s);
					}
					oldMillis = System.currentTimeMillis();
				}
			}
			fillInBravo(alphaN, alpha, bravo);
			final long[] charlie = alpha;
			alpha = bravo;
			bravo = charlie;
		}
		final long f = alpha[1];
		final String s = String.format("\n\nStarted at %s, finished at %s, f[%d].", //
				startTimeString, GetStrings.getCurrentTimeString(), f);
		System.out.print(s);
		if (debugPs != null) {
			debugPs.print(s);
		}
		return f;
	}

	protected void fillInBravo(final int alphaN, final long[] alpha, final long[] bravo) {
		final int bravoN = alphaN - 2;
		Arrays.fill(bravo, 0, bravoN, 0L);
		for (int k = 1; k <= alphaN; ++k) {
			final int nInCycles = alphaN - k;
			if (nInCycles == 1) {
				continue;
			}
			final long oldAlphaValue = alpha[k - 1];
			if (k > 2) {
				final int i = k - 3;
				bravo[i] = (bravo[i] + oldAlphaValue * (k - 1)) % _Modulo;
			}
			final int maxCycleLen = alphaN - k;
			for (int cycleLen = 2; cycleLen <= maxCycleLen; ++cycleLen) {
				if (k + cycleLen == alphaN - 1) {
					continue;
				}
				final int i = k + cycleLen - 3;
				bravo[i] = (bravo[i] + oldAlphaValue) % _Modulo;
			}
		}
	}

	public static void main(final String[] args) {
		final FeynmanF1 feynmanF0 = _Run0 ? new FeynmanF0() : null;
		final FeynmanF1 feynmanF1 = _Run1 ? new FeynmanF1() : null;
		final FeynmanF1 feynmanF2 = _Run2 ? new FeynmanF2() : null;
		for (int nStar = _LoN; nStar <= _HiN; nStar += 2) {
			final long f0 = _Run0 ? feynmanF0.compute(nStar) : 0L;
			final long f1 = _Run1 ? feynmanF1.compute(nStar) : 0L;
			final long f2 = _Run2 ? feynmanF2.compute(nStar) : 0L;
			System.out.printf("\n%s nStar[%d] f0[%d] f1[%d] f2[%d]\n", //
					GetStrings.getCurrentTimeString(), nStar, f0, f1, f2);
		}
	}
}
