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
package org.talend.designer.runprocess.java;

import org.eclipse.core.runtime.IPath;
import org.talend.core.model.runprocess.IJavaProcessorStates;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: EditJavaStatus.java EditJavaStatus 2007-1-23 下午05:17:43 +0000 (下午05:17:43, 2007-1-23 2007) yzhang $
 *
 */
public class JavaProcessorEditStates implements IJavaProcessorStates {

    private JavaProcessor javaProcessor;

    /**
     * DOC yzhang EditJavaStatus constructor comment.
     */

    public JavaProcessorEditStates(JavaProcessor javaProcessor) {

        this.javaProcessor = javaProcessor;
        this.javaProcessor.setStatus(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.java.IJavaStatus#getCodePath()
     */
    @Override
    public IPath getCodePath() {
        return this.javaProcessor.getSrcCodePath();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.java.IJavaStatus#getContextPath()
     */
    @Override
    public IPath getContextPath() {
        return this.javaProcessor.getSrcContextPath();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.java.IJavaStatus#getDataSetPath()
     */
    public IPath getDataSetPath() {
        return this.javaProcessor.getSrcDataSetPath();
    }

}
