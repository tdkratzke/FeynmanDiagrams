package com.skagit.feynman;

public class CrudeFeynmanF extends FeynmanF {
	final public int _nBlueArcs;

	CrudeFeynmanF(final int nStar) {
		super(nStar);
		_nBlueArcs = nStar + 1;
	}

	@Override
	public long compute() {
		long feynmanF = 0L;
		final BlueVectorIt it = new BlueVectorIt(_nStar);
		while (it.hasNext()) {
			final int[] blueVector = it.next();
			final BlueGraph blueGraph = new BlueGraph(blueVector);
			final long nRedCompletions = blueGraph.getNRedCompletions();
			feynmanF = (feynmanF + nRedCompletions) % _Modulo;
			if (_Debug) {
				System.out.printf("\n%s, NRedCompletions[%d], RunningTotal[%d]", //
						BlueGraph.blueVectorToString(blueVector), nRedCompletions, feynmanF);
			}
		}
		return (int) feynmanF;
	}

}
