// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package shadow;

import junit.framework.TestCase;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.Project;

/**
 * DOC chuger class global comment. Detailled comment <br/>
 * 
 * $Id: ShadowProcessTest.java 38013 2010-03-05 14:21:59Z mhirt $
 * 
 */
public class ShadowProcessTest extends TestCase {

    private static final int FIELDS_COUNT = 3;

    private static final int ROW_COUNT = 5;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        Project project = new Project("ShadowProcessTest"); //$NON-NLS-1$
        project.setLanguage(ECodeLanguage.PERL);

        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        repositoryContext.setProject(project);
        super.setUp();
    }

    /**
     * Test method for {@link org.talend.designer.runprocess.shadow.ShadowProcess#run()}.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void testRun() {

        System.out.print("Waiting for JET to initialize."); //$NON-NLS-1$
        // CodeGeneratorPlugin.getDefault().initialize(ECodeLanguage.PERL);
        // synchronized(this) {
        // try {
        // for (int i = 0; i < 36; i++) {
        // wait(1000);
        // System.out.print(".");
        // }
        // }
        // catch (InterruptedException ie) {
        // // Do nohting
        // }
        // }
        System.out.println("\nJet is normally up."); //$NON-NLS-1$

        try {
            // ProcessDescription desc = new ProcessDescription();
            // desc.setFilepath("D:\\tlData\\data1.csv");
            // desc.setRowSeparator("\"\\n\"");
            // desc.setFieldSeparator("';'");
            //
            // ShadowProcess shadowProcess = new ShadowProcess(desc, EShadowProcessType.FILE_DELIMITED);
            // XmlArray array = shadowProcess.run();
            // assertNotNull("No XML generated.", array);
            // assertEquals("Wrong row count.", array.getRows().size(), ROW_COUNT);
            // assertEquals("Wrong col count.", array.getRows().get(0).getFields().size(), FIELDS_COUNT);
        } catch (Exception pe) {
            System.out.println("Be carefull of preferences : PERL interpreter."); //$NON-NLS-1$

            // pe.printStackTrace();
            ExceptionHandler.process(pe);
            fail(pe.getMessage());
        }
    }

}
