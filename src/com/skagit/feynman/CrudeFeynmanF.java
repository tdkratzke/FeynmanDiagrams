package com.skagit.feynman;

public class CrudeFeynmanF extends FeynmanF {
	final public int _nBlueArcs;

	CrudeFeynmanF(final int nStar) {
		super(nStar);
		_nBlueArcs = nStar + 1;
	}

	@Override
	public int compute() {
		long feynmanF = 0;
		final BlueVectorIt it = new BlueVectorIt(_nStar);
		while (it.hasNext()) {
			final int[] blueVector = it.next();
			final BlueGraph blueGraph = new BlueGraph(blueVector);
			final int nRedCompletions = blueGraph.getNRedCompletions();
			feynmanF = (feynmanF + nRedCompletions) % _Modulo;
			if (_Debug) {
				System.out.printf("\n%s, NRedCompletions[%d], RunningTotal[%d]", //
						BlueGraph.blueVectorToString(blueVector), nRedCompletions, feynmanF);
			}
		}
		return (int) feynmanF;
	}

	public static int crudeFeynmanF(final int nStar) {
		final CrudeFeynmanF crudeFeynmanF = new CrudeFeynmanF(nStar);
		return crudeFeynmanF.compute();
	}

	public static void main(final String[] args) {
		final boolean doSingleBlueVector = false;
		if (doSingleBlueVector) {
			final int[] blueVector = BlueGraph.stringToBlueVector("[5]");
			final BlueGraph blueGraph = new BlueGraph(blueVector);
			final int nRedCompletions = blueGraph.getNRedCompletions();
			System.out.printf("\nNRedCompletions[%d] for %s", nRedCompletions,
					BlueGraph.blueVectorToString(blueVector));
			System.exit(33);
		} else {
			final int loN = 4, hiN = 12, inc = 2;
			for (int nStar = loN; nStar <= hiN; nStar += inc) {
				final FeynmanF feynmanF = new FeynmanF(nStar);
				final int fastFeynman = feynmanF.compute();
				final CrudeFeynmanF crudeFeynmanF = new CrudeFeynmanF(nStar);
				final int crudeFeynman = crudeFeynmanF.compute();
				System.out.printf("\n%d: Fast[%d]:Crude[%d]\n\n", nStar, fastFeynman, crudeFeynman);
			}
		}
	}

}
