package de.andre.chart.data;

import java.util.Collection;
import java.util.HashMap;

public class OrderItems {
    private final HashMap<Integer, OrderItem> items = new HashMap<>();

    public void add(OrderItem item) {
	this.items.put(item.commkey(), item);
    }

    public void addAll(Collection<OrderItem> items) {
	items.forEach(this::add);
    }

    public OrderItem getByCommkey(int commkey) {
	return items.get(commkey);
    }
}
