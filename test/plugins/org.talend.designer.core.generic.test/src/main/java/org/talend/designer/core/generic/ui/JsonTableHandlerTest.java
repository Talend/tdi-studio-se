// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.generic.ui;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * created by ycbai on 2016年9月6日 Detailled comment
 *
 */
@SuppressWarnings("nls")
public class JsonTableHandlerTest {

    private static final String TEST_JSON_WITH_TITLES = "{\r\n" + "  \"columnNames\": [\r\n" + "    \"Id\",\r\n"
            + "    \"Name\",\r\n"
            + "    \"Age\"\r\n" + "  ],\r\n" + "  \"data\": [\r\n" + "    [\r\n" + "      \"1\",\r\n" + "      \"Tom\",\r\n"
            + "      \"22\"\r\n" + "    ],\r\n" + "    [\r\n" + "      \"2\",\r\n" + "      \"Mike\",\r\n" + "      \"33\"\r\n"
            + "    ],\r\n" + "    [\r\n" + "      \"3\",\r\n" + "      \"Lucy\",\r\n" + "      \"18\"\r\n" + "    ]\r\n"
            + "  ]\r\n" + "}";

    private static final String TEST_JSON_WITHOUT_TITLES = "{\r\n" + "  \"data\": [\r\n" + "    [\r\n" + "      \"1\",\r\n"
            + "      \"Tom\",\r\n" + "      \"22\"\r\n" + "    ],\r\n" + "    [\r\n" + "      \"2\",\r\n" + "      \"Mike\",\r\n"
            + "      \"33\"\r\n" + "    ],\r\n" + "    [\r\n" + "      \"3\",\r\n" + "      \"Lucy\",\r\n" + "      \"18\"\r\n"
            + "    ]\r\n" + "  ]\r\n" + "}";

    @Test
    public void testParse() {
        String[][] dataArray = new String[][] { new String[] { "1", "Tom", "22" }, new String[] { "2", "Mike", "33" },
                new String[] { "3", "Lucy", "18" } };

        JsonTableVO vo = JsonTableHandler.getInstance().parse(TEST_JSON_WITH_TITLES);
        List<String> titles = vo.getTitles();
        List<Map<String, Object>> data = vo.getData();

        String[] titlesArray = new String[] { "Id", "Name", "Age" };
        List<String> expectTitles = getExpectTitles(titlesArray);
        List<Map<String, Object>> expectData = getExpectData(titlesArray, dataArray);

        assertTrue(expectTitles.equals(titles));
        assertTrue(expectData.equals(data));

        vo = JsonTableHandler.getInstance().parse(TEST_JSON_WITHOUT_TITLES);
        titles = vo.getTitles();
        data = vo.getData();

        titlesArray = new String[] { "Column 1", "Column 2", "Column 3" };
        expectTitles = getExpectTitles(titlesArray);
        expectData = getExpectData(titlesArray, dataArray);

        assertTrue(expectTitles.equals(titles));
        assertTrue(expectData.equals(data));
    }

    private List<String> getExpectTitles(String[] titlesArray) {
        return new ArrayList<>(Arrays.asList(titlesArray));
    }

    public List<Map<String, Object>> getExpectData(String[] titlesArray, String[][] data) {
        List<Map<String, Object>> expectData = new ArrayList<>();
        for (String[] row : data) {
            expectData.add(createRow(titlesArray, row));
        }
        return expectData;
    }

    private Map<String, Object> createRow(String[] keys, String values[]) {
        Map<String, Object> row = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            row.put(keys[i], values[i]);
        }
        return row;
    }

}
