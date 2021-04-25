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
package org.talend.designer.runprocess;

import java.util.Set;

import org.junit.Test;
import org.talend.core.model.process.JobInfo;
import org.talend.core.runtime.process.LastGenerationInfo;

public class JobErrorsCheckerTest {

    @Test(expected = ProcessorException.class)
    public void testCheckSubJobMultipleVersionsError_HasError() throws ProcessorException {
        Set<JobInfo> jobInfos = LastGenerationInfo.getInstance().getLastGeneratedjobs();
        jobInfos.clear();
        jobInfos.add(creatJobInfo("father", "aaa", "0.1"));
        jobInfos.add(creatJobInfo("subjob1", "bbb", "0.1"));
        jobInfos.add(creatJobInfo("subjob1", "bbb", "0.2"));
        JobErrorsChecker.checkSubJobMultipleVersionsError();

    }

    @Test
    public void testCheckSubJobMultipleVersionsError_NoError() throws ProcessorException {
        Set<JobInfo> jobInfos = LastGenerationInfo.getInstance().getLastGeneratedjobs();
        jobInfos.clear();
        jobInfos.add(creatJobInfo("father", "aaa", "0.1"));
        jobInfos.add(creatJobInfo("subjob1", "bbb", "0.1"));
        jobInfos.add(creatJobInfo("subjob2", "ccc", "0.2"));
        JobErrorsChecker.checkSubJobMultipleVersionsError();

        jobInfos.clear();
        jobInfos.add(creatJobInfo("father", "aaa", "0.1"));
        jobInfos.add(creatJobInfo("subjob1", "bbb", "0.1"));
        jobInfos.add(creatJobInfo("subjob1", "bbb", "0.1"));
        JobErrorsChecker.checkSubJobMultipleVersionsError();

    }

    private JobInfo creatJobInfo(String jobId, String jobName, String jobVersion) {
        JobInfo jobInfo = new JobInfo(jobId, "", jobVersion);
        jobInfo.setJobName(jobName);
        return jobInfo;
    }

}
