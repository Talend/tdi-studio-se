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
import org.apache.spark.streaming.api.java.JavaDStream;
import org.talend.spark.function.DistinctFunction;
import org.talend.spark.function.SampleFunction;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class TalendDStreamRDD<T> extends TalendRDD<T> {
	private JavaDStream<T> rdd;

	public TalendDStreamRDD(JavaDStream<T> rdd) {
		this.rdd = rdd;
	}

	public JavaDStream<T> getRdd() {
		return rdd;
	}

	@Override
	public TalendRDD<T> getTalendRDD() {
		return this;
	}

	public void setRdd(JavaDStream<T> rdd) {
		this.rdd = rdd;
	}

	@Override
	public <R> TalendRDD<R> map(Function<T, R> func) {
		return new TalendDStreamRDD<R>(this.rdd.map(func));
	}

	@Override
	public TalendRDD<T> filter(Function<T, Boolean> func) {
		return new TalendDStreamRDD<T>(this.rdd.filter(func));
	}

	@Override
	public TalendRDD<T> sample(boolean isWithReplacement, double fraction,
			int seed) {
		return new TalendDStreamRDD<T>(rdd.transform(new SampleFunction<T>(
				isWithReplacement, fraction, seed)));
	}

	@Override
	public TalendRDD<T> distinct() {
		return new TalendDStreamRDD<T>(rdd.transform(new DistinctFunction<T>()));
	}

	@Override
	public TalendRDD<T> union(TalendRDD<T> rdd) {
		throw new NotImplementedException();
	}

	@Override
	public void saveAsTextFile(String filename) {
		this.rdd.dstream().saveAsTextFiles(filename, "");
	}

	@Override
	public void toConsole() {
		this.rdd.print();
	}

	 @Override
	 public List<T> collect() {
	 throw new NotImplementedException();
	 }

	@Override
	public <K2, V2> TalendPairRDD<K2, V2> mapToPair(PairFunction<T, K2, V2> func) {
		return new TalendDStreamPairRDD<K2, V2>(this.rdd.mapToPair(func));
	}

	@Override
	public <U> TalendRDD<U> flatMap(FlatMapFunction<T, U> func) {
		return new TalendDStreamRDD<U>(this.rdd.flatMap(func));
	}

	@Override
	public TalendRDD<T> cache() {
		return new TalendDStreamRDD<T>(this.rdd.cache());
	}
}
