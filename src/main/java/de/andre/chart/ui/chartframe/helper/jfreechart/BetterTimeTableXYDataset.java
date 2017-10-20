package de.andre.chart.ui.chartframe.helper.jfreechart;

import java.util.ArrayList;

import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeTableXYDataset;

public class BetterTimeTableXYDataset extends TimeTableXYDataset {
    private static final long serialVersionUID = 1L;

    /** names (and order) of all series */
    private final ArrayList<Comparable<?>> series = new ArrayList<>();
    /** every x-value */
    private final ArrayList<ArrayList<Long>> seriesXValues = new ArrayList<>();
    /** every y-value */
    private final ArrayList<ArrayList<Number>> seriesYValues = new ArrayList<>();

    @Override
    public void add(TimePeriod period, double y, @SuppressWarnings("rawtypes") Comparable seriesName) {
	addNumber(period, y, seriesName);
    }

    @Override
    public void add(TimePeriod period, Number y, @SuppressWarnings("rawtypes") Comparable seriesName, boolean notify) {
	addNumber(period, y, seriesName);
    }

    private void addNumber(TimePeriod period, Number y, Comparable<?> seriesKey) {
	Long x = toNumber(period);
	int seriesIdx = indexOf(seriesKey);
	this.seriesYValues.get(seriesIdx).add(y);
	this.seriesXValues.get(seriesIdx).add(x);
    }

    /**
     * Returns the x-value for a time period.
     *
     * @param period
     *            the time period.
     *
     * @return The x-value.
     */
    private long toNumber(TimePeriod period) {
	long result = 0L;
	if (getXPosition() == TimePeriodAnchor.START) {
	    result = period.getStart().getTime();
	} else if (getXPosition() == TimePeriodAnchor.MIDDLE) {
	    long t0 = period.getStart().getTime();
	    long t1 = period.getEnd().getTime();
	    result = t0 + (t1 - t0) / 2L;
	} else if (getXPosition() == TimePeriodAnchor.END) {
	    result = period.getEnd().getTime();
	}
	return result;
    }

    @Override
    public void clear() {
	series.clear();
	seriesYValues.forEach(e -> e.clear());
	seriesYValues.clear();
	seriesXValues.forEach(e -> e.clear());
	seriesXValues.clear();
    }

    @Override
    public int getItemCount() {
	// number of x values (should be cached)
	if (seriesXValues.isEmpty()) {
	    return 0;
	}
	return seriesXValues.get(0).size();
    }

    @Override
    public int getItemCount(int seriesIndex) {
	// number of x values
	return seriesYValues.get(seriesIndex).size();
    }

    @Override
    public Number getX(int seriesIndex, int itemIndex) {
	return this.seriesXValues.get(seriesIndex).get(itemIndex);
    }

    @Override
    public double getXValue(int seriesIndex, int itemIndex) {
	Number x = getX(seriesIndex, itemIndex);
	if (x == null) {
	    return Double.NaN;
	}
	return x.doubleValue();
    }

    @Override
    public Range getDomainBounds(boolean includeInterval) {
	// x-values range
	Long minValue = null;
	Long maxValue = null;

	for (ArrayList<Long> singleSeries: this.seriesXValues) {
	    for (Long value: singleSeries) {
		if (minValue == null) {
		    minValue = value;
		    maxValue = value;
		} else {
		    if (value.longValue() < minValue.longValue()) {
			minValue = value;
		    } else if (value.longValue() > maxValue.longValue()) {
			maxValue = value;
		    }
		}
	    }
	}

	if (minValue == null) {
	    return new Range(0, 0);
	}

	return new Range(minValue, maxValue);
    }

    @Override
    public Number getY(int seriesIndex, int itemIndex) {
	return this.seriesYValues.get(seriesIndex).get(itemIndex);
    }

    @Override
    public double getYValue(int seriesIndex, int itemIndex) {
	Number y = getY(seriesIndex, itemIndex);
	if (y == null) {
	    return Double.NaN;
	}
	return y.doubleValue();
    }

    @Override
    public int getSeriesCount() {
	// number of categories
	return series.size();
    }

    @Override
    public Comparable<?> getSeriesKey(int seriesIndex) {
	// 1st series has index 0
	return series.get(seriesIndex);
    }

    @Override
    public int indexOf(@SuppressWarnings("rawtypes") Comparable seriesKey) {
	int idx = this.series.indexOf(seriesKey);
	if (idx == -1) {
	    // a new series
	    this.series.add(seriesKey);
	    this.seriesXValues.add(new ArrayList<>());
	    this.seriesYValues.add(new ArrayList<>());
	    idx = this.series.indexOf(seriesKey);
	}
	return idx;
    }

    @Override
    public DomainOrder getDomainOrder() {
	return DomainOrder.NONE;
    }
}
