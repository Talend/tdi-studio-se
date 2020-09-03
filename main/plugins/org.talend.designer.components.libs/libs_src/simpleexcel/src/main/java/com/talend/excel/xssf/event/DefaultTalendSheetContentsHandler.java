// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.talend.excel.xssf.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFComment;

/**
 * created by wwang on 2012-9-27 Detailled comment
 *
 */
public class DefaultTalendSheetContentsHandler implements TalendXSSFSheetXMLHandler.TalendSheetContentsHandler {

    private DataBufferCache cache = null;

    private List<String> row = null;

    private boolean stop = false;

    private int currentColumnIndex = -1;

    private int lastColumnIndex = -1;

    private int endColumnIndex = 0;

    private boolean isCellDateType = false;

    //Cache Formatters
    private Map<Integer, SimpleDateFormat> formatters = new HashMap<Integer, SimpleDateFormat>();

    private SimpleDateFormat currentExcelFormatter;

    private SimpleDateFormat dynamicDateFormatter;

    private final String dynamicDatePattern;

    private final int startDynamicIndex;

    private final int endDynamicIndex;

    public DefaultTalendSheetContentsHandler(DataBufferCache cache) {
        this(cache,null,0,0);
    }

    public DefaultTalendSheetContentsHandler(DataBufferCache cache, String dynamicDatePattern, int startDynamicIndex,
            int endDynamicIndex) {
        this.cache = cache;
        this.dynamicDatePattern = dynamicDatePattern;
        this.startDynamicIndex = startDynamicIndex;
        this.endDynamicIndex = endDynamicIndex;
    }

    @Override
    public void startRow(int rowNum) {
        row = new ArrayList<String>();
    }

    @Override
    public void endRow(int rowNum) {
        cache.writeData(row);
        row = null;
        //Store count of columns to determine date pattern in case of dynamic value.
        //It can be applied since in XLSX file has to be a header, so we can count total number of columns
        if (dynamicDatePattern != null && endColumnIndex == 0) {
            endColumnIndex = lastColumnIndex;
        }
        // when each row end ,reset lastColumnIndex
        lastColumnIndex = -1;
        if (stop) {
            throw new EnoughDataException("Get enough data,now stop the xml parse action");
        }
    }

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
        checkHasNullValue(cellReference);
        if (isCellDateType) {
            try {
                if (formattedValue != null) {
                    if (dynamicDatePattern != null && (currentColumnIndex >= startDynamicIndex
                            && currentColumnIndex <= endColumnIndex - endDynamicIndex)) {
                        if (dynamicDateFormatter == null) {
                            dynamicDateFormatter = new SimpleDateFormat(dynamicDatePattern);
                        }
                        row.add(dynamicDateFormatter.format(currentExcelFormatter.parse(formattedValue)));
                    } else {
                        row.add(String.valueOf(currentExcelFormatter.parse(formattedValue).getTime()));
                    }
                }
            } catch (ParseException e) {
                row.add(null);
            }
            isCellDateType = false;
        } else {
            row.add(formattedValue);
        }
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
    }

    @Override
    public void endSheet() {
        formatters.clear();
    }

    public void stop() {
        this.stop = true;
    }

    /**
     * when currentCellReferecnce is "A1" ,and next currentCellReferecnce is "C1",we need add a null value for "B1"
     */
    private void checkHasNullValue(String cellReference) {
        currentColumnIndex = getIndexOfColumn(cellReference);
        // Might be the empty string.
        for (int i = lastColumnIndex; i < currentColumnIndex - 1; i++) {
            row.add(null);
        }
        // Update column
        if (currentColumnIndex > -1) {
            lastColumnIndex = currentColumnIndex;
        }
    }

    /**
     * get index of column,like "AB12" return 27
     */
    private int getIndexOfColumn(String cellReference) {
        int columnIndex = -1;
        // get First Digit index ,like cellReferecnce is "AA13" ,firstDigitIndex return 2
        int firstDigitIndex = -1;
        for (int c = 0; c < cellReference.length(); ++c) {
            if (Character.isDigit(cellReference.charAt(c))) {
                firstDigitIndex = c;
                break;
            }
        }
        // get String part for cellReference,like "AB2" ,return "AB"
        String cellStringPart = cellReference.substring(0, firstDigitIndex);
        for (int i = 0; i < cellStringPart.length(); ++i) {
            int c = cellStringPart.charAt(i);
            columnIndex = (columnIndex + 1) * 26 + c - 'A';
        }
        return columnIndex;
    }

    public void setExcelDateFormat(String excelDateFormat) {
        SimpleDateFormat temp;
        if ((temp = formatters.get(currentColumnIndex)) == null) {
            temp = new SimpleDateFormat(excelDateFormat);
            formatters.put(currentColumnIndex, temp);
        }
        currentExcelFormatter = temp;
    }

    public void setCellDateType(boolean isCellDateType) {
        this.isCellDateType = isCellDateType;
    }

}
