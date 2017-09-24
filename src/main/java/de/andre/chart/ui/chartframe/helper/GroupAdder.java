package de.andre.chart.ui.chartframe.helper;

import java.time.LocalDateTime;
import java.util.HashMap;

import lombok.Getter;

@Getter
public class GroupAdder {
    private final HashMap<LocalDateTime, Integer> data = new HashMap<>();

    public void add(LocalDateTime time, int amount) {
	Integer oldValue = data.get(time);
	if (oldValue == null) {
	    this.data.put(time, amount);
	} else {
	    this.data.put(time, oldValue.intValue() + amount);
	}
    }
}
