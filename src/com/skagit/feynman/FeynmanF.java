package com.skagit.feynman;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class FeynmanF {
	final static DateTimeFormatter _TimeFormatter = DateTimeFormatter.ofPattern("MMM-dd hh:mm:ss");

	final static long _Modulo = 1000000007;
	final static boolean _Debug = true;
	final static long _MillisInterval = 10000L;
	// final static int _LoN = 4, _HiN = 26;
	final static int _LoN = 2000, _HiN = 2004;
	final static boolean _RunCrude = false;
	final static boolean _RunF = true;
	final static boolean _RunPar = true;

	public long compute(final int nStar) {
		long[] alpha = new long[nStar];
		long[] bravo = new long[nStar];
		Arrays.fill(alpha, 1);
		alpha[nStar - 2] = 0;
		long oldMillis = System.currentTimeMillis();
		final String startTimeString = formatCurrentTime();
		int oldN = nStar;
		for (int alphaN = nStar; alphaN > 2; alphaN -= 2) {
			if (_Debug) {
				final long millis = System.currentTimeMillis();
				if ((millis >= oldMillis + _MillisInterval) || alphaN >= nStar - 10 || alphaN < 100) {
					final double secs = (millis - oldMillis) / 1000.0;
					final int nDone = oldN - alphaN;
					final double avg = secs > 0d ? (nDone / secs) : 0d;
					oldN = alphaN;
					System.out.printf("%s n[%d], %d done in %.3f seconds (avg=%.3f/sec).\n", //
							formatCurrentTime(), alphaN, nDone, secs, avg);
					oldMillis = System.currentTimeMillis();
				}
			}
			fillInBravo(alphaN, alpha, bravo);
			final long[] charlie = alpha;
			alpha = bravo;
			bravo = charlie;
		}
		System.out.printf("\n\nStarted at %s, finished at %s", startTimeString, formatCurrentTime());
		return alpha[1];
	}

	protected void fillInBravo(final int alphaN, final long[] alpha, final long[] bravo) {
		final int bravoN = alphaN - 2;
		Arrays.fill(bravo, 0, bravoN, 0L);
		for (int k = 1; k <= alphaN; ++k) {
			final int nInCycles = alphaN - k;
			if (nInCycles == 1) {
				continue;
			}
			final long multiplier = alpha[k - 1];
			if (k > 2) {
				final int i = k - 3;
				bravo[i] = (bravo[i] + multiplier * (k - 1)) % _Modulo;
			}
			final int maxCycleLen = alphaN - k;
			for (int cycleLen = 2; cycleLen <= maxCycleLen; ++cycleLen) {
				if (k + cycleLen == alphaN - 1) {
					continue;
				}
				final int i = k + cycleLen - 3;
				bravo[i] = (bravo[i] + multiplier) % _Modulo;
			}
		}
	}

	static String formatCurrentTime() {
		final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
		final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		return zonedDateTime.format(_TimeFormatter);
	}

	static String getLongArrString(final long[] longArr, final int len) {
		if (true) {
			return "";
		}
		String s = "[";
		for (int k = 0; k < len; ++k) {
			s += String.format((k == 0 ? "" : ",") + "%d", longArr[k]);
		}
		s += "]";
		return s;
	}

	public void shutDown() {
	}

	@SuppressWarnings("unused")
	public static void main(final String[] args) {
		final FeynmanF crudeFeynmanF = _RunCrude ? new CrudeFeynmanF() : null;
		final FeynmanF feynmanF = _RunF ? new FeynmanF() : null;
		final FeynmanF parFeynmanF = _RunPar ? new ParFeynmanF() : null;
		for (int nStar = _LoN; nStar <= _HiN; nStar += 2) {
			final long crudeF = _RunCrude ? crudeFeynmanF.compute(nStar) : 0L;
			final long f = _RunF ? feynmanF.compute(nStar) : 0L;
			final long parF = _RunPar ? parFeynmanF.compute(nStar) : 0L;
			System.out.printf("\n%s nStar[%d] crudeF[%d] F[%d] parF[%d]\n", //
					formatCurrentTime(), nStar, crudeF, f, parF);
		}
		if (_RunCrude) {
			crudeFeynmanF.shutDown();
		}
		if (_RunF) {
			feynmanF.shutDown();
		}
		if (_RunPar) {
			parFeynmanF.shutDown();
		}
	}
}
