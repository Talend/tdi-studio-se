package com.talend.csv;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.talend.csv.CSVWriter.QuoteStatus;

class CSVWriterTest {

    @Test
    void write() throws IOException {
        final StringWriter result = new StringWriter();
        try (final CSVWriter writer = new CSVWriter(result)) {

            writer.setSeparator(',').setEscapeChar('@').setQuoteChar('"').setLineEnd("\n").setQuoteStatus(QuoteStatus.AUTO);

            writer.writeAll(Arrays.asList(new String[] { "1", "Hello", "\"Mad\" World" },
                    new String[] { "2", "Good by", "world @ by" }));
        }
        Assertions.assertEquals("1,Hello,\"@\"Mad@\" World\"\n2,Good by,world @@ by\n",
                result.toString());

    }
}