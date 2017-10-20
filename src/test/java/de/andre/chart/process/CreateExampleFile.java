package de.andre.chart.process;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.junit.Test;

public class CreateExampleFile {
    private static final int ITEMS = 100_000;

    @Test
    public void create() throws Exception {
	// open file writer
	File file = new File("example_" + ITEMS + ".csv");
	FileWriter fw = null;
	try {
	    fw = new FileWriter(file);
	    String header = "\"OD_COMMKEY\";\"OD_KNOWN\";\"ORDER_COMMKEY\";\"ORDER_DATE\";\"COMPANYGROUP\";\"ID_ITEMOPTION\";\"DELIVERYCONDITION\""
		    + ";\"QUANTITY\";\"TS_HAS_NO_STOCK\";\"TS_HAS_STOCK\";\"LGR_BEREICH\";\"TS_ABWICKELBAR\";\"TS_FAKTURIERT\";\"TS_NALI\""
		    + ";\"TS_VERWORFEN\";\"TS_WARTEND\"\n";
	    String pattern = header.replace("\"", "") //
		    // .replace("OD_COMMKEY", "1") //
		    .replace("OD_KNOWN", "1") //
		    .replace("ORDER_COMMKEY", "1") //
		    // .replace("ORDER_DATE", "") //
		    .replace("COMPANYGROUP", "") //
		    .replace("ID_ITEMOPTION", "") //
		    .replace("DELIVERYCONDITION", "") //
		    // .replace("QUANTITY", "1") //
		    .replace("TS_HAS_NO_STOCK", "") //
		    .replace("TS_HAS_STOCK", "") //
		    .replace("LGR_BEREICH", "") //
		    .replace("TS_ABWICKELBAR", "") //
		    // .replace("TS_FAKTURIERT", "") //
		    .replace("TS_NALI", "") //
		    // .replace("TS_VERWORFEN", "") //
		    .replace("TS_WARTEND", "") //
		    ;
	    // "4184325;1;4193025;2017-09-20
	    // 10:11;BonPrix;856943;Normalservice;1;2017-09-20 10:11;2017-09-20
	    // 16:19;HDL;2017-09-20 10:12;2017-09-20 16:19;2017-09-20 10:11;;");

	    fw.append(header);
	    Random r = new Random();
	    LocalDateTime now = LocalDateTime.of(2017, 10, 12, 0, 0);
	    for (int item = 1; item <= ITEMS; item++) {
		int commkey = item;
		int quantity = r.nextInt(5) + 1;
		LocalDateTime orderDate = now;
		LocalDateTime processedTime = orderDate.plusMinutes(r.nextInt(96 * 60) + 1);
		LocalDateTime canceledTime = orderDate.plusMinutes(r.nextInt(60) + 1);

		if (r.nextInt(100) > 5) {
		    canceledTime = null;
		} else {
		    processedTime = null;
		}

		String line = pattern //
			.replace("OD_COMMKEY", "" + commkey) //
			.replace("ORDER_DATE", toStr(orderDate)) //
			.replace("QUANTITY", "" + quantity) //
			.replace("TS_FAKTURIERT", toStr(processedTime)) //
			.replace("TS_VERWORFEN", toStr(canceledTime)) //
			;
		fw.append(line);
		now = now.plusSeconds(1);
	    }
	} finally {
	    if (fw != null)
		fw.close();
	}

	System.out.println("File '" + file.getAbsolutePath() + "' created.");
    }

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private CharSequence toStr(LocalDateTime value) {
	if (value == null) {
	    return "";
	}
	return dtf.format(value);
    }
}
