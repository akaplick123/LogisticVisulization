package de.andre.chart.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@ToString
public class OrderItem {
    private int commkey;
    private int quantity;
    private boolean fromNali;
    private String company;
    private byte orderItemState = OrderItemState.NONE_EXISTENT.getId();
    
    public void applyEvent(OrderItemEvent event) {
	if (event.commkey() == commkey()) {
	    orderItemState(event.newState());
	}
    }
}
