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

package org.talend.designer.components.tsort.io;

import java.io.IOException;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public interface IMapHashFile<V> {

	public V get(String container, long cursorPosition, int hashcode)
			throws Exception;

	public long put(String container, V bean) throws IOException;

	public void initPut(String container) throws IOException;

	public void endPut() throws IOException;

	public void initGet(String container) throws IOException;

	public void endGet(String container) throws IOException;

	public long getTotalSize();

}
