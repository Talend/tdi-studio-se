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
package org.talend.designer.dbmap.language.oracle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.talend.designer.dbmap.language.AbstractDbLanguage;
import org.talend.designer.dbmap.language.IJoinType;
import org.talend.designer.dbmap.language.operator.GenericDbOperatorsManager;

/**
 *
 * wzhang class global comment. Detailled comment
 */
public class OracleLanguage extends AbstractDbLanguage {

    /**
     * DOC amaumont PerlLanguage constructor comment.
     */
    public OracleLanguage() {
        super(new GenericDbOperatorsManager());
    }

    /**
     *
     * Oracle joins.
     *
     * $Id$
     *
     */
    public static enum ORACLEJOIN implements IJoinType {
        // the LEFT_OUTER_JOIN_ORACLE and RIGHT_OUTER_JOIN_ORACLE don't support explicit join
        LEFT_OUTER_JOIN_ORACLE("LEFT OUTER JOIN (+)"), //$NON-NLS-1$
        RIGHT_OUTER_JOIN_ORACLE("RIGHT OUTER JOIN (+)"); //$NON-NLS-1$

        String label;

        ORACLEJOIN(String label) {
            this.label = label;
        }

        /**
         * Getter for label.
         *
         * @return the label
         */
        public String getLabel() {
            return this.label;
        }

        public static IJoinType getJoin(String joinType) {
            return valueOf(joinType);
        }

    };

    @Override
    public IJoinType[] getAvailableJoins() {
        List<IJoinType> joins = new ArrayList<IJoinType>();
        joins.addAll(Arrays.asList(super.getAvailableJoins()));
        joins.addAll(Arrays.asList(ORACLEJOIN.values()));
        return (IJoinType[]) joins.toArray(new IJoinType[0]);
    }

    @Override
    public IJoinType getJoin(String joinType) {
        IJoinType superJoin;
        try {
            // if JOIN doesn'e contain the join type, that throw exception.
            superJoin = super.getJoin(joinType);
        } catch (java.lang.IllegalArgumentException e) {
            return ORACLEJOIN.getJoin(joinType);
        }
        return superJoin;

    }

    @Override
    public List<IJoinType> unuseWithExplicitJoin() {
        List<IJoinType> joins = super.unuseWithExplicitJoin();
        joins.add(ORACLEJOIN.LEFT_OUTER_JOIN_ORACLE);
        joins.add(ORACLEJOIN.RIGHT_OUTER_JOIN_ORACLE);
        return joins;
    }
}
