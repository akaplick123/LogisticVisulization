package de.andre.chart.ui.chartframe;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;

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
import de.andre.chart.data.groups.TimeToGroup;
import de.andre.chart.ui.chartframe.helper.SimpleFilterAndOrderConfiguration;
import de.andre.chart.ui.chartframe.helper.SubgroupAdder;

public class OrdersByCompanyAndTimeChartFrame extends JInternalFrameBase {
	private static final long serialVersionUID = 1L;

	private TimeTableXYDataset dataset = new TimeTableXYDataset();
	private final Datacenter data;
	private final LocalDateTimeLookUp dateTimeLookup;
	private final SimpleFilterAndOrderConfiguration filter1 = new SimpleFilterAndOrderConfiguration();

	public OrdersByCompanyAndTimeChartFrame(JDesktopPane desktop, Datacenter data, LocalDateTimeLookUp dateTimeLookup) {
		super();
		this.data = data;
		this.dateTimeLookup = dateTimeLookup;
		setTitle("Ordered Quantity");

		setLayout(new BorderLayout(5, 5));

		ChartPanel panel = createChartPanel();
		add(panel, BorderLayout.CENTER);
		JPanel pFilter = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JButton bFilter = new JButton("advanced configuration");
		pFilter.add(bFilter);
		add(pFilter, BorderLayout.SOUTH);

		bFilter.addActionListener(e1 -> {
			FilterAndOrderConfigurationFrame frame = new FilterAndOrderConfigurationFrame(filter1);
			frame.center(OrdersByCompanyAndTimeChartFrame.this);
			frame.addInternalFrameClosedListener(e2 -> {
				updateChartData();
			});
			frame.show(desktop);
		});

		updateChartData();
	}

	private void updateChartData() {
		dataset.setNotify(false);
		dataset.clear();
		final HashSet<Integer> seenCommkeys = new HashSet<>();
		final SubgroupAdder adder = new SubgroupAdder();
		final OrderItemEventGroups events = data.getEvents();
		final TimeToGroup<OrderItemEvent> grouper = new TimeToGroup<>(e -> dateTimeLookup.getTimeById(e.timestampId()));

		events.getAllKeys() //
				.sorted() //
				.flatMap(group -> events.getValues(group)) // convert LocalDate
															// to events
				.forEach(event -> {
					final int commkey = event.commkey();
					if (seenCommkeys.contains(commkey)) {
						return;
					}
					seenCommkeys.add(commkey);
					final OrderItem orderItem = data.getItemByCommkey(commkey);
					if (orderItem == null) {
						return;
					}
					if (orderItem.fromNali()) {
						return;
					}
					if (filter1.isExcluded(orderItem.company())) {
						return;
					}
					LocalDateTime time = grouper.getGroupOf(event);
					OrderItem item = data.getItemByCommkey(commkey);
					adder.add(time, item.company(), item.quantity());
					filter1.add(item.company());
				});

		List<LocalDateTime> dates = adder.allDates();
		for (LocalDateTime date : dates) {
			TimePeriod minute = toMinute(date);
			for (String company : filter1.getOrderedItems()) {
				if (filter1.isIncluded(company)) {
					int value = adder.getValue(date, company);
					dataset.add(minute, Double.valueOf(value), company);
				}
			}
		}

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

		XYAreaRenderer2 renderer = new StackedXYAreaRenderer2();
		renderer.setBaseToolTipGenerator(toolTipGenerator);
		renderer.setURLGenerator(urlGenerator);
		plot.setRenderer(renderer);

		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
		ChartFactory.getChartTheme().apply(chart);
		return chart;
	}
}
