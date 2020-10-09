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

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.SAXException;

/**
 * created by wwang on 2012-9-27 Detailled comment
 *
 */
public class TalendXSSFSheetXMLHandler extends XSSFSheetXMLHandler {

    private TalendSheetContentsHandler output = null;

    public TalendXSSFSheetXMLHandler(StylesTable styles, ReadOnlySharedStringsTable strings,
            TalendSheetContentsHandler sheetContentsHandler, DataFormatter dataFormatter, boolean formulasNotResults) {
        super(styles, strings, sheetContentsHandler, dataFormatter, formulasNotResults);
        this.output = sheetContentsHandler;
    }

    public interface TalendSheetContentsHandler extends SheetContentsHandler {

        public void endSheet();

    }

    @Override
    public void endDocument() throws SAXException {
        this.output.endSheet();
    }

}
