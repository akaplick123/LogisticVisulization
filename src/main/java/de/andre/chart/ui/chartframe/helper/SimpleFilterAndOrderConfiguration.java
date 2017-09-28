package de.andre.chart.ui.chartframe.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.ToString;

@ToString
public class SimpleFilterAndOrderConfiguration {
	private final List<String> allOrderedItems = new ArrayList<>();
	private final HashSet<String> allItems = new HashSet<>();
	private final Set<String> excludedItems = new HashSet<>();

	public void add(String item) {
		if (!this.allItems.contains(item)) {
			this.allOrderedItems.add(item);
			this.allItems.add(item);
		}
	}

	public void addAll(Collection<String> items) {
		items.forEach(this::add);
	}

	public List<String> getOrderedItems() {
		return allOrderedItems;
	}

	public boolean isIncluded(String item) {
		return !excludedItems.contains(item);
	}

	public boolean isExcluded(String item) {
		return excludedItems.contains(item);
	}

	public void exclude(String item) {
		this.excludedItems.add(item);
	}

	public void include(String item) {
		this.excludedItems.remove(item);
	}

	public void moveTo(int idx, String item) {
		int currentIdx = this.allOrderedItems.indexOf(item);
		if (currentIdx == idx) {
			// item is at the correct position
			return;
		}
		if (currentIdx > idx) {
			// move item one position upwards (decrease index) and recurse
			String otherItem = this.allOrderedItems.get(currentIdx - 1);
			this.allOrderedItems.remove(currentIdx - 1);
			this.allOrderedItems.add(currentIdx, otherItem);
		} else if (currentIdx < idx) {
			// move item one position downwards (increase index) and recurse
			String otherItem = this.allOrderedItems.get(currentIdx + 1);
			this.allOrderedItems.remove(currentIdx + 1);
			this.allOrderedItems.add(currentIdx, otherItem);
		}
		moveTo(idx, item);
	}
}
