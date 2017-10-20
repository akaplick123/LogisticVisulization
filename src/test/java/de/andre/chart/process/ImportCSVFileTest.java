package de.andre.chart.process;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.andre.chart.data.Datacenter;
import de.andre.chart.data.LocalDateTimeLookUp;
import de.andre.chart.data.OrderItem;
import de.andre.chart.data.OrderItemEvent;
import de.andre.chart.data.OrderItemState;

public class ImportCSVFileTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testParseLineWithoutHeader() {
    FakeDatacenter dc = new FakeDatacenter();
    LocalDateTimeLookUp lookup = new LocalDateTimeLookUp();
    ImportCSVFile importer = new ImportCSVFile(lookup, dc);
    importer.parseLine(
        "4184325;1;4193025;2017-09-20 10:11;BonPrix;856943;Normalservice;1;2017-09-20 10:11;2017-09-20 16:19;HDL;2017-09-20 10:12;2017-09-20 16:19;2017-09-20 10:11;;");

    assertThat(dc.getOrderItems()).describedAs("items").isEmpty();
    assertThat(dc.getOrderItemEvents()).describedAs("events").isEmpty();
  }

  @Test
  public void testParseLine_SimpleOrderItem() {
    FakeDatacenter dc = new FakeDatacenter();
    LocalDateTimeLookUp lookup = new LocalDateTimeLookUp();
    ImportCSVFile importer = new ImportCSVFile(lookup, dc);
    importer.parseHeader(
        "\"OD_COMMKEY\";\"OD_KNOWN\";\"ORDER_COMMKEY\";\"ORDER_DATE\";\"COMPANYGROUP\";\"ID_ITEMOPTION\";\"DELIVERYCONDITION\";\"QUANTITY\";\"TS_HAS_NO_STOCK\";\"TS_HAS_STOCK\";\"LGR_BEREICH\";\"TS_ABWICKELBAR\";\"TS_FAKTURIERT\";\"TS_NALI\";\"TS_VERWORFEN\";\"TS_WARTEND\"");
    importer.parseLine(
        "4184325;1;4193025;2017-09-20 10:11;BonPrix;856943;Normalservice;1;2017-09-20 10:11;2017-09-20 16:19;HDL;2017-09-20 10:12;2017-09-20 16:19;2017-09-20 10:11;;");

    assertThat(dc.getOrderItems()).describedAs("items").hasSize(1);
    assertThat(dc.getOrderItems()).describedAs("items").hasOnlyOneElementSatisfying(o -> {
      assertThat(o.commkey()).isEqualTo(4184325);
    });

    assertThat(dc.getOrderItemEvents()).describedAs("events").anySatisfy(e -> {
      assertThat(e.commkey()).isEqualTo(4184325);
      assertThat(e.timestampId()).isEqualTo(lookup.findId(LocalDateTime.of(2017, 9, 20, 10, 11)));
      assertThat(e.newState()).isEqualTo(OrderItemState.WAITING.getId());
    });
    assertThat(dc.getOrderItemEvents()).describedAs("events").anySatisfy(e -> {
      assertThat(e.commkey()).isEqualTo(4184325);
      assertThat(e.timestampId()).isEqualTo(lookup.findId(LocalDateTime.of(2017, 9, 20, 16, 19)));
      assertThat(e.newState()).isEqualTo(OrderItemState.PROCESSED.getId());
    });
    assertThat(dc.getOrderItemEvents()).describedAs("events").hasSize(4);
  }

  @Test
  public void testParseLine_NoStockItem() {
    FakeDatacenter dc = new FakeDatacenter();
    LocalDateTimeLookUp lookup = new LocalDateTimeLookUp();
    ImportCSVFile importer = new ImportCSVFile(lookup, dc);
    importer.parseHeader(
        "\"OD_COMMKEY\";\"OD_KNOWN\";\"ORDER_COMMKEY\";\"ORDER_DATE\";\"COMPANYGROUP\";\"ID_ITEMOPTION\";\"DELIVERYCONDITION\";\"QUANTITY\";\"TS_HAS_NO_STOCK\";\"TS_HAS_STOCK\";\"LGR_BEREICH\";\"TS_ABWICKELBAR\";\"TS_FAKTURIERT\";\"TS_NALI\";\"TS_VERWORFEN\";\"TS_WARTEND\"");
    importer
        .parseLine("251931;0;0;;;0;;;;2017-09-18 00:34;HDL;2017-09-16 14:12;2017-09-18 00:34;;;");

    assertThat(dc.getOrderItems()).describedAs("items").hasSize(1);
    assertThat(dc.getOrderItems()).describedAs("items").hasOnlyOneElementSatisfying(o -> {
      assertThat(o.commkey()).isEqualTo(251931);
    });

    assertThat(dc.getOrderItemEvents()).describedAs("events").anySatisfy(e -> {
      assertThat(e.commkey()).isEqualTo(251931);
      assertThat(e.timestampId()).isEqualTo(lookup.findId(LocalDateTime.of(2017, 9, 16, 14, 12)));
      assertThat(e.newState()).isEqualTo(OrderItemState.PROCESSABLE.getId());
    });
    assertThat(dc.getOrderItemEvents()).describedAs("events").anySatisfy(e -> {
      assertThat(e.commkey()).isEqualTo(251931);
      assertThat(e.timestampId()).isEqualTo(lookup.findId(LocalDateTime.of(2017, 9, 18, 0, 34)));
      assertThat(e.newState()).isEqualTo(OrderItemState.PROCESSED.getId());
    });
    assertThat(dc.getOrderItemEvents()).describedAs("events").hasSize(2);
  }


  private static class FakeDatacenter extends Datacenter {
    private final ArrayList<OrderItem> orderItems = new ArrayList<>();
    private final ArrayList<OrderItemEvent> orderItemEvents = new ArrayList<>();

    @Override
    public void add(OrderItem item) {
      orderItems.add(item);
    }
    
    @Override
    public void add(OrderItemEvent event) {
      orderItemEvents.add(event);
    }

    public ArrayList<OrderItem> getOrderItems() {
      return orderItems;
    }
    
    public ArrayList<OrderItemEvent> getOrderItemEvents() {
      return orderItemEvents;
    }
  }
}
