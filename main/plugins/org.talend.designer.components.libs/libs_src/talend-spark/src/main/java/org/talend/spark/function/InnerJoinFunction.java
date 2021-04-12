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

public class InnerJoinFunction implements PairFunction<Tuple2<List<Object>, Tuple2<List<Object>, List<Object>>>, List<Object>, List<Object>> {
        /**
	 *
	 */
	private static final long serialVersionUID = 1L;

        public Tuple2<List<Object>, List<Object>> call(Tuple2<List<Object>, Tuple2<List<Object>, List<Object>>> s) throws Exception {

            List<Object> combined = new ArrayList<Object>();
            combined.addAll(s._2()._1());
            combined.addAll(s._2()._2());
            return new Tuple2<List<Object>, List<Object>>(s._1(), combined);
        }
    }
