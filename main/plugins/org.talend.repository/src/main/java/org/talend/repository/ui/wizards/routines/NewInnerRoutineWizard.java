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
package org.talend.repository.ui.wizards.routines;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.designer.maven.utils.CodesJarMavenUtil;
import org.talend.repository.model.RepositoryNode;

public class NewInnerRoutineWizard extends NewRoutineWizard {

    private RepositoryNode node;

    public NewInnerRoutineWizard(RepositoryNode node, IPath path) {
        super(path);
        this.node = node;
        setUpCodePackage();
    }

    private void setUpCodePackage() {
        String gavPackage = StringUtils
                .replace(CodesJarMavenUtil.getGAVPackageForCodesJar(node.getObject().getProperty().getItem()), "/", ".");
        gavPackage = "package " + gavPackage + ";";
        ByteArray contents = routineItem.getContent();
        String newInnerContent = StringUtils.replaceOnce(new String(contents.getInnerContent()), "package routines;", gavPackage);
        contents.setInnerContent(newInnerContent.getBytes());
    }

    @Override
    protected void addAdditionalProperties(Property property) {
        RoutinesUtil.setInnerCodes(property, ERepositoryObjectType.ROUTINESJAR);
    }

}
