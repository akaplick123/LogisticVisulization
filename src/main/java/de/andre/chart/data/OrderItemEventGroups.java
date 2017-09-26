package de.andre.chart.data;

import de.andre.chart.data.groups.TimeToGroup;
import de.andre.chart.data.groups.TimedGroups;

public class OrderItemEventGroups extends TimedGroups<OrderItemEvent> {

    public OrderItemEventGroups(final LocalDateTimeLookUp lookup) {
	super(new TimeToGroup<OrderItemEvent>(e -> lookup.getTimeById(e.timestampId())));
    }

}
