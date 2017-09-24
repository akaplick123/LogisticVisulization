package de.andre.chart.ui.main.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;

import de.andre.chart.ui.main.MyInternalFrame;

public class CreateNewInternalFrameAction implements ActionListener {
    private final JDesktopPane desktop;
    
    public CreateNewInternalFrameAction(JDesktopPane desktop) {
	this.desktop = desktop;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	MyInternalFrame frame = new MyInternalFrame();
	frame.setVisible(true); // necessary as of 1.3
	desktop.add(frame);
	try {
	    frame.setSelected(true);
	} catch (java.beans.PropertyVetoException ex) {
	}
    }
}
