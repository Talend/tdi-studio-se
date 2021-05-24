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
package org.talend.designer.gefabstractmap.part.directedit;

/**
 * DOC talend class global comment. Detailled comment
 */
public enum DirectEditType {

    EXPRESSION(1),
    VAR_NODE_TYPE(2),
    EXPRESSION_FILTER(3),
    LOOKUP_MODEL(4),
    MATCH_MODEL(5),
    JOIN_MODEL(6),
    PERSISTENT_MODEL(7),
    OUTPUT_REJECT(8),
    LOOK_UP_INNER_JOIN_REJECT(9),
    NODE_NAME(10),
    ALL_IN_ONE(11),
    ENABLE_EMPTY_ELEMENT(12),
    JOIN_OPTIMIZATION(13),
    CUSTOM_PARTITIONER(14),
    INCREASE_PARALLELISM(15),
    SERACH(16);

    private int id;

    private DirectEditType(int id) {
        this.id = id;
    }
}
