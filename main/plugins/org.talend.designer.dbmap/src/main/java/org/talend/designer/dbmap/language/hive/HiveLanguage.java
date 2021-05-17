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
package org.talend.designer.dbmap.language.hive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.talend.designer.dbmap.language.AbstractDbLanguage;
import org.talend.designer.dbmap.language.IJoinType;

/**
 * created by wchen on 2012-8-14 Detailled comment
 *
 */
public class HiveLanguage extends AbstractDbLanguage {

    /**
     * DOC wchen HiveLanguage constructor comment.
     *
     * @param operatorsManager
     */
    public HiveLanguage() {
        super(new HiveOperatorsManager());
    }

    @Override
    public IJoinType[] getAvailableJoins() {
        List<IJoinType> joins = new ArrayList<IJoinType>();
        joins.addAll(Arrays.asList(super.getAvailableJoins()));
        joins.addAll(Arrays.asList(HIVEJOIN.values()));
        int superJoin = joins.indexOf(JOIN.INNER_JOIN);
        int currentJoin = joins.indexOf(HIVEJOIN.JOIN);
        Collections.swap(joins, superJoin, currentJoin);
        joins.remove(JOIN.INNER_JOIN);
        joins.remove(JOIN.CROSS_JOIN);
        joins.remove(JOIN.NO_JOIN);
        return joins.toArray(new IJoinType[0]);
    }

    @Override
    public IJoinType getJoin(String joinType) {
        IJoinType superJoin;
        try {
            superJoin = super.getJoin(joinType);
        } catch (java.lang.IllegalArgumentException e) {
            return HIVEJOIN.getJoin(joinType);
        }
        return superJoin;

    }

    /**
     *
     * Hive joins.
     *
     * $Id$
     *
     */
    public static enum HIVEJOIN implements IJoinType {
        LEFT_SEMI_JOIN("LEFT SEMI JOIN"), //$NON-NLS-1$
        JOIN("JOIN"); //$NON-NLS-1$

        String label;

        HIVEJOIN(String label) {
            this.label = label;
        }

        /**
         * Getter for label.
         *
         * @return the label
         */
        @Override
        public String getLabel() {
            return this.label;
        }

        public static IJoinType getJoin(String joinType) {
            return valueOf(joinType);
        }

    };

}
