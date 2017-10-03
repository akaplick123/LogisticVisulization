package de.andre.chart.data.groups;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import de.andre.chart.ui.chartframe.helper.SortedList;

public class TimedGroups<V> {
  private final HashMap<LocalDateTime, SortedList<V>> groups = new HashMap<>();
  private final GroupKeyExtractor<V> grouper;

  @FunctionalInterface
  public static interface GroupKeyExtractor<V> {
    public LocalDateTime getGroupOf(V value);
  }

  public TimedGroups(GroupKeyExtractor<V> grouper) {
    this.grouper = grouper;
  }

  public void add(V value) {
    LocalDateTime groupKey = groupOf(value);
    SortedList<V> group = groups.get(groupKey);
    if (group == null) {
      group = new SortedList<>();
      groups.put(groupKey, group);
    }
    group.add(value);
  }

  private LocalDateTime groupOf(V value) {
    return grouper.getGroupOf(value);
  }

  public void addAll(Collection<V> values) {
    values.forEach(this::add);
  }

  public Stream<LocalDateTime> getAllKeys() {
    return getAllSortedKeys().stream();
  }

  public List<LocalDateTime> getAllSortedKeys() {
    List<LocalDateTime> result = new ArrayList<>(groups.keySet());
    Collections.sort(result);
    return result;
  }

  public Stream<LocalDateTime> getAllKeysBetween(final LocalDateTime minDateExclusive,
      final LocalDateTime maxDateInclusive) {
    return getAllKeys() //
        .filter(time -> time.isAfter(minDateExclusive)) //
        .filter(time -> !time.isAfter(maxDateInclusive));
  }

  public Stream<V> getValues(LocalDateTime timeGroup) {
    SortedList<V> group = groups.get(timeGroup);
    if (group == null) {
      return new SortedList<V>().stream();
    }
    return group.stream();
  }

  public List<V> getSortedEvents(LocalDateTime timeGroup, Comparator<V> comp) {
    SortedList<V> group = groups.get(timeGroup);
    if (group == null) {
      return new SortedList<V>();
    }
    group.singleSort(comp);
    return group;
  }

  public void clear() {
    groups.clear();
  }
}
