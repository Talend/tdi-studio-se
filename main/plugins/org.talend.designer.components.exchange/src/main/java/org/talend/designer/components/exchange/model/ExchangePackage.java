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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.talend.designer.components.exchange.model.ExchangeFactory
 * @model kind="package"
 * @generated
 */
public interface ExchangePackage extends EPackage {
    /**
     * The package name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNAME = "model";

    /**
     * The package namespace URI.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_URI = "exchange";

    /**
     * The package namespace name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_PREFIX = "exchange";

    /**
     * The singleton instance of the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    ExchangePackage eINSTANCE = org.talend.designer.components.exchange.model.impl.ExchangePackageImpl.init();

    /**
     * The meta object id for the '{@link org.talend.designer.components.exchange.model.impl.CategoryImpl <em>Category</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.components.exchange.model.impl.CategoryImpl
     * @see org.talend.designer.components.exchange.model.impl.ExchangePackageImpl#getCategory()
     * @generated
     */
    int CATEGORY = 0;

    /**
     * The feature id for the '<em><b>Category Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CATEGORY__CATEGORY_ID = 0;

    /**
     * The feature id for the '<em><b>Category Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CATEGORY__CATEGORY_NAME = 1;

    /**
     * The number of structural features of the '<em>Category</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CATEGORY_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.talend.designer.components.exchange.model.impl.VersionRevisionImpl <em>Version Revision</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.components.exchange.model.impl.VersionRevisionImpl
     * @see org.talend.designer.components.exchange.model.impl.ExchangePackageImpl#getVersionRevision()
     * @generated
     */
    int VERSION_REVISION = 1;

    /**
     * The feature id for the '<em><b>Version Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VERSION_REVISION__VERSION_ID = 0;

    /**
     * The feature id for the '<em><b>Version Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VERSION_REVISION__VERSION_NAME = 1;

    /**
     * The number of structural features of the '<em>Version Revision</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VERSION_REVISION_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl <em>Component Extension</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl
     * @see org.talend.designer.components.exchange.model.impl.ExchangePackageImpl#getComponentExtension()
     * @generated
     */
    int COMPONENT_EXTENSION = 2;

    /**
     * The feature id for the '<em><b>Id Extension</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__ID_EXTENSION = 0;

    /**
     * The feature id for the '<em><b>Label</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__LABEL = 1;

    /**
     * The feature id for the '<em><b>Type Extension</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__TYPE_EXTENSION = 2;

    /**
     * The feature id for the '<em><b>Version Extension</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__VERSION_EXTENSION = 3;

    /**
     * The feature id for the '<em><b>Last Version Available</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__LAST_VERSION_AVAILABLE = 4;

    /**
     * The feature id for the '<em><b>Publication Date</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__PUBLICATION_DATE = 5;

    /**
     * The feature id for the '<em><b>Description</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__DESCRIPTION = 6;

    /**
     * The feature id for the '<em><b>Rate</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__RATE = 7;

    /**
     * The feature id for the '<em><b>Author</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__AUTHOR = 8;

    /**
     * The feature id for the '<em><b>Language</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__LANGUAGE = 9;

    /**
     * The feature id for the '<em><b>Reviews</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__REVIEWS = 10;

    /**
     * The feature id for the '<em><b>Downloaded Version</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__DOWNLOADED_VERSION = 11;

    /**
     * The feature id for the '<em><b>Date Download</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__DATE_DOWNLOAD = 12;

    /**
     * The feature id for the '<em><b>List Version Compatibles</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__LIST_VERSION_COMPATIBLES = 13;

    /**
     * The feature id for the '<em><b>Filename</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__FILENAME = 14;

    /**
     * The feature id for the '<em><b>Link Download</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION__LINK_DOWNLOAD = 15;

    /**
     * The number of structural features of the '<em>Component Extension</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int COMPONENT_EXTENSION_FEATURE_COUNT = 16;

    /**
     * The meta object id for the '{@link org.talend.designer.components.exchange.model.impl.AvailableExtensionViewDetailImpl <em>Available Extension View Detail</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.components.exchange.model.impl.AvailableExtensionViewDetailImpl
     * @see org.talend.designer.components.exchange.model.impl.ExchangePackageImpl#getAvailableExtensionViewDetail()
     * @generated
     */
    int AVAILABLE_EXTENSION_VIEW_DETAIL = 3;

