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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapred.TableOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.spark.api.java.function.PairFunction;
import org.talend.spark.TalendPairRDD;
import org.talend.spark.TalendRDD;

import scala.Tuple2;

@SuppressWarnings("deprecation")
public class HBaseStore<T> {

	public static void run(String zookeeperHost, String zookeeperPort,
			String table, final String columns, Map<String, String> properties, TalendRDD<List<Object>> rdd,
			final List<Integer> keyList) throws IOException {

		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", zookeeperHost);
		conf.set("hbase.zookeeper.property.clientPort", zookeeperPort);
		conf.set("hbase.mapred.tablecolumns", columns);

		for(Entry<String, String> e:properties.entrySet()) {
			conf.set(e.getKey(), e.getValue());
		}

		TalendPairRDD<ImmutableBytesWritable, Put> hbaseRdd = rdd
				.mapToPair(new PairFunction<List<Object>, ImmutableBytesWritable, Put>() {
					private static final long serialVersionUID = 1L;

					public Tuple2<ImmutableBytesWritable, Put> call(
							List<Object> t) throws Exception {

						String key = "";
						for (int i : keyList) {
							key = key + t.get(i);
						}
						org.apache.hadoop.hbase.client.Put put = new org.apache.hadoop.hbase.client.Put(
								DigestUtils.md5("".equals(key)?t.toString():key));
						String[] cols = columns.split(" ");
						int i = 0;
						for (Object o : t) {
							if (cols.length > i) {
								put.add(org.apache.hadoop.hbase.util.Bytes
										.toBytes(cols[i].split(":")[0]),
										org.apache.hadoop.hbase.util.Bytes
												.toBytes(cols[i].split(":")[1]),
										(o != null ? org.apache.hadoop.hbase.util.Bytes
												.toBytes(o.toString()) : null));
							}
							i++;
						}

						return new Tuple2<ImmutableBytesWritable, Put>(
								new ImmutableBytesWritable(), put);
					}
				});

		JobConf config = new JobConf(conf);
		config.set(TableOutputFormat.OUTPUT_TABLE, table);
		config.setOutputFormat(TableOutputFormat.class);

		hbaseRdd.saveAsHadoopDataset(config);
	}
}
