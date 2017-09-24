package de.andre.chart.data;

import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class Datacenter {
    private final OrderItems items = new OrderItems();
    private final OrderItemEventGroups events = new OrderItemEventGroups();

    public void clear() {
	items.clear();
	events.clear();
    }
}
