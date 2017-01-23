package org.talend.designer.runprocess;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.process.BasicJobInfo;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.process.Problem.ProblemType;
import org.talend.core.model.process.TalendProblem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.runprocess.ErrorDetailTreeBuilder.JobErrorEntry;

public class ErrorDetailTreeBuilderTest {

    private List<Problem> errors;

    private Map<String, Boolean> jobIds;

    @Before
    public void setUp() throws Exception {
        errors = new ArrayList<Problem>();
        
        Item item = PropertiesFactory.eINSTANCE.createProcessItem();
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setLabel("job_1");
        item.setProperty(property);
        
        TalendProblem compileProblem = new TalendProblem(ProblemStatus.ERROR, item, null, "", 0, "aa", 1, 2, ProblemType.JOB);
        BasicJobInfo info = new BasicJobInfo("id_1", "Default", "0.1");
        info.setJobName("job_1");
        compileProblem.setJobInfo(info);
        errors.add(compileProblem);
        
        item = PropertiesFactory.eINSTANCE.createProcessItem();
        property = PropertiesFactory.eINSTANCE.createProperty();
        property.setLabel("job_2");
        item.setProperty(property);
        compileProblem = new TalendProblem(ProblemStatus.ERROR, item, null, "", 0, "aa", 1, 2, ProblemType.JOB);
        info = new BasicJobInfo("id_2", "Default", "0.1");
        info.setJobName("job_2");
        compileProblem.setJobInfo(info);
        errors.add(compileProblem);
        
        Problem jobProblem = new Problem();
        info = new BasicJobInfo("id_3", "Default", "0.1");
        info.setJobName("job_3");
        jobProblem.setJobInfo(info);
        errors.add(jobProblem);
        
        jobProblem = new Problem();
        info = new BasicJobInfo("id_4", "Default", "0.1");
        info.setJobName("job_4");
        jobProblem.setJobInfo(info);
        errors.add(jobProblem);
        
        jobIds = new HashMap<String, Boolean>();
        jobIds.put("id_1", true);
        jobIds.put("id_2", false);
        jobIds.put("id_3", true);
        jobIds.put("id_4", false);
    }

    @Test
    public void testCreateTreeInput() {
        ErrorDetailTreeBuilder builder = new ErrorDetailTreeBuilder();
        List<JobErrorEntry> result = builder.createTreeInput(errors, jobIds);
        assertTrue(result.size() == 3);
        for (JobErrorEntry entry : result) {
            assertFalse(entry.getLabel().equals("id_1"));
        }
    }

}
