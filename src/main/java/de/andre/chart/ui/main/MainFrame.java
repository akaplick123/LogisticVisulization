package de.andre.chart.ui.main;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.annotation.PostConstruct;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.springframework.stereotype.Component;

import de.andre.chart.ui.main.actions.CreateNewInternalFrameAction;
import de.andre.chart.ui.main.actions.LoadFileAction;
import de.andre.chart.ui.main.actions.QuitAction;

@Component
public class MainFrame extends JFrame {
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
	setContentPane(desktop);
	setJMenuBar(createMenuBar());

	// Make dragging a little faster but perhaps uglier.
	desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
    }

    private JMenuBar createMenuBar() {
	JMenuBar menuBar = new JMenuBar();

	// Set up the lone menu.
	JMenu menu = new JMenu("Document");
	menu.setMnemonic(KeyEvent.VK_D);
	menuBar.add(menu);

	// Set up the first menu item.
	JMenuItem menuItem = new JMenuItem("New");
	menuItem.setMnemonic(KeyEvent.VK_N);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
	menuItem.addActionListener(new CreateNewInternalFrameAction(desktop));
	menu.add(menuItem);

	menuItem = new JMenuItem("Load");
	menuItem.setMnemonic(KeyEvent.VK_L);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
	menuItem.addActionListener(new LoadFileAction(desktop, this));
	menu.add(menuItem);

	// Set up the second menu item.
	menuItem = new JMenuItem("Quit");
	menuItem.setMnemonic(KeyEvent.VK_Q);
	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
	menuItem.addActionListener(new QuitAction());
	menu.add(menuItem);

	return menuBar;
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
