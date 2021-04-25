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

import org.eclipse.swt.widgets.Composite;
import org.talend.designer.runprocess.ui.JobJvmComposite;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public interface IProcessViewHelper {

    /**
     * DOC amaumont Comment method "getProcessComposite".
     *
     * @param container
     * @return
     */
    public JobJvmComposite getProcessComposite(final Composite container);

    /*
     * public JobTargetExecutionComposite getTargetComposite(final Composite container);
     *
     * public JvmArgumentsComposite getJvmArgumentsComposite(final Composite container);
     */

}
