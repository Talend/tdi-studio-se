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
import org.talend.spark.function.FilterColumnsFunction;
import org.talend.spark.function.FilterRowFunction;
import org.talend.spark.utils.FilterObject;

/**
 * Company : Altic - LIPN User: Tugdual Sarazin Date: 23/04/14 Time: 17:46
 */
public class Filter {

	public static TalendRDD<List<Object>> run(
			final TalendRDD<List<Object>> dataM,
			final List<FilterObject> filters) throws Exception {
		return dataM.filter(new FilterRowFunction(filters));
	}

	public static TalendRDD<List<Object>> filterColumns(
			final TalendRDD<List<Object>> dataM, final List<Integer> colsId)
			throws Exception {
		return dataM.map(new FilterColumnsFunction(colsId));
	}

}
