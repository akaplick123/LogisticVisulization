package de.andre.chart.ui.chartframe;

import javax.swing.JInternalFrame;

public abstract class JInternalFrameBase extends JInternalFrame {
    private static final long serialVersionUID = 1L;

    private static final int xOffset = 30, yOffset = 30;
    private static int openFrameCount = 0;

    public JInternalFrameBase() {
	super("Document #" + (++openFrameCount), //
		true, // resizable
		true, // closable
		true, // maximizable
		true);// iconifiable
	
	// ...Then set the window size or call pack...
	setSize(500, 300);

	// Set the window's location.
	setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
    }
}
