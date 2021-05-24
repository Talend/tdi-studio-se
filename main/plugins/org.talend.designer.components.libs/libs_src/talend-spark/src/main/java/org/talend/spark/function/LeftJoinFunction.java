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
package org.talend.spark.function;

import java.util.ArrayList;
import java.util.List;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import com.google.common.base.Optional;

public class LeftJoinFunction
		implements
		PairFunction<Tuple2<List<Object>, Tuple2<List<Object>, Optional<List<Object>>>>, List<Object>, List<Object>> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public Tuple2<List<Object>, List<Object>> call(
			Tuple2<List<Object>, Tuple2<List<Object>, Optional<List<Object>>>> s) {
		// left and right elements
		if (s._2()._2().isPresent()) {
			List<Object> combined = new ArrayList<Object>();
			combined.addAll(s._2()._1());
			combined.addAll(s._2()._2().get());
			return new Tuple2<List<Object>, List<Object>>(s._1(), combined);
		}
		// Only left elements
		return new Tuple2<List<Object>, List<Object>>(s._1(), s._2()._1());
	}
}
