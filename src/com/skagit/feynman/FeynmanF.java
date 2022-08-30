package com.skagit.feynman;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class FeynmanF {
	final public int _nBlueArcs;
	final public int _modValue;
	final public boolean _trackBlueVectorSets;
	final public boolean _dumpResults;
	public int _feynmanF;
	final public TreeMap<Integer, BlueVectorSet> _blueVectorSets;

	FeynmanF(final int n, final int modValue, final boolean trackBlueVectorSets, final boolean dumpResults) {
		_nBlueArcs = n + 1;
		_modValue = modValue;
		_trackBlueVectorSets = trackBlueVectorSets;
		_dumpResults = dumpResults;
		_feynmanF = 0;
		_blueVectorSets = new TreeMap<>();
	}

	public void addBlueVector(final int nRedCompletions, final int[] blueVector) {
		if (_trackBlueVectorSets) {
			BlueVectorSet blueVectorSet = _blueVectorSets.get(nRedCompletions);
			if (blueVectorSet == null) {
				blueVectorSet = new BlueVectorSet(nRedCompletions);
				_blueVectorSets.put(nRedCompletions, blueVectorSet);
			}
			blueVectorSet.addBlueVector(blueVector);
		}
	}

	public String getString() {
		String s = String.format("\nFeynmanF[%d]", _feynmanF);
		if (_modValue >= 2) {
			s += String.format(" (modValue[%d])", _modValue);
		}
		if (_trackBlueVectorSets) {
			final int nBlueVectorSets = _blueVectorSets.size();
			final Iterator<Map.Entry<Integer, BlueVectorSet>> it = //
					_blueVectorSets.entrySet().iterator();
			for (int k = 0; it.hasNext(); ++k) {
				final Map.Entry<Integer, BlueVectorSet> entry = it.next();
				final BlueVectorSet blueVectorSet = entry.getValue();
				s += String.format("\n  %d of %d Sets, %s", //
						k + 1, nBlueVectorSets, blueVectorSet.getString());
			}
		}
		return s;
	}

	@Override
	public String toString() {
		return getString();
	}

	static String blueVectorToString(final int[] blueVector) {
		String s = String.format("[%d", blueVector[0]);
		final int n = blueVector.length;
		for (int k = 1; k < n; ++k) {
			s += String.format("%s%d", k == 1 ? " " : ",", blueVector[k]);
		}
		s += "]";
		return s;
	}

	static int[] stringToBlueVector(final String s) {
		final String[] fields = s.trim().split("[\\s,\\[\\]]+");
		final int nFieldsX = fields.length;
		final ArrayList<Integer> fieldsList = new ArrayList<>();
		for (int k = 0; k < nFieldsX; ++k) {
			try {
				fieldsList.add(Integer.parseInt(fields[k]));
			} catch (final NumberFormatException e) {
			}
		}
		final int nFields = fieldsList.size();
		if (nFields < 1 || fieldsList.get(0) < 2) {
			return null;
		}
		int maxNonZeroField = 0;
		for (int k = 0; k < nFields; ++k) {
			final int field = fieldsList.get(k);
			if (field > 0) {
				maxNonZeroField = k;
			}
		}
		final int[] blueVector = new int[maxNonZeroField + 1];
		for (int k = 0; k <= maxNonZeroField; ++k) {
			blueVector[k] = fieldsList.get(k);
		}
		return blueVector;
	}

	/** Computes a + b*c. */
	static int accumulate(int a, int b, int c, final int modValue) {
		if (modValue <= 1) {
			return a + b * c;
		}
		a %= modValue;
		b %= modValue;
		c %= modValue;
		final int answer = a + (((b % modValue) * (c % modValue)) % modValue);
		return answer % modValue;
	}

	public static int feynmanF(final int n) {
		final FeynmanF feynmanF = new FeynmanF( //
				n, /* modValue= */1000000007, //
				/* trackBlueVectorSets= */false, /* dumpResults= */false);
		feynmanF.compute();
		return feynmanF._feynmanF;
	}

	public void compute() {
		final int n = _nBlueArcs - 1;
		int[] blueVector = new int[] {
				_nBlueArcs
		};
		if (_dumpResults) {
			System.out.printf("n[%d] (nBlueArcs[%d])", n, _nBlueArcs);
		}
		for (; blueVector != null; blueVector = NextBlueVector.nextBlueVector(blueVector)) {
			final BlueGraph blueGraph = new BlueGraph(blueVector, _modValue);
			final int nRedCompletions = blueGraph.getNRedCompletions();
			if (_trackBlueVectorSets) {
				addBlueVector(nRedCompletions, blueVector);
			}
			_feynmanF = accumulate(_feynmanF, 1, nRedCompletions, _modValue);
			if (_dumpResults) {
				System.out.printf("\n%s, NRedCompletions[%d], RunningTotal[%d]", //
						blueVectorToString(blueVector), nRedCompletions, _feynmanF);
			}
		}
	}

	public final static void main(final String[] args) {
		final boolean doSingleBlueVector = false;
		if (doSingleBlueVector) {
			final int[] blueVector = stringToBlueVector("[2 1]");
			final BlueGraph blueGraph = new BlueGraph(blueVector);
			final int nRedCompletions = blueGraph.getNRedCompletions();
			System.out.printf("\nNRedCompletions[%d] for %s", nRedCompletions, blueVectorToString(blueVector));
		} else {
			final int modValue = 1000000007;
			final boolean trackBlueVectorSets = true;
			final boolean dumpResults = true;
			final int loN = 4, hiN = 8, inc = 2;
			for (int n = loN; n <= hiN; n += inc) {
				if (false) {
					System.out.printf("\nn[%d] feynmanF[%d]", n, feynmanF(n));
					continue;
				}
				final FeynmanF feynmanF = new FeynmanF( //
						n, modValue, trackBlueVectorSets, dumpResults);
				feynmanF.compute();
				System.out.printf("%s", feynmanF.getString());
				System.out.printf("\n\n");
			}
		}
	}
}
