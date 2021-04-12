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

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.function.FlatMapFunction;

public class NormalizeFunction implements FlatMapFunction<List<Object>,List<Object>> {

	private static final long serialVersionUID = 1L;
	private String separator;
	private int index;

	public NormalizeFunction(String separator, int index) {
		this.separator = separator;
		this.index = index;
	}

	public Iterable<List<Object>> call(List<Object> data)
			throws Exception {
		String dataToNormalize = (String)data.get(this.index);
		List<List<Object>> list = new ArrayList<List<Object>>();
		for(String s : dataToNormalize.split(this.separator)) {
			List<Object> dataCopy = new ArrayList<Object>(data);
			dataCopy.set(this.index, s);
			list.add(dataCopy);
		}
		return list;
	}

}
