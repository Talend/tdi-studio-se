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

import org.apache.spark.api.java.function.Function;
import org.talend.spark.TalendPairRDD;
import org.talend.spark.TalendRDD;
import org.talend.spark.function.AggregateFunction;
import org.talend.spark.utils.Aggregation;

import scala.Tuple2;

/**
 * Company : Altic - LIPN User: Tugdual Sarazin Date: 18/04/14 Time: 18:16
 */
public class Aggregate {
	public static TalendPairRDD<List<Object>, Iterable<List<Object>>> run(
			final TalendRDD<List<Object>> dataM, final List<Integer> colsId)
			throws Exception {
		TalendPairRDD<List<Object>, List<Object>> pairDataM = KeyBy.run(dataM,
				colsId);
		return pairDataM.groupByKey();
	}

	public static TalendRDD<List<Object>> run(
			final TalendRDD<List<Object>> dataM, final List<Integer> colsId,
			final List<Aggregation> agg) throws Exception {
		TalendPairRDD<List<Object>, List<Object>> pairDataM = KeyBy.run(dataM,
				colsId);
		TalendPairRDD<List<Object>, Iterable<List<Object>>> aggres = pairDataM
				.groupByKey();
		TalendPairRDD<List<Object>, List<Object>> aggregationResult = (((TalendPairRDD<List<Object>, Iterable<List<Object>>>) aggres)
				.mapValues(new AggregateFunction(agg)));
		return aggregationResult
				.map(new Function<Tuple2<List<Object>, List<Object>>, List<Object>>() {
					private static final long serialVersionUID = 1L;

					public List<Object> call(
							Tuple2<List<Object>, List<Object>> arg0)
							throws Exception {
						return arg0._2;
					}
				});
	}
}
