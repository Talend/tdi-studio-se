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
package org.talend.spark.function;

import java.util.Iterator;
import java.util.List;

import org.apache.spark.api.java.function.Function;

import scala.Tuple2;

public class StoreJavaPairRDDFunction implements Function<Tuple2<List<Object>, List<Object>>, Object> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String separator;

	public StoreJavaPairRDDFunction(String separator) {
		this.separator = separator;
	}

	public Object call(Tuple2<List<Object>, List<Object>> row) {
        StringBuilder line = new StringBuilder();
        // add keys to the row string
        for (Iterator<Object> it = row._1().iterator(); it.hasNext();) {
            line.append(it.next());
            if (it.hasNext()) {
                line.append(separator);
            }
        }
        // add values to the row string
        for (Iterator<Object> it = row._2().iterator(); it.hasNext();) {
            line.append(separator);
            line.append(it.next());
        }
        return line.toString();
    }
}
