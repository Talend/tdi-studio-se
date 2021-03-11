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

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.talend.spark.function.LoadFunction;

/**
 * Company : Altic - LIPN User: Tugdual Sarazin Date: 18/04/14 Time: 16:15
 */
public class Load<T> {
	public static JavaRDD<List<Object>> run(JavaSparkContext ctx,
			String filePath, final String regex) throws Exception {
		JavaRDD<String> rawData = ctx.textFile(filePath);
		return rawData.map(new LoadFunction(regex));
	}

	public static JavaDStream<List<Object>> run(JavaStreamingContext ctx,
			String filePath, final String regex) throws Exception {
		JavaDStream<String> rawData = ctx.textFileStream((filePath));
		return rawData.map(new LoadFunction(regex));
	}
}
