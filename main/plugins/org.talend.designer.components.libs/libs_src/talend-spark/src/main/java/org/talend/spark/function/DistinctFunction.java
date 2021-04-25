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

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;

public class DistinctFunction<T> implements Function<JavaRDD<T>, JavaRDD<T>> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public JavaRDD<T> call(JavaRDD<T> rdd) throws Exception {
		return rdd.distinct();
	}
}
