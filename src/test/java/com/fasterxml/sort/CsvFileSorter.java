package com.fasterxml.sort;

import java.io.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.sort.std.StdComparator;

public class CsvFileSorter<T extends Comparable<T>> extends Sorter<T>
{
    public CsvFileSorter(Class<T> entryType) throws IOException {
        this(entryType, new SortConfig(), new CsvMapper());
    }

    public CsvFileSorter(Class<T> entryType, SortConfig config, CsvMapper mapper) throws IOException {
        this(mapper.constructType(entryType), config, mapper, CsvSchema.emptySchema());
    }

    public CsvFileSorter(Class<T> entryType, SortConfig config, CsvMapper mapper, CsvSchema schema) throws IOException {
        this(mapper.constructType(entryType), config, mapper, schema);
    }

    public CsvFileSorter(JavaType entryType, SortConfig config, CsvMapper mapper, CsvSchema schema)
        throws IOException
    {
        super(config, new ReaderFactory<T>(mapper.readerFor(entryType), schema),
                new WriterFactory<T>(mapper, schema),
                new StdComparator<T>());
    }

    static class ReaderFactory<R> extends DataReaderFactory<R>
    {
        private final ObjectReader _reader;
        private final CsvSchema _schema;

        public ReaderFactory(ObjectReader r, CsvSchema schema) {
            _reader = r;
            _schema = schema;
        }
        
        @Override
        public DataReader<R> constructReader(InputStream in) throws IOException {
            MappingIterator<R> it = _reader.with(_schema).readValues(in);
            return new Reader<R>(it);
        }
    }

    static class Reader<E> extends DataReader<E>
    {
        protected final MappingIterator<E> _iterator;
 
        public Reader(MappingIterator<E> it) {
            _iterator = it;
        }

        @Override
        public E readNext() throws IOException {
            if (_iterator.hasNext()) {
                return _iterator.nextValue();
            }
            return null;
        }

        @Override
        public int estimateSizeInBytes(E item) {
            // 2 int fields, object, rough approximation
            return 24;
        }

        @Override
        public void close() throws IOException {
            // auto-closes when we reach end
        }
    }
    
    static class WriterFactory<W> extends DataWriterFactory<W>
    {
        protected final ObjectMapper _mapper;
        private final CsvSchema _schema;
        
        public WriterFactory(CsvMapper m, CsvSchema schema) {
            _mapper = m;
            _schema = schema;
        }
        
        @Override
        public DataWriter<W> constructWriter(OutputStream out) throws IOException {
            return new Writer<W>(_mapper, out, _schema);
        }
    }

    static class Writer<E> extends DataWriter<E>
    {
        protected final ObjectMapper _mapper;
        protected final JsonGenerator _generator;

        public Writer(ObjectMapper mapper, OutputStream out, CsvSchema schema) throws IOException {
            _mapper = mapper;
            _generator = _mapper.getFactory().createGenerator(out);
            _generator.setSchema(schema);
        }

        @Override
        public void writeEntry(E item) throws IOException {
            _mapper.writeValue(_generator, item);
        }

        @Override
        public void close() throws IOException {
            _generator.close();
        }
    }
}
