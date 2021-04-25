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

import org.apache.spark.api.java.function.Function;

public class FilterColumnsFunction implements
		Function<List<Object>, List<Object>> {
	private static final long serialVersionUID = 1L;
	private List<Integer> colsId;

	public FilterColumnsFunction(List<Integer> colsId) {
		this.colsId = colsId;
	}

	public List<Object> call(List<Object> list) throws Exception {
		List<Object> listToReturn = new ArrayList<Object>();
		for (int i = 0; i < this.colsId.size(); i++) {
			if (this.colsId.get(i) < 0) {
				listToReturn.add("");
			} else
				listToReturn.add(list.get(this.colsId.get(i)));
		}
		return listToReturn;
	}
}
