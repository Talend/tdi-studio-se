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
package org.talend.spark.operation;

import java.util.List;

import org.talend.spark.TalendRDD;
import org.talend.spark.function.RDDConverterFunction;

import scala.Tuple2;

public class RDDConverter {
	public static TalendRDD<List<Object>> convert(
			final TalendRDD<Tuple2<List<Object>, List<Object>>> data,
			final List<Integer> colIds) throws Exception {

		return data.map(new RDDConverterFunction(colIds));
	}

	public static TalendRDD<List<Object>> convert(
			final TalendRDD<Tuple2<List<Object>, List<Object>>> data)
			throws Exception {
		return convert(data, null);
	}
}
