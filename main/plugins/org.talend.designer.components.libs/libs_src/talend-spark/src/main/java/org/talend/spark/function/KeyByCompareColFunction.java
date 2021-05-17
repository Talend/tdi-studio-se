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
import org.talend.spark.utils.CompareCol;

import scala.Tuple2;

public class KeyByCompareColFunction implements
		PairFunction<List<Object>, List<Object>, List<Object>> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private List<CompareCol> compCols;

	public KeyByCompareColFunction(List<CompareCol> compCols) {
		this.compCols = compCols;
	}

	public Tuple2<List<Object>, List<Object>> call(List<Object> d) {
		List<Object> key = new ArrayList<Object>(compCols.size());
		for (int i = 0; i < compCols.size(); i++) {
			key.add(i, d.get(compCols.get(i).getColId()));
		}

		List<Object> values = new ArrayList<Object>(d.size());
		int valId = 0;
		for (Integer i = 0; i < d.size(); i++) {
			boolean contain = false;
			for (CompareCol compCol : compCols) {
				if (compCol.getColId().equals(i)) {
					contain = true;
					break;
				}
			}
			if (!contain) {
				values.add(valId, d.get(i));
				valId++;
			}
		}
		return new Tuple2<List<Object>, List<Object>>(key, values);
	}
}
