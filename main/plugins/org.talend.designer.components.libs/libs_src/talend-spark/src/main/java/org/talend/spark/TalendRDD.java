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
package org.talend.spark;

import java.util.List;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;


public abstract class TalendRDD<T> {
	public abstract <R> TalendRDD<R> map(Function<T, R> func);
	public abstract TalendRDD<T> filter(Function<T, Boolean> func);
	public abstract TalendRDD<T> sample(boolean isWithReplacement, double fraction, int seed);
	public abstract TalendRDD<T> distinct();
	public abstract TalendRDD<T> union(TalendRDD<T> rdd);
	public abstract void saveAsTextFile(String filename);
	public abstract void toConsole();
	public abstract List<T> collect();
	public abstract TalendRDD<T> getTalendRDD();
	public abstract <K2,V2> TalendPairRDD<K2, V2> mapToPair(PairFunction<T, K2, V2> func);
	public abstract <U> TalendRDD<U> flatMap(FlatMapFunction<T, U> func);
	public abstract TalendRDD<T> cache();
}
