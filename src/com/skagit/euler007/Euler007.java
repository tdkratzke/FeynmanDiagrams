package com.skagit.euler007;

import java.util.BitSet;

public class Euler007 {

	static private int _TargetNumber = 10001;

	static int getNthPrime(final int nStar) {
		/** From wiki, we get that a good guess for the nth prime is n*ln(n). */
		for (int k = (int) (2d * nStar / Math.log(nStar));; k *= 2) {
			final BitSet bitSet = getPrimesUpTo(k);
			if (bitSet.cardinality() >= _TargetNumber) {
				int prime = bitSet.nextSetBit(0);
				for (int j = 1; j < nStar; prime = bitSet.nextSetBit(prime + 1), ++j) {
				}
				return prime;
			}
		}
	}

	private static BitSet getPrimesUpTo(final int n) {
		final BitSet primes = new BitSet(1 + n);
		primes.set(2, n + 1);
		for (int k = primes.nextSetBit(2); k >= 0; k = primes.nextSetBit(k + 1)) {
			for (int kk = k;;) {
				if (kk > n - k) {
					break;
				}
				kk += k;
				primes.clear(kk);
			}
		}
		return primes;
	}

	public static void main(final String[] args) {
		final int answer = getNthPrime(_TargetNumber);
		System.out.printf("\n%d-th prime is %d.", _TargetNumber, answer);
	}

}
