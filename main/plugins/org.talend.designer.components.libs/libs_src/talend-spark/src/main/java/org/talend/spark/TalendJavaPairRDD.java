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

import java.util.Comparator;

import org.apache.hadoop.mapred.JobConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

import com.google.common.base.Optional;

public class TalendJavaPairRDD<K, V> extends TalendPairRDD<K, V> {
	private JavaPairRDD<K, V> rdd;

	public TalendJavaPairRDD(JavaPairRDD<K, V> rdd) {
		this.rdd = rdd;
	}

	public JavaPairRDD<K, V> getRdd() {
		return rdd;
	}

	@Override
	public TalendPairRDD<K, V> getTalendRDD() {
		return this;
	}

	public void setRdd(JavaPairRDD<K, V> rdd) {
		this.rdd = rdd;
	}

	@Override
	public <R> TalendRDD<R> map(Function<Tuple2<K, V>, R> func) {
		return new TalendJavaRDD<R>(this.rdd.map(func));
	}

	@Override
	public TalendRDD<Tuple2<K, V>> toJavaRDD() {
		return new TalendJavaRDD<Tuple2<K, V>>(rdd.rdd().toJavaRDD());
	}

	@Override
	public TalendPairRDD<K, Iterable<V>> groupByKey() {
		return new TalendJavaPairRDD<K, Iterable<V>>(this.rdd.groupByKey());
	}

	@Override
	public TalendPairRDD<K, V> sortByKey(Comparator<K> comparator) {
		return new TalendJavaPairRDD<K, V>(this.rdd.sortByKey(comparator));
	}

	@Override
	public <K2, V2> TalendPairRDD<K2, V2> mapToPair(PairFunction<Tuple2<K, V>, K2, V2> func) {
		return new TalendJavaPairRDD<K2, V2>(this.rdd.mapToPair(func));
	}

	@Override
	public <W> TalendPairRDD<K, Tuple2<V, W>> join(TalendPairRDD<K, W> dataM2) {
		return new TalendJavaPairRDD<K, Tuple2<V, W>>(
				this.rdd.join(((TalendJavaPairRDD<K, W>) dataM2).getRdd()));
	}

	@Override
	public <W> TalendPairRDD<K, Tuple2<V, Optional<W>>> leftOuterJoin(
			TalendPairRDD<K, W> dataM2) {
		return new TalendJavaPairRDD<K, Tuple2<V, Optional<W>>>(
				this.rdd.leftOuterJoin(((TalendJavaPairRDD<K, W>) dataM2)
						.getRdd()));
	}

	@Override
	public <W> TalendPairRDD<K, Tuple2<Iterable<V>, Iterable<W>>> cogroup(
			TalendPairRDD<K, W> dataM2) {
		return new TalendJavaPairRDD<K, Tuple2<Iterable<V>, Iterable<W>>>(
				this.rdd.cogroup(((TalendJavaPairRDD<K, W>) dataM2).getRdd()));
	}

	@Override
	public <U> TalendPairRDD<K, U> mapValues(Function<V, U> func) {
		return new TalendJavaPairRDD<K, U>(this.rdd.mapValues(func));
	}

	@Override
	public TalendPairRDD<K, V> reduceByKey(Function2<V, V, V> func) {
		return new TalendJavaPairRDD<K, V>(this.rdd.reduceByKey(func));
	}

	@Override
	public void saveAsHadoopDataset(JobConf conf) {
		this.rdd.saveAsHadoopDataset(conf);
	}

	@Override
	public TalendPairRDD<K, V> cache() {
		return new TalendJavaPairRDD<K, V>(this.rdd.cache());
	}
}
