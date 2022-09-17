package com.skagit.feynman;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class FeynmanF_NSq {
	final static DateTimeFormatter _TimeFormatter = DateTimeFormatter.ofPattern("MMM-dd hh:mm:ss");

	final static long _Modulo = 1000000007L;
	final static int _LoN = 2, _HiN = 50;
	final static long x = _Modulo * _LoN;
	final static int y = Integer.MAX_VALUE;
	final static long z = Long.MAX_VALUE;

	public long compute(final int nStar) {
		long[] alpha = new long[nStar];
		Arrays.fill(alpha, 1);
		alpha[nStar - 2] = 0;
		final long startTimeMs = System.currentTimeMillis();
		long oldMillis = startTimeMs;
		int oldN = nStar;

		int nPrinted = 0;
		for (int alphaN = nStar; alphaN > 2; alphaN -= 2) {
			final long millis = System.currentTimeMillis();
			if (millis > oldMillis || alphaN >= nStar - 10 || alphaN < 100) {
				final double secs = (millis - oldMillis) / 1000.0;
				final int nDone = oldN - alphaN;
				final double avg = secs > 0d ? (nDone / secs) : 0d;
				oldN = alphaN;
				System.out.printf("%s%s n[%d], %d done in %.3f seconds (avg=%.3f/sec).", //
						nPrinted > 0 ? "\n" : "", formatTimeMs(millis), alphaN, nDone, secs, avg);
				oldMillis = millis;
				++nPrinted;
			}

			final int bravoN = alphaN - 2;
			final long[] bravo = new long[bravoN];
			long cum = 0;
			for (int i = 0; i < bravoN; ++i) {
				cum = (cum + alpha[i]) % _Modulo;
				if (i == bravoN - 2) {
					bravo[i] = 0L;
					continue;
				}
				bravo[i] = (alpha[i + 2] * (i + 2) + cum) % _Modulo;
			}
			alpha = bravo;
		}
		final long f = alpha[1];
		System.out.printf("%sStarted at %s, finished at %s, f[%d].", //
				nPrinted > 0 ? "\n" : "", //
				formatTimeMs(startTimeMs), formatTimeMs(System.currentTimeMillis()), f);
		return f;

	}

	static String formatTimeMs(final long timeMs) {
		final Instant instant = Instant.ofEpochMilli(timeMs);
		final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		return zonedDateTime.format(_TimeFormatter);
	}

	public static void main(final String[] args) {
		final FeynmanF_NSq feynmanF_NSq = new FeynmanF_NSq();
		final FeynmanF1 feynmanF1 = new FeynmanF1();
		for (int nStar = _LoN; nStar <= _HiN; nStar += 2) {
			final long f = feynmanF_NSq.compute(nStar);
			final long f1 = feynmanF1.compute(nStar);
			System.out.printf("\n%s nStar[%d] f[%d] f1[%d] %s", //
					formatTimeMs(System.currentTimeMillis()), nStar, f, f1, nStar + 2 <= _HiN ? "\n\n" : "\n");
			if (f != f1) {
				System.err.printf("Anomaly! nStar[%d] f[%d] f1[%d]", nStar, f, f1);
				System.exit(33);
			}
		}
	}
}
