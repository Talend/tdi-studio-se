// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.List;

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

    public DefaultTalendSheetContentsHandler(DataBufferCache cache) {
        this.cache = cache;
    }

    @Override
    public void startRow(int rowNum) {
        row = new ArrayList<String>();
    }

    @Override
    public void endRow(int rowNum) {
        cache.writeData(row);
        row = null;
        // when each row end ,reset lastColumnIndex
        lastColumnIndex = -1;
        if (stop) {
            throw new EnoughDataException("Get enough data,now stop the xml parse action");
        }
    }

    @Override
    public void cell(String cellReference, String formattedValue,XSSFComment comment) {
        checkHasNullValue(cellReference);
        row.add(formattedValue);
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
    }

    @Override
    public void endSheet() {

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
}
