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

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.talend.spark.TalendRDD;

import scala.Tuple2;

/**
 * Company : Altic - LIPN User: Tugdual Sarazin Date: 05/05/14 Time: 15:23
 */
public class Log {
	public static void run(final JavaRDD<List<Object>> dataM) throws Exception {
		for (List<Object> row : dataM.collect()) {
			System.out.println(row);
		}
	}

	public static void run(final JavaPairRDD<List<Object>, List<Object>> dataM)
			throws Exception {
		for (Tuple2<List<Object>, List<Object>> row : dataM.collect()) {
			System.out.println(row);
		}
	}

	public static void run(final JavaDStream<List<Object>> dataM)
			throws Exception {
		dataM.print();
	}

	public static void run(final TalendRDD<List<Object>> dataM)
			throws Exception {
		dataM.toConsole();
	}
}
