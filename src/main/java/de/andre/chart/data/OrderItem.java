package de.andre.chart.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class OrderItem {
    private int commkey;
    private OrderItemState orderItemState = OrderItemState.NONE_EXISTENT;
    
    public void applyEvent(OrderItemEvent event) {
	if (event.commkey() == commkey()) {
	    orderItemState(event.newState());
	}
    }
}
