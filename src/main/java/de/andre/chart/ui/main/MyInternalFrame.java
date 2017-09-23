package de.andre.chart.ui.main;

import javax.swing.JInternalFrame;

public class MyInternalFrame extends JInternalFrame {
    private static final long serialVersionUID = 1L;
    static int openFrameCount = 0;
    static final int xOffset = 30, yOffset = 30;

    public MyInternalFrame() {
	super("Document #" + (++openFrameCount), true, // resizable
		true, // closable
		true, // maximizable
		true);// iconifiable

	// ...Create the GUI and put it in the window...

	// ...Then set the window size or call pack...
	setSize(300, 300);

	// Set the window's location.
	setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
    }
}