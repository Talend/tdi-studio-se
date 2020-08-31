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
package org.talend.designer.runprocess.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * created by hwang on Dec 27, 2017 Detailled comment
 *
 */
public class JobVMArgumentsUtilTest {

    /**
     * Test method for
     * {@link org.talend.designer.runprocess.ui.JobVMArgumentsComposite#readString(String stringList)}
     * .
     */
    @Test
    public void testReadString() {
        JobVMArgumentsUtil jobVM = new JobVMArgumentsUtil();
        String vm = null;
        Assert.assertTrue(jobVM.readString(vm).isEmpty());
        vm = "";
        Assert.assertTrue(jobVM.readString(vm).isEmpty());
        vm = "hhh jjjj kkkk";
        List<String> vmargs = jobVM.readString(vm);

        Assert.assertEquals(jobVM.DEFAULT_JVM_ARGS.length, vmargs.size());

        for (String vmarg : jobVM.DEFAULT_JVM_ARGS) {
            Assert.assertTrue(vmargs.contains(vmarg));
        }

        String value = "{\"JOB_RUN_VM_ARGUMENTS\":[\"{[<,>]};:' \\\"/\\\\+\\\\t+\\\\b+\\\\f+\\\\n=\\\\r|\",\"String a = \\\\\\\\ + [array];\"]}";
        List<String> list = jobVM.readString(value);
        Assert.assertTrue(list.get(0).equals("{[<,>]};:' \"/\\+\\t+\\b+\\f+\\n=\\r|"));
        Assert.assertTrue(list.get(1).equals("String a = \\\\ + [array];"));
    }

    /**
     * Test method for
     * {@link org.talend.designer.runprocess.ui.JobVMArgumentsComposite#writeString(List<String> items)}
     * .
     */
    @Test
    public void testWriteString() {
        JobVMArgumentsUtil jobVM = new JobVMArgumentsUtil();
        List<String> list = new ArrayList<String>();
        String srg0 = "{[<,>]};:' \"/\\+\t+\b+\f+\n=\r|";
        list.add(srg0);

        String srg2 = "String a = \\\\ + [array];";
        list.add(srg2);

        String result = jobVM.writeString(list);
        String expectResult = "{\"JOB_RUN_VM_ARGUMENTS\":[\"{[<,>]};:' \\\"/\\\\+\\t+\\b+\\f+\\n=\\r|\",\"String a = \\\\\\\\ + [array];\"]}";
        Assert.assertTrue(result.equals(expectResult));

    }
}
