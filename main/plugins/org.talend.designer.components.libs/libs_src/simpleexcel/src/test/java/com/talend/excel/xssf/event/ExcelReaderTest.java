package com.talend.excel.xssf.event;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ExcelReaderTest {

    @ParameterizedTest
    @CsvSource(
            value = {
                    "dd-MM-yyyy@200201,11-05-2020",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'@200201,2020-05-11T06:15:35.000Z",
                    "yyyy@200201,2020"
            },
            delimiter = '@'
    )
    public void checkSetParseDateFormat(String datePattern, String expected) throws IOException {

        int readLines = 0;
        List<String> row = Collections.emptyList();

        try (InputStream stream = getClass().getResourceAsStream("datetest.xlsx")) {
            Assertions.assertNotNull(stream, "Can not find a resource file");

            ExcelReader reader = new ExcelReader();
            reader.addDateFormat(1, new SimpleDateFormat(datePattern));
            reader.addSheetName(".*", true);
            reader.parse(stream, "UTF-8", null);

            while (reader.hasNext()) {
                readLines++;
                row = reader.next();
            }
        }

        Assertions.assertEquals(1, readLines);
        Assertions.assertFalse(row.isEmpty());
        Assertions.assertEquals(expected, String.join(",", row));
    }
}
