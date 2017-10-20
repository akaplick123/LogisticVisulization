package de.andre.chart.ui.chartframe;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
import lombok.extern.log4j.Log4j;

@Log4j
public class OrdersFulfillmentByTimeChartFrame2 extends JInternalFrameBase {
  private static final String STATE_FUTURE_ORDER = "future";
  private static final String STATE_ORDERED = "ordered";
  // private static final String STATE_EX_NALI = "ex NALI";
  private static final String STATE_CANCELED = "canceled";
  private static final String STATE_PROCESSED = "processed";

  private static final long serialVersionUID = 1L;

  /** all data */
  private final Datacenter data;
  /** mapping from time-ID to LocalDateTime */
  private final LocalDateTimeLookUp dateTimeLookup;
  /** all companies */
  private final SimpleFilterAndOrderConfiguration companyFilter =
      new SimpleFilterAndOrderConfiguration();
  /** all item states */
  private final SimpleFilterAndOrderConfiguration stateOrder =
      new SimpleFilterAndOrderConfiguration();

  /** dataset for JFreeChart */
  private final TimeTableXYDataset dataset = new TimeTableXYDataset();
  /** renderer for JFreeChart */
  private XYAreaRenderer2 renderer;

  /** current time */
  private LocalDateTime currentTime = LocalDateTime.MIN;
  /** all available times */
  private List<LocalDateTime> timeGroupList = new ArrayList<>();
  /** container for all item states */
  private final ItemStates itemStates = new ItemStates();

  public OrdersFulfillmentByTimeChartFrame2(JDesktopPane desktop, Datacenter data,
      LocalDateTimeLookUp dateTimeLookup) {
    super();
    this.data = data;
    this.dateTimeLookup = dateTimeLookup;
    setTitle("Ordered Quantity");

    setLayout(new BorderLayout());

    ChartPanel panel = createChartPanel();
    add(panel, BorderLayout.CENTER);

    JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    JLabel lCurrentTime = new JLabel("now");
    JButton bNext = new JButton("start");
    bNext.setActionCommand("start");
    StoppableThread thread = new StoppableThread(() -> {
      DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
      incrementTimeAndUpdateChart();
      lCurrentTime.setText(dtf.format(this.currentTime).replace('T', ' '));

      // check for end of thread
      int idx = this.timeGroupList.indexOf(this.currentTime);
      if (idx == -1) {
        Thread.currentThread().interrupt();
      }
    });

    bNext.addActionListener(e -> {
      if ("start".equals(bNext.getActionCommand())) {
        thread.start();
        bNext.setActionCommand("pause");
        bNext.setText("pause");
      } else if ("pause".equals(bNext.getActionCommand())) {
        thread.stop();
        bNext.setActionCommand("start");
        bNext.setText("restart");
      }
    });

    pBottom.add(lCurrentTime);
    pBottom.add(bNext);
    add(pBottom, BorderLayout.SOUTH);
    addInternalFrameClosedListener(e -> {
      thread.stop();
    });

    stateOrder.add(STATE_CANCELED);
    stateOrder.add(STATE_FUTURE_ORDER);
    // stateOrder.add(STATE_EX_NALI);
    stateOrder.add(STATE_ORDERED);
    stateOrder.add(STATE_PROCESSED);

    new Thread(() -> {
      bNext.setEnabled(false);
      initializeChartContent();
      updateChart();
      bNext.setEnabled(true);
    } , "firstDrawing").start();
  }

  private void initializeChartContent() {
    log.debug("start initializeChartContent at date: " + currentTime);
    final OrderItemEventGroups events = data.getEvents();
    final TimeToGroup<OrderItemEvent> grouper =
        new TimeToGroup<>(e -> dateTimeLookup.getTimeById(e.timestampId()));
    final HashSet<LocalDateTime> timeGroupSet = new HashSet<>();
    final Comparator<OrderItemEvent> eventByTimeComparator = (a, b) -> {
      LocalDateTime timeA = dateTimeLookup.getTimeById(a.timestampId());
      LocalDateTime timeB = dateTimeLookup.getTimeById(b.timestampId());
      return timeA.compareTo(timeB);
    };

    this.currentTime = LocalDateTime.MIN;
    this.itemStates.clear();

    // grep all order items (without "ex NALI"s)
    for (LocalDateTime timeGroup : events.getAllSortedKeys()) {
      for (OrderItemEvent event : events.getSortedEvents(timeGroup, eventByTimeComparator)) {
        final int commkey = event.commkey();
        final OrderItem orderItem = this.data.getItemByCommkey(commkey);
        if (orderItem == null || orderItem.fromNali()) {
          // skip ex NALIs
          continue;
        }

        this.companyFilter.add(orderItem.company());

        LocalDateTime time = grouper.getGroupOf(event);

        // it's probably a new item, so add it as FUTURE state
        this.itemStates.addFutureItem(orderItem, time);

        // remember time
        timeGroupSet.add(time);
      }
    }

    // calibrate scrollbar
    this.timeGroupList.clear();
    this.timeGroupList.addAll(timeGroupSet);
    Collections.sort(this.timeGroupList);

    // select first time as current time
    if (!this.timeGroupList.isEmpty()) {
      this.currentTime = this.timeGroupList.get(0);
    }

    log.debug("finished initializeChartContent at date: " + currentTime);
  }

