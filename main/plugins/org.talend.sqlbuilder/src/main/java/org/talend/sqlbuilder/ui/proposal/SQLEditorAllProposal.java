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
package org.talend.sqlbuilder.ui.proposal;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.swt.graphics.Image;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * dev class global comment. Detailled comment <br/>
 *
 * $Id: SQLEditorAllProposal.java,v 1.9 2006/11/06 08:15:37 qiang.zhang Exp $
 *
 */
public class SQLEditorAllProposal implements IContentProposal {

    private String content;

    private String label;

    private String description;

    private int position;

    private Image image;

    private String allString;

    private String dbType;

    /**
     * dev SQLEditorAllProposal constructor comment.
     *
     * @param hasString has input String
     * @param allString full String
     * @param position Cursor current positioon
     * @param contents text edited all content
     */
    public SQLEditorAllProposal(String hasString, String allString, int position, String[] contents, String dbType) {
        super();
        this.allString = allString;
        this.dbType = dbType;
        String tmpC = contents[0] + contents[1];

        if (!TextUtil.isDoubleQuotesNeededDbType(dbType)) { //$NON-NLS-1$
            hasString = initHasString(hasString);
            if (!label.contains(" ")) { //$NON-NLS-1$
                label = label.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            hasString = initHasStringForPostgres(hasString);
        }
        if (label.length() < hasString.length()) {
            label = ""; //$NON-NLS-1$
            content = ""; //$NON-NLS-1$
            description = ""; //$NON-NLS-1$
            this.position = position;
            return;
        }
        setImages();
        hasString = label.substring(0, hasString.length());

        label = label.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
        initContent(hasString, contents, dbType, position);
        description = allString;
    }

    /**
     * qzhang Comment method "initContent".
     *
     * @param hasString
     * @param contents
     * @param dbType
     * @param position2
     * @return
     */
    private String initContent(String hasString, String[] contents, String dbType, int position2) {
        String tmp = initLabel(dbType);
        String c1 = ""; //$NON-NLS-1$
        if (label.contains(" ") && !TextUtil.isDoubleQuotesNeededDbType(dbType)) { //$NON-NLS-1$
            tmp = TalendTextUtils.addQuotesWithSpaceField(tmp, dbType);
            c1 = contents[0].substring(0, contents[0].length() - hasString.length());
            // contents[0] += tmp.substring(0, tmp.indexOf(hasString) + hasString.length());
            content = c1;
            content += tmp; // .substring(hasString.length() - 1 > 0 ? (hasString.length() - 1) : 0);
            content += contents[1];
            this.position = position2 + tmp.substring(hasString.length()).length();
        } else {
            c1 = contents[0].substring(0, contents[0].length() - hasString.length()) + hasString;
            content = c1;
            content += tmp.substring(hasString.length());
            content += contents[1];
            this.position = position2 + tmp.substring(hasString.length()).length();
        }
        return tmp;
    }

    private void setImages() {
        if (this.allString.indexOf(".") != -1) { //$NON-NLS-1$
            if (this.allString.indexOf(".") == this.allString.lastIndexOf(".")) { //$NON-NLS-1$ //$NON-NLS-2$
                image = ImageUtil.getImage("Images.TableIcon"); //$NON-NLS-1$
            } else {
                image = ImageUtil.getImage("Images.ColumnIcon"); //$NON-NLS-1$
            }
        }
        if (allString.indexOf("alias: ") != -1) { //$NON-NLS-1$
            image = ImageUtil.getImage("Image.sqlAliasIcon"); //$NON-NLS-1$
            this.allString = this.allString.substring(0, this.allString.indexOf("\n")); //$NON-NLS-1$
        }

    }

    private String initHasStringForPostgres(String hasString) {
        label = allString;
        int index = label.indexOf("."); //$NON-NLS-1$
        int index2 = label.lastIndexOf("."); //$NON-NLS-1$
        String qualityName = ""; //$NON-NLS-1$
        if (index > -1) {
            qualityName = label.substring(index + 1, label.length());
            if (index == index2) {
                label = qualityName;
            } else {
                hasString = getColumnHasString(hasString, qualityName);
                label = label.substring(index2 + 1, label.length());
            }
        }
        return hasString;
    }

    private String getColumnHasString(String hasString, String qualityName) {
        int index3;

        String newQualityName = qualityName.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
        int hasX = hasString.lastIndexOf("\""); //$NON-NLS-1$

        if (hasX != -1) {
            String newQualityName2 = qualityName.replaceFirst("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
            newQualityName2 = newQualityName2.replaceFirst("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
            if (hasString.indexOf("\"") == newQualityName2.indexOf("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                index3 = newQualityName2.indexOf("."); //$NON-NLS-1$
            } else {
                index3 = qualityName.indexOf("."); //$NON-NLS-1$
            }
        } else {
            index3 = newQualityName.indexOf("."); //$NON-NLS-1$
        }

        String newHasString = hasString.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
        if (!"".equals(hasString) //$NON-NLS-1$
                && (qualityName.toLowerCase().startsWith(hasString.toLowerCase()) || newQualityName.toLowerCase().startsWith(
                        newHasString.toLowerCase()))) {
            if (hasString.indexOf(".") > -1) { //$NON-NLS-1$
                hasString = hasString.substring(index3 + 1);
            }
        }
        return hasString;
    }

    /**
     * dev Comment method "initLabel".
     *
     * @param dbType
     * @return
     */
    private String initLabel(String dbType) {
        String tmp = label;
        int index = allString.indexOf("."); //$NON-NLS-1$
        if (index > -1 && TextUtil.isDoubleQuotesNeededDbType(dbType)) { //$NON-NLS-1$
            tmp = "\"" + label + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            tmp = label;
        }
        return tmp;
    }

    /**
     * dev Comment method "initHasString".
     *
     * @param hasString
     * @param allString
     * @return
     */
    protected String initHasString(String hasString) {
        label = allString;
        int index = label.indexOf("."); //$NON-NLS-1$
        int index2 = label.lastIndexOf("."); //$NON-NLS-1$
        String qualityName = ""; //$NON-NLS-1$
        if (index > -1) {
            qualityName = label.substring(index + 1, label.length());
            if (index == index2) {
                label = qualityName;
            } else {
                int index3;
                String newHasString = hasString; //$NON-NLS-1$ //$NON-NLS-2$

                String newQualityName = qualityName; //$NON-NLS-1$ //$NON-NLS-2$
                if (!hasString.contains(" ")) { //$NON-NLS-1$
                    newQualityName = newQualityName.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
                }
                index3 = newQualityName.indexOf("."); //$NON-NLS-1$
                boolean b = qualityName.replaceAll("\"", "").toLowerCase().startsWith( //$NON-NLS-1$ //$NON-NLS-2$
                        TalendTextUtils.removeQuotesForField(hasString, dbType).toLowerCase());
                b = b || newQualityName.replaceAll("\"", "").toLowerCase().startsWith( //$NON-NLS-1$ //$NON-NLS-2$
                        TalendTextUtils.removeQuotesForField(newHasString, dbType).toLowerCase());
                b = b || qualityName.toLowerCase().startsWith(hasString.toLowerCase())
                        || newQualityName.toLowerCase().startsWith(newHasString.toLowerCase());
                if (!"".equals(hasString) && b) { //$NON-NLS-1$
                    if (hasString.indexOf(".") > -1) { //$NON-NLS-1$
                        hasString = hasString.substring(index3 + 1);
                    }
                }
                label = label.substring(index2 + 1, label.length());
            }

        }

        return hasString;
    }

    /**
     * Getter for image.
     *
     * @return the image
     */
    public Image getImage() {
        return this.image;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getContent()
     */
    /**
     * Get edit text Content.
     *
     * @return the content String.
     */
    public String getContent() {
        return content;
    }

    /**
     * Get cursor position after insert accepted string.
     *
     * @return the position int.
     */
    public int getCursorPosition() {
        return position;
    }

    /**
     * Get label's description information and show in InfoPopupDialog.
     *
     * @return the description Information String.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get label and show in ContentProposalPopup.
     *
     * @return the label String.
     */
    public String getLabel() {
        return label;
    }

}
