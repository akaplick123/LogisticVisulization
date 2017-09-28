package de.andre.chart.data;

import lombok.Getter;

@Getter
public enum OrderItemState {
    NONE_EXISTENT(0), WAITING(1), NO_STOCK(2), PROCESSABLE(3), PROCESSED(4), CANCELED(5);

    private final byte sortId;

    private OrderItemState(int sortId) {
	this.sortId = (byte)sortId;
    }

    public byte getId() {
	return sortId;
    }

    public static OrderItemState of(byte stateId) {
	for (OrderItemState state: values()) {
	    if (state.getId() == stateId) {
		return state;
	    }
	}
	return null;
    }
}
