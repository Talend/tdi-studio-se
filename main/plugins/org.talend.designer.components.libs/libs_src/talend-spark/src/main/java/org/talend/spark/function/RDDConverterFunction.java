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
import org.talend.spark.utils.Utils;

import scala.Tuple2;

public class RDDConverterFunction implements
		Function<Tuple2<List<Object>, List<Object>>, List<Object>> {

	private static final long serialVersionUID = 1L;
	private List<Integer> colIds;

	public RDDConverterFunction(List<Integer> colIds) {
		this.colIds = colIds;
	}

	public List<Object> call(Tuple2<List<Object>, List<Object>> input)
			throws Exception {
		List<Object> keys = input._1();
		List<Object> valuesWithoutKey = input._2();
		List<Object> output = new ArrayList<Object>();
		output.addAll(valuesWithoutKey);

		if(this.colIds != null) { // If colIds is null, then we don't want the keys in output
			int size = output.size();
			for (int i = size; i < size + this.colIds.size(); i++) {
				output.add(null);
			}

			if (this.colIds != null && this.colIds.size() > 0) {
				for (int index = 0; index < keys.size(); index++) {
					if (output.get(this.colIds.get(index)) != null) {
						output = Utils.ListShift(output, this.colIds.get(index));
					}
					output.set(this.colIds.get(index), keys.get(index));
				}
			} else {
				output.addAll(0, keys);
			}
		}
		return output;
	}
}
