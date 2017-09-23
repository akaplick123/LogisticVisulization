package de.andre.chart.ui.main;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYDataset;

public class MyInternalFrame extends JInternalFrame {
    private static final long serialVersionUID = 1L;
    private static final int xOffset = 30, yOffset = 30;
    private static int openFrameCount = 0;

    public MyInternalFrame() {
	super("Document #" + (++openFrameCount), true, // resizable
		true, // closable
		true, // maximizable
		true);// iconifiable

	// ...Create the GUI and put it in the window...
	ChartPanel panel = createChartPanel();
	setLayout(new BorderLayout());
	add(panel);

	// ...Then set the window size or call pack...
	setSize(300, 300);

	// Set the window's location.
	setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
    }

    protected ChartPanel createChartPanel() {
//	TimeSeriesCollection dataset = new TimeSeriesCollection();
//	TimeSeries series1 = new TimeSeries("Cat 1");
//	series1.add(new Minute(10, 14, 13, 10, 2017), 10);
//	series1.add(new Minute(12, 14, 13, 10, 2017), 15);
//	series1.add(new Minute(13, 14, 13, 10, 2017), 20);
//	series1.add(new Minute(20, 14, 13, 10, 2017), 7);
//	dataset.addSeries(series1);
//
//	TimeSeries series2 = new TimeSeries("Cat 2");
//	series2.add(new Minute(10, 14, 13, 10, 2017), 5);
//	series2.add(new Minute(12, 14, 13, 10, 2017), 10);
//	series2.add(new Minute(13, 14, 13, 10, 2017), 10);
//	series2.add(new Minute(20, 14, 13, 10, 2017), 24);
//	dataset.addSeries(series2);
	
	TimeTableXYDataset dataset = new TimeTableXYDataset();
	dataset.add(new Minute(10, 14, 13, 10, 2017), 10, "Cat 1");
	dataset.add(new Minute(12, 14, 13, 10, 2017), 15, "Cat 1");
	dataset.add(new Minute(13, 14, 13, 10, 2017), 20, "Cat 1");
	dataset.add(new Minute(20, 14, 13, 10, 2017), 7, "Cat 1");
	dataset.add(new Minute(10, 14, 13, 10, 2017), 5, "Cat 2");
	dataset.add(new Minute(12, 14, 13, 10, 2017), 10, "Cat 2");
	dataset.add(new Minute(13, 14, 13, 10, 2017), 10, "Cat 2");
	dataset.add(new Minute(20, 14, 13, 10, 2017), 24, "Cat 2");

	JFreeChart chart = createTimeSeriesChart("title", "time", "value", dataset);
	ChartPanel panel = new ChartPanel(chart);
	panel.setFillZoomRectangle(true);
	panel.setMouseWheelEnabled(true);
	return panel;
    }

    /**
     * Creates and returns a time series chart. A time series chart is an
     * {@link XYPlot} with a {@link DateAxis} for the x-axis and a
     * {@link NumberAxis} for the y-axis. The default renderer is an
     * {@link XYLineAndShapeRenderer}.
     * <P>
     * A convenient dataset to use with this chart is a
     * {@link org.jfree.data.time.TimeSeriesCollection}.
     *
     * @param title
     *            the chart title (<code>null</code> permitted).
     * @param timeAxisLabel
     *            a label for the time axis (<code>null</code> permitted).
     * @param valueAxisLabel
     *            a label for the value axis (<code>null</code> permitted).
     * @param dataset
     *            the dataset for the chart (<code>null</code> permitted).
     *
     * @return A time series chart.
     * 
     * @since 1.0.16
     */
    public static JFreeChart createTimeSeriesChart(String title, String timeAxisLabel, String valueAxisLabel,
	    XYDataset dataset) {
	return createTimeSeriesChart(title, timeAxisLabel, valueAxisLabel, dataset, true, true, false);
    }

    /**
     * Creates and returns a time series chart. A time series chart is an
     * {@link XYPlot} with a {@link DateAxis} for the x-axis and a
     * {@link NumberAxis} for the y-axis. The default renderer is an
     * {@link XYLineAndShapeRenderer}.
     * <P>
     * A convenient dataset to use with this chart is a
     * {@link org.jfree.data.time.TimeSeriesCollection}.
     *
     * @param title
     *            the chart title (<code>null</code> permitted).
     * @param timeAxisLabel
     *            a label for the time axis (<code>null</code> permitted).
     * @param valueAxisLabel
     *            a label for the value axis (<code>null</code> permitted).
     * @param dataset
     *            the dataset for the chart (<code>null</code> permitted).
     * @param legend
     *            a flag specifying whether or not a legend is required.
     * @param tooltips
     *            configure chart to generate tool tips?
     * @param urls
     *            configure chart to generate URLs?
     *
     * @return A time series chart.
     */
    public static JFreeChart createTimeSeriesChart(String title, String timeAxisLabel, String valueAxisLabel,
	    XYDataset dataset, boolean legend, boolean tooltips, boolean urls) {

	ValueAxis timeAxis = new DateAxis(timeAxisLabel);
	timeAxis.setLowerMargin(0.02); // reduce the default margins
	timeAxis.setUpperMargin(0.02);
	NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
	valueAxis.setAutoRangeIncludesZero(false); // override default
	XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, null);

	XYToolTipGenerator toolTipGenerator = StandardXYToolTipGenerator.getTimeSeriesInstance();

	XYURLGenerator urlGenerator = new StandardXYURLGenerator();

	XYAreaRenderer renderer = new StackedXYAreaRenderer(StackedXYAreaRenderer.AREA);
	renderer.setBaseToolTipGenerator(toolTipGenerator);
	renderer.setURLGenerator(urlGenerator);
	plot.setRenderer(renderer);

	JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
	ChartFactory.getChartTheme().apply(chart);
	return chart;

    }
}
