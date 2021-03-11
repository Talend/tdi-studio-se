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
package org.talend.designer.scd.model;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class SurrogateKey {

    private String column;

    private SurrogateCreationType creation;

    private String complement;

    public SurrogateKey() {
        column = ""; //$NON-NLS-1$
        complement = ""; //$NON-NLS-1$
        creation = SurrogateCreationType.AUTO_INCREMENT;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public SurrogateCreationType getCreation() {
        return creation;
    }

    public void setCreation(SurrogateCreationType creation) {
        this.creation = creation;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement == null ? "" : complement; //$NON-NLS-1$
    }

}
