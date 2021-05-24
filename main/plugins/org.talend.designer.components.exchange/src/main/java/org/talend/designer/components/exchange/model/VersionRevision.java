/**
 * // ============================================================================
 * //
 * // Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 * //
 * // This source code is available under agreement available at
 * // %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * //
 * // You should have received a copy of the agreement
 * // along with this program; if not, write to Talend SA
 * // 9 rue Pages 92150 Suresnes, France
 * //
 * // ============================================================================
 */
package org.talend.designer.components.exchange.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Version Revision</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.designer.components.exchange.model.VersionRevision#getVersionId <em>Version Id</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.VersionRevision#getVersionName <em>Version Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.designer.components.exchange.model.ExchangePackage#getVersionRevision()
 * @model
 * @generated
 */
public interface VersionRevision extends EObject {
    /**
     * Returns the value of the '<em><b>Version Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Version Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Version Id</em>' attribute.
     * @see #setVersionId(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getVersionRevision_VersionId()
     * @model required="true"
     * @generated
     */
    String getVersionId();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.VersionRevision#getVersionId <em>Version Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Version Id</em>' attribute.
     * @see #getVersionId()
     * @generated
     */
    void setVersionId(String value);

    /**
     * Returns the value of the '<em><b>Version Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Version Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Version Name</em>' attribute.
     * @see #setVersionName(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getVersionRevision_VersionName()
     * @model required="true"
     * @generated
     */
    String getVersionName();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.VersionRevision#getVersionName <em>Version Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Version Name</em>' attribute.
     * @see #getVersionName()
     * @generated
     */
    void setVersionName(String value);

} // VersionRevision
