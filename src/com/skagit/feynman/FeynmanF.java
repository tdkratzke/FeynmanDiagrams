package com.skagit.feynman;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class FeynmanF {
	final static DateTimeFormatter _TimeFormatter = DateTimeFormatter.ofPattern("MMM-dd hh:mm:ss");

	final static int _Modulo = 1000000007;
	final static boolean _Debug = false;
	final static long _MillisInterval = -1L;

	protected final int _nStar;
	private long _OldMillis;

	FeynmanF(final int nStar) {
		_nStar = nStar;
	}

	public int compute() {
		int[] alpha = new int[_nStar];
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

	protected int[] getBravo(final int[] alpha) {
		final int n = alpha.length;
		final int[] bravo = new int[n - 2];
		Arrays.fill(bravo, 0);
		for (int k = 1; k <= n; ++k) {
			final int nInCycles = n - k;
			if (nInCycles == 1) {
				continue;
			}
			final long multiplier = alpha[k - 1];
			if (k > 2) {
				final int i = k - 3;
				bravo[i] = accumulate(bravo[i], multiplier, k - 1);
			}
			final int maxCycleLen = n - k;
			for (int cycleLen = 2; cycleLen <= maxCycleLen; ++cycleLen) {
				if (k + cycleLen == n - 1) {
					continue;
				}
				final int i = k + cycleLen - 3;
				bravo[i] = accumulate(bravo[i], 1, multiplier);
			}
		}
		return bravo;
	}

	static String formatCurrentTime() {
		final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
		final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		return zonedDateTime.format(_TimeFormatter);
	}

	static String getString(final int[] intArr) {
		final int n = intArr == null ? 0 : intArr.length;
		String s = "[";
		for (int k = 0; k < n; ++k) {
			s += String.format((k == 0 ? "" : ",") + "%d", intArr[k]);
		}
		s += "]";
		return s;
	}

	/** Computes a + b*c. */
	static int accumulate(long a, long b, long c) {
		if (_Modulo < 2) {
			return (int) (a + b * c);
		}
		a %= _Modulo;
		b %= _Modulo;
		c %= _Modulo;
		final long bc = (b * c) % _Modulo;
		final long abc = (a + bc) % _Modulo;
		return (int) (abc % _Modulo);
	}

	public static void main(final String[] args) {
		System.out.printf("\n%s", formatCurrentTime());
		for (int nStar = 4; nStar <= 30; nStar += 2) {
			final FeynmanF feynmanF = new FeynmanF(nStar);
			final FeynmanF parFeynmanF = new ParFeynmanF(nStar);
			final FeynmanF crudeFeynmanF = new CrudeFeynmanF(nStar);
			final int f = feynmanF.compute();
			final int parF = parFeynmanF.compute();
			final int crudeF = crudeFeynmanF.compute();
			System.out.printf("\n%s nStar[%d] F[%d] parF[%d] crudeF[%d]\n", //
					formatCurrentTime(), nStar, f, parF, crudeF);
		}
	}
}

/**
 * <pre>

Sep-15 07:55:42
Sep-15 07:55:42 4: [0,5]
Sep-15 07:55:42 nStar[4] feynmanF[5]

Sep-15 07:55:42 6: [3,5,0,9]
Sep-15 07:55:42 4: [0,35]
Sep-15 07:55:42 nStar[6] feynmanF[35]

Sep-15 07:55:42 8: [3,5,7,9,0,13]
Sep-15 07:55:42 6: [17,35,0,89]
Sep-15 07:55:42 4: [0,319]
Sep-15 07:55:42 nStar[8] feynmanF[319]
 * </pre>
 */