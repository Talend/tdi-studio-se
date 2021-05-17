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
package org.talend.designer.xmlmap.generation;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.utils.data.text.StringHelper;
import org.talend.core.model.process.BlockCode;

/**
 * DOC rdubois class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40Z rdubois $
 *
 */
public class GenerationManager {
	private List<BlockCode> clocksCodeToClose;

    private static final String PREFIX_VARIABLE_NAME = ""; //$NON-NLS-1$

    private static final String PREFIX_FIELD_NAME = "."; //$NON-NLS-1$

    private static final String SUFFIX_FIELD_NAME = ""; //$NON-NLS-1$

    private static final String PREFIX_TABLE_NAME = PREFIX_VARIABLE_NAME;

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    private static final String TEMPLATE_GENERATED_CODE_TABLE_COLUMN_VARIABLE = PREFIX_TABLE_NAME
            + "{1}" + PREFIX_FIELD_NAME + "{2}" //$NON-NLS-1$ //$NON-NLS-2$
            + SUFFIX_FIELD_NAME;

	public GenerationManager() {

	}

	public List<BlockCode> getBlocksCodeToClose() {
		return clocksCodeToClose;
	}

	/**
	 * DOC rdubois Comment method "getBlocksCodeToClose".
	 */
	public List<BlockCode> setBlocksCodeToClose(BlockCode blockCode) {
		return clocksCodeToClose;
	}

	/**
	 * DOC rdubois Comment method "getBlocksCodeToClose".
	 */
	public void addBlocksCodeToClose(BlockCode blockCode) {
		if (clocksCodeToClose == null) {
			clocksCodeToClose = new ArrayList<BlockCode>();
		}
		clocksCodeToClose.add(0, blockCode);
	}

	public String getGeneratedCodeTableColumnVariable(
			String uniqueNameComponent, String tableName, String columnName,
			boolean prefixTableNameWithComponentName) {
		String template = null;
		if (prefixTableNameWithComponentName) {
			template = getTemplateGeneratedCodeTableColumnVariableWithComponentNamePrefix();
		} else {
			template = getTemplateGeneratedCodeTableColumnVariable();
		}
		return StringHelper.replacePrms(template, new Object[] {
				uniqueNameComponent, tableName, columnName });
	}

	public String getTemplateGeneratedCodeTableColumnVariableWithComponentNamePrefix() {
		return TEMPLATE_GENERATED_CODE_TABLE_COLUMN_VARIABLE;
	}

	public String getTemplateGeneratedCodeTableColumnVariable() {
		return TEMPLATE_GENERATED_CODE_TABLE_COLUMN_VARIABLE;
	}

}
