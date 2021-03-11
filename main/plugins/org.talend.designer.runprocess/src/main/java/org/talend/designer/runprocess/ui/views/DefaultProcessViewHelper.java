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
package org.talend.designer.runprocess.ui.views;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.talend.designer.runprocess.ui.JobJvmComposite;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public class DefaultProcessViewHelper implements IProcessViewHelper {

    private static Logger log = Logger.getLogger(ProcessView.class);

    /**
     * DOC amaumont Comment method "getProcessComposite".
     *
     * @param container
     * @return
     */
    public JobJvmComposite getProcessComposite(final Composite container) {
        return new JobJvmComposite(container, SWT.NONE);
    }

}
