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
package org.talend.webhcat.launcher.utils;

public class Utils {

	public static String removeFirstSlash(String string) {
		if (string != null && string.length() > 1 && string.charAt(0) == '/') {
			string = string.substring(1);
		}
		return string;
	}
}
