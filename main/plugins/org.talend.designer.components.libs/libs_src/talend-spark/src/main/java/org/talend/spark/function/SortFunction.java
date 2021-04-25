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

import java.util.Comparator;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;

public class SortFunction<K, V> implements Function<JavaPairRDD<K, V>, JavaPairRDD<K, V>> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Comparator<K> comparator;

	public SortFunction(Comparator<K> comparator) {
		this.comparator = comparator;
	}

	public JavaPairRDD<K, V> call(JavaPairRDD<K, V> rdd) throws Exception {
		return rdd.sortByKey(this.comparator);
	}
}
