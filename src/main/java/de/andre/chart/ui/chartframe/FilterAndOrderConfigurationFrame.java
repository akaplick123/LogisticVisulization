package de.andre.chart.ui.chartframe;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import de.andre.chart.ui.chartframe.helper.SimpleFilterAndOrderConfiguration;
import de.andre.chart.ui.component.JCheckBoxList;

public class FilterAndOrderConfigurationFrame extends JInternalFrameBase {
    private static final long serialVersionUID = 1L;
    private final DefaultListModel<JCheckBox> model = new DefaultListModel<>();
    private final SimpleFilterAndOrderConfiguration filter;
    private final HashMap<JCheckBox, String> boxToItemLookUp = new HashMap<>();

    public FilterAndOrderConfigurationFrame(SimpleFilterAndOrderConfiguration filter) {
	super();
	setTitle("Filter configuration");
	setResizable(false);
	setMaximizable(false);
	setIconifiable(false);
	setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

	initComponent();
	this.filter = filter;

	// add data
	for (String item: filter.getOrderedItems()) {
	    JCheckBox box = new JCheckBox(item);
	    box.setSelected(filter.isIncluded(item));
	    model.addElement(box);
	    boxToItemLookUp.put(box, item);
	}

	pack();
    }

    private void initComponent() {
	setLayout(new BorderLayout(5, 20));

	JCheckBoxList list = new JCheckBoxList(model);
	list.setBorder(BorderFactory.createTitledBorder(" options "));
	JButton bUp = new JButton("up");
	bUp.setEnabled(false);
	JButton bDown = new JButton("down");
	bDown.setEnabled(false);

	JPanel pUpDownButtons = new JPanel();
	pUpDownButtons.add(Box.createVerticalGlue());
	pUpDownButtons.add(bUp);
	pUpDownButtons.add(Box.createVerticalStrut(10));
	pUpDownButtons.add(bDown);
	pUpDownButtons.add(Box.createVerticalGlue());
	pUpDownButtons.setLayout(new BoxLayout(pUpDownButtons, BoxLayout.Y_AXIS));

	JPanel pOkButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
	JButton bOk = new JButton("apply and close");
	pOkButtons.add(bOk);

	add(list, BorderLayout.CENTER);
	add(pUpDownButtons, BorderLayout.EAST);
	add(pOkButtons, BorderLayout.SOUTH);

	list.addListSelectionListener(e -> {
	    bUp.setEnabled(!list.isSelectionEmpty() && list.getSelectedIndex() > 0);
	    bDown.setEnabled(!list.isSelectionEmpty() && list.getSelectedIndex() < model.size() - 1);
	});

	// add buttons action
	bUp.addActionListener(e -> {
	    int oldSelectionIndex = list.getSelectedIndex();
	    if (list.isSelectionEmpty() || oldSelectionIndex <= 0) {
		return;
	    }
	    JCheckBox othersElement = model.getElementAt(oldSelectionIndex - 1);

	    // swap both elements (remove others element)
	    model.remove(oldSelectionIndex - 1);
	    model.add(oldSelectionIndex, othersElement);
	    list.setSelectedIndex(oldSelectionIndex - 1);
	});
	bDown.addActionListener(e -> {
	    int oldSelectionIndex = list.getSelectedIndex();
	    if (list.isSelectionEmpty() || oldSelectionIndex >= model.size() - 1) {
		return;
	    }
	    JCheckBox othersElement = model.getElementAt(oldSelectionIndex + 1);

	    // swap both elements (remove others element)
	    model.remove(oldSelectionIndex + 1);
	    model.add(oldSelectionIndex, othersElement);
	    list.setSelectedIndex(oldSelectionIndex + 1);
	});
	bOk.addActionListener(e -> {
	    applyToFilter();
	    setVisible(false);
	    try {
		setClosed(true);
	    } catch (Exception e1) {
	    }
	});
    }

    private void applyToFilter() {
	for (int idx=0; idx < model.size(); idx++) {
	    JCheckBox cb = model.getElementAt(idx);
	    String item = boxToItemLookUp.get(cb);
	    if (cb.isSelected()) {
		filter.include(item);
	    } else {
		filter.exclude(item);
	    }
	    filter.moveTo(idx, item);
	}
    }
}
