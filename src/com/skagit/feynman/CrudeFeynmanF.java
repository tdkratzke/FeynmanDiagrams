package com.skagit.feynman;

public class CrudeFeynmanF extends FeynmanF {

	@Override
	public long compute(final int nStar) {
		long feynmanF = 0L;
		final BlueVectorIt it = new BlueVectorIt(nStar);
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
