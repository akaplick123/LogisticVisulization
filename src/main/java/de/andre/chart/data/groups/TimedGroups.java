package de.andre.chart.data.groups;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class TimedGroups<V> {
    private final HashMap<LocalDateTime, List<V>> groups = new HashMap<>();
    private final GroupKeyExtractor<V> grouper;

    @FunctionalInterface
    public static interface GroupKeyExtractor<V> {
	public LocalDateTime getGroupOf(V value);
    }

    public TimedGroups(GroupKeyExtractor<V> grouper) {
	this.grouper = grouper;
    }

    public void add(V value) {
	LocalDateTime groupKey = grouper.getGroupOf(value);
	List<V> group = groups.get(groupKey);
	if (group == null) {
	    group = new ArrayList<>();
	    groups.put(groupKey, group);
	}
	group.add(value);
    }

    public Stream<LocalDateTime> getAllKeys() {
	List<LocalDateTime> result = new ArrayList<>(groups.keySet());
	Collections.sort(result);
	return result.stream();
    }

    public Stream<LocalDateTime> getAllKeysBetween(final LocalDateTime minDateExclusive,
	    final LocalDateTime maxDateInclusive) {
	return getAllKeys() //
		.filter(time -> time.isAfter(minDateExclusive)) //
		.filter(time -> !time.isAfter(maxDateInclusive));
    }

    public Stream<V> getValues(LocalDateTime groupKey) {
	List<V> group = groups.get(groupKey);
	if (group == null) {
	    return new ArrayList<V>().stream();
	}
	return group.stream();
    }
}
