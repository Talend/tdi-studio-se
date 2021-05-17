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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.skife.csv.ReaderCallback;
import org.skife.csv.SimpleReader;

/**
 * Created by Marvin Wang on May 10, 2013.
 */
public class CSVFileColumnConnter extends SimpleReader {

    public static final String EXCEPTION_CODE_CSV99 = "CSV99"; //$NON-NLS-1$

    public CSVFileColumnConnter() {
        super();
    }

    public int countMaxColumnNumber(File file, int limitNum) throws IOException {
        InputStream fileInputStream = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(fileInputStream);
        BufferedReader bfReader = new BufferedReader(reader);
        TalendReaderCallback talendCallbackReader = new TalendReaderCallback(limitNum);

        try {
            parse(bfReader, talendCallbackReader);
        } catch (RuntimeException e) {
            if (EXCEPTION_CODE_CSV99.equals(e.getMessage())) {
                return talendCallbackReader.getMaxColumnNum();
            }
        } finally {
            bfReader.close();
        }
        return talendCallbackReader.getMaxColumnNum();
    }

    class TalendReaderCallback implements ReaderCallback {

        private int maxColumnNum = 0;

        private int limitNum;

        private int count = 0;

        public TalendReaderCallback(int limitNum) {
            this.limitNum = limitNum;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.skife.csv.ReaderCallback#onRow(java.lang.String[])
         */
        @Override
        public void onRow(String[] arg0) {
            if (count > limitNum) {
                throw new RuntimeException(EXCEPTION_CODE_CSV99);
            }
            count++;
            if (arg0 != null && arg0.length > 0) {
                if (arg0.length > maxColumnNum) {
                    maxColumnNum = arg0.length;
                }
            }
        }

        /**
         * Getter for maxColumnNum.
         *
         * @return the maxColumnNum
         */
        public int getMaxColumnNum() {
            return this.maxColumnNum;
        }
    }
}
