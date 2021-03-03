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
package org.talend.designer.oas.external;

import java.nio.file.Path;
import java.util.Map;

import org.eclipse.core.runtime.MultiStatus;

/**
 * DOC dsergent class global comment. Detailled comment
 */
public interface IOASDecoder {

    public void setOASSourceFile(Path path);

    public ETranslationStatus getTranslationStatus() throws TranslationException;

    public MultiStatus getProblems();

    public String getEndpoint();

    public String getName();

    public String getDocumentationComment();

    public Map<String, RestAPIMapping> getMappings();

    public byte[] getAsOASSwagger20();

}
