// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.ooxml.util.PackageHelper;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * created by wwang on 2012-9-27 Detailled comment
 * 
 */
public class ExcelReader implements Callable {

    private DataBufferCache cache;

    private Thread task;

    private FutureTask futureTask;

    private String fileURL = null;

    private String charset = "UTF-8";

    private java.io.InputStream is;

    private List<String> sheetNames = new ArrayList<String>();

    private List<Integer> sheetPositions = new ArrayList<Integer>();

    private List<Boolean> asRegexs = new ArrayList<Boolean>();

    DefaultTalendSheetContentsHandler sheetContentsHandler = null;

    private boolean includePhoneticRuns;

    public ExcelReader() {
        cache = DataBufferCache.getInstance();
        futureTask = new FutureTask(this);
        task = new Thread(futureTask);
    }

    public void parse(String fileURL, String charset) {
        this.fileURL = fileURL;
        this.charset = charset;
        task.start();
    }

    public void parse(java.io.InputStream is, String charset) {
        this.is = is;
        this.charset = charset;
        task.start();
    }

    public boolean hasNext() {
        return cache.hasData();
    }

    public List<String> next() {
        return cache.readData();
    }

    public void addSheetName(String name, Boolean asRegex) {
        this.sheetNames.add(name);
        this.asRegexs.add(asRegex);
    }

    public void addSheetName(int position, Boolean asRegex) {
        // Keep "asRegex" here for code generated
        if (!this.sheetPositions.contains(position)) {
            this.sheetPositions.add(position);
        }
    }

    public void stopRead() {
        sheetContentsHandler.stop();
    }

    public boolean isIncludePhoneticRuns() {
        return includePhoneticRuns;
    }

    public void setIncludePhoneticRuns(boolean includePhoneticRuns) {
        this.includePhoneticRuns = includePhoneticRuns;
    }

    /**
     * handle the exception in task. FutureTask.get() is a block method waiting for the Task over and it can throw the
     * exception in Task(Callable,Thread,Runnable)
     * 
     * @throws Exception
     */
    public void handleException() throws Exception {
        futureTask.get();
    }

    public Object call() throws Exception {
        OPCPackage pkg = null;
        try {
            if (fileURL != null) {
                pkg = OPCPackage.open(fileURL);
            } else {
                pkg = PackageHelper.open(is);
            }
            XSSFReader r = new XSSFReader(pkg);

            StylesTable styles = r.getStylesTable();
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg, includePhoneticRuns);
            sheetContentsHandler = new DefaultTalendSheetContentsHandler(cache);
            DataFormatter formatter = new DataFormatter();
            boolean formulasNotResults = false;

            XMLReader parser = XMLReaderFactory.createXMLReader();
            ContentHandler handler = new TalendXSSFSheetXMLHandler(styles, strings, sheetContentsHandler, formatter,
                    formulasNotResults);
            parser.setContentHandler(handler);

            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) r.getSheetsData();
            // List<InputStream> iss = new ArrayList<InputStream>();
            LinkedHashMap<String, InputStream> issmap = new LinkedHashMap<String, InputStream>();
            List<String> allSheetNames = new ArrayList<String>();
            int sheetPosition = 0;
            while (sheets.hasNext()) {
                InputStream sheet = sheets.next();
                String sheetName = sheets.getSheetName();
                if (allSheetNames.contains(sheetName)) {
                    sheet.close();
                    continue;
                } else {
                    allSheetNames.add(sheetName);
                }
                boolean match = false;
                if (sheetNames.size() > 0) {
                    for (int i = 0; i < sheetNames.size(); i++) {
                        if ((asRegexs.get(i) && sheetName.matches(sheetNames.get(i)))
                                || (!asRegexs.get(i) && sheetName.equalsIgnoreCase(sheetNames.get(i)))) {
                            match = true;
                            // iss.add(sheet);
                            issmap.put(sheetName, sheet);
                            break;
                        }
                    }
                }

                if (sheetPositions.size() > 0) {
                    if (sheetPositions.contains(sheetPosition)) {
                        match = true;
                        issmap.put(sheetName, sheet);
                        // remove from the list for index out of range check.
                        sheetPositions.remove(sheetPositions.indexOf(sheetPosition));
                    }
                }
                sheetPosition++;
                if (!match) {
                    sheet.close();
                }
            }

            if (issmap.size() < 1) {
                throw new RuntimeException("No match sheets");
            }

            // if the "sheetPositions" still not empty, means thsoe positions out of range.
            if (sheetPositions.size() > 0) {
                throw new IllegalArgumentException(
                        "Sheet index " + sheetPositions + " is out of range (0.." + (sheetPosition - 1) + ")");
            }

            for (InputStream is : issmap.values()) {
                try {
                    InputSource sheetSource = new InputSource(is);
                    sheetSource.setEncoding(charset);
                    parser.parse(sheetSource);
                } finally {
                    is.close();
                }
            }

        } finally {
            if (pkg != null) {
                pkg.revert();
            }
            cache.notifyErrorOccurred();
        }
        return null;
    }
}
