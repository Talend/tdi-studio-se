/**
 */
package org.talend.repository.generic.model.genericMetadata.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.talend.repository.generic.model.genericMetadata.GenericMetadataPackage;
import org.talend.repository.generic.model.genericMetadata.SubContainer;

import orgomg.cwm.objectmodel.core.impl.PackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sub Container</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.talend.repository.generic.model.genericMetadata.impl.SubContainerImpl#getCompProperties <em>Comp Properties</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SubContainerImpl extends PackageImpl implements SubContainer {
    /**
     * The default value of the '{@link #getCompProperties() <em>Comp Properties</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCompProperties()
     * @generated
     * @ordered
     */
    protected static final String COMP_PROPERTIES_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getCompProperties() <em>Comp Properties</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCompProperties()
     * @generated
     * @ordered
     */
    protected String compProperties = COMP_PROPERTIES_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected SubContainerImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return GenericMetadataPackage.Literals.SUB_CONTAINER;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getCompProperties() {
        return compProperties;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCompProperties(String newCompProperties) {
        String oldCompProperties = compProperties;
        compProperties = newCompProperties;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GenericMetadataPackage.SUB_CONTAINER__COMP_PROPERTIES, oldCompProperties, compProperties));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case GenericMetadataPackage.SUB_CONTAINER__COMP_PROPERTIES:
                return getCompProperties();
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
            case GenericMetadataPackage.SUB_CONTAINER__COMP_PROPERTIES:
                setCompProperties((String)newValue);
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
            case GenericMetadataPackage.SUB_CONTAINER__COMP_PROPERTIES:
                setCompProperties(COMP_PROPERTIES_EDEFAULT);
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
            case GenericMetadataPackage.SUB_CONTAINER__COMP_PROPERTIES:
                return COMP_PROPERTIES_EDEFAULT == null ? compProperties != null : !COMP_PROPERTIES_EDEFAULT.equals(compProperties);
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
        result.append(" (compProperties: ");
        result.append(compProperties);
        result.append(')');
        return result.toString();
    }

} //SubContainerImpl
