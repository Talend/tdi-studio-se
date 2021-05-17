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
package org.talend.designer.components.thash.io.beans;

/**
 *
 * Interface to load/unload instance data from/to a byte array.
 *
 * @param <B>
 */
public interface ILightSerializable<B> extends Comparable<B> {

    public ILightSerializable<B> createInstance(byte[] byteArray);

    public byte[] toByteArray();

}
