package de.andre.chart.ui.main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

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

import de.andre.chart.ui.chartframe.JInternalFrameBase;

public class MyInternalFrame extends JInternalFrameBase {
    private static final long serialVersionUID = 1L;

    private TimingThread worker = null;
    private TimeTableXYDataset dataset = new TimeTableXYDataset();
    private Minute currentMinute = null;
    private int value1 = 0;
    private int value2 = 0;

    public MyInternalFrame() {
	super();

	// ...Create the GUI and put it in the window...
	ChartPanel panel = createChartPanel();
	setLayout(new BorderLayout());
	add(panel, BorderLayout.CENTER);

	JPanel pThreadController = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
	JButton bStop = new JButton("stop");
	bStop.setEnabled(false);
	JButton bPause = new JButton("pause");
	bPause.setEnabled(false);
	JButton bPlay = new JButton("start");
	JButton bSlower = new JButton("slower");
	bSlower.setEnabled(false);
	JButton bFaster = new JButton("faster");
	bFaster.setEnabled(bSlower.isEnabled());
	
	pThreadController.add(bStop);
	pThreadController.add(bPause);
	pThreadController.add(bPlay);
	pThreadController.add(bSlower);
	pThreadController.add(bFaster);
	add(pThreadController, BorderLayout.SOUTH);

	bStop.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (worker != null) {
		    worker.interrupt();
		    worker = null;
		}
		bStop.setEnabled(false);
		bPause.setEnabled(false);
		bPause.setText("pause");
		bPlay.setEnabled(true);
		bPlay.setText("restart");
		bSlower.setEnabled(false);
		bFaster.setEnabled(bSlower.isEnabled());
	    }
	});

	bPause.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (worker != null && worker.isPaused()) {
		    worker.doUnpause();
		    bPause.setText("pause");
		} else if (worker != null) {
		    worker.doPause();
		    bPause.setText("unpause");
		}
	    }
	});

	bPlay.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		bStop.setEnabled(true);
		bPause.setEnabled(true);
		bPause.setText("pause");
		bPlay.setEnabled(false);
		bSlower.setEnabled(true);
		bFaster.setEnabled(bSlower.isEnabled());

		if (worker != null && worker.isAlive()) {
		    worker.interrupt();
		    worker = null;
		}
		// reset dataset and time
		initializeDataset();

		// create one
		worker = new TimingThread();
		worker.start();
	    }
	});
	
	bSlower.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (worker != null) {
		    worker.decreaseSpeed();
		}
	    }
	});

	bFaster.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (worker != null) {
		    worker.increaseSpeed();
		}
	    }
	});

	addInternalFrameListener(new InternalFrameAdapter() {
	    @Override
	    public void internalFrameClosing(InternalFrameEvent e) {
		bStop.doClick();
		dataset.clear();
	    }
	});
    }

    protected ChartPanel createChartPanel() {
	// TimeSeriesCollection dataset = new TimeSeriesCollection();
	// TimeSeries series1 = new TimeSeries("Cat 1");
	// series1.add(new Minute(10, 14, 13, 10, 2017), 10);
	// series1.add(new Minute(12, 14, 13, 10, 2017), 15);
	// series1.add(new Minute(13, 14, 13, 10, 2017), 20);
	// series1.add(new Minute(20, 14, 13, 10, 2017), 7);
	// dataset.addSeries(series1);
	//
	// TimeSeries series2 = new TimeSeries("Cat 2");
	// series2.add(new Minute(10, 14, 13, 10, 2017), 5);
	// series2.add(new Minute(12, 14, 13, 10, 2017), 10);
	// series2.add(new Minute(13, 14, 13, 10, 2017), 10);
	// series2.add(new Minute(20, 14, 13, 10, 2017), 24);
	// dataset.addSeries(series2);

	// TimeTableXYDataset dataset = new TimeTableXYDataset();
	initializeDataset();

	JFreeChart chart = createTimeSeriesChart("title", "time", "value", dataset);
	ChartPanel panel = new ChartPanel(chart);
	panel.setFillZoomRectangle(true);
	panel.setMouseWheelEnabled(true);
	return panel;
    }

    private void initializeDataset() {
	dataset.clear();
	dataset.add(new Minute(10, 14, 13, 10, 2017), 10, "Cat 1");
	dataset.add(new Minute(12, 14, 13, 10, 2017), 15, "Cat 1");
	dataset.add(new Minute(13, 14, 13, 10, 2017), 20, "Cat 1");
	dataset.add(new Minute(20, 14, 13, 10, 2017), 7, "Cat 1");
	dataset.add(new Minute(10, 14, 13, 10, 2017), 5, "Cat 2");
	dataset.add(new Minute(12, 14, 13, 10, 2017), 10, "Cat 2");
	dataset.add(new Minute(13, 14, 13, 10, 2017), 10, "Cat 2");
	dataset.add(new Minute(20, 14, 13, 10, 2017), 24, "Cat 2");

	currentMinute = new Minute(20, 14, 13, 10, 2017);
	value1 = 20;
	value2 = 24;
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

    private class TimingThread extends Thread {
	private boolean paused = false;
	private Random r = new Random();
	private int milliSecondsWait = 500;

	@Override
	public void run() {
	    try {
		int millisWaited = 0;
		while (true) {
		    if (!isPaused() && millisWaited >= milliSecondsWait) {
			currentMinute = (Minute) currentMinute.next();
			value1 += r.nextInt(10) - 5;
			value2 += Math.round(r.nextGaussian() * 5);		
			dataset.add(currentMinute, value1, "Cat 1");
			dataset.add(currentMinute, value2, "Cat 2");
			millisWaited = 0;
		    }

		    // sleep 500ms
		    TimeUnit.MILLISECONDS.sleep(100);
		    millisWaited += 100;
		}
	    } catch (InterruptedException e) {
		// exit loop
	    }
	}

	public void doPause() {
	    synchronized (this) {
		this.paused = true;
	    }
	}

	public void doUnpause() {
	    synchronized (this) {
		this.paused = false;
	    }
	}

	public boolean isPaused() {
	    synchronized (this) {
		return paused;
	    }
	}
	
	public void increaseSpeed() {
	    milliSecondsWait /= 1.3d;
	    milliSecondsWait = Math.max(50, milliSecondsWait);
	}
	
	public void decreaseSpeed() {
	    milliSecondsWait *= 1.3d;
	}
    }
}