    /**
     * The feature id for the '<em><b>Author</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int AVAILABLE_EXTENSION_VIEW_DETAIL__AUTHOR = 0;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int AVAILABLE_EXTENSION_VIEW_DETAIL__TITLE = 1;

    /**
     * The feature id for the '<em><b>Comment</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int AVAILABLE_EXTENSION_VIEW_DETAIL__COMMENT = 2;

    /**
     * The feature id for the '<em><b>Reviewrate</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int AVAILABLE_EXTENSION_VIEW_DETAIL__REVIEWRATE = 3;

    /**
     * The feature id for the '<em><b>Extension</b></em>' container reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION = 4;

    /**
     * The number of structural features of the '<em>Available Extension View Detail</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int AVAILABLE_EXTENSION_VIEW_DETAIL_FEATURE_COUNT = 5;

    /**
     * The meta object id for the '{@link org.talend.designer.components.exchange.model.Language <em>Language</em>}' enum.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.components.exchange.model.Language
     * @see org.talend.designer.components.exchange.model.impl.ExchangePackageImpl#getLanguage()
     * @generated
     */
    int LANGUAGE = 4;


    /**
     * Returns the meta object for class '{@link org.talend.designer.components.exchange.model.Category <em>Category</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Category</em>'.
     * @see org.talend.designer.components.exchange.model.Category
     * @generated
     */
    EClass getCategory();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.Category#getCategoryId <em>Category Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Category Id</em>'.
     * @see org.talend.designer.components.exchange.model.Category#getCategoryId()
     * @see #getCategory()
     * @generated
     */
    EAttribute getCategory_CategoryId();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.Category#getCategoryName <em>Category Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Category Name</em>'.
     * @see org.talend.designer.components.exchange.model.Category#getCategoryName()
     * @see #getCategory()
     * @generated
     */
    EAttribute getCategory_CategoryName();

    /**
     * Returns the meta object for class '{@link org.talend.designer.components.exchange.model.VersionRevision <em>Version Revision</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Version Revision</em>'.
     * @see org.talend.designer.components.exchange.model.VersionRevision
     * @generated
     */
    EClass getVersionRevision();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.VersionRevision#getVersionId <em>Version Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Version Id</em>'.
     * @see org.talend.designer.components.exchange.model.VersionRevision#getVersionId()
     * @see #getVersionRevision()
     * @generated
     */
    EAttribute getVersionRevision_VersionId();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.VersionRevision#getVersionName <em>Version Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Version Name</em>'.
     * @see org.talend.designer.components.exchange.model.VersionRevision#getVersionName()
     * @see #getVersionRevision()
     * @generated
     */
    EAttribute getVersionRevision_VersionName();

    /**
     * Returns the meta object for class '{@link org.talend.designer.components.exchange.model.ComponentExtension <em>Component Extension</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Component Extension</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension
     * @generated
     */
    EClass getComponentExtension();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getIdExtension <em>Id Extension</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Id Extension</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getIdExtension()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_IdExtension();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getLabel <em>Label</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Label</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getLabel()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_Label();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getTypeExtension <em>Type Extension</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type Extension</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getTypeExtension()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_TypeExtension();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getVersionExtension <em>Version Extension</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Version Extension</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getVersionExtension()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_VersionExtension();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getLastVersionAvailable <em>Last Version Available</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Last Version Available</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getLastVersionAvailable()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_LastVersionAvailable();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getPublicationDate <em>Publication Date</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Publication Date</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getPublicationDate()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_PublicationDate();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getDescription <em>Description</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Description</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getDescription()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_Description();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getRate <em>Rate</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Rate</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getRate()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_Rate();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getAuthor <em>Author</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Author</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getAuthor()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_Author();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getLanguage <em>Language</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Language</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getLanguage()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_Language();

    /**
     * Returns the meta object for the containment reference list '{@link org.talend.designer.components.exchange.model.ComponentExtension#getReviews <em>Reviews</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Reviews</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getReviews()
     * @see #getComponentExtension()
     * @generated
     */
    EReference getComponentExtension_Reviews();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getDownloadedVersion <em>Downloaded Version</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Downloaded Version</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getDownloadedVersion()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_DownloadedVersion();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getDateDownload <em>Date Download</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Date Download</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getDateDownload()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_DateDownload();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getListVersionCompatibles <em>List Version Compatibles</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>List Version Compatibles</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getListVersionCompatibles()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_ListVersionCompatibles();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getFilename <em>Filename</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Filename</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getFilename()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_Filename();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.ComponentExtension#getLinkDownload <em>Link Download</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Link Download</em>'.
     * @see org.talend.designer.components.exchange.model.ComponentExtension#getLinkDownload()
     * @see #getComponentExtension()
     * @generated
     */
    EAttribute getComponentExtension_LinkDownload();

