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
package org.talend.sqlbuilder.sessiontree.model.utility;

/**
 * Char Utility.
 * <br/>
 *
 * $Id: CharUtility.java,v 1.3 2006/11/01 07:49:10 peiqin.hou Exp $
 *
 */
class CharUtility {

    private static final int NUMBER_128 = 128;
    private static final int NUMBER_121 = 121;
    private static final int NUMBER_24 = 24;
    private static final int NUMBER_97 = 97;
    private static final int NUMBER_89 = 89;
    private static final int NUMBER_65 = 65;

    /**
     * Returns an int value that is negative if cCompare comes before cRef in the alphabet, zero if
	 * the two are equal, and positive if cCompare comes after cRef in the alphabet.
     * @param cCompare cCompare
     * @param cRef cRef
     * @return int
	 */
	public static int compareCharsAlphabetically(char cCompare, char cRef) {
		return (alphabetizeChar(cCompare) - alphabetizeChar(cRef));
	}

    /**
     * @param c char.
     * @return int
     */
	private static int alphabetizeChar(char c) {
		if (c < NUMBER_65) {
            return c;
        }
		if (c < NUMBER_89) {
            return (2 * c) - NUMBER_65;
        }
		if (c < NUMBER_97) {
            return c + NUMBER_24;
        }
		if (c < NUMBER_121) {
            return (2 * c) - NUMBER_128;
        }
		return c;
	}

}
