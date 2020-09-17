// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.fileprocess.delimited;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This factory is used to create a properiate DelimitedDataReader accordiong to the field delimiter(or field separator)
 * and record delimiter(or row separator). All the APIs that needed by tFileInputDelimited are provided by
 * DelimitedDataReader. And we can not directly creat the instance of subclasses of DelimitedDataReader, in fact, all
 * the subclasses are invisible out of this package.<br/>
 *
 * @author gke
 */
public class DelimitedDataReaderFactory {

    /**
     * DOC ke Comment method "createDelimitedDataReader".
     *
     * @param inputStream A java Reader source of delimited data
     * @param delimiter the field delimiter(field separator)
     * @param recordDelimiter the record delimiter(row separator)
     * @param skipEmptyRecords flag for skip the empty records
     * @return an instance of DelmitedDataReader
     * @throws IOException
     */
    public static DelimitedDataReader createDelimitedDataReader(BufferedReader inputStream, String delimiter,
            String recordDelimiter, boolean skipEmptyRecords) throws IOException {
        int dLength = delimiter.length();
        int rLength = recordDelimiter.length();
        if (dLength == 1 && rLength == 1) {

            return new SimpleDelimitedDataReader(inputStream, delimiter.charAt(0), recordDelimiter.charAt(0),
                    skipEmptyRecords);

        } else if (dLength == 1 && rLength > 1) {

            return new ComplexDelimitedDataReader1(inputStream, delimiter.charAt(0), recordDelimiter, skipEmptyRecords);

        } else if (dLength > 1 && rLength == 1) {

            return new ComplexDelimitedDataReader2(inputStream, delimiter, recordDelimiter.charAt(0), skipEmptyRecords);

        } else if (dLength == rLength) {

            return new ComplexDelimitedDataReader3(inputStream, delimiter, recordDelimiter, skipEmptyRecords);

        } else if (dLength > rLength) {

            return new ComplexDelimitedDataReader4(inputStream, delimiter, recordDelimiter, skipEmptyRecords);

        } else {

            return new ComplexDelimitedDataReader5(inputStream, delimiter, recordDelimiter, skipEmptyRecords);

        }
    }

    /**
     * DOC ke Comment method "createDelimitedDataReader".
     *
     * @param file a delimited file
     * @param delimiter the field delimiter(field separator)
     * @param recordDelimiter the record delimiter(row separator)
     * @param skipEmptyRecords flag for skip the empty records
     * @return an instance of DelmitedDataReader
     * @throws IOException
     */
    public static DelimitedDataReader createDelimitedDataReader(String file, String delimiter, String recordDelimiter,
            boolean skipEmptyRecords) throws IOException {

        BufferedReader inputStream = new BufferedReader(new FileReader(file));
        return createDelimitedDataReader(inputStream, delimiter, recordDelimiter, skipEmptyRecords);

    }

    /**
     * DOC ke Comment method "createDelimitedDataReader".
     *
     * @param file a delimited file
     * @param encoding
     * @param delimiter the field delimiter(field separator)
     * @param recordDelimiter the record delimiter(row separator)
     * @param skipEmptyRecords flag for skip the empty records
     * @return an instance of DelmitedDataReader
     * @throws IOException
     */
    public static DelimitedDataReader createDelimitedDataReader(String file, String encoding, String delimiter,
            String recordDelimiter, boolean skipEmptyRecords) throws IOException {

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        return createDelimitedDataReader(inputStream, delimiter, recordDelimiter, skipEmptyRecords);

    }

    /**
     * DOC ke Comment method "createDelimitedDataReader".
     *
     * @param inputStream A java stream source of delimited data
     * @param delimiter the field delimiter(field separator)
     * @param recordDelimiter the record delimiter(row separator)
     * @param skipEmptyRecords flag for skip the empty records
     * @return an instance of DelmitedDataReader
     * @throws IOException
     */
    public static DelimitedDataReader createDelimitedDataReader(InputStream inputStream, String delimiter,
            String recordDelimiter, boolean skipEmptyRecords) throws IOException {

        BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(inputStream));
        return createDelimitedDataReader(inputStreamReader, delimiter, recordDelimiter, skipEmptyRecords);

    }

    /**
     * DOC ke Comment method "createDelimitedDataReader".
     *
     * @param inputStream A java stream source of delimited data
     * @param encoding
     * @param delimiter the field delimiter(field separator)
     * @param recordDelimiter the record delimiter(row separator)
     * @param skipEmptyRecords flag for skip the empty records
     * @return an instance of DelmitedDataReader
     * @throws IOException
     */
    public static DelimitedDataReader createDelimitedDataReader(InputStream inputStream, String encoding,
            String delimiter, String recordDelimiter, boolean skipEmptyRecords) throws IOException {

        BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(inputStream, encoding));
        return createDelimitedDataReader(inputStreamReader, delimiter, recordDelimiter, skipEmptyRecords);

    }
}
