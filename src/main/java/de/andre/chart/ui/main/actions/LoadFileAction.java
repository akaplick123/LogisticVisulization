package de.andre.chart.ui.main.actions;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class LoadFileAction implements ActionListener {
    private final JDesktopPane desktop;
    private final JFrame mainFrame;
    private File lastSelectedDirectory = null;

    public LoadFileAction(JDesktopPane desktop, JFrame mainFrame) {
	this.desktop = desktop;
	this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	JFileChooser fc = new JFileChooser(lastSelectedDirectory);
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setAcceptAllFileFilterUsed(true);
	fc.setMultiSelectionEnabled(true);
	FileFilter csvFileFilter = new FileNameExtensionFilter("CSV files", "csv");
	fc.addChoosableFileFilter(csvFileFilter);
	fc.setFileFilter(csvFileFilter);

	int rc = fc.showOpenDialog(desktop);
	lastSelectedDirectory = fc.getCurrentDirectory();
	if (rc == JFileChooser.APPROVE_OPTION) {
	    File[] files = fc.getSelectedFiles();
	    if (files != null && files.length > 0) {
		loadFiles(Arrays.asList(files));
	    }
	}
    }

    private void loadFiles(List<File> files) {
	files.forEach(System.out::println);

	JProgressDialog dialog = new JProgressDialog(mainFrame);
	dialog.pack();
	dialog.setBounds(center(dialog, mainFrame));
	dialog.setVisible(true);
    }

    private Rectangle center(JDialog dialog, JFrame parent) {
	int widthParent = parent.getWidth();
	int heightParent = parent.getHeight();
	int widthDialog = dialog.getWidth();
	int heigthDialog = dialog.getHeight();

	int x = (widthParent - widthDialog) / 2 + parent.getX();
	int y = (heightParent - heigthDialog) / 2 + parent.getY();
	return new Rectangle(x, y, widthDialog, heigthDialog);
    }

    private static class JProgressDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public JProgressDialog(JFrame mainFrame) {
	    super(mainFrame);
	    setModal(true);
	    setTitle("Progress...");
	    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

	    Container contentPane = new JPanel();
	    contentPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    contentPane.setLayout(new GridLayout(3, 1, 5, 5));
	    JLabel lDesc = new JLabel("loading files...");
	    JProgressBar progressBar = new JProgressBar();
	    progressBar.setIndeterminate(true);
	    JButton bAbort = new JButton("abort");

	    JPanel pProgressBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
	    pProgressBar.add(progressBar);
	    JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
	    pButtons.add(bAbort);

	    contentPane.add(lDesc);
	    contentPane.add(pProgressBar);
	    contentPane.add(pButtons);

	    setContentPane(contentPane);
	    pack();
	}
    }
}
