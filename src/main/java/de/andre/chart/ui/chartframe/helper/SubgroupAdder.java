package de.andre.chart.ui.chartframe.helper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

public class SubgroupAdder {
    private final HashMap<String, HashMap<LocalDateTime, Integer>> data = new HashMap<>();

    public void add(LocalDateTime time, String subgroup, int amount) {
	HashMap<LocalDateTime, Integer> group = this.data.get(subgroup);
	if (group == null) {
	    group = new HashMap<>();
	    group.put(time, amount);
	    this.data.put(subgroup, group);
	} else {
	    // group is not null
	    Integer oldValue = group.get(time);
	    if (oldValue == null) {
		group.put(time, amount);
	    } else {
		group.put(time, oldValue.intValue() + amount);
	    }
	}
    }

    public Collection<Tripel> getEntries() {
	ArrayList<Tripel> result = new ArrayList<>();
	for (Entry<String, HashMap<LocalDateTime, Integer>> e1: data.entrySet()) {
	    for (Entry<LocalDateTime, Integer> e2: e1.getValue().entrySet()) {
		result.add(new Tripel(e2.getKey(), e1.getKey(), e2.getValue()));
	    }
	}
	return result;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class Tripel {
	private final LocalDateTime time;
	private final String subgroup;
	private final int value;
    }
}
