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

import org.talend.spark.TalendPairRDD;
import org.talend.spark.TalendRDD;
import org.talend.spark.utils.CompareCol;
import org.talend.spark.utils.SortComparator;

/**
 * Company : Altic - LIPN User: Tugdual Sarazin Date: 22/04/14 Time: 16:20
 */
public class Sort {
	public static TalendPairRDD<List<Object>, List<Object>> run(
			final TalendRDD<List<Object>> dataM, final List<CompareCol> compCols)
			throws Exception {
		TalendPairRDD<List<Object>, List<Object>> pairDataM = KeyByCompareCol
				.run(dataM, compCols);
		return pairDataM.sortByKey(new SortComparator(compCols));
	}
}
