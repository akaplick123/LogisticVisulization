package de.andre.chart.data.groups;

import java.time.LocalDateTime;

import de.andre.chart.data.groups.TimedGroups.GroupKeyExtractor;

public class TimeToGroup<V> implements GroupKeyExtractor<V> {

  @FunctionalInterface
  public static interface TimeExtractor<V> {
    public LocalDateTime getTime(V value);
  }

  private final TimeExtractor<V> extractor;

  public TimeToGroup(TimeExtractor<V> extractor) {
    this.extractor = extractor;
  }

  @Override
  public LocalDateTime getGroupOf(V value) {
    return toGroup(extractor.getTime(value));
  }

  private static LocalDateTime toGroup(LocalDateTime base) {
    return LocalDateTime.of(base.getYear(), base.getMonth(), base.getDayOfMonth(), base.getHour(),
        Math.floorDiv(base.getMinute(), 5) * 5);
  }

}
