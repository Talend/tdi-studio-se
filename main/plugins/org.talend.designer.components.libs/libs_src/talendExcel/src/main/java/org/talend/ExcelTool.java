package org.talend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookType;

public class ExcelTool {

    private Workbook wb = null;

    private Workbook preWb = null;

    private String sheetName = null;

    private Sheet sheet = null;

    private Sheet preSheet = null;

    private Row curRow = null;

    private Row preRow = null;

    private Cell curCell = null;

    private Cell preCell = null;

    private boolean appendWorkbook = false;

    private boolean appendSheet = false;

    private int startX = 0;

    private int curY = 0;

    private int xOffset = 0;

    private boolean isAbsY = false;

    private int absX = 0;

    private int absY = 0;

    private boolean keepCellFormat = false;

    private Font font = null;

    private Map<String, CellStyle> cellStylesMapping;

    private boolean recalculateFormula = false;

    private int rowAccessWindowSize = SXSSFWorkbook.DEFAULT_WINDOW_SIZE;// used in auto flush

    private boolean isTrackAllColumns = false;

    private String password = null;

    public ExcelTool() {
        cellStylesMapping = new HashMap<>();
    }

    public void setAppend(boolean appendWorkbook, boolean appendSheet) {
        this.appendWorkbook = appendWorkbook;
        this.appendSheet = appendSheet;
    }

    public void setXY(boolean isAbsY, int absX, int absY, boolean keepFormat) {
        this.isAbsY = isAbsY;
        this.absX = absX;
        this.absY = absY;
        this.keepCellFormat = keepFormat;
    }

    public void setSheet(String sheetName) {
        this.sheetName = sheetName;
    }

    public void setRecalculateFormula(boolean recalculateFormula) {
        this.recalculateFormula = recalculateFormula;
    }

    public void prepareStream() {
        wb = new SXSSFWorkbook(rowAccessWindowSize);
        sheet = wb.createSheet(sheetName);
        if (isAbsY) {
            startX = absX;
            curY = absY;
        }
    }

    public void prepareXlsxFile(String fileName) throws Exception {
        File xlsxFile = new File(fileName);
        if (xlsxFile.exists()) {
            if (isAbsY && keepCellFormat) {
                initPreXlsx(fileName);
            }
            if (appendWorkbook) {
                appendActionForFile(fileName);
            } else {
                xlsxFile.delete();
                wb = new SXSSFWorkbook(rowAccessWindowSize);
                sheet = wb.createSheet(sheetName);
            }
        } else {
            wb = new SXSSFWorkbook(rowAccessWindowSize);
            sheet = wb.createSheet(sheetName);
        }
        if (isAbsY) {
            startX = absX;
            curY = absY;
        }
    }

    public void prepareXlsmFile(String fileName) throws Exception {
        File xlsmFile = new File(fileName);
        if (xlsmFile.exists()) {
            if (isAbsY && keepCellFormat) {
                initPreXlsx(fileName);
            }
            if (appendWorkbook) {
                appendActionForFile(fileName);
            } else {
                xlsmFile.delete();
                wb = new SXSSFWorkbook(new XSSFWorkbook(XSSFWorkbookType.XLSM), rowAccessWindowSize);
                sheet = wb.createSheet(sheetName);
            }
        } else {
            wb = new SXSSFWorkbook(new XSSFWorkbook(XSSFWorkbookType.XLSM), rowAccessWindowSize);
            sheet = wb.createSheet(sheetName);
        }
        if (isAbsY) {
            startX = absX;
            curY = absY;
        }
    }

    private void appendActionForFile(String fileName) throws Exception {
        if (password == null) {
            InputStream inp = new FileInputStream(fileName);
            wb = WorkbookFactory.create(inp);
        } else {
           wb = readEncryptedFile(fileName);
        }
        sheet = wb.getSheet(sheetName);
        if (sheet != null) {
            if (appendSheet) {
                if (sheet.getLastRowNum() != 0 || sheet.getRow(0) != null) {
                    curY = sheet.getLastRowNum() + 1;
                }
            } else {
                wb.removeSheetAt(wb.getSheetIndex(sheetName));
                sheet = wb.createSheet(sheetName);
            }
        } else {
            sheet = wb.createSheet(sheetName);
        }
    }

    /**
     *
     * @return start insert row index.
     */
    public int getStartRow() {
        return curY;
    }

    private void initPreXlsx(String fileName) throws Exception {
        if(password == null) {
            InputStream preIns = new FileInputStream(fileName);
            preWb = WorkbookFactory.create(preIns);
        } else {
            preWb = readEncryptedFile(fileName);
        }
        preSheet = preWb.getSheet(sheetName);
    }
    
    private Workbook readEncryptedFile(String fileName)
            throws IOException, GeneralSecurityException {
        InputStream inp = new FileInputStream(fileName);
        POIFSFileSystem fs = new POIFSFileSystem(inp);
        EncryptionInfo info = new EncryptionInfo(fs);
        Decryptor decryptor = Decryptor.getInstance(info);
        if (!decryptor.verifyPassword(password)) {
            throw new GeneralSecurityException("Error: Wrong password provided!");
        }
        InputStream dataStream = decryptor.getDataStream(fs);
        return WorkbookFactory.create(dataStream);
    }

    public void setFont(String fontName) {
        if (StringUtils.isNotEmpty(fontName)) {
            font = wb.createFont();
            font.setFontName(fontName);
        }
    }