    /**
     * Returns the meta object for class '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail <em>Available Extension View Detail</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Available Extension View Detail</em>'.
     * @see org.talend.designer.components.exchange.model.AvailableExtensionViewDetail
     * @generated
     */
    EClass getAvailableExtensionViewDetail();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getAuthor <em>Author</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Author</em>'.
     * @see org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getAuthor()
     * @see #getAvailableExtensionViewDetail()
     * @generated
     */
    EAttribute getAvailableExtensionViewDetail_Author();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getTitle <em>Title</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Title</em>'.
     * @see org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getTitle()
     * @see #getAvailableExtensionViewDetail()
     * @generated
     */
    EAttribute getAvailableExtensionViewDetail_Title();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getComment <em>Comment</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Comment</em>'.
     * @see org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getComment()
     * @see #getAvailableExtensionViewDetail()
     * @generated
     */
    EAttribute getAvailableExtensionViewDetail_Comment();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getReviewrate <em>Reviewrate</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Reviewrate</em>'.
     * @see org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getReviewrate()
     * @see #getAvailableExtensionViewDetail()
     * @generated
     */
    EAttribute getAvailableExtensionViewDetail_Reviewrate();

    /**
     * Returns the meta object for the container reference '{@link org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getExtension <em>Extension</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the container reference '<em>Extension</em>'.
     * @see org.talend.designer.components.exchange.model.AvailableExtensionViewDetail#getExtension()
     * @see #getAvailableExtensionViewDetail()
     * @generated
     */
    EReference getAvailableExtensionViewDetail_Extension();

    /**
     * Returns the meta object for enum '{@link org.talend.designer.components.exchange.model.Language <em>Language</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for enum '<em>Language</em>'.
     * @see org.talend.designer.components.exchange.model.Language
     * @generated
     */
    EEnum getLanguage();

    /**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
    ExchangeFactory getExchangeFactory();

    /**
     * <!-- begin-user-doc -->
     * Defines literals for the meta objects that represent
     * <ul>
     *   <li>each class,</li>
     *   <li>each feature of each class,</li>
     *   <li>each enum,</li>
     *   <li>and each data type</li>
     * </ul>
     * <!-- end-user-doc -->
     * @generated
     */
    interface Literals {
        /**
         * The meta object literal for the '{@link org.talend.designer.components.exchange.model.impl.CategoryImpl <em>Category</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.components.exchange.model.impl.CategoryImpl
         * @see org.talend.designer.components.exchange.model.impl.ExchangePackageImpl#getCategory()
         * @generated
         */
        EClass CATEGORY = eINSTANCE.getCategory();

        /**
         * The meta object literal for the '<em><b>Category Id</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute CATEGORY__CATEGORY_ID = eINSTANCE.getCategory_CategoryId();

        /**
         * The meta object literal for the '<em><b>Category Name</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute CATEGORY__CATEGORY_NAME = eINSTANCE.getCategory_CategoryName();

        /**
         * The meta object literal for the '{@link org.talend.designer.components.exchange.model.impl.VersionRevisionImpl <em>Version Revision</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.components.exchange.model.impl.VersionRevisionImpl
         * @see org.talend.designer.components.exchange.model.impl.ExchangePackageImpl#getVersionRevision()
         * @generated
         */
        EClass VERSION_REVISION = eINSTANCE.getVersionRevision();

        /**
         * The meta object literal for the '<em><b>Version Id</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute VERSION_REVISION__VERSION_ID = eINSTANCE.getVersionRevision_VersionId();

        /**
         * The meta object literal for the '<em><b>Version Name</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute VERSION_REVISION__VERSION_NAME = eINSTANCE.getVersionRevision_VersionName();

        /**
         * The meta object literal for the '{@link org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl <em>Component Extension</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.components.exchange.model.impl.ComponentExtensionImpl
         * @see org.talend.designer.components.exchange.model.impl.ExchangePackageImpl#getComponentExtension()
         * @generated
         */
        EClass COMPONENT_EXTENSION = eINSTANCE.getComponentExtension();

