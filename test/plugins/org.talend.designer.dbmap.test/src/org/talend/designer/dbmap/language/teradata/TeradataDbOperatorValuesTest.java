// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.language.teradata;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.talend.designer.dbmap.language.operator.IDbOperator;

/**
 * @author hwang
 *
 */
public class TeradataDbOperatorValuesTest {

	@Test
	public void testGetDefaultOperator() {
		TeradataDbOperatorValues dbOpe = new TeradataDbOperatorValues();
		IDbOperator o = dbOpe.getDefaultOperator();
		assertEquals(o.getOperator(), "=");
	}
	
	@Test
	public void testinitOperators() {
		TeradataDbOperatorValues dbOpe = new TeradataDbOperatorValues();
		List<IDbOperator> operators = new ArrayList<IDbOperator>();
		dbOpe.initOperators(operators);
		for(IDbOperator o : operators) {
			if("DIFFERENT".equals(o.getName())) {
				assertEquals(o.getOperator(), "<>");
			}else if("STRICTLY_INFERIOR".equals(o.getName())) {
				assertEquals(o.getOperator(), "<");
			}else if("INFERIOR_OR_EQUAL".equals(o.getName())) {
				assertEquals(o.getOperator(), "<=");
			}else if("SUPERIOR".equals(o.getName())) {
				assertEquals(o.getOperator(), ">");
			}else if("SUPERIOR_OR_EQUAL".equals(o.getName())) {
				assertEquals(o.getOperator(), ">=");
			}else if("EQUAL".equals(o.getName())) {
				assertEquals(o.getOperator(), "=");
			}
		}
	}
}
