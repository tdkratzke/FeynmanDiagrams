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
	final static long _MillisInterval = 5000L;

	protected final int _nStar;
	private long _OldMillis;

	FeynmanF(final int nStar) {
		_nStar = nStar;
	}

	public long compute() {
		long[] alpha = new long[_nStar];
		Arrays.fill(alpha, 1);
		alpha[_nStar - 2] = 0;
		_OldMillis = System.currentTimeMillis();
		for (int n = _nStar; n > 2; n -= 2) {
			alpha = getBravo(alpha);
			final long currentMillis = System.currentTimeMillis();
			if (_Debug && currentMillis >= _OldMillis + _MillisInterval) {
				System.out.printf("\n%s %d: %s", formatCurrentTime(), n, getString(alpha));
				_OldMillis = currentMillis;
			}
		}
		return alpha[1];
	}

	protected long[] getBravo(final long[] alpha) {
		final int n = alpha.length;
		final long[] bravo = new long[n - 2];
		Arrays.fill(bravo, 0L);
		for (int k = 1; k <= n; ++k) {
			final int nInCycles = n - k;
			if (nInCycles == 1) {
				continue;
			}
			final long multiplier = alpha[k - 1];
			if (k > 2) {
				final int i = k - 3;
				bravo[i] = (bravo[i] + multiplier * (k - 1)) % _Modulo;
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
		return bravo;
	}

	static String formatCurrentTime() {
		final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
		final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		return zonedDateTime.format(_TimeFormatter);
	}

	static String getString(final long[] longArr) {
		if (true) {
			return "";
		}
		final int n = longArr == null ? 0 : longArr.length;
		String s = "[";
		for (int k = 0; k < n; ++k) {
			s += String.format((k == 0 ? "" : ",") + "%d", longArr[k]);
		}
		s += "]";
		return s;
	}

	@SuppressWarnings("unused")
	public static void main(final String[] args) {
		System.out.printf("\n%s", formatCurrentTime());
		for (int nStar = 50000; nStar <= 50000; nStar += 2) {
			final FeynmanF feynmanF = new FeynmanF(nStar);
			final FeynmanF parFeynmanF = new ParFeynmanF(nStar);
			final FeynmanF crudeFeynmanF = new CrudeFeynmanF(nStar);
			final long crudeF = 0; // crudeFeynmanF.compute();
			final long f = feynmanF.compute();
			final long parF = parFeynmanF.compute();
			System.out.printf("\n%s nStar[%d] crudeF[%d] F[%d] parF[%d]\n", //
					formatCurrentTime(), nStar, f, parF, crudeF);
		}
	}
}
