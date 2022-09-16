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
	final static int _LoN = 50000, _HiN = 50000;
	final static boolean _RunCrude = false;
	final static boolean _RunF = false;
	final static boolean _RunPar = true;

	protected final int _nStar;
	protected long _alpha[], _bravo[];

	FeynmanF(final int nStar) {
		_nStar = nStar;
		_alpha = new long[_nStar];
		_bravo = new long[_nStar];
	}

	public long compute() {
		Arrays.fill(_alpha, 1);
		_alpha[_nStar - 2] = 0;
		long oldMillis = System.currentTimeMillis();
		final String startTimeString = formatCurrentTime();
		int oldN = _nStar;
		for (int alphaN = _nStar; alphaN > 2; alphaN -= 2) {
			if (_Debug) {
				final long millis = System.currentTimeMillis();
				if ((millis >= oldMillis + _MillisInterval) || alphaN >= _nStar - 10 || alphaN < 100) {
					final double secs = (millis - oldMillis) / 1000.0;
					final int nDone = oldN - alphaN;
					final double avg = secs > 0d ? (nDone / secs) : 0d;
					oldN = alphaN;
					System.out.printf("%s n[%d], %d done in %.3f seconds (avg=%.3f/sec).\n", //
							formatCurrentTime(), alphaN, nDone, secs, avg);
					oldMillis = System.currentTimeMillis();
				}
			}
			fillInBravo(alphaN);
			final long[] charlie = _alpha;
			_alpha = _bravo;
			_bravo = charlie;
		}
		System.out.printf("\n\nStarted at %s, finished at %s", startTimeString, formatCurrentTime());
		return _alpha[1];
	}

	protected void fillInBravo(final int alphaN) {
		final int bravoN = alphaN - 2;
		Arrays.fill(_bravo, 0, bravoN, 0L);
		for (int k = 1; k <= alphaN; ++k) {
			final int nInCycles = alphaN - k;
			if (nInCycles == 1) {
				continue;
			}
			final long multiplier = _alpha[k - 1];
			if (k > 2) {
				final int i = k - 3;
				_bravo[i] = (_bravo[i] + multiplier * (k - 1)) % _Modulo;
			}
			final int maxCycleLen = alphaN - k;
			for (int cycleLen = 2; cycleLen <= maxCycleLen; ++cycleLen) {
				if (k + cycleLen == alphaN - 1) {
					continue;
				}
				final int i = k + cycleLen - 3;
				_bravo[i] = (_bravo[i] + multiplier) % _Modulo;
			}
		}
	}

	static String formatCurrentTime() {
		final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
		final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		return zonedDateTime.format(_TimeFormatter);
	}

	static String getString(final long[] longArr, final int len) {
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

	@SuppressWarnings("unused")
	public static void main(final String[] args) {
		for (int nStar = _LoN; nStar <= _HiN; nStar += 2) {
			final FeynmanF feynmanF = new FeynmanF(nStar);
			final FeynmanF parFeynmanF = new ParFeynmanF(nStar);
			final FeynmanF crudeFeynmanF = new CrudeFeynmanF(nStar);
			final long crudeF = _RunCrude ? crudeFeynmanF.compute() : 0L;
			final long f = _RunF ? feynmanF.compute() : 0L;
			final long parF = _RunPar ? parFeynmanF.compute() : 0L;
			System.out.printf("\n%s nStar[%d] crudeF[%d] F[%d] parF[%d]\n", //
					formatCurrentTime(), nStar, crudeF, f, parF);
		}
	}
}
