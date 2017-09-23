package de.andre.chart.data;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@ToString
public class OrderItemEvent implements Comparable<OrderItemEvent> {
    private int commkey;
    private LocalDateTime timestamp;
    private OrderItemState newState;

    public int compareTo(OrderItemEvent o) {
	int result = 0;
	if (result == 0) {
	    result = timestamp.compareTo(o.timestamp);
	}
	if (result == 0) {
	    result = newState.getSortId() - o.newState.getSortId();
	}
	if (result == 0) {
	    result = commkey - o.commkey;
	}
	return result;
    }
}
