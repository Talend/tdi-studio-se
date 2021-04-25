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

import org.talend.spark.TalendPairRDD;
import org.talend.spark.TalendRDD;
import org.talend.spark.function.CoGroupJoinFunction;
import org.talend.spark.function.InnerJoinFunction;
import org.talend.spark.function.LeftJoinFunction;

import scala.Tuple2;

import com.google.common.base.Optional;

/**
 * Company : Altic - LIPN User: Tugdual Sarazin Date: 22/04/14 Time: 16:48
 */
public class Join {
	/* Inner join */
	public static TalendPairRDD<List<Object>, List<Object>> inner(
			final TalendPairRDD<List<Object>, List<Object>> dataM1,
			final TalendPairRDD<List<Object>, List<Object>> dataM2)
			throws Exception {
		TalendPairRDD<List<Object>, Tuple2<List<Object>, List<Object>>> joinData = dataM1
				.join(dataM2);
		return joinData.mapToPair(new InnerJoinFunction());
	}

	public static TalendPairRDD<List<Object>, List<Object>> inner(
			final TalendRDD<List<Object>> dataM1, final List<Integer> colsId1,
			final TalendPairRDD<List<Object>, List<Object>> dataM2)
			throws Exception {
		return Join.inner(KeyBy.run(dataM1, colsId1), dataM2);
	}

	public static TalendPairRDD<List<Object>, List<Object>> inner(
			final TalendRDD<List<Object>> dataM1, final List<Integer> colsId1,
			final TalendRDD<List<Object>> dataM2, final List<Integer> colsId2)
			throws Exception {
		return Join.inner(KeyBy.run(dataM1, colsId1),
				KeyBy.run(dataM2, colsId2));
	}

	/* Left join */
	public static TalendPairRDD<List<Object>, List<Object>> left(
			final TalendPairRDD<List<Object>, List<Object>> dataM1,
			final TalendPairRDD<List<Object>, List<Object>> dataM2)
			throws Exception {
		TalendPairRDD<List<Object>, Tuple2<List<Object>, Optional<List<Object>>>> joinData = dataM1
				.leftOuterJoin(dataM2);

		return joinData.mapToPair(new LeftJoinFunction());
	}

	public static TalendPairRDD<List<Object>, List<Object>> left(
			final TalendRDD<List<Object>> dataM1, final List<Integer> colsId1,
			final TalendPairRDD<List<Object>, List<Object>> dataM2)
			throws Exception {
		return Join.left(KeyBy.run(dataM1, colsId1), dataM2);
	}

	public static TalendPairRDD<List<Object>, List<Object>> left(
			final TalendRDD<List<Object>> dataM1, final List<Integer> colsId1,
			final TalendRDD<List<Object>> dataM2, final List<Integer> colsId2)
			throws Exception {
		return Join
				.left(KeyBy.run(dataM1, colsId1), KeyBy.run(dataM2, colsId2));
	}

	/* CoGroup */
	public static TalendPairRDD<List<Object>, List<Object>> cogroup(
			final TalendPairRDD<List<Object>, List<Object>> dataM1,
			final TalendPairRDD<List<Object>, List<Object>> dataM2)
			throws Exception {
		TalendPairRDD<List<Object>, Tuple2<Iterable<List<Object>>, Iterable<List<Object>>>> joinData = dataM1
				.cogroup(dataM2);

		return joinData.mapToPair(new CoGroupJoinFunction());
	}

	public static TalendPairRDD<List<Object>, List<Object>> cogroup(
			final TalendRDD<List<Object>> dataM1, final List<Integer> colsId1,
			final TalendPairRDD<List<Object>, List<Object>> dataM2)
			throws Exception {
		return Join.cogroup(KeyBy.run(dataM1, colsId1), dataM2);
	}

	public static TalendPairRDD<List<Object>, List<Object>> cogroup(
			final TalendRDD<List<Object>> dataM1, final List<Integer> colsId1,
			final TalendRDD<List<Object>> dataM2, final List<Integer> colsId2)
			throws Exception {
		return Join.cogroup(KeyBy.run(dataM1, colsId1),
				KeyBy.run(dataM2, colsId2));
	}
}
