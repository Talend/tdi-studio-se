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
package org.talend.designer.core.ui.views.problems;

import org.eclipse.osgi.util.NLS;
import org.talend.core.model.process.Problem;
import org.talend.designer.core.i18n.Messages;

/**
 * DOC bqian class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ProblemCategory extends Problem {

    ProblemList problems;

    public ProblemCategory(ProblemList problems) {
        this.problems = problems;
    }

    /**
     * Getter for problems.
     *
     * @return the problems
     */
    public ProblemList getProblems() {
        return this.problems;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.Problem#isConcrete()
     */
    @Override
    public boolean isConcrete() {
        return false;
    }

    public String name;

    /**
     * Getter for name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.Problem#getProblemName()
     */
    @Override
    public String getProblemResource() {
        return Problem.EMPTY_STRING;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.Problem#getDescription()
     */
    @Override
    public String getDescription() {
        int size = getChildren().length;
        if (size == 1) {
            // return NLS.bind(MarkerMessages.Category_One_Item_Label, new Object[] { name });
            return NLS.bind(Messages.getString("MarkerMessages.Category_One_Item_Label"), new Object[] { name }); //$NON-NLS-1$
        }
        // return NLS.bind(MarkerMessages.Category_Label, new Object[] { name, String.valueOf(size) });
        return NLS.bind(Messages.getString("MarkerMessages.Category_Label"), new Object[] { name, String.valueOf(size) }); //$NON-NLS-1$
    }
}