  private void incrementTimeAndUpdateChart() {
    log.debug("start incrementTimeAndUpdateChart at date: " + currentTime);
    // process all events of currentTime and increment currentTime at the
    // end
    LocalDateTime timeToProcess = this.currentTime;
    incrementCurrentTime();

    // process all events from that time
    final OrderItemEventGroups events = data.getEvents();
    final Comparator<OrderItemEvent> eventByTimeComparator = (a, b) -> {
      LocalDateTime timeA = dateTimeLookup.getTimeById(a.timestampId());
      LocalDateTime timeB = dateTimeLookup.getTimeById(b.timestampId());
      return timeA.compareTo(timeB);
    };

    for (OrderItemEvent event : events.getSortedEvents(timeToProcess, eventByTimeComparator)) {
      int commkey = event.commkey();
      byte newStateId = event.newState();
      String newState;
      if (newStateId == OrderItemState.PROCESSED.getId()) {
        newState = STATE_PROCESSED;
      } else if (newStateId == OrderItemState.CANCELED.getId()) {
        newState = STATE_CANCELED;
      } else {
        newState = STATE_ORDERED;
      }

      this.itemStates.updateItemState(commkey, newState);
    }

    updateChart();
    log.debug("finished incrementTimeAndUpdateChart at date: " + currentTime);
  }

