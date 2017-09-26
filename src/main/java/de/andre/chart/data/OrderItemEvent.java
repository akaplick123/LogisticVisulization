package de.andre.chart.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@ToString
public class OrderItemEvent {
    private int commkey;
    private int timestampId;
    private byte newState;
}
