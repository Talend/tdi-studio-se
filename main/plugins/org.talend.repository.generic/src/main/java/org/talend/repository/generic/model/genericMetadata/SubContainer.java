/**
 */
package org.talend.repository.generic.model.genericMetadata;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sub Container</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.talend.repository.generic.model.genericMetadata.SubContainer#getCompProperties <em>Comp Properties</em>}</li>
 * </ul>
 *
 * @see org.talend.repository.generic.model.genericMetadata.GenericMetadataPackage#getSubContainer()
 * @model
 * @generated
 */
public interface SubContainer extends orgomg.cwm.objectmodel.core.Package {
    /**
     * Returns the value of the '<em><b>Comp Properties</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Comp Properties</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Comp Properties</em>' attribute.
     * @see #setCompProperties(String)
     * @see org.talend.repository.generic.model.genericMetadata.GenericMetadataPackage#getSubContainer_CompProperties()
     * @model
     * @generated
     */
    String getCompProperties();

    /**
     * Sets the value of the '{@link org.talend.repository.generic.model.genericMetadata.SubContainer#getCompProperties <em>Comp Properties</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Comp Properties</em>' attribute.
     * @see #getCompProperties()
     * @generated
     */
    void setCompProperties(String value);

} // SubContainer
