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
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.talend.spark.function.SortFunction;

import scala.Tuple2;

import com.google.common.base.Optional;

public class TalendDStreamPairRDD<K, V> extends TalendPairRDD<K, V> {
	private JavaPairDStream<K, V> rdd;

	public TalendDStreamPairRDD(JavaPairDStream<K, V> javaPairDStream) {
		this.rdd = javaPairDStream;
	}

	public JavaPairDStream<K, V> getRdd() {
		return rdd;
	}

	@Override
	public TalendPairRDD<K, V> getTalendRDD() {
		return this;
	}

	public void setRdd(JavaPairDStream<K, V> rdd) {
		this.rdd = rdd;
	}

	@Override
	public <R> TalendRDD<R> map(Function<Tuple2<K, V>, R> function) {
		return new TalendDStreamRDD<R>(this.rdd.map(function));
	}

	@Override
	public TalendRDD<Tuple2<K, V>> toJavaRDD() {
		return new TalendDStreamRDD<Tuple2<K, V>>(rdd.toJavaDStream());
	}

	@Override
	public TalendPairRDD<K, Iterable<V>> groupByKey() {
		return new TalendDStreamPairRDD<K, Iterable<V>>(this.rdd.groupByKey());
	}

	@Override
	public TalendPairRDD<K, V> sortByKey(Comparator<K> comparator) {
		return new TalendDStreamPairRDD<K, V>(
				this.rdd.transformToPair(new SortFunction<K, V>(comparator)));
	}

	@Override
	public <K2, V2> TalendPairRDD<K2, V2> mapToPair(
			PairFunction<Tuple2<K, V>, K2, V2> func) {
		return new TalendDStreamPairRDD<K2, V2>(this.rdd.mapToPair(func));
	}

	@Override
	public <W> TalendPairRDD<K, Tuple2<V, Optional<W>>> leftOuterJoin(
			TalendPairRDD<K, W> dataM2) {
		if (dataM2 instanceof TalendDStreamPairRDD) {
			return new TalendDStreamPairRDD<K, Tuple2<V, Optional<W>>>(
					this.rdd.leftOuterJoin(((TalendDStreamPairRDD<K, W>) dataM2)
							.getRdd()));
		} else {
			final TalendJavaPairRDD<K, W> dataRDDM2 = (TalendJavaPairRDD<K, W>) dataM2;

			return new TalendDStreamPairRDD<K, Tuple2<V, Optional<W>>>(
					this.rdd.transformToPair(new Function<JavaPairRDD<K, V>, JavaPairRDD<K, Tuple2<V, Optional<W>>>>() {

						private static final long serialVersionUID = 1L;

						public JavaPairRDD<K, Tuple2<V, Optional<W>>> call(
								JavaPairRDD<K, V> dataRDDM1) throws Exception {
							return dataRDDM1.leftOuterJoin(dataRDDM2.getRdd());
						}

					}));
		}
	}

	@Override
	public <W> TalendPairRDD<K, Tuple2<V, W>> join(TalendPairRDD<K, W> dataM2) {
		if (dataM2 instanceof TalendDStreamPairRDD) {
			return new TalendDStreamPairRDD<K, Tuple2<V, W>>(
					this.rdd.join(((TalendDStreamPairRDD<K, W>) dataM2)
							.getRdd()));
		} else {
			final TalendJavaPairRDD<K, W> dataRDDM2 = (TalendJavaPairRDD<K, W>) dataM2;

			return new TalendDStreamPairRDD<K, Tuple2<V, W>>(
					this.rdd.transformToPair(new Function<JavaPairRDD<K, V>, JavaPairRDD<K, Tuple2<V, W>>>() {

						private static final long serialVersionUID = 1L;

						public JavaPairRDD<K, Tuple2<V, W>> call(
								JavaPairRDD<K, V> dataRDDM1) throws Exception {
							return dataRDDM1.join(dataRDDM2.getRdd());
						}

					}));
		}
	}

	@Override
	public <W> TalendPairRDD<K, Tuple2<Iterable<V>, Iterable<W>>> cogroup(
			TalendPairRDD<K, W> dataM2) {
		return new TalendDStreamPairRDD<K, Tuple2<Iterable<V>, Iterable<W>>>(
				this.rdd.cogroup(((TalendDStreamPairRDD<K, W>) dataM2).getRdd()));
	}

	@Override
	public <U> TalendPairRDD<K, U> mapValues(Function<V, U> func) {
		return new TalendDStreamPairRDD<K, U>(this.rdd.mapValues(func));
	}

	@Override
	public TalendPairRDD<K, V> reduceByKey(Function2<V, V, V> func) {
		return new TalendDStreamPairRDD<K, V>(this.rdd.reduceByKey(func));
	}

	@Override
	public void saveAsHadoopDataset(JobConf conf) {
		final JobConf config = conf;
		this.rdd.foreachRDD(new Function<JavaPairRDD<K, V>, Void>() {

			private static final long serialVersionUID = 1L;

			public Void call(JavaPairRDD<K, V> v1) throws Exception {
				v1.saveAsHadoopDataset(config);
				return null;
			}
		});
	}

	@Override
	public TalendPairRDD<K, V> cache() {
		return new TalendDStreamPairRDD<K, V>(this.rdd.cache());
	}
}
