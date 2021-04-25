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
import org.talend.spark.function.NormalizeFunction;

/**
 * Company : Talend User: rdubois
 */
public class Normalize {
	public static TalendRDD<List<Object>> run(
			final TalendRDD<List<Object>> dataM, final String separator,
			final int index) throws Exception {
		return dataM.flatMap(new NormalizeFunction(separator, index));
	}
}
