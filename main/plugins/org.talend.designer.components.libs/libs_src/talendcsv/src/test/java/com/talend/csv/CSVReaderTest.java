package com.talend.csv;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CSVReaderTest {
    @Test
    void basic() throws IOException {

        final String line = "\"v1\"v2\", \"v3\"";
        final CSVReader reader = new CSVReader(new StringReader(line), ',');
        reader.setEscapeChar('\\').setStoreRawRecord(true).setTrimWhitespace(false);
        Assertions.assertTrue(reader.readNext());

        final String[] values = reader.getValues();
        Assertions.assertNotNull(values);
    }


    @Test
    void readNext() throws IOException {

        final String lines = "\"event_id\",\"event_name\",\"event_value\",\"source\"\n" +     // titles.
                "\"001\",\"CN\",\"This is some \\\\ttext\",\"event_200\"\n" +                // normal line
                "\"002\",\"CN\",\"This is some text, with sep\",\"event_250\"\n" +      // test field sep inside value
                "\"003\",\"CN\",\"This is some \\\"text\\\" inside value\",\"event_300\"\n" +  // escape quote inside value
                "\"004\",\"CN\",\"This is some other \" text\" inside value\",\"event_400\"\n" + // unescape quote inside value
                "\"005\"  , \"CN\" , \"This is some text\"  ,  event_500\n" + // spaced field
                "006, CN , \"Text\" ,\"  xx  \" \n" +         // unquoted fields.
                "007,,\"\",\"  xx  \" ";                                      // empty record.

        final CSVReader reader = new CSVReader(new StringReader(lines), ',');
        reader.setEscapeChar('\\').setStoreRawRecord(true).setTrimWhitespace(false);
        reader.readHeaders();
        final String[] headers = reader.getHeaders();
        Assertions.assertAll(
                () -> Assertions.assertEquals(4, headers.length),
                () -> Assertions.assertEquals("event_id", headers[0]),
                () -> Assertions.assertEquals("event_name", headers[1]),
                () -> Assertions.assertEquals("event_value", headers[2]),
                () -> Assertions.assertEquals("source", headers[3])
        );
        Assertions.assertTrue(reader.readNext());

        Assertions.assertAll(
                () -> this.checkNextValues("normal", reader, "001", "CN", "This is some \\ttext", "event_200"),
                () -> this.checkNextValues("field sep in value", reader, "002", "CN", "This is some text, with sep", "event_250"),
                () -> this.checkNextValues("escape quote inside value", reader, "003", "CN", "This is some \"text\" inside value", "event_300"),
                () -> this.checkNextValues("unescape quote inside value", reader, "004", "CN", "This is some other ", "event_400"),
                () -> this.checkNextValues("spaced field", reader, "005", " \"CN\" "," \"This is some text\"  ", "  event_500"),
                () -> this.checkNextValues("unquoted fields", reader, "006", " CN "," \"Text\" ", "  xx  "),
                () -> this.checkNextValues("empty record", reader, "007", "", "", "  xx  ")
        );
        Assertions.assertFalse(reader.readNext());

        final CSVReader reader2 = new CSVReader(new StringReader(lines), ',');
        reader2.setEscapeChar('\\').setStoreRawRecord(true).setTrimWhitespace(true);
        reader2.readHeaders();
        reader2.getHeaders();
        Assertions.assertTrue(reader2.readNext());

        Assertions.assertAll(
                () -> this.checkNextValues("normal 2", reader2, "001", "CN", "This is some \\ttext", "event_200"),
                () -> this.checkNextValues("field sep in value 2", reader2, "002", "CN", "This is some text, with sep", "event_250"),
                () -> this.checkNextValues("escape quote inside value 2", reader2, "003", "CN", "This is some \"text\" inside value", "event_300"),
                () -> this.checkNextValues("unescape quote inside value", reader2, "004", "CN", "This is some other ", "event_400"),
                () -> this.checkNextValues("spaced field 2", reader2, "005", "CN","This is some text", "event_500"),
                () -> this.checkNextValues("unquoted fields 2", reader2, "006", "CN","Text", "  xx  "),
                () -> this.checkNextValues("empty record 2", reader2, "007", "", "", "  xx  ")
        );
        Assertions.assertFalse(reader2.readNext());
    }

    @Test
    void readNextEmptyRecord() throws IOException {
        final String line = "0\\t07,, \"\" ,\"  x\\tx  \" ";
        final CSVReader reader = new CSVReader(new StringReader(line), ',');
        reader.setEscapeChar('\\');
        reader.setTrimWhitespace(true);
        reader.setSkipEmptyRecords(true);
        Assertions.assertAll(
                () -> this.checkNextValues("empty record", reader, "0\\t07", "", "", "  x\tx  ")
        );
    }

    @Test
    void lineSepTest() throws IOException {

        final String lines = "line1@line2";

        final CSVReader reader = new CSVReader(new StringReader(lines), ',');
        reader.setLineEnd("@");

        Assertions.assertAll(
                () -> this.checkNextValues("line 1 for line sep", reader, "line1"),
                () -> this.checkNextValues("line 2 for line sep", reader, "line2")
        );

        final String linesBis = "line1\r\nline2";
        final CSVReader reader2 = new CSVReader(new StringReader(linesBis), ',');

        Assertions.assertAll(
                () -> this.checkNextValues("line 1 for line sep", reader2, "line1"),
                () -> this.checkNextValues("line 2 for line sep", reader2, "line2")
        );
    }

    @Test
    void testEscapeIsQuote() {
        final String lines = "\"L\"\"in\" ,  \"te 1\"\nLine,  \"t\"\"2";
        final CSVReader reader = new CSVReader(new StringReader(lines), ',');

        Assertions.assertAll(
                () -> this.checkNextValues("line 1", reader, "L\"in", "te 1"),
                () -> this.checkNextValues("line 2", reader, "Line", "t\"2")
        );
    }

    @Test
    void testQuoted() throws IOException {
        final String input = "\"Hello\",\"ss\"\n\"World\",\"ddzs\"\n\"One\"\"Column\",\"ddzs\"\n";

        final CSVReader reader = new CSVReader(new StringReader(input), ',');

        reader.setQuoteChar('"');
        reader.setTrimWhitespace(false);
        reader.setEscapeChar('"');
        reader.setSkipEmptyRecords(false);

        Assertions.assertAll(
                () -> this.checkNextValues("line 1", reader, "Hello", "ss"),
                () -> this.checkNextValues("line 2", reader, "World", "ddzs"),
                () -> this.checkNextValues("line 3", reader, "One\"Column", "ddzs")
        );
        Assertions.assertFalse(reader.readNext());
    }

    void checkNextValues(final String comment, final CSVReader reader, final String... excepted) throws IOException {
        Assertions.assertTrue(reader.readNext());
        final String[] values = reader.getValues();
        Assertions.assertEquals(excepted.length, values.length, comment + " : wrong length");
        for (int i = 0; i < excepted.length; i++) {
            Assertions.assertEquals(excepted[i], values[i], comment + " : field " + i + " in error");
        }
    }
}