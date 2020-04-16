package com.talend.csv;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UnicodeReaderTest {

    @Test
    void read() throws IOException {
        final URL ficBom = Thread.currentThread().getContextClassLoader().getResource("./ficUTF8WithBom.txt");

        try (final FileInputStream input = new FileInputStream(ficBom.getPath());
             final UnicodeReader reader = new UnicodeReader(input, "")) {

            Assertions.assertEquals("UTF8", reader.getEncoding());

            final char[] line = new char[40];
            final int res = reader.read(line);
            Assertions.assertEquals("hé !! ", new String(line, 0, res));
        }
    }
}