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

import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

import com.google.common.base.Optional;

public abstract class TalendPairRDD<K, V> {
	public abstract TalendPairRDD<K, V> getTalendRDD();
	public abstract <R> TalendRDD<R> map(Function<Tuple2<K, V>, R> func);
	public abstract TalendRDD<Tuple2<K, V>> toJavaRDD();
	public abstract TalendPairRDD<K, Iterable<V>> groupByKey();
	public abstract TalendPairRDD<K, V> sortByKey(Comparator<K> comparator);
	public abstract <K2, V2> TalendPairRDD<K2, V2> mapToPair(PairFunction<Tuple2<K, V>, K2, V2> func);
	public abstract <W> TalendPairRDD<K, Tuple2<V, Optional<W>>> leftOuterJoin(TalendPairRDD<K, W> dataM2);
	public abstract <W> TalendPairRDD<K, Tuple2<V, W>> join(TalendPairRDD<K, W> dataM2);
	public abstract <W> TalendPairRDD<K, Tuple2<Iterable<V>, Iterable<W>>> cogroup(TalendPairRDD<K, W> dataM2);
	public abstract <U> TalendPairRDD<K, U> mapValues(Function<V, U> func);
	public abstract TalendPairRDD<K, V> reduceByKey(Function2<V, V, V> func);
	public abstract void saveAsHadoopDataset(org.apache.hadoop.mapred.JobConf conf);
	public abstract TalendPairRDD<K, V> cache();
}
