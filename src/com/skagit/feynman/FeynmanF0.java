package com.skagit.feynman;

public class FeynmanF0 extends FeynmanF1 {

	@Override
	public long compute(final int nStar) {
		long f = 0L;
		final BlueVectorIt it = new BlueVectorIt(nStar);
		while (it.hasNext()) {
			final int[] blueVector = it.next();
			final BlueGraph blueGraph = new BlueGraph(blueVector);
			final long nRedCompletions = blueGraph.getNRedCompletions();
			f = (f + nRedCompletions) % _Modulo;
			if (_Debug) {
				System.out.printf("\n%s, NRedCompletions[%d], RunningTotal[%d]", //
						BlueGraph.blueVectorToString(blueVector), nRedCompletions, f);
			}
		}
		return (int) f;
	}

}
