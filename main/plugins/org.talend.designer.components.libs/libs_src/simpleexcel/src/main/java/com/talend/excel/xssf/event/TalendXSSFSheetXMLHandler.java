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

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * created by wwang on 2012-9-27 Detailled comment
 *
 */
public class TalendXSSFSheetXMLHandler extends XSSFSheetXMLHandler {
    private static final Set<String> TYPES_TO_SKIP_NON_DATES = new HashSet<>(Arrays.asList("b", "e", "inlineStr", "s", "str"));

    private final TalendSheetContentsHandler output;

    /**
     * put here date pattern for studio\talend columns
     * we pass it to parser and during the date parse parser will use them instead of internal one
     */
    private final Map<Integer, DateFormat> columnDateFormats;

    private final StylesTable stylesTable;

    private final DataFormatter formatter;

    /**
     * maybe replaced with stack if start and end element for xss has recursive call
     */
    private String lastChangedFormatString;

    public TalendXSSFSheetXMLHandler(StylesTable styles, ReadOnlySharedStringsTable strings,
            TalendSheetContentsHandler sheetContentsHandler, DataFormatter dataFormatter, boolean formulasNotResults,
            Map<Integer, DateFormat> columnDateFormats) {
        super(styles, strings, sheetContentsHandler, dataFormatter, formulasNotResults);
        this.output = sheetContentsHandler;
        this.stylesTable = styles;
        this.formatter = dataFormatter;
        this.columnDateFormats = columnDateFormats;
    }

    public interface TalendSheetContentsHandler extends SheetContentsHandler {

        public void endSheet();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (uri == null || uri.equals("http://schemas.openxmlformats.org/spreadsheetml/2006/main")) {
            if ("c".equals(localName)) {
                swapExcelDateFormatOnTalendIfNeed(attributes);
            }
        }
    }

    /**
     * find the Date (Talend) column and set custom dateformat from the Studio into event parser
     */
    private void swapExcelDateFormatOnTalendIfNeed(Attributes attributes) {
        if (stylesTable == null) {
            return;
        }

        // calculate the index of column
        Integer columnIndex = ColumnUtil.calculateIndexOfColumn(attributes.getValue("r"));
        String cellStyleStr = attributes.getValue("s");
        String cellType = attributes.getValue("t");

        XSSFCellStyle style = null;
        if (cellStyleStr != null) {
            int styleIndex = Integer.parseInt(cellStyleStr);
            style = this.stylesTable.getStyleAt(styleIndex);
        } else if (this.stylesTable.getNumCellStyles() > 0) {
            style = this.stylesTable.getStyleAt(0);
        }

        if (style != null) {
            String formatString = style.getDataFormatString();
            short formatIndex = style.getDataFormat();
            if (formatString == null) {
                formatString = BuiltinFormats.getBuiltinFormat(formatIndex);
            }

            DateFormat format = columnDateFormats.get(columnIndex);
            if (formatString != null && !TYPES_TO_SKIP_NON_DATES.contains(cellType) && DateUtil.isADateFormat(formatIndex, formatString)
                    && format != null) {
                lastChangedFormatString = formatString;
                formatter.addFormat(formatString, format);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            super.endElement(uri, localName, qName);
        } finally {
            // clean up
            if (lastChangedFormatString != null) {
                formatter.addFormat(lastChangedFormatString, null);
                lastChangedFormatString = null;
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        this.output.endSheet();
    }

}
