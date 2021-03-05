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
package org.talend.xml.sax.commons;

import java.util.Iterator;
import java.util.Map;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public interface ISAXLooper {

    public void parse(String fileName, String charset);

    public void parse(java.io.InputStream is, String charset);

    public Iterator<Map<String, String>> iterator();

    public Iterator<Map<String, Map<String, String>>> multiIterator();

    public void setIgnoreDTD(boolean ignoreDTD);
}
