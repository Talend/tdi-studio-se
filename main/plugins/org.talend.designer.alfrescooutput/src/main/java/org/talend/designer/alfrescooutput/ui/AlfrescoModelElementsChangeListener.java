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
package org.talend.designer.alfrescooutput.ui;

import org.dom4j.Element;


/**
 * Interface for listening to changes within Alfresco models.
 *
 * @author Marc Dutoo - Open Wide SA
 *
 * $Id: AlfrescoModelElementsChangeListener.java,v 1.1 2008/10/07 21:27:31 mdutoo Exp $
 */
public interface AlfrescoModelElementsChangeListener {

	public void alfrescoModelElementAdded(Element alfrescoModelElement);
	public void alfrescoModelElementRemoved(Element alfrescoModelElement);

}