  private void updateChart() {
    try {
      SwingUtilities.invokeAndWait(() -> {
        log.debug("start updateChart at date: " + currentTime);
        final SubgroupAdder barChartData = this.itemStates.getBarChartData();

        // draw chart
        dataset.setNotify(false);
        dataset.clear();
        List<LocalDateTime> dates = barChartData.allDates();
        log.debug("start loop");
        for (LocalDateTime date : dates) {
          TimePeriod minute = toMinute(date);
          for (String state : stateOrder.getOrderedItems()) {
            if (stateOrder.isIncluded(state)) {
              int value = barChartData.getValue(date, state);
              dataset.add(minute, Double.valueOf(value), state);
            }
          }
        }

        log.debug("end loop and notify");
        // todo: // renderer.setSeriesFillPaint(series, paint, notify);
        dataset.setNotify(true);
        log.debug("finished updateChart at date: " + currentTime);
      });
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void incrementCurrentTime() {
    int idx = this.timeGroupList.indexOf(this.currentTime);
    if (idx >= 0) {
      idx++;
      if (idx >= this.timeGroupList.size()) {
        // we have reached the last time, so simply add 5 minutes
        this.currentTime = this.currentTime.plus(5, ChronoUnit.MINUTES);
      } else {
        // next valid time
        this.currentTime = this.timeGroupList.get(idx);
      }
    } else {
      // current time not in the list, so there is nothing to do
    }
  }

  private static TimePeriod toMinute(LocalDateTime value) {
    return CachedMinute.toMinute(value);
  }

  private ChartPanel createChartPanel() {
    JFreeChart chart = createTimeSeriesChart("Ordered Quantity", "time", "Quantity [AK]", dataset);
    ChartPanel panel = new ChartPanel(chart);
    panel.setFillZoomRectangle(true);
    panel.setMouseWheelEnabled(true);
    return panel;
  }

  /**
   * Creates and returns a time series chart. A time series chart is an {@link XYPlot} with a
   * {@link DateAxis} for the x-axis and a {@link NumberAxis} for the y-axis. The default renderer
   * is an {@link XYLineAndShapeRenderer}.
   * <P>
   * A convenient dataset to use with this chart is a
   * {@link org.jfree.data.time.TimeSeriesCollection}.
   *
   * @param title the chart title (<code>null</code> permitted).
   * @param timeAxisLabel a label for the time axis (<code>null</code> permitted).
   * @param valueAxisLabel a label for the value axis (<code>null</code> permitted).
   * @param dataset the dataset for the chart (<code>null</code> permitted).
   *
   * @return A time series chart.
   */
  private JFreeChart createTimeSeriesChart(String title, String timeAxisLabel,
      String valueAxisLabel, XYDataset dataset) {
    return createTimeSeriesChart(title, timeAxisLabel, valueAxisLabel, dataset, true, true, false);
  }

  /**
   * Creates and returns a time series chart. A time series chart is an {@link XYPlot} with a
   * {@link DateAxis} for the x-axis and a {@link NumberAxis} for the y-axis. The default renderer
   * is an {@link XYLineAndShapeRenderer}.
   * <P>
   * A convenient dataset to use with this chart is a
   * {@link org.jfree.data.time.TimeSeriesCollection}.
   *
   * @param title the chart title (<code>null</code> permitted).
   * @param timeAxisLabel a label for the time axis (<code>null</code> permitted).
   * @param valueAxisLabel a label for the value axis (<code>null</code> permitted).
   * @param dataset the dataset for the chart (<code>null</code> permitted).
   * @param legend a flag specifying whether or not a legend is required.
   * @param tooltips configure chart to generate tool tips?
   * @param urls configure chart to generate URLs?
   *
   * @return A time series chart.
   */
  private JFreeChart createTimeSeriesChart(String title, String timeAxisLabel,
      String valueAxisLabel, XYDataset dataset, boolean legend, boolean tooltips, boolean urls) {

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

  /**
   * states of all relevant items
   * 
   * @author andre
   *
   */
  private static class ItemStates {
    /**
     * add item if it was not added before
     * 
     * @param orderItem the item to add
     * @param creationTime the creation time of that order (is the first time an event for that item
     *        occurs)
     */
    public void addFutureItem(OrderItem orderItem, LocalDateTime creationTime) {
      int commkey = orderItem.commkey();
      int quantity = orderItem.quantity();
      String state = STATE_FUTURE_ORDER;

      if (!commkeysToTimeBins.containsKey(commkey)) {
        commkeysToTimeBins.put(commkey, creationTime);
        barChartData.add(creationTime, state, quantity);
        commkeysToStates.put(commkey, state);
        commkeysToItems.put(commkey, orderItem);
      }
    }

    public void updateItemState(int commkey, String newState) {
      String oldState = getItemState(commkey);
      if (STATE_FUTURE_ORDER.equals(oldState)) {
        updateItemStateInternal(commkey, oldState, newState);
      } else if (STATE_ORDERED.equals(oldState)) {
        updateItemStateInternal(commkey, oldState, newState);
      } else {
        // do nothing on final states (PROCESSED and CANCELED)
      }
    }

    private void updateItemStateInternal(int commkey, String oldState, String newState) {
      if (oldState == null || newState == null) {
        return;
      }

      int quantity = commkeysToItems.get(commkey).quantity();
      LocalDateTime creationTime = commkeysToTimeBins.get(commkey);
      barChartData.add(creationTime, oldState, -1 * quantity);
      barChartData.add(creationTime, newState, quantity);
      commkeysToStates.put(commkey, newState);
    }

    public void clear() {
      barChartData.clear();
      commkeysToTimeBins.clear();
      commkeysToStates.clear();
      commkeysToItems.clear();
    }

    /**
     * @param commkey an commkey
     * @return current state or <code>null</code> if commkey (item) is unknown
     */
    private String getItemState(int commkey) {
      return commkeysToStates.get(commkey);
    }

    public SubgroupAdder getBarChartData() {
      return barChartData;
    }

    /**
     * contains the current bar chart data. dimensions: (time, state) -> sum(quantity)
     */
    private final SubgroupAdder barChartData = new SubgroupAdder();
    /** map each order item commkey to it's corresponding creation time */
    private final HashMap<Integer, LocalDateTime> commkeysToTimeBins = new HashMap<>();
    /** map all commkeys to there current state */
    private final HashMap<Integer, String> commkeysToStates = new HashMap<>();
    /** map all commkeys to there order item */
    private final HashMap<Integer, OrderItem> commkeysToItems = new HashMap<>();
  }

  private static interface RepeatedTask {
    public void execute();
  }

  private static class CachedMinute extends Minute {
    private static final long serialVersionUID = 1L;

    private static HashMap<LocalDateTime, CachedMinute> minutes = new HashMap<>();

    private boolean pegExecuted = false;

    public CachedMinute(int minute, int hour, int day, int month, int year) {
      super(minute, hour, day, month, year);
    }

    @Override
    public void peg(Calendar calendar) {
      if (!pegExecuted) {
        super.peg(calendar);
        pegExecuted = true;
      }
    }

    public static Minute toMinute(LocalDateTime time) {
      CachedMinute result = minutes.get(time);
      if (result == null) {
        result = new CachedMinute(time.getMinute(), time.getHour(), time.getDayOfMonth(),
            time.getMonthValue(), time.getYear());
        minutes.put(time, result);
      }
      return result;
    }
  }

  private static class RepeatedTaskThread extends Thread {
    private final RepeatedTask task;
    private boolean stopRequested = false;

    public RepeatedTaskThread(RepeatedTask task) {
      super("Animation");
      this.task = task;
    }

    @Override
    public void run() {
      try {
        while (!stopRequested) {
          Thread.sleep(50);
          task.execute();
        }
      } catch (InterruptedException e) {
        // forced exit thread
      }
    }

    /**
     * request to stop thread after the current task was finished. That can take a little while.
     */
    public void requestStop() {
      this.stopRequested = true;
    }
  }

  private static class StoppableThread {
    private final RepeatedTask task;
    private RepeatedTaskThread worker;

    public StoppableThread(RepeatedTask task) {
      this.task = task;
    }

    public void start() {
      if (this.worker == null || !this.worker.isAlive()) {
        // don't start a task twice
        worker = new RepeatedTaskThread(task);
        worker.start();
      }
    }

    public void stop() {
      if (worker != null) {
        worker.requestStop();
      }
    }
  }
}
