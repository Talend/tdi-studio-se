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

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.process.Problem.ProblemType;
import org.talend.core.model.process.TalendProblem;

/**
 * DOC bqian class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ProblemList {

    private List<Problem> problemList = new ArrayList<Problem>();

    /**
     * Getter for problemList.
     *
     * @return the problemList
     */
    public List<Problem> getProblemList() {
        return this.problemList;
    }

    public int[] getMarkerCounts() {
        int errors = 0;
        int warnings = 0;
        int infos = 0;
        for (Problem problem : problemList) {
            if (problem.getStatus().equals(ProblemStatus.ERROR)) {
                errors++;
            } else if (problem.getStatus().equals(ProblemStatus.WARNING)) {
                warnings++;
            } else if (problem.getStatus().equals(ProblemStatus.INFO)) {
                infos++;
            }
        }
        return new int[] { errors, warnings, infos };
    }

    /**
     * DOC bqian Comment method "getProblemsBySeverity".
     *
     * @param status
     */
    public List<Problem> getProblemsBySeverity(ProblemStatus status) {
        List<Problem> list = new ArrayList<Problem>();

        for (Problem problem : problemList) {
            if (problem.getStatus().equals(status)) {
                list.add(problem);
            }
        }
        return list;
    }

    public List<Problem> getProblemsByType(ProblemType status) {
        List<Problem> list = new ArrayList<Problem>();
        // Check the problem is belong to job or routine
        if (status.equals(ProblemType.JOB)) {

            for (Problem problem : problemList) {
                if (problem.getJobInfo() != null) {
                    list.add(problem);
                }
            }
        } else if (status.equals(ProblemType.ROUTINE)) {
            for (Problem problem : problemList) {
                if (problem instanceof TalendProblem) {
                    TalendProblem routineProblem = (TalendProblem) problem;
                    if (routineProblem.getMarker() != null) {
                        list.add(problem);
                    }
                }
            }

        }

        return list;
    }

    public void clear() {
        problemList.clear();
    }

    /**
     * DOC bqian Comment method "add".
     *
     * @param problem
     */
    public void add(Problem problem) {
        if (!problemList.contains(problem)) {
            problemList.add(problem);
        }

    }
}
