package de.andre.chart.ui.main;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.annotation.PostConstruct;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.springframework.stereotype.Component;

@Component
public class MainFrame extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JDesktopPane desktop;

    @PostConstruct
    public void init() {
	setTitle("InternalFrameDemo");

	// Make the big window be indented 50 pixels from each edge
	// of the screen.
	final int insetPercent = 5;
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setBounds(insetPercent * screenSize.width / 100, //
		insetPercent * screenSize.height / 100, //
		(100 - 2 * insetPercent) * screenSize.width / 100, //
		(100 - 2 * insetPercent) * screenSize.height / 100);

	// Set up the GUI.
	desktop = new JDesktopPane(); // a specialized layered pane
	createFrame(); // create first "window"
	setContentPane(desktop);
	setJMenuBar(createMenuBar());

	// Make dragging a little faster but perhaps uglier.
	desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
    }

    protected JMenuBar createMenuBar() {
	JMenuBar menuBar = new JMenuBar();

	// Set up the lone menu.
	JMenu menu = new JMenu("Document");
	menu.setMnemonic(KeyEvent.VK_D);
	menuBar.add(menu);

	// Set up the first menu item.
	JMenuItem menuItem = new JMenuItem("New");
	menuItem.setMnemonic(KeyEvent.VK_N);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
	menuItem.setActionCommand("new");
	menuItem.addActionListener(this);
	menu.add(menuItem);

	// Set up the second menu item.
	menuItem = new JMenuItem("Quit");
	menuItem.setMnemonic(KeyEvent.VK_Q);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
	menuItem.setActionCommand("quit");
	menuItem.addActionListener(this);
	menu.add(menuItem);

	return menuBar;
    }

    // React to menu selections.
    public void actionPerformed(ActionEvent e) {
	if ("new".equals(e.getActionCommand())) { // new
	    createFrame();
	} else { // quit
	    quit();
	}
    }

    // Create a new internal frame.
    protected void createFrame() {
	MyInternalFrame frame = new MyInternalFrame();
	frame.setVisible(true); // necessary as of 1.3
	desktop.add(frame);
	try {
	    frame.setSelected(true);
	} catch (java.beans.PropertyVetoException e) {
	}
    }

    // Quit the application.
    protected void quit() {
	System.exit(0);
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event-dispatching thread.
     */
    public void createAndShowGUI() {
	// Schedule a job for the event-dispatching thread:
	// creating and showing this application's GUI.
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Display the window.
		setAutoRequestFocus(true);
		setVisible(true);
	    }
	});
    }
}
