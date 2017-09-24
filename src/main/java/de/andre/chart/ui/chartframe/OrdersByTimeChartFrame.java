package de.andre.chart.ui.chartframe;

import java.awt.BorderLayout;
import java.time.LocalDateTime;
import java.util.HashSet;

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
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYDataset;

import de.andre.chart.data.Datacenter;
import de.andre.chart.data.OrderItemEvent;
import de.andre.chart.data.OrderItemEventGroups;
import de.andre.chart.data.groups.TimeToGroup;
import de.andre.chart.ui.chartframe.helper.GroupAdder;

public class OrdersByTimeChartFrame extends JInternalFrameBase {
    private static final long serialVersionUID = 1L;

    private TimeTableXYDataset dataset = new TimeTableXYDataset();
    private Datacenter data;

    public OrdersByTimeChartFrame(Datacenter data) {
	super();
	this.data = data;
	setTitle("Ordered Quantity");

	ChartPanel panel = createChartPanel();

	fillChartwithData();

	setLayout(new BorderLayout());
	add(panel, BorderLayout.CENTER);
    }

    private void fillChartwithData() {
	dataset.clear();
	final HashSet<Integer> seenCommkeys = new HashSet<>();
	final GroupAdder adder = new GroupAdder();
	final OrderItemEventGroups events = data.getEvents();
	final TimeToGroup<OrderItemEvent> grouper = new TimeToGroup<>(OrderItemEvent::timestamp);

	events.getAllKeys() //
		.sorted().flatMap(group -> events.getValues(group)) //
		.filter(event -> !data.getItems().getByCommkey(event.commkey()).fromNali()) //
		.forEach(event -> {
		    final int commkey = event.commkey();
		    if (!seenCommkeys.contains(commkey)) {
			seenCommkeys.add(commkey);
			LocalDateTime time = grouper.getGroupOf(event);
			adder.add(time, data.getItems().getByCommkey(event.commkey()).quantity());
		    }
		});

	adder.getData().entrySet().stream() //
		.forEach(entry -> {
		    dataset.add(toMinute(entry.getKey()), entry.getValue(), "ordered");
		});
    }

    private static TimePeriod toMinute(LocalDateTime value) {
	return new Minute(value.getMinute(), value.getHour(), value.getDayOfMonth(), value.getMonthValue(),
		value.getYear());
    }

    private ChartPanel createChartPanel() {
	JFreeChart chart = createTimeSeriesChart("Ordered Quantity", "time", "Quantity [AK]", dataset);
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
