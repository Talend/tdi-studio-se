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
import org.talend.spark.function.StoreJavaRDDFunction;

/**
 * Company : Altic - LIPN User: Tugdual Sarazin Date: 05/05/14 Time: 15:42
 */
public class Store {
	public static void run(String pathName,
			final TalendRDD<List<Object>> dataM, final String separator)
			throws Exception {
		TalendRDD<Object> rddDataTxt = dataM.map(new StoreJavaRDDFunction(
				separator));
		rddDataTxt.saveAsTextFile(pathName);
	}
}
