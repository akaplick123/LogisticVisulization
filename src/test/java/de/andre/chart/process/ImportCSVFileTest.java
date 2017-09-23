package de.andre.chart.process;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.andre.chart.data.OrderItemState;

public class ImportCSVFileTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testParseLineWithoutHeader() {
	ImportCSVFile importer = new ImportCSVFile();
	importer.parseLine(
		"4184325;1;4193025;2017-09-20 10:11;BonPrix;856943;Normalservice;1;2017-09-20 10:11;2017-09-20 16:19;HDL;2017-09-20 10:12;2017-09-20 16:19;2017-09-20 10:11;;");

	assertThat(importer.getOrderItems()).describedAs("items").isEmpty();
	assertThat(importer.getOrderItemEvents()).describedAs("events").isEmpty();
    }

    @Test
    public void testParseLine_SimpleOrderItem() {
	ImportCSVFile importer = new ImportCSVFile();
	importer.parseHeader(
		"\"OD_COMMKEY\";\"OD_KNOWN\";\"ORDER_COMMKEY\";\"ORDER_DATE\";\"COMPANYGROUP\";\"ID_ITEMOPTION\";\"DELIVERYCONDITION\";\"QUANTITY\";\"TS_HAS_NO_STOCK\";\"TS_HAS_STOCK\";\"LGR_BEREICH\";\"TS_ABWICKELBAR\";\"TS_FAKTURIERT\";\"TS_NALI\";\"TS_VERWORFEN\";\"TS_WARTEND\"");
	importer.parseLine(
		"4184325;1;4193025;2017-09-20 10:11;BonPrix;856943;Normalservice;1;2017-09-20 10:11;2017-09-20 16:19;HDL;2017-09-20 10:12;2017-09-20 16:19;2017-09-20 10:11;;");

	assertThat(importer.getOrderItems()).describedAs("items").hasSize(1);
	assertThat(importer.getOrderItems()).describedAs("items").hasOnlyOneElementSatisfying(o -> {
	    assertThat(o.commkey()).isEqualTo(4184325);
	});

	assertThat(importer.getOrderItemEvents()).describedAs("events").anySatisfy(e -> {
	    assertThat(e.commkey()).isEqualTo(4184325);
	    assertThat(e.timestamp()).isEqualTo(LocalDateTime.of(2017, 9, 20, 10, 11));
	    assertThat(e.newState()).isEqualTo(OrderItemState.WAITING);
	});
	assertThat(importer.getOrderItemEvents()).describedAs("events").anySatisfy(e -> {
	    assertThat(e.commkey()).isEqualTo(4184325);
	    assertThat(e.timestamp()).isEqualTo(LocalDateTime.of(2017, 9, 20, 16, 19));
	    assertThat(e.newState()).isEqualTo(OrderItemState.PROCESSED);
	});
	assertThat(importer.getOrderItemEvents()).describedAs("events").hasSize(4);
    }

    @Test
    public void testParseLine_NoStockItem() {
	ImportCSVFile importer = new ImportCSVFile();
	importer.parseHeader(
		"\"OD_COMMKEY\";\"OD_KNOWN\";\"ORDER_COMMKEY\";\"ORDER_DATE\";\"COMPANYGROUP\";\"ID_ITEMOPTION\";\"DELIVERYCONDITION\";\"QUANTITY\";\"TS_HAS_NO_STOCK\";\"TS_HAS_STOCK\";\"LGR_BEREICH\";\"TS_ABWICKELBAR\";\"TS_FAKTURIERT\";\"TS_NALI\";\"TS_VERWORFEN\";\"TS_WARTEND\"");
	importer.parseLine("251931;0;0;;;0;;;;2017-09-18 00:34;HDL;2017-09-16 14:12;2017-09-18 00:34;;;");

	assertThat(importer.getOrderItems()).describedAs("items").hasSize(1);
	assertThat(importer.getOrderItems()).describedAs("items").hasOnlyOneElementSatisfying(o -> {
	    assertThat(o.commkey()).isEqualTo(251931);
	});

	assertThat(importer.getOrderItemEvents()).describedAs("events").anySatisfy(e -> {
	    assertThat(e.commkey()).isEqualTo(251931);
	    assertThat(e.timestamp()).isEqualTo(LocalDateTime.of(2017, 9, 16, 14, 12));
	    assertThat(e.newState()).isEqualTo(OrderItemState.PROCESSABLE);
	});
	assertThat(importer.getOrderItemEvents()).describedAs("events").anySatisfy(e -> {
	    assertThat(e.commkey()).isEqualTo(251931);
	    assertThat(e.timestamp()).isEqualTo(LocalDateTime.of(2017, 9, 18, 0, 34));
	    assertThat(e.newState()).isEqualTo(OrderItemState.PROCESSED);
	});
	assertThat(importer.getOrderItemEvents()).describedAs("events").hasSize(2);
    }
}
