package com.skagit.feynman;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class FeynmanF {
	final static DateTimeFormatter _TimeFormatter = DateTimeFormatter.ofPattern("MMM-dd hh:mm:ss");

	final static int _Modulo = 1000000007;
	final static boolean _Debug = true;
	final static long _MillisInterval = 5000;

	protected final int _nStar;

	FeynmanF(final int nStar) {
		_nStar = nStar;
	}

	public int compute() {
		long[] alpha = new long[_nStar];
		Arrays.fill(alpha, 1);
		long oldMillis = System.currentTimeMillis();
		for (int n = _nStar; n > 2; n -= 2) {
			final long[] bravo = new long[n - 2];
			Arrays.fill(bravo, 0);
			for (int k = 1; k <= n; ++k) {
				final int nInCycles = n - k;
				if (nInCycles == 1) {
					continue;
				}
				final long multiplier = alpha[k - 1];
				if (k > 2) {
					final int i = k - 3;
					bravo[i] = (bravo[i] + (multiplier * (k - 1))) % _Modulo;
				}
				final int maxCycleLen = n - k;
				for (int cycleLen = 2; cycleLen <= maxCycleLen; ++cycleLen) {
					if (k + cycleLen == n - 1) {
						continue;
					}
					final int i = k + cycleLen - 3;
					bravo[i] = (bravo[i] + multiplier) % _Modulo;
				}
			}
			alpha = bravo;
			final long currentMillis = System.currentTimeMillis();
			if (_Debug && currentMillis >= oldMillis + _MillisInterval) {
				System.out.printf("\n%s %d", formatCurrentTime(), n);
				oldMillis = currentMillis;
			}
		}
		return (int) alpha[1];
	}

	static String longArrToString(final long[] longArr) {
		final int n = longArr == null ? 0 : longArr.length;
		String s = "[";
		for (int k = 0; k < n; ++k) {
			s += String.format((k == 0 ? "" : ",") + "%d", longArr[k]);
		}
		s += "]";
		return s;
	}

	static String formatCurrentTime() {
		final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
		final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		return zonedDateTime.format(_TimeFormatter);
	}

	public static void main(final String[] args) {
		System.out.printf("\n%s", formatCurrentTime());
		for (int nStar = 50000; nStar <= 50000; nStar += 2) {
			final FeynmanF feynmanF = new FeynmanF(nStar);
			final int f = feynmanF.compute();
			System.out.printf("\n%s nStar[%d] feynmanF[%d]", //
					formatCurrentTime(), nStar, f);
		}
	}

}
