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

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;

public class TalendJavaRDD<T> extends TalendRDD<T> {
	private JavaRDD<T> rdd;

	public TalendJavaRDD(JavaRDD<T> rdd) {
		this.rdd = rdd;
	}

	public JavaRDD<T> getRdd() {
		return rdd;
	}

	@Override
	public TalendRDD<T> getTalendRDD() {
		return this;
	}

	public void setRdd(JavaRDD<T> rdd) {
		this.rdd = rdd;
	}

	@Override
	public <R> TalendRDD<R> map(Function<T, R> func) {
		return new TalendJavaRDD<R>(this.rdd.map(func));
	}

	@Override
	public TalendRDD<T> filter(Function<T, Boolean> func) {
		return new TalendJavaRDD<T>(this.rdd.filter(func));
	}

	@Override
	public TalendRDD<T> distinct() {
		return new TalendJavaRDD<T>(this.rdd.distinct());
	}

	@Override
	public TalendRDD<T> sample(boolean isWithReplacement, double fraction,
			int seed) {
		return new TalendJavaRDD<T>(this.rdd.sample(isWithReplacement,
				fraction, seed));
	}

	@Override
	public TalendRDD<T> union(TalendRDD<T> rdd) {
		return new TalendJavaRDD<T>(this.rdd.union(((TalendJavaRDD<T>) rdd)
				.getRdd()));
	}

	@Override
	public void saveAsTextFile(String filename) {
		this.rdd.saveAsTextFile(filename);
	}

	@Override
	public void toConsole() {
		for (T row : this.rdd.collect()) {
			System.out.println(row);
		}
	}

	@Override
	public List<T> collect() {
		return this.rdd.collect();
	}

	@Override
	public <K2, V2> TalendPairRDD<K2, V2> mapToPair(PairFunction<T, K2, V2> func) {
		return new TalendJavaPairRDD<K2, V2>(this.rdd.mapToPair(func));
	}

	@Override
	public <U> TalendRDD<U> flatMap(FlatMapFunction<T, U> func) {
		return new TalendJavaRDD<U>(this.rdd.flatMap(func));
	}

	@Override
	public TalendRDD<T> cache() {
		return new TalendJavaRDD<T>(this.rdd.cache());
	}
}
