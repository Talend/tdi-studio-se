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
package org.talend.parquet.data.simple;

import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.RecordConsumer;

public class Int96Value extends Primitive {

	private final Binary value;

	public Int96Value(Binary value) {
		this.value = value;
	}

	@Override
	public Binary getInt96() {
		return value;
	}

	@Override
	public void writeValue(RecordConsumer recordConsumer) {
		recordConsumer.addBinary(value);
	}

	@Override
	public String toString() {
		return "Int96Value{" + String.valueOf(value) + "}";
	}
}
