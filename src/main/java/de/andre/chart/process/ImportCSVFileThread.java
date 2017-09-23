package de.andre.chart.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

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

    private final File csvFile;
    private final ImportCSVFile importer = new ImportCSVFile();
    private Throwable caughtException;
    private ThreadStartedListener startedListener;
    private ThreadFinishedListener finishedListener;

    public ImportCSVFileThread(File csvFile) {
	super("import csv");
	this.csvFile = csvFile;
    }

    @Override
    public void run() {
	this.caughtException = null;

	LineNumberReader lnr = null;
	try {
	    lnr = new LineNumberReader(new FileReader(csvFile));
	    String line = null;
	    if (startedListener != null) {
		startedListener.started(this);
	    }
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
	    if (finishedListener != null) {
		finishedListener.finished(this);
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
