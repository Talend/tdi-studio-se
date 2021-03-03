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
package org.talend.spark.utils;

import java.io.Serializable;

/**
 * Company : Altic - LIPN
 * User: Tugdual Sarazin
 * Date: 23/04/14
 * Time: 17:49
 */
public class FilterObject implements Serializable {
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public enum Operator {
        EQUAL, NEQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, STARTS_WITH, ENDS_WITH, CONTAINS, MATCHES
    }

    public enum LogicOp {
        AND, OR
    }

    public int idCol;
    public Operator op;
    public Object value;
    public LogicOp logicOp;
    public Class<?> javaType;
    public boolean isNullable;


    public FilterObject(int idCol, Operator op, Object value, String javaType, boolean isNullable) throws ClassNotFoundException{
        this(idCol, op, value, LogicOp.AND, javaType, isNullable);
    }

    public FilterObject(int idCol, Operator op, Object value, LogicOp logicOp, String javaType, boolean isNullable) throws ClassNotFoundException{
        this.idCol = idCol;
        this.op = op;
        this.value = value;
        this.logicOp = logicOp;
		this.javaType = Class.forName("java.lang." + javaType);
		this.isNullable = isNullable;
    }
}

