package com.tomgibara.crinch.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.tomgibara.crinch.record.ProcessContext;
import com.tomgibara.crinch.record.ReaderSource;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;
import com.tomgibara.crinch.record.StringRecord;


import au.com.bytecode.opencsv.CSVReader;

public class CsvProducer implements RecordProducer<StringRecord> {

	private final Iterable<ReaderSource> readerSources;
	private final char separator;
	private long recordCount = -1L;
	private ProcessContext context;

	public CsvProducer(Iterable<ReaderSource> readerSources) {
		this(readerSources, ',');
	}

	public CsvProducer(Iterable<ReaderSource> readerSources, char separator) {
		if (readerSources == null) throw new IllegalArgumentException("null readerSources");
		this.readerSources = readerSources;
		this.separator = separator;
	}
	
	@Override
	public void prepare(ProcessContext context) {
		this.context = context;
	}
	
	@Override
	public RecordSequence<StringRecord> open() {
		context.setRecordCount(recordCount);
		
		return new RecordSequence<StringRecord>() {

			private Iterator<ReaderSource> iterator = readerSources.iterator();
			private Reader reader = null;
			CSVReader csvReader = null;
			private StringRecord next;
			private long recordCount = 0L;
			
			{ while (advance()); }
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public StringRecord next() {
				if (next == null) throw new NoSuchElementException();
				try {
					return next;
				} finally {
					while (advance());
				}
			}
			
			@Override
			public boolean hasNext() {
				return next != null;
			}
			
			@Override
			public void close() {
				if (iterator != null) {
					try {
						reader.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						reader = null;
						csvReader = null;
						iterator = null;
						CsvProducer.this.recordCount = this.recordCount;
					}					
				}
			}
			
			private boolean advance() {
				try {
					if (iterator == null) throw new NoSuchElementException();
					next = null;
					if (reader == null) {
						if (!iterator.hasNext()) {
							iterator = null;
							CsvProducer.this.recordCount = this.recordCount;
							return false;
						}
						ReaderSource source = iterator.next();
						reader = source.open();
						csvReader = new CSVReader(reader, separator);
						return true;
					}
					String[] strs = csvReader.readNext();
					if (strs == null) {
						reader.close();
						reader = null;
						csvReader = null;
						return true;
					}
					next = new StringRecord(recordCount++, -1L, strs);
					return false;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	@Override
	public void complete() {
		context = null;
	}
	
}