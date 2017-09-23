package de.andre.chart.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OrderItemEventTest {

    private static final int COMMKEY1 = 1000;
    private static final int COMMKEY2 = 2000;
    private static final OrderItemState STATE1 = OrderItemState.NONE_EXISTENT;
    private static final OrderItemState STATE2 = OrderItemState.PROCESSABLE;
    private static final LocalDateTime TIME1 = LocalDateTime.of(2017, 9, 20, 14, 45);
    private static final LocalDateTime TIME2 = LocalDateTime.of(2017, 9, 21, 16, 15);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCompareTo_Same() {
	OrderItemEvent a = new OrderItemEvent().commkey(COMMKEY1).newState(STATE1).timestamp(TIME1);
	OrderItemEvent b = new OrderItemEvent().commkey(COMMKEY1).newState(STATE1).timestamp(TIME1);

	assertThat(a.compareTo(b)).isEqualTo(0);
    }

    @Test
    public void testCompareTo_SortDiffersByTime() {
	OrderItemEvent a = new OrderItemEvent().commkey(COMMKEY1).newState(STATE1).timestamp(TIME1);
	OrderItemEvent b = new OrderItemEvent().commkey(COMMKEY1).newState(STATE1).timestamp(TIME2);
	List<OrderItemEvent> list = Arrays.asList(b, a);
	Collections.sort(list);

	assertThat(list.get(0)).isEqualTo(a);
	assertThat(list.get(1)).isEqualTo(b);
    }

    @Test
    public void testCompareTo_SortDiffersByState() {
	OrderItemEvent a = new OrderItemEvent().commkey(COMMKEY1).newState(STATE1).timestamp(TIME1);
	OrderItemEvent b = new OrderItemEvent().commkey(COMMKEY1).newState(STATE2).timestamp(TIME1);
	List<OrderItemEvent> list = Arrays.asList(b, a);
	Collections.sort(list);

	assertThat(list.get(0)).isEqualTo(a);
	assertThat(list.get(1)).isEqualTo(b);
    }

    @Test
    public void testCompareTo_SortDiffersByCommkey() {
	OrderItemEvent a = new OrderItemEvent().commkey(COMMKEY1).newState(STATE1).timestamp(TIME1);
	OrderItemEvent b = new OrderItemEvent().commkey(COMMKEY2).newState(STATE1).timestamp(TIME1);
	List<OrderItemEvent> list = Arrays.asList(b, a);
	Collections.sort(list);

	assertThat(list.get(0)).isEqualTo(a);
	assertThat(list.get(1)).isEqualTo(b);
    }

}
