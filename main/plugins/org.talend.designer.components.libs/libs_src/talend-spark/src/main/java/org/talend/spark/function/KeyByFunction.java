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

import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

public class KeyByFunction implements PairFunction<List<Object>, List<Object>, List<Object>> {
	private static final long serialVersionUID = 1L;
	private List<Integer> colsId;

	public KeyByFunction(List<Integer> list) {
		this.colsId = list;
	}

	public Tuple2<List<Object>, List<Object>> call(List<Object> d) {
		List<Object> key = null;

		if (colsId != null && colsId.size() > 0) {
			key = new ArrayList<Object>(colsId.size());
			for (int i = 0; i < colsId.size(); i++) {
			    Object tmp = d.get(colsId.get(i));
				key.add(i, tmp!=null?String.valueOf(tmp):tmp);
			}
		}

		List<Object> values = new ArrayList<Object>(d.size());
		int valId = 0;
		for (Integer i = 0; i < d.size(); i++) {
			values.add(valId, d.get(i));
			valId++;
		}

		return new Tuple2<List<Object>, List<Object>>(key, values);
	}
}
