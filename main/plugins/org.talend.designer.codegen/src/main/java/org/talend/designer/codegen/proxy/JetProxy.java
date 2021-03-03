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
package org.talend.designer.codegen.proxy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.codegen.jet.JETEmitter;
import org.eclipse.emf.codegen.jet.JETException;
import org.talend.designer.codegen.config.JetBean;
import org.talend.designer.codegen.model.CodeGeneratorEmittersPoolFactory;

/**
 * This class allows the Code Generation with for a fully initiated JetBean.
 *
 * $Id$
 *
 */
public class JetProxy {

    private JetBean jetBean = null;

    private IProgressMonitor monitor = null;

    public JetProxy(JetBean jetBean) {
        this.jetBean = jetBean;
        monitor = new NullProgressMonitor();
    }

    /**
     * Get the initalized Emitter for this Bean and use it to generate code.
     *
     * @param monitor
     * @return
     * @throws CoreException
     * @throws JETException
     */
    public String generate() throws CoreException, JETException {
        JETEmitter emitter = CodeGeneratorEmittersPoolFactory.getJETEmitter(jetBean);
        IProgressMonitor sub = new SubProgressMonitor(monitor, 1);
        String result = ""; //$NON-NLS-1$
        if (emitter != null) {
            result = emitter.generate(sub, new Object[] { jetBean });
            sub.worked(1);
        }
        return result;
    }
}
