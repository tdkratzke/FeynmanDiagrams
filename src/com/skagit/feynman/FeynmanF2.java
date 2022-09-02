package com.skagit.feynman;

import java.util.Comparator;
import java.util.TreeMap;

public class FeynmanF2 {
	final public int _nBlueArcs;
	final public int _modValue;
	final public boolean _trackBlueVectorSets;
	final public boolean _dumpResults;
	final public TreeMap<int[], int[]> _pairToCount;

	FeynmanF2(final int n, final int modValue, final boolean trackBlueVectorSets, final boolean dumpResults) {
		_nBlueArcs = n + 1;
		_modValue = modValue;
		_trackBlueVectorSets = trackBlueVectorSets;
		_dumpResults = dumpResults;
		_pairToCount = new TreeMap<>(new Comparator<int[]>() {

			@Override
			public int compare(final int[] arr0, final int[] arr1) {
				final int n0 = arr0 == null ? 0 : arr0.length;
				final int n1 = arr1 == null ? 0 : arr1.length;
				if (n0 != n1) {
					return n0 < n1 ? -1 : 1;
				}
				for (int k = 0; k < n0; ++k) {
					if (arr0[k] != arr1[k]) {
						return arr0[k] < arr1[k] ? -1 : 1;
					}
				}
				return 0;
			}
		});
	}

	public void addCount(final int[] pair, final int count) {
		final int[] countArr = _pairToCount.get(pair);
		if (countArr != null) {
			countArr[0] += count;
		} else {
			_pairToCount.put(pair, new int[] {
					count
			});
		}
	}

}
