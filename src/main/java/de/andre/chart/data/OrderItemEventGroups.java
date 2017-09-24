package de.andre.chart.data;

import de.andre.chart.data.groups.TimeToGroup;
import de.andre.chart.data.groups.TimedGroups;

public class OrderItemEventGroups extends TimedGroups<OrderItemEvent> {

    public OrderItemEventGroups() {
	super(new TimeToGroup<OrderItemEvent>(OrderItemEvent::timestamp));
    }

}
