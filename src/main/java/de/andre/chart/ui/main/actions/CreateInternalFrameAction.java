package de.andre.chart.ui.main.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

public class CreateInternalFrameAction implements ActionListener {
    private final JDesktopPane desktop;
    private final FrameBuilder frameBuilder;
    
    @FunctionalInterface
    public static interface FrameBuilder {
	public JInternalFrame buildFrame();
    }

    public CreateInternalFrameAction(JDesktopPane desktop, FrameBuilder frameBuilder) {
	this.desktop = desktop;
	this.frameBuilder = frameBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	JInternalFrame frame = frameBuilder.buildFrame();
	frame.setVisible(true); // necessary as of 1.3
	desktop.add(frame);
	try {
	    frame.setSelected(true);
	} catch (java.beans.PropertyVetoException ex) {
	}
    }
}
