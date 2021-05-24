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

import java.util.List;

import org.apache.spark.api.java.function.Function;


public class StoreJavaRDDFunction implements Function<List<Object>, Object> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String separator;

	public StoreJavaRDDFunction(String separator) {
		this.separator = separator;
	}

	public Object call(List<Object> row) {
		StringBuilder line = new StringBuilder();
		for (int i = 0; i < row.size(); i++) {
			// Append word
			line.append(row.get(i));
			// Append separator
			if (i < row.size() - 1) {
				line.append(separator);
			}
		}
		return line.toString();
	}

}
