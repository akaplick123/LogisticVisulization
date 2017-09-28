package de.andre.chart.ui.chartframe;

import java.awt.BorderLayout;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JScrollBar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYDataset;

import de.andre.chart.data.Datacenter;
import de.andre.chart.data.LocalDateTimeLookUp;
import de.andre.chart.data.OrderItem;
import de.andre.chart.data.OrderItemEvent;
import de.andre.chart.data.OrderItemEventGroups;
import de.andre.chart.data.OrderItemState;
import de.andre.chart.data.groups.TimeToGroup;
import de.andre.chart.ui.chartframe.helper.SimpleFilterAndOrderConfiguration;
import de.andre.chart.ui.chartframe.helper.SubgroupAdder;

public class OrdersFullfillmentByTimeChartFrame extends JInternalFrameBase {
    private static final String STATE_FUTURE_ORDER = "future";
    private static final String STATE_ORDERED = "ordered";
    private static final String STATE_EX_NALI = "ex NALI";
    private static final String STATE_CANCELED = "canceled";
    private static final String STATE_PROCESSED = "processed";

    private static final long serialVersionUID = 1L;

    private final TimeTableXYDataset dataset = new TimeTableXYDataset();
    private final Datacenter data;
    private final LocalDateTimeLookUp dateTimeLookup;
    private final SimpleFilterAndOrderConfiguration companyFilter = new SimpleFilterAndOrderConfiguration();
    private final SimpleFilterAndOrderConfiguration stateOrder = new SimpleFilterAndOrderConfiguration();
    private XYAreaRenderer2 renderer;
    private LocalDateTime currentTime = LocalDateTime.MIN;
    private List<LocalDateTime> timeGroupList = new ArrayList<>();
    private JScrollBar timeScrollBar;

    public OrdersFullfillmentByTimeChartFrame(JDesktopPane desktop, Datacenter data,
	    LocalDateTimeLookUp dateTimeLookup) {
	super();
	this.data = data;
	this.dateTimeLookup = dateTimeLookup;
	setTitle("Ordered Quantity");

	setLayout(new BorderLayout());

	ChartPanel panel = createChartPanel();
	add(panel, BorderLayout.CENTER);

	timeScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
	add(timeScrollBar, BorderLayout.SOUTH);

	stateOrder.add(STATE_CANCELED);
	stateOrder.add(STATE_FUTURE_ORDER);
	stateOrder.add(STATE_EX_NALI);
	stateOrder.add(STATE_ORDERED);
	stateOrder.add(STATE_PROCESSED);
	updateChartContent();

	timeScrollBar.addAdjustmentListener(e -> {
	    int idx = e.getValue();
	    currentTime = timeGroupList.get(idx);
	    updateChartContent();
	});
    }

    private void updateChartContent() {
	dataset.setNotify(false);
	dataset.clear();
	final HashMap<Integer, LocalDateTime> commkeysToBins = new HashMap<>();
	final SubgroupAdder adder = new SubgroupAdder();
	final OrderItemEventGroups events = data.getEvents();
	final TimeToGroup<OrderItemEvent> grouper = new TimeToGroup<>(e -> dateTimeLookup.getTimeById(e.timestampId()));
	final HashSet<LocalDateTime> timeGroups = new HashSet<>();

	events.getAllKeys() //
		.sorted().flatMap(group -> events.getValues(group)) //
		.filter(event -> !data.getItems().getByCommkey(event.commkey()).fromNali()) //
		.forEach(event -> {
		    final int commkey = event.commkey();
		    OrderItem orderItem = data.getItemByCommkey(commkey);
		    companyFilter.add(orderItem.company());
		    // apply some filtering ...

		    // begin count logic
		    LocalDateTime time = commkeysToBins.get(commkey);
		    boolean newItem = (time == null);
		    if (time == null) {
			// commkey is new
			time = grouper.getGroupOf(event);
			commkeysToBins.put(commkey, time);
		    }
		    timeGroups.add(time);

		    String subgroupIncrease = null;
		    String subgroupDecrease = null;
		    if (time.isAfter(currentTime)) {
			// check later
		    } else if (event.newState() == OrderItemState.PROCESSED.getId()) {
			subgroupIncrease = STATE_PROCESSED;
			if (orderItem.fromNali()) {
			    subgroupDecrease = STATE_EX_NALI;
			} else {
			    subgroupDecrease = STATE_ORDERED;
			}
		    } else if (event.newState() == OrderItemState.CANCELED.getId()) {
			subgroupIncrease = STATE_CANCELED;
			if (orderItem.fromNali()) {
			    subgroupDecrease = STATE_EX_NALI;
			} else {
			    subgroupDecrease = STATE_ORDERED;
			}
		    }
		    if (subgroupIncrease != null) {
			adder.add(time, subgroupIncrease, orderItem.quantity());
		    }
		    if (subgroupDecrease != null) {
			adder.add(time, subgroupDecrease, -1 * orderItem.quantity());
		    }

		    if (newItem) {
			if (time.isAfter(currentTime)) {
			    subgroupIncrease = STATE_FUTURE_ORDER;
			} else if (orderItem.fromNali()) {
			    subgroupIncrease = STATE_EX_NALI;
			} else {
			    subgroupIncrease = STATE_ORDERED;
			}
			adder.add(time, subgroupIncrease, orderItem.quantity());
		    }
		});

	// draw chart
	List<LocalDateTime> dates = adder.allDates();
	for (LocalDateTime date : dates) {
	    TimePeriod minute = toMinute(date);
	    for (String state : stateOrder.getOrderedItems()) {
		if (stateOrder.isIncluded(state)) {
		    int value = adder.getValue(date, state);
		    dataset.add(minute, Double.valueOf(value), state);
		}
	    }
	}

	// calibrate scrollbar
	this.timeGroupList.clear();
	this.timeGroupList.addAll(timeGroups);
	Collections.sort(this.timeGroupList);
	this.timeScrollBar.setMinimum(0);
	this.timeScrollBar.setMaximum(this.timeGroupList.size() - 1 + this.timeScrollBar.getVisibleAmount());

	// todo: // renderer.setSeriesFillPaint(series, paint, notify);

	dataset.setNotify(true);
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
     */
    private JFreeChart createTimeSeriesChart(String title, String timeAxisLabel, String valueAxisLabel,
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
    private JFreeChart createTimeSeriesChart(String title, String timeAxisLabel, String valueAxisLabel,
	    XYDataset dataset, boolean legend, boolean tooltips, boolean urls) {

	ValueAxis timeAxis = new DateAxis(timeAxisLabel);
	timeAxis.setLowerMargin(0.02); // reduce the default margins
	timeAxis.setUpperMargin(0.02);
	NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
	valueAxis.setAutoRangeIncludesZero(false); // override default
	XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, null);

	XYToolTipGenerator toolTipGenerator = StandardXYToolTipGenerator.getTimeSeriesInstance();

	XYURLGenerator urlGenerator = new StandardXYURLGenerator();

	renderer = new StackedXYAreaRenderer2();
	renderer.setBaseToolTipGenerator(toolTipGenerator);
	renderer.setURLGenerator(urlGenerator);
	plot.setRenderer(renderer);

	JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
	ChartFactory.getChartTheme().apply(chart);
	return chart;
    }
}
