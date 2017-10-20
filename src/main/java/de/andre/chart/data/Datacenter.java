package de.andre.chart.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class Datacenter {
  @Autowired
  private LocalDateTimeLookUp lookup;

  private OrderItems items = new OrderItems();
  private OrderItemEventGroups events;

  public void clear() {
    getItems().clear();
    getEvents().clear();
  }

  public OrderItem getItemByCommkey(int commkey) {
    return items.getByCommkey(commkey);
  }

  public OrderItemEventGroups getEvents() {
    if (events == null) {
      events = new OrderItemEventGroups(lookup);
    }
    return events;
  }

  public void add(OrderItem item) {
    this.items.add(item);
  }

  public void add(OrderItemEvent event) {
    this.events.add(event);
  }
}
