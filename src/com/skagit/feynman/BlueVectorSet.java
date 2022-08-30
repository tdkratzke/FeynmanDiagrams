package com.skagit.feynman;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class BlueVectorSet {
	public final int _nRedCompletions;
	public final TreeSet<int[]> _blueVectors;

	public BlueVectorSet(final int nRedCompletions) {
		_nRedCompletions = nRedCompletions;
		_blueVectors = new TreeSet<>(new Comparator<int[]>() {

			@Override
			public int compare(final int[] blueVector0, final int[] blueVector1) {
				if (blueVector0[0] != blueVector1[0]) {
					return blueVector0[0] < blueVector1[0] ? 1 : -1;
				}
				final int len0 = blueVector0.length;
				final int len1 = blueVector1.length;
				if (len0 != len1) {
					return len0 < len1 ? -1 : 1;
				}
				for (int k = len0 - 1; k >= 1; --k) {
					final int v0 = blueVector0[k];
					final int v1 = blueVector1[k];
					if (v0 != v1) {
						return v0 < v1 ? -1 : 1;
					}
				}
				return 0;
			}
		});
	}

	public void addBlueVector(final int[] blueVector) {
		_blueVectors.add(blueVector);
	}

	public String getString() {
		final int nBlueVectors = _blueVectors.size();
		if (nBlueVectors > 1) {
			String s = String.format(" %d RedCompletions, nBlueVectors[%d]:", //
					_nRedCompletions, nBlueVectors);
			final Iterator<int[]> it = _blueVectors.iterator();
			while (it.hasNext()) {
				final int[] blueVector = it.next();
				s += String.format("\n    %s", //
						FeynmanF.blueVectorToString(blueVector));
			}
			return s;
		}
		final int[] blueVector = _blueVectors.first();
		final String s = String.format(" %d RedCompletions,  %s", _nRedCompletions,
				FeynmanF.blueVectorToString(blueVector));
		return s;
	}

	@Override
	public String toString() {
		return getString();
	}

}