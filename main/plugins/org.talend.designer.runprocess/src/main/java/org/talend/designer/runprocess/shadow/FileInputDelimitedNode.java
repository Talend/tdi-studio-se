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
package org.talend.designer.runprocess.shadow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Level;
import org.eclipse.core.runtime.Path;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.StringUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.shadow.ShadowProcess.EShadowProcessType;
import org.talend.fileprocess.FileInputDelimited;
import org.talend.librariesmanager.prefs.LibrariesManagerUtils;

/**
 * DOC chuger class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class FileInputDelimitedNode extends FileInputNode {

    /**
     * Constructs a new FileInputNode.
     */
    public FileInputDelimitedNode(String filename, String rowSep, String fieldSep, int limitRows, int headerRows, int footerRows,
            String escapeChar, String textEnclosure, boolean removeEmptyRow, boolean spitRecord, String encoding,
            EShadowProcessType fileType) {
        super("tFileInputDelimited"); //$NON-NLS-1$

        boolean csvoption = false;
        String languageName = LanguageManager.getCurrentLanguage().getName();
        switch (fileType) {

        case FILE_DELIMITED:
            csvoption = false;

            if (languageName.equals("perl")) { //$NON-NLS-1$
                int max = getColumnCount(filename, rowSep, fieldSep, limitRows, headerRows, escapeChar, textEnclosure,
                        EShadowProcessType.FILE_DELIMITED);
                this.setColumnNumber(max);

            } else {

                int max = 0;
                try {

                    max = FileInputDelimited
                            .getMaxColumnCount(trimParameter(filename), trimParameter(encoding),
                                    trimParameter(StringUtils.loadConvert(fieldSep, languageName)),
                                    trimParameter(StringUtils.loadConvert(rowSep, languageName)), true, spitRecord, headerRows,
                                    limitRows);

                } catch (IOException e) {
                    // e.printStackTrace();
                    ExceptionHandler.process(e);
                }
                if (max > 0) {
                    this.setColumnNumber(max);
                }

            }
            break;

        case FILE_CSV:
            csvoption = true;
            if (languageName.equals("perl")) { //$NON-NLS-1$
                int max = getColumnCount(filename, rowSep, fieldSep, limitRows, headerRows, escapeChar, textEnclosure,
                        EShadowProcessType.FILE_CSV);
                this.setColumnNumber(max);
            } else {
                CSVFileColumnConnter cr = null;
                try {
                    cr = new CSVFileColumnConnter();
                    cr.setSeperator(trimParameter(StringUtils.loadConvert(fieldSep, languageName)).charAt(0));
                    int columnCount = 0;
                    columnCount = cr.countMaxColumnNumber(new File(TalendTextUtils.removeQuotes(filename)), limitRows);
                    if (columnCount > 0) {
                        this.setColumnNumber(columnCount);
                    }
                } catch (UnsupportedEncodingException e) {
                    ExceptionHandler.process(e);
                } catch (FileNotFoundException e) {
                    ExceptionHandler.process(e);
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }
            break;

        default:
            break;
        }

        String[] paramNames = null;

        if (!csvoption) {
            paramNames = new String[] { "FILENAME", "ROWSEPARATOR", "FIELDSEPARATOR", "LIMIT", "HEADER", "FOOTER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                    "ESCAPE_CHAR", "TEXT_ENCLOSURE", "REMOVE_EMPTY_ROW", "ENCODING", "CSV_OPTION", "SPLITRECORD" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        } else {
            paramNames = new String[] { "FILENAME", "CSVROWSEPARATOR", "FIELDSEPARATOR", "LIMIT", "HEADER", "FOOTER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                    "ESCAPE_CHAR", "TEXT_ENCLOSURE", "REMOVE_EMPTY_ROW", "ENCODING", "CSV_OPTION", "SPLITRECORD" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        }
        String[] paramValues = new String[] { filename, rowSep, fieldSep, Integer.toString(limitRows),
                Integer.toString(headerRows), Integer.toString(footerRows), escapeChar, textEnclosure,
                Boolean.toString(removeEmptyRow), encoding, Boolean.toString(csvoption), Boolean.toString(spitRecord) };

        IComponent component = ComponentsFactoryProvider.getInstance().get("tFileInputDelimited", //$NON-NLS-1$
                ComponentCategory.CATEGORY_4_DI.getName());
        this.setElementParameters(component.createElementParameters(this));
        for (int i = 0; i < paramNames.length; i++) {
            if (paramValues[i] != null) {
                IElementParameter param = this.getElementParameter(paramNames[i]);
                if (param != null) {
                    param.setValue(paramValues[i]);
                }
            }
        }
    }

    private int getColumnCount(String filename, String rowSep, String fieldSep, int limitRows, int headerRows, String escapeChar,
            String textEnclosure, EShadowProcessType fileType) {

        File config = new File(CorePlugin.getDefault().getPreferenceStore().getString(ITalendCorePrefConstants.FILE_PATH_TEMP)
                + "/conf.pl"); //$NON-NLS-1$
        if (config.exists()) {
            config.delete();
        }
        String modulepath = LibrariesManagerUtils.getLibrariesPath(ECodeLanguage.PERL);
        FileWriter filewriter;
        String str = "0"; //$NON-NLS-1$
        File resultFile = new File(CorePlugin.getDefault().getPreferenceStore()
                .getString(ITalendCorePrefConstants.FILE_PATH_TEMP)
                + "/result.txt"); //$NON-NLS-1$

        if (resultFile.exists()) {
            resultFile.delete();
        }

        try {
            filewriter = new FileWriter(config, true);
            switch (fileType) {
            case FILE_DELIMITED:
                filewriter.write("$conf{filename} = " + filename + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{row_separator} = " + rowSep + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{field_separator} = " + fieldSep + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{header} = " + headerRows + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{limit} = " + limitRows + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{result_file} =\'" + resultFile.toString() + "\';"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{type} = \'delimited\';"); //$NON-NLS-1$
                break;
            case FILE_CSV:
                filewriter.write("$conf{filename} = " + filename + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{row_separator} = " + rowSep + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{field_separator} = " + fieldSep + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{escape_char} = " + escapeChar + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{text_enclosure} = " + textEnclosure + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{header} = " + headerRows + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{limit} = " + limitRows + ";"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{result_file} =\'" + resultFile.toString() + "\';"); //$NON-NLS-1$ //$NON-NLS-2$
                filewriter.write("$conf{type} = \'CSV\';"); //$NON-NLS-1$
                break;
            default:
                break;
            }

            filewriter.close();
            modulepath = modulepath + "/column_counter_delimited.pl"; //$NON-NLS-1$
            StringBuffer out = new StringBuffer();
            StringBuffer err = new StringBuffer();

            IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                    IRunProcessService.class);
            service.perlExec(out, err, new Path(modulepath), null, Level.DEBUG, "", null, -1, -1, new String[] { "--conf=" //$NON-NLS-1$ //$NON-NLS-2$
                    + config });

            FileReader filereader = new FileReader(resultFile);
            BufferedReader reader = new BufferedReader(filereader);
            str = reader.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            ExceptionHandler.process(e);
        } catch (ProcessorException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        return Integer.parseInt(str);
    }
}
