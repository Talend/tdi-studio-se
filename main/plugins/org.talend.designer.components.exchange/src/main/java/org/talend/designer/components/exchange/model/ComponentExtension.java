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

import java.util.Date;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Component Extension</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getIdExtension <em>Id Extension</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getLabel <em>Label</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getTypeExtension <em>Type Extension</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getVersionExtension <em>Version Extension</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getLastVersionAvailable <em>Last Version Available</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getPublicationDate <em>Publication Date</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getDescription <em>Description</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getRate <em>Rate</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getAuthor <em>Author</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getLanguage <em>Language</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getReviews <em>Reviews</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getDownloadedVersion <em>Downloaded Version</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getDateDownload <em>Date Download</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getListVersionCompatibles <em>List Version Compatibles</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getFilename <em>Filename</em>}</li>
 *   <li>{@link org.talend.designer.components.exchange.model.ComponentExtension#getLinkDownload <em>Link Download</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension()
 * @model
 * @generated
 */
public interface ComponentExtension extends EObject {
    /**
     * Returns the value of the '<em><b>Id Extension</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Id Extension</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Id Extension</em>' attribute.
     * @see #setIdExtension(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_IdExtension()
     * @model required="true"
     * @generated
     */
    String getIdExtension();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getIdExtension <em>Id Extension</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Id Extension</em>' attribute.
     * @see #getIdExtension()
     * @generated
     */
    void setIdExtension(String value);

    /**
     * Returns the value of the '<em><b>Label</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Label</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Label</em>' attribute.
     * @see #setLabel(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_Label()
     * @model required="true"
     * @generated
     */
    String getLabel();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getLabel <em>Label</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Label</em>' attribute.
     * @see #getLabel()
     * @generated
     */
    void setLabel(String value);

    /**
     * Returns the value of the '<em><b>Type Extension</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Type Extension</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Type Extension</em>' attribute.
     * @see #setTypeExtension(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_TypeExtension()
     * @model required="true"
     * @generated
     */
    String getTypeExtension();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getTypeExtension <em>Type Extension</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Type Extension</em>' attribute.
     * @see #getTypeExtension()
     * @generated
     */
    void setTypeExtension(String value);

    /**
     * Returns the value of the '<em><b>Version Extension</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Version Extension</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Version Extension</em>' attribute.
     * @see #setVersionExtension(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_VersionExtension()
     * @model required="true"
     * @generated
     */
    String getVersionExtension();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getVersionExtension <em>Version Extension</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Version Extension</em>' attribute.
     * @see #getVersionExtension()
     * @generated
     */
    void setVersionExtension(String value);

    /**
     * Returns the value of the '<em><b>Last Version Available</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Last Version Available</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Last Version Available</em>' attribute.
     * @see #setLastVersionAvailable(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_LastVersionAvailable()
     * @model required="true"
     * @generated
     */
    String getLastVersionAvailable();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getLastVersionAvailable <em>Last Version Available</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Last Version Available</em>' attribute.
     * @see #getLastVersionAvailable()
     * @generated
     */
    void setLastVersionAvailable(String value);

    /**
     * Returns the value of the '<em><b>Publication Date</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Publication Date</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Publication Date</em>' attribute.
     * @see #setPublicationDate(Date)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_PublicationDate()
     * @model required="true"
     * @generated
     */
    Date getPublicationDate();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getPublicationDate <em>Publication Date</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Publication Date</em>' attribute.
     * @see #getPublicationDate()
     * @generated
     */
    void setPublicationDate(Date value);

    /**
     * Returns the value of the '<em><b>Description</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Description</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Description</em>' attribute.
     * @see #setDescription(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_Description()
     * @model required="true"
     * @generated
     */
    String getDescription();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getDescription <em>Description</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Description</em>' attribute.
     * @see #getDescription()
     * @generated
     */
    void setDescription(String value);

    /**
     * Returns the value of the '<em><b>Rate</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Rate</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Rate</em>' attribute.
     * @see #setRate(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_Rate()
     * @model required="true"
     * @generated
     */
    String getRate();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getRate <em>Rate</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Rate</em>' attribute.
     * @see #getRate()
     * @generated
     */
    void setRate(String value);

    /**
     * Returns the value of the '<em><b>Author</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Author</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Author</em>' attribute.
     * @see #setAuthor(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_Author()
     * @model required="true"
     * @generated
     */
    String getAuthor();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getAuthor <em>Author</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Author</em>' attribute.
     * @see #getAuthor()
     * @generated
     */
    void setAuthor(String value);

    /**
     * Returns the value of the '<em><b>Language</b></em>' attribute.
     * The default value is <code>""</code>.
     * The literals are from the enumeration {@link org.talend.designer.components.exchange.model.Language}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Language</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Language</em>' attribute.
     * @see org.talend.designer.components.exchange.model.Language
     * @see #setLanguage(Language)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_Language()
     * @model default="" required="true"
     * @generated
     */
    Language getLanguage();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getLanguage <em>Language</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Language</em>' attribute.
     * @see org.talend.designer.components.exchange.model.Language
     * @see #getLanguage()
     * @generated
     */
    void setLanguage(Language value);

    /**
     * Returns the value of the '<em><b>Reviews</b></em>' containment reference list.
     * The list contents are of type {@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail}.
     * It is bidirectional and its opposite is '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getExtension <em>Extension</em>}'.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Reviews</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Reviews</em>' containment reference list.
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_Reviews()
     * @see org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getExtension
     * @model opposite="extension" containment="true" required="true"
     * @generated
     */
    EList<AvailableExtensionViewDetail> getReviews();

    /**
     * Returns the value of the '<em><b>Downloaded Version</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Downloaded Version</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Downloaded Version</em>' attribute.
     * @see #setDownloadedVersion(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_DownloadedVersion()
     * @model required="true"
     * @generated
     */
    String getDownloadedVersion();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getDownloadedVersion <em>Downloaded Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Downloaded Version</em>' attribute.
     * @see #getDownloadedVersion()
     * @generated
     */
    void setDownloadedVersion(String value);

    /**
     * Returns the value of the '<em><b>Date Download</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Date Download</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Date Download</em>' attribute.
     * @see #setDateDownload(Date)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_DateDownload()
     * @model required="true"
     * @generated
     */
    Date getDateDownload();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getDateDownload <em>Date Download</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Date Download</em>' attribute.
     * @see #getDateDownload()
     * @generated
     */
    void setDateDownload(Date value);

    /**
     * Returns the value of the '<em><b>List Version Compatibles</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>List Version Compatibles</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>List Version Compatibles</em>' attribute.
     * @see #setListVersionCompatibles(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_ListVersionCompatibles()
     * @model required="true"
     * @generated
     */
    String getListVersionCompatibles();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getListVersionCompatibles <em>List Version Compatibles</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>List Version Compatibles</em>' attribute.
     * @see #getListVersionCompatibles()
     * @generated
     */
    void setListVersionCompatibles(String value);

    /**
     * Returns the value of the '<em><b>Filename</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Filename</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Filename</em>' attribute.
     * @see #setFilename(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_Filename()
     * @model required="true"
     * @generated
     */
    String getFilename();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getFilename <em>Filename</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Filename</em>' attribute.
     * @see #getFilename()
     * @generated
     */
    void setFilename(String value);

    /**
     * Returns the value of the '<em><b>Link Download</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Link Download</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Link Download</em>' attribute.
     * @see #setLinkDownload(String)
     * @see org.talend.designer.components.exchange.model.ExchangePackage#getComponentExtension_LinkDownload()
     * @model required="true"
     * @generated
     */
    String getLinkDownload();

    /**
     * Sets the value of the '{@link org.talend.designer.components.exchange.model.ComponentExtension#getLinkDownload <em>Link Download</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Link Download</em>' attribute.
     * @see #getLinkDownload()
     * @generated
     */
    void setLinkDownload(String value);

} // ComponentExtension
