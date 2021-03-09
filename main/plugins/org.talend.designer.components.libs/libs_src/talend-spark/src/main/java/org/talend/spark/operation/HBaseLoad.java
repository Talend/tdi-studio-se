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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import scala.Tuple2;

public class HBaseLoad<T> {

	@SuppressWarnings("deprecation")
	public static JavaRDD<List<Object>> hbaseRDD(JavaSparkContext ctx,
			String zookeeperHost, String zookeeperPort, String table,
			final String columns, Map<String, String> properties) {

		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", zookeeperHost);
		conf.set("hbase.zookeeper.property.clientPort", zookeeperPort);
		conf.set("mapred.input.dir", table);
		conf.set("hbase.mapred.tablecolumns", columns);

		for(Entry<String, String> e:properties.entrySet()) {
			conf.set(e.getKey(), e.getValue());
		}

		JavaPairRDD<ImmutableBytesWritable, Result> hbaseRDD = ctx.hadoopRDD(
				new org.apache.hadoop.mapred.JobConf(conf),
				org.apache.hadoop.hbase.mapred.TableInputFormat.class,
				org.apache.hadoop.hbase.io.ImmutableBytesWritable.class,
				org.apache.hadoop.hbase.client.Result.class);

		JavaRDD<List<Object>> rdd = hbaseRDD
				.map(new Function<Tuple2<ImmutableBytesWritable, Result>, List<Object>>() {

					private static final long serialVersionUID = 1L;

					public List<Object> call(
							Tuple2<ImmutableBytesWritable, Result> in)
							throws Exception {
						Result res = in._2;
						List<Object> ra = new ArrayList<Object>();

						for (String s : columns.split(" ")) {
							byte[] rowResult = res.getValue(
									org.apache.hadoop.hbase.util.Bytes
											.toBytes(s.split(":")[0]),
									org.apache.hadoop.hbase.util.Bytes
											.toBytes(s.split(":")[1]));
							ra.add(org.apache.hadoop.hbase.util.Bytes
									.toString(rowResult));
						}

						return ra;
					}
				});

		return rdd;
	}
}
