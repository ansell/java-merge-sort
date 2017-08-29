package com.fasterxml.sort;

import java.io.*;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;

public class TestCsvSort extends SortTestBase
{
    static class Point implements Comparable<Point>
    {
        public int x, y;
        
        @Override
        public int compareTo(Point o) {
            int diff = y - o.y;
            if (diff == 0) {
                diff = x - o.x;
            }
            return diff;
        }
    }
    
    /*
    /********************************************************************** 
    /* Unit tests
    /********************************************************************** 
     */
    
    public void testSimple() throws IOException
    {
        final String input =
        		"x,y\n"
        		+"1,1\n"
        		+"2,8\n"
        		+"3,2\n"
        		+"4,4\n"
        		+"5,5\n"
        		+"6,0\n"
        		+"7,10\n"
        		+"8,-4\n"
                ;
        CsvFactory csvFactory = new CsvFactory();
        csvFactory.enable(CsvParser.Feature.TRIM_SPACES);
        //csvFactory.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        csvFactory.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        CsvMapper mapper = new CsvMapper(csvFactory);
		mapper.enable(CsvParser.Feature.TRIM_SPACES);
		//mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
		mapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
		//mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        CsvSchema schema = CsvSchema.builder().setUseHeader(true).addColumn("x", ColumnType.NUMBER)
        		.addColumn("y", ColumnType.NUMBER).build();
		CsvFileSorter<Point> sorter = new CsvFileSorter<Point>(Point.class, 
        		new SortConfig(), mapper, schema);
        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        sorter.sort(new ByteArrayInputStream(input.getBytes("UTF-8")), out);
        final String output = out.toString("UTF-8");
        assertEquals("x,y\n"
        		+"8,-4\n"
        		+"6,0\n"
        		+"1,1\n"
        		+"3,2\n"
        		+"4,4\n"
        		+"5,5\n"
        		+"2,8\n"
        		+"7,10\n"
                ,output);
    }
}
