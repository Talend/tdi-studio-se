/*
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.parquet.hadoop;

import static org.apache.parquet.schema.MessageTypeParser.parseMessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.io.api.RecordConsumer;
import org.apache.parquet.schema.MessageType;
import org.talend.parquet.data.Group;
import org.talend.parquet.data.GroupWriter;

public class TalendGroupWriteSupport extends WriteSupport<Group> {

	public static final String PARQUET_SCHEMA = "parquet.talend.schema";

	public static void setSchema(MessageType schema, Configuration configuration) {
		configuration.set(PARQUET_SCHEMA, schema.toString());
	}

	public static MessageType getSchema(Configuration configuration) {
		return parseMessageType(Objects.requireNonNull(configuration.get(PARQUET_SCHEMA), PARQUET_SCHEMA));
	}

	private MessageType schema;
	private GroupWriter groupWriter;
	private Map<String, String> extraMetaData;

	public TalendGroupWriteSupport() {
		this(null, new HashMap<String, String>());
	}

	TalendGroupWriteSupport(MessageType schema) {
		this(schema, new HashMap<String, String>());
	}

	TalendGroupWriteSupport(MessageType schema, Map<String, String> extraMetaData) {
		this.schema = schema;
		this.extraMetaData = extraMetaData;
	}

	@Override
	public String getName() {
		return "Talend";
	}

	@Override
	public org.apache.parquet.hadoop.api.WriteSupport.WriteContext init(Configuration configuration) {
		// if present, prefer the schema passed to the constructor
		if (schema == null) {
			schema = getSchema(configuration);
		}
		return new WriteContext(schema, this.extraMetaData);
	}

	@Override
	public void prepareForWrite(RecordConsumer recordConsumer) {
		groupWriter = new GroupWriter(recordConsumer, schema);
	}

	@Override
	public void write(Group record) {
		groupWriter.write(record);
	}

}
