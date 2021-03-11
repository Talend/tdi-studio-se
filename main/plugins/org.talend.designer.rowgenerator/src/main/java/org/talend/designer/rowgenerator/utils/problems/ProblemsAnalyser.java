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
package org.talend.designer.rowgenerator.utils.problems;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.process.Problem;
import org.talend.designer.rowgenerator.external.data.ExternalRowGeneratorData;
import org.talend.designer.rowgenerator.managers.RowGeneratorManager;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: ProblemsAnalyser.java,v 1.5 2007/01/31 05:20:51 pub Exp $
 *
 */
public class ProblemsAnalyser {

    private RowGeneratorManager generatorManager;

    private List<Problem> problems = new ArrayList<Problem>();

    /**
     * qzhang ProblemsAnalyser constructor comment.
     */
    public ProblemsAnalyser(RowGeneratorManager generatorManager) {
        super();
        this.generatorManager = generatorManager;
    }

    public List<Problem> checkProblems(ExternalRowGeneratorData externalData) {
        problems.clear();
        return getProblems();

    }

    /**
     * qzhang Comment method "getProblems".
     *
     * @return
     */
    private List<Problem> getProblems() {
        // TODO Auto-generated method stub
        return new ArrayList<Problem>();
    }

    public RowGeneratorManager getGeneratorManager() {
        return this.generatorManager;
    }
}
