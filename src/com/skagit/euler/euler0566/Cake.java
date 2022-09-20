package com.skagit.euler.euler0566;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class Cake {
	final TreeMap<Double, Interval> _intervals;

	public Cake() {
		_intervals = new TreeMap<>();
		final Interval interval = new Interval(/* up= */true, /* start= */0d, /* length= */1d);
		_intervals.put(/* start= */interval._start, interval);
	}

	public void flip(final Interval interval) {
		final Double start = interval._start;
		final Double end = interval._end;
		final boolean wraps = start > end;
		final SortedMap<Double, Interval> tailMap = _intervals.tailMap(start);

		final ArrayList<Interval> oldIntervals = new ArrayList<>();
		final ArrayList<Double> oldKeys = new ArrayList<>();
		final Iterator<Double> it0 = tailMap.keySet().iterator();
		while (it0.hasNext()) {
			final Interval intervalX = tailMap.get(it0.next());
			oldIntervals.add(intervalX);
			oldKeys.add(intervalX._start);
			if (!wraps && intervalX._end >= end) {
				break;
			}
		}
		if (wraps) {
			final Iterator<Double> it1 = _intervals.keySet().iterator();
			while (it1.hasNext()) {
				final Interval intervalX = _intervals.get(it1.next());
				oldIntervals.add(intervalX);
				oldKeys.add(intervalX._start);
				if (intervalX._end >= end) {
					break;
				}
			}
		}
		_intervals.keySet().removeAll(oldKeys);

		final ArrayList<Interval> newIntervals = new ArrayList<>();
		final Interval interval0 = oldIntervals.get(0);
		final int size = oldIntervals.size();
		final Interval intervalN = oldIntervals.get(size - 1);
		final boolean pink0 = interval0._pink;
		if (interval0._start < start) {
			if (interval0._end <= start) {
				newIntervals.add(interval0);
			} else {
				newIntervals.add(new Interval(interval0._start, start, pink0));
				oldIntervals.set(0, new Interval(start, interval0._end, pink0));
			}
		}
		if (intervalN._end > end) {
			if (intervalN._start >= end) {
				newIntervals.add(interval0);
			} else {
				newIntervals.add(new Interval(end, intervalN._end, intervalN._pink));
				oldIntervals.set(size - 1, new Interval(start, interval._end, interval0._pink));
			}
		}

	}
}
