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
package org.talend.designer.components.exchange.model.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.talend.designer.components.exchange.model.ExchangePackage;
import org.talend.designer.components.exchange.model.VersionRevision;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Version Revision</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.VersionRevisionImpl#getVersionId <em>Version Id</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.impl.VersionRevisionImpl#getVersionName <em>Version Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class VersionRevisionImpl extends EObjectImpl implements VersionRevision {
    /**
     * The default value of the '{@link #getVersionId() <em>Version Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getVersionId()
     * @generated
     * @ordered
     */
    protected static final String VERSION_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getVersionId() <em>Version Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getVersionId()
     * @generated
     * @ordered
     */
    protected String versionId = VERSION_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getVersionName() <em>Version Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getVersionName()
     * @generated
     * @ordered
     */
    protected static final String VERSION_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getVersionName() <em>Version Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getVersionName()
     * @generated
     * @ordered
     */
    protected String versionName = VERSION_NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected VersionRevisionImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ExchangePackage.Literals.VERSION_REVISION;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setVersionId(String newVersionId) {
        String oldVersionId = versionId;
        versionId = newVersionId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.VERSION_REVISION__VERSION_ID, oldVersionId, versionId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setVersionName(String newVersionName) {
        String oldVersionName = versionName;
        versionName = newVersionName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ExchangePackage.VERSION_REVISION__VERSION_NAME, oldVersionName, versionName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case ExchangePackage.VERSION_REVISION__VERSION_ID:
                return getVersionId();
            case ExchangePackage.VERSION_REVISION__VERSION_NAME:
                return getVersionName();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case ExchangePackage.VERSION_REVISION__VERSION_ID:
                setVersionId((String)newValue);
                return;
            case ExchangePackage.VERSION_REVISION__VERSION_NAME:
                setVersionName((String)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case ExchangePackage.VERSION_REVISION__VERSION_ID:
                setVersionId(VERSION_ID_EDEFAULT);
                return;
            case ExchangePackage.VERSION_REVISION__VERSION_NAME:
                setVersionName(VERSION_NAME_EDEFAULT);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case ExchangePackage.VERSION_REVISION__VERSION_ID:
                return VERSION_ID_EDEFAULT == null ? versionId != null : !VERSION_ID_EDEFAULT.equals(versionId);
            case ExchangePackage.VERSION_REVISION__VERSION_NAME:
                return VERSION_NAME_EDEFAULT == null ? versionName != null : !VERSION_NAME_EDEFAULT.equals(versionName);
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (versionId: ");
        result.append(versionId);
        result.append(", versionName: ");
        result.append(versionName);
        result.append(')');
        return result.toString();
    }

} //VersionRevisionImpl
