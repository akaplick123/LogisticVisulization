package de.andre.chart.process;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;

import de.andre.chart.data.LocalDateTimeLookUp;
import de.andre.chart.data.OrderItem;
import de.andre.chart.data.OrderItemEvent;
import de.andre.chart.data.OrderItemState;
import lombok.extern.log4j.Log4j;

@Log4j
public class ImportCSVFile {
    private final Collection<OrderItem> orderItems = new ArrayList<>();
    private final Collection<OrderItemEvent> orderItemEvents = new ArrayList<>();
    private final LocalDateTimeLookUp dateTimeLookup;

    private int IDX_COMMKEY = -1;
    private int IDX_KNOWN_OD = -1;
    private int IDX_QUANTITY = -1;
    private int IDX_COMPANY = -1;
    private int IDX_TS_ORDER = -1;
    private int IDX_TS_PROCESSABLE = -1;
    private int IDX_TS_PROCESSED = -1;
    private int IDX_TS_LOST_STOCK = -1;
    private int IDX_TS_CANCELED = -1;
    private int IDX_TS_WAITING = -1;

    public ImportCSVFile(LocalDateTimeLookUp dateTimeLookup) {
	this.dateTimeLookup = dateTimeLookup;
    }

    public void parseHeader(String line) {
	// "OD_COMMKEY";"OD_KNOWN";"ORDER_COMMKEY";"ORDER_DATE";
	// "COMPANYGROUP";"ID_ITEMOPTION";"DELIVERYCONDITION";"QUANTITY";
	// "TS_HAS_NO_STOCK";"TS_HAS_STOCK";"LGR_BEREICH";"TS_ABWICKELBAR";
	// "TS_FAKTURIERT";"TS_NALI";"TS_VERWORFEN";"TS_WARTEND"
	String[] parts = line.split(";");
	for (int idx = 0; idx < parts.length; idx++) {
	    String part = parts[idx];

	    IDX_COMMKEY = isOneOf(IDX_COMMKEY, idx, part, "OD_COMMKEY");
	    IDX_KNOWN_OD = isOneOf(IDX_KNOWN_OD, idx, part, "OD_KNOWN");
	    IDX_QUANTITY = isOneOf(IDX_QUANTITY, idx, part, "QUANTITY");
	    IDX_COMPANY = isOneOf(IDX_COMPANY, idx, part, "COMPANYGROUP");
	    IDX_TS_ORDER = isOneOf(IDX_TS_ORDER, idx, part, "ORDER_DATE");
	    IDX_TS_PROCESSABLE = isOneOf(IDX_TS_PROCESSABLE, idx, part, "TS_ABWICKELBAR");
	    IDX_TS_PROCESSED = isOneOf(IDX_TS_PROCESSED, idx, part, "TS_FAKTURIERT");
	    IDX_TS_LOST_STOCK = isOneOf(IDX_TS_LOST_STOCK, idx, part, "TS_NALI");
	    IDX_TS_CANCELED = isOneOf(IDX_TS_CANCELED, idx, part, "TS_VERWORFEN");
	    IDX_TS_WAITING = isOneOf(IDX_TS_WAITING, idx, part, "TS_WARTEND");
	}
    }

    private static int isOneOf(int currentValue, int currentIdx, String currentPart, String... pattern) {
	int result = currentValue;
	for (String p : pattern) {
	    if (currentPart.equalsIgnoreCase(p) || currentPart.equalsIgnoreCase("\"" + p + "\"")) {
		result = currentIdx;
	    }
	}
	return result;
    }

    public void parseLine(String line) {
	String[] parts = line.split(";");

	int commkey = extractInt(-1, IDX_COMMKEY, parts);
	if (commkey >= 0) {
	    int quantity = extractInt(1, IDX_QUANTITY, parts);
	    int odKnown = extractInt(0, IDX_KNOWN_OD, parts);
	    String company = extractString("N/A", IDX_COMPANY, parts);

	    OrderItem item = new OrderItem().commkey(commkey).quantity(quantity).fromNali(odKnown == 0)
		    .company(company);
	    this.orderItems.add(item);

	    extractAndAddEvent(IDX_TS_ORDER, OrderItemState.WAITING, commkey, parts);
	    extractAndAddEvent(IDX_TS_PROCESSABLE, OrderItemState.PROCESSABLE, commkey, parts);
	    extractAndAddEvent(IDX_TS_PROCESSED, OrderItemState.PROCESSED, commkey, parts);
	    extractAndAddEvent(IDX_TS_LOST_STOCK, OrderItemState.NO_STOCK, commkey, parts);
	    extractAndAddEvent(IDX_TS_CANCELED, OrderItemState.CANCELED, commkey, parts);
	    extractAndAddEvent(IDX_TS_WAITING, OrderItemState.WAITING, commkey, parts);
	}
    }

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private void extractAndAddEvent(int idx, OrderItemState newState, int commkey, String[] parts) {
	if (0 <= idx && idx < parts.length) {
	    String part = parts[idx];
	    if (part != null && part.length() > 0) {
		try {
		    Integer timestampId = dateTimeLookup.findId(part);
		    if (timestampId == null) {
			LocalDateTime timestamp = LocalDateTime.parse(part, dtf);
			timestampId = dateTimeLookup.add(part, timestamp);
		    }
		    OrderItemEvent event = new OrderItemEvent().commkey(commkey).timestampId(timestampId)
			    .newState(newState.getId());
		    this.orderItemEvents.add(event);
		} catch (DateTimeParseException e) {
		    log.trace("parse", e);
		}
	    }
	}
    }

    private static String extractString(String defaultValue, int idx, String[] parts) {
	if (0 <= idx && idx < parts.length) {
	    String part = parts[idx];
	    if (part != null && part.length() > 0) {
		return part;
	    }
	}
	return defaultValue;
    }

    private static int extractInt(int defaultValue, int idx, String[] parts) {
	if (0 <= idx && idx < parts.length) {
	    String part = parts[idx];
	    if (part != null && part.length() > 0) {
		return Integer.parseInt(part);
	    }
	}
	return defaultValue;
    }

    public Collection<OrderItem> getOrderItems() {
	return orderItems;
    }

    public Collection<OrderItemEvent> getOrderItemEvents() {
	return orderItemEvents;
    }

    public int getNumberOfItems() {
	return orderItems.size();
    }

    public int getNumberOfEvents() {
	return orderItemEvents.size();
    }
}