        /**
         * The meta object literal for the '<em><b>Id Extension</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__ID_EXTENSION = eINSTANCE.getComponentExtension_IdExtension();

        /**
         * The meta object literal for the '<em><b>Label</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__LABEL = eINSTANCE.getComponentExtension_Label();

        /**
         * The meta object literal for the '<em><b>Type Extension</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__TYPE_EXTENSION = eINSTANCE.getComponentExtension_TypeExtension();

        /**
         * The meta object literal for the '<em><b>Version Extension</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__VERSION_EXTENSION = eINSTANCE.getComponentExtension_VersionExtension();

        /**
         * The meta object literal for the '<em><b>Last Version Available</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__LAST_VERSION_AVAILABLE = eINSTANCE.getComponentExtension_LastVersionAvailable();

        /**
         * The meta object literal for the '<em><b>Publication Date</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__PUBLICATION_DATE = eINSTANCE.getComponentExtension_PublicationDate();

        /**
         * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__DESCRIPTION = eINSTANCE.getComponentExtension_Description();

        /**
         * The meta object literal for the '<em><b>Rate</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__RATE = eINSTANCE.getComponentExtension_Rate();

        /**
         * The meta object literal for the '<em><b>Author</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__AUTHOR = eINSTANCE.getComponentExtension_Author();

        /**
         * The meta object literal for the '<em><b>Language</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__LANGUAGE = eINSTANCE.getComponentExtension_Language();

        /**
         * The meta object literal for the '<em><b>Reviews</b></em>' containment reference list feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference COMPONENT_EXTENSION__REVIEWS = eINSTANCE.getComponentExtension_Reviews();

        /**
         * The meta object literal for the '<em><b>Downloaded Version</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__DOWNLOADED_VERSION = eINSTANCE.getComponentExtension_DownloadedVersion();

        /**
         * The meta object literal for the '<em><b>Date Download</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__DATE_DOWNLOAD = eINSTANCE.getComponentExtension_DateDownload();

        /**
         * The meta object literal for the '<em><b>List Version Compatibles</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__LIST_VERSION_COMPATIBLES = eINSTANCE.getComponentExtension_ListVersionCompatibles();

        /**
         * The meta object literal for the '<em><b>Filename</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__FILENAME = eINSTANCE.getComponentExtension_Filename();

        /**
         * The meta object literal for the '<em><b>Link Download</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute COMPONENT_EXTENSION__LINK_DOWNLOAD = eINSTANCE.getComponentExtension_LinkDownload();

        /**
         * The meta object literal for the '{@link org.talend.designer.components.exchange.model.impl.AvailableExtensionViewDetailImpl <em>Available Extension View Detail</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.components.exchange.model.impl.AvailableExtensionViewDetailImpl
         * @see org.talend.designer.components.exchange.model.impl.ExchangePackageImpl#getAvailableExtensionViewDetail()
         * @generated
         */
        EClass AVAILABLE_EXTENSION_VIEW_DETAIL = eINSTANCE.getAvailableExtensionViewDetail();

        /**
         * The meta object literal for the '<em><b>Author</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute AVAILABLE_EXTENSION_VIEW_DETAIL__AUTHOR = eINSTANCE.getAvailableExtensionViewDetail_Author();

        /**
         * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute AVAILABLE_EXTENSION_VIEW_DETAIL__TITLE = eINSTANCE.getAvailableExtensionViewDetail_Title();

        /**
         * The meta object literal for the '<em><b>Comment</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute AVAILABLE_EXTENSION_VIEW_DETAIL__COMMENT = eINSTANCE.getAvailableExtensionViewDetail_Comment();

        /**
         * The meta object literal for the '<em><b>Reviewrate</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute AVAILABLE_EXTENSION_VIEW_DETAIL__REVIEWRATE = eINSTANCE.getAvailableExtensionViewDetail_Reviewrate();

        /**
         * The meta object literal for the '<em><b>Extension</b></em>' container reference feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference AVAILABLE_EXTENSION_VIEW_DETAIL__EXTENSION = eINSTANCE.getAvailableExtensionViewDetail_Extension();

        /**
         * The meta object literal for the '{@link org.talend.designer.components.exchange.model.Language <em>Language</em>}' enum.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.components.exchange.model.Language
         * @see org.talend.designer.components.exchange.model.impl.ExchangePackageImpl#getLanguage()
         * @generated
         */
        EEnum LANGUAGE = eINSTANCE.getLanguage();

    }

} //ExchangePackage
