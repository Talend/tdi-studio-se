/**
 */
package org.talend.repository.generic.model.genericMetadata.impl;

import org.eclipse.emf.ecore.EClass;
import org.talend.core.model.metadata.builder.connection.impl.ConnectionImpl;

import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericMetadataPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Generic Connection</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class GenericConnectionImpl extends ConnectionImpl implements GenericConnection {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected GenericConnectionImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return GenericMetadataPackage.Literals.GENERIC_CONNECTION;
    }

} //GenericConnectionImpl
