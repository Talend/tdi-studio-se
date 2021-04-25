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
package org.talend.spark.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class Utils {

	public static List<Object> ListShift(List<Object> list, Integer index) {
		List<Object> localList = list;
		boolean shift = false;
		for (int i = list.size() - 1; i > index; i--) {
			if (localList.get(i) == null || shift) {
				shift = true;
				localList.set(i, localList.get(i - 1));
			}
		}
		return localList;
	}

	public static int compareTo(Object object1, Object object2,
			Class<?> javaType) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException, InstantiationException {
		Constructor<?> c = javaType.getConstructor(String.class);
		Object o1 = c.newInstance(object1.toString());
		Object o2 = c.newInstance(object2.toString());
		Method compareTo = javaType.getMethod("compareTo", javaType);
		int ret = (Integer) compareTo.invoke(o1, o2);
		return ret;
	}
}
