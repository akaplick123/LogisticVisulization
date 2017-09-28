package de.andre.chart.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;

import de.andre.chart.data.LocalDateTimeLookUp;
import lombok.extern.log4j.Log4j;

@Log4j
public class ImportCSVFileThread extends Thread {

    @FunctionalInterface
    public static interface ThreadFinishedListener {
	void finished(ImportCSVFileThread thread);
    }

    @FunctionalInterface
    public static interface ThreadStartedListener {
	void started(ImportCSVFileThread thread);
    }

    private final Collection<File> csvFiles;
    private final ImportCSVFile importer;
    private Throwable caughtException;
    private ThreadStartedListener startedListener;
    private ThreadFinishedListener finishedListener;

    public ImportCSVFileThread(Collection<File> csvFiles, LocalDateTimeLookUp dateTimeLookup) {
	super("import csv");
	this.csvFiles = new ArrayList<>(csvFiles);
	this.importer = new ImportCSVFile(dateTimeLookup);
    }

    @Override
    public void run() {
	    log.debug("start import thread");
	this.caughtException = null;

	try {
	    if (startedListener != null) {
		startedListener.started(this);
	    }
	    for (File file: csvFiles) {
		importFile(file);
	    }
	    log.debug("file import finished");
	} catch (InterruptedException e) {
	    // make nothing others then stop execution
	} finally {
	    if (finishedListener != null) {
		finishedListener.finished(this);
	    log.debug("finished listener finished");
	    }
	}
    }

    private void importFile(File csvFile) throws InterruptedException {
	LineNumberReader lnr = null;
	try {
	    lnr = new LineNumberReader(new FileReader(csvFile));
	    String line = null;
	    while ((line = lnr.readLine()) != null) {
		if (lnr.getLineNumber() <= 1) {
		    importer.parseHeader(line);
		} else {
		    importer.parseLine(line);
		}
	    }
	} catch (IOException e) {
	    log.warn("Import", e);
	    this.caughtException = e;
	} finally {
	    if (lnr != null) {
		try {
		    lnr.close();
		} catch (IOException e) {
		    log.warn("close file", e);
		}
	    }
	}
    }

    public ImportCSVFile getImporter() {
	return importer;
    }

    public boolean hadErrors() {
	return (this.caughtException != null);
    }

    public void setStartedListener(ThreadStartedListener startedListener) {
	this.startedListener = startedListener;
    }

    public void setFinishedListener(ThreadFinishedListener finishedListener) {
	this.finishedListener = finishedListener;
    }
}