    public void addRow() {
        if (isAbsY && keepCellFormat) {
            if (preSheet != null) {
                preRow = preSheet.getRow(curY);
            } else {
                preRow = null;
            }
        }
        curRow = sheet.getRow(curY);
        if (curRow == null) {
            curRow = sheet.createRow(curY);
        }
        if (keepCellFormat) {
            short rowHeight = curRow.getHeight();
            if (rowHeight != -1) {
                curRow.setHeight(rowHeight);
            }
        }
        curY = curY + 1;
        xOffset = 0;
    }

    private void addCell() {
        if (isAbsY && keepCellFormat) {
            if (preRow != null) {
                preCell = preRow.getCell(startX + xOffset);
            } else {
                preCell = null;
            }
        }
        curCell = curRow.createCell(startX + xOffset);
        xOffset++;
    }

    private CellStyle getNormalCellStyle() {
        CellStyle preCellStyle = getPreCellStyle();
        if (preCellStyle == null) {
            if (cellStylesMapping.get("normal") != null) {
                return cellStylesMapping.get("normal");
            } else {
                CellStyle style = wb.createCellStyle();
                if (font != null) {
                    style.setFont(font);
                }
                cellStylesMapping.put("normal", style);
                return style;
            }
        } else {
            return preCellStyle;
        }

    }

    private CellStyle getDateCellStyle(String pattern) {
        CellStyle preCellStyle = getPreCellStyle();
        if (preCellStyle == null) {
            if (cellStylesMapping.get(pattern) != null) {
                return cellStylesMapping.get(pattern);
            } else {
                CellStyle style = wb.createCellStyle();
                if (font != null) {
                    style.setFont(font);
                }
                if (StringUtils.isNotEmpty(pattern)) {
                    style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(pattern));
                }
                cellStylesMapping.put(pattern, style);
                return style;
            }
        } else {
            return preCellStyle;
        }
    }

    private CellStyle getPreCellStyle() {
        if (preSheet != null && isAbsY && keepCellFormat) {
            CellStyle preCellStyle;
            if (preCell == null) {
                preCellStyle = preSheet.getColumnStyle(curCell.getColumnIndex());
            } else {
                preCellStyle = preCell.getCellStyle();
            }

            CellStyle targetCellStyle = wb.createCellStyle();
            targetCellStyle.cloneStyleFrom(preCellStyle);

            return targetCellStyle;

        } else {
            return null;
        }
    }

    public void addCellValue(boolean booleanValue) {
        addCell();
        curCell.setCellValue(booleanValue);
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void addCellValue(Date dateValue, String pattern) {
        addCell();
        curCell.setCellValue(dateValue);
        curCell.setCellStyle(getDateCellStyle(pattern));
    }

    public void addCellValue(double doubleValue) {
        addCell();
        curCell.setCellValue(doubleValue);
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void addCellValue(String stringValue) {
        addCell();
        curCell.setCellValue(stringValue);
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void addCellNullValue() {
        addCell();
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void setColAutoSize(int colNum) {
        if (!isTrackAllColumns) {
            trackAllColumnsForAutoSizing();
        }
        sheet.autoSizeColumn(startX + colNum, true);
    }

    public void trackAllColumnsForAutoSizing() {
        if (sheet instanceof SXSSFSheet) {
            ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        }
        isTrackAllColumns = true;
    }

    public void setRowAccessWindowSize(int rowAccessWindowSize) {
        this.rowAccessWindowSize = rowAccessWindowSize;
    }

    public void writeExcel(OutputStream outputStream) throws Exception {
        try {
            wb.write(outputStream);
            wb.close();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public void writeExcel(String fileName, boolean createDir) throws Exception {
        if (createDir) {
            File file = new File(fileName);
            File pFile = file.getParentFile();
            if (pFile != null && !pFile.exists()) {
                pFile.mkdirs();
            }
        }
        if (appendWorkbook && appendSheet && recalculateFormula) {
            evaluateFormulaCell();
        }
        FileOutputStream fileOutput = new FileOutputStream(fileName);
        POIFSFileSystem fs = null;
        try {
            if (password == null) {
                wb.write(fileOutput);
            } else {
                fs = new POIFSFileSystem();
                Encryptor encryptor = new EncryptionInfo(EncryptionMode.agile).getEncryptor();
                encryptor.confirmPassword(password);
                OutputStream encryptedDataStream = encryptor.getDataStream(fs);
                wb.write(encryptedDataStream);
                encryptedDataStream.close(); // this is mandatory to do that at that point
                fs.writeFilesystem(fileOutput);
            }
        } finally {
            wb.close();
            fileOutput.close();
            if (fs != null) {
                fs.close();
            }
        }
    }

    private void evaluateFormulaCell() {
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
            sheet = wb.getSheetAt(sheetNum);
            for (Row r : sheet) {
                for (Cell c : r) {
                    if (c.getCellTypeEnum() == CellType.FORMULA) {
                        evaluator.evaluateFormulaCellEnum(c);
                    }
                }
            }
        }
    }

    public void flushRowInMemory() throws Exception {
        if (wb instanceof SXSSFWorkbook) {
            ((SXSSFSheet) sheet).flushRows();
        }
    }

    public void setPasswordProtection(String password) {
        this.password = password;
    }

}
