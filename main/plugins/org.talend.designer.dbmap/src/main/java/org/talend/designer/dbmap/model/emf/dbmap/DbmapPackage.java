/**
 * <copyright> </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapFactory
 * @model kind="package"
 * @generated
 */
public interface DbmapPackage extends EPackage {

    /**
     * The package name.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    String eNAME = "dbmap";

    /**
     * The package namespace URI.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    String eNS_URI = "http://www.talend.org/dbmap";

    /**
     * The package namespace name.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    String eNS_PREFIX = "TalendDBMap";

    /**
     * The singleton instance of the package.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    DbmapPackage eINSTANCE = org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl.init();

    /**
     * The meta object id for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapDataImpl <em>DB Map Data</em>}' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapDataImpl
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getDBMapData()
     * @generated
     */
    int DB_MAP_DATA = 0;

    /**
     * The feature id for the '<em><b>Var Tables</b></em>' containment reference list.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @generated
     * @ordered
     */
    int DB_MAP_DATA__VAR_TABLES = TalendFilePackage.ABSTRACT_EXTERNAL_DATA_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Input Tables</b></em>' containment reference list.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @generated
     * @ordered
     */
    int DB_MAP_DATA__INPUT_TABLES = TalendFilePackage.ABSTRACT_EXTERNAL_DATA_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Output Tables</b></em>' containment reference list.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @generated
     * @ordered
     */
    int DB_MAP_DATA__OUTPUT_TABLES = TalendFilePackage.ABSTRACT_EXTERNAL_DATA_FEATURE_COUNT + 2;

    /**
     * The number of structural features of the '<em>DB Map Data</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated
     * @ordered
     */
    int DB_MAP_DATA_FEATURE_COUNT = TalendFilePackage.ABSTRACT_EXTERNAL_DATA_FEATURE_COUNT + 3;

    /**
     * The meta object id for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapperTableEntryImpl <em>DB Mapper Table Entry</em>}' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapperTableEntryImpl
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getDBMapperTableEntry()
     * @generated
     */
    int DB_MAPPER_TABLE_ENTRY = 1;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DB_MAPPER_TABLE_ENTRY__NAME = 0;

    /**
     * The feature id for the '<em><b>Expression</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DB_MAPPER_TABLE_ENTRY__EXPRESSION = 1;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DB_MAPPER_TABLE_ENTRY__TYPE = 2;

    /**
     * The feature id for the '<em><b>Nullable</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DB_MAPPER_TABLE_ENTRY__NULLABLE = 3;

    /**
     * The feature id for the '<em><b>Join</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DB_MAPPER_TABLE_ENTRY__JOIN = 4;

    /**
     * The feature id for the '<em><b>Operator</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DB_MAPPER_TABLE_ENTRY__OPERATOR = 5;

    /**
     * The number of structural features of the '<em>DB Mapper Table Entry</em>' class.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @generated
     * @ordered
     */
    int DB_MAPPER_TABLE_ENTRY_FEATURE_COUNT = 6;

    /**
     * The meta object id for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.AbstractDBDataMapTableImpl <em>Abstract DB Data Map Table</em>}' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.AbstractDBDataMapTableImpl
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getAbstractDBDataMapTable()
     * @generated
     */
    int ABSTRACT_DB_DATA_MAP_TABLE = 2;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_DB_DATA_MAP_TABLE__NAME = 0;

    /**
     * The feature id for the '<em><b>Minimized</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_DB_DATA_MAP_TABLE__MINIMIZED = 1;

    /**
     * The feature id for the '<em><b>Readonly</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_DB_DATA_MAP_TABLE__READONLY = 2;

    /**
     * The feature id for the '<em><b>DB Mapper Table Entries</b></em>' containment reference list.
     * <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_DB_DATA_MAP_TABLE__DB_MAPPER_TABLE_ENTRIES = 3;

    /**
     * The feature id for the '<em><b>Table Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_DB_DATA_MAP_TABLE__TABLE_NAME = 4;

    /**
     * The number of structural features of the '<em>Abstract DB Data Map Table</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_DB_DATA_MAP_TABLE_FEATURE_COUNT = 5;

    /**
     * The meta object id for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.AbstaceDBInOutTableImpl <em>Abstace DB In Out Table</em>}' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.AbstaceDBInOutTableImpl
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getAbstaceDBInOutTable()
     * @generated
     */
    int ABSTACE_DB_IN_OUT_TABLE = 3;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTACE_DB_IN_OUT_TABLE__NAME = ABSTRACT_DB_DATA_MAP_TABLE__NAME;

    /**
     * The feature id for the '<em><b>Minimized</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTACE_DB_IN_OUT_TABLE__MINIMIZED = ABSTRACT_DB_DATA_MAP_TABLE__MINIMIZED;

    /**
     * The feature id for the '<em><b>Readonly</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTACE_DB_IN_OUT_TABLE__READONLY = ABSTRACT_DB_DATA_MAP_TABLE__READONLY;

    /**
     * The feature id for the '<em><b>DB Mapper Table Entries</b></em>' containment reference list.
     * <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTACE_DB_IN_OUT_TABLE__DB_MAPPER_TABLE_ENTRIES = ABSTRACT_DB_DATA_MAP_TABLE__DB_MAPPER_TABLE_ENTRIES;

    /**
     * The feature id for the '<em><b>Table Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTACE_DB_IN_OUT_TABLE__TABLE_NAME = ABSTRACT_DB_DATA_MAP_TABLE__TABLE_NAME;

    /**
     * The number of structural features of the '<em>Abstace DB In Out Table</em>' class.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTACE_DB_IN_OUT_TABLE_FEATURE_COUNT = ABSTRACT_DB_DATA_MAP_TABLE_FEATURE_COUNT + 0;

    /**
     * The meta object id for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.VarTableImpl <em>Var Table</em>}' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.VarTableImpl
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getVarTable()
     * @generated
     */
    int VAR_TABLE = 4;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VAR_TABLE__NAME = ABSTRACT_DB_DATA_MAP_TABLE__NAME;

    /**
     * The feature id for the '<em><b>Minimized</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VAR_TABLE__MINIMIZED = ABSTRACT_DB_DATA_MAP_TABLE__MINIMIZED;

    /**
     * The feature id for the '<em><b>Readonly</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VAR_TABLE__READONLY = ABSTRACT_DB_DATA_MAP_TABLE__READONLY;

    /**
     * The feature id for the '<em><b>DB Mapper Table Entries</b></em>' containment reference list.
     * <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VAR_TABLE__DB_MAPPER_TABLE_ENTRIES = ABSTRACT_DB_DATA_MAP_TABLE__DB_MAPPER_TABLE_ENTRIES;

    /**
     * The feature id for the '<em><b>Table Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VAR_TABLE__TABLE_NAME = ABSTRACT_DB_DATA_MAP_TABLE__TABLE_NAME;

    /**
     * The number of structural features of the '<em>Var Table</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated
     * @ordered
     */
    int VAR_TABLE_FEATURE_COUNT = ABSTRACT_DB_DATA_MAP_TABLE_FEATURE_COUNT + 0;

    /**
     * The meta object id for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.InputTableImpl <em>Input Table</em>}' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.InputTableImpl
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getInputTable()
     * @generated
     */
    int INPUT_TABLE = 5;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__NAME = ABSTACE_DB_IN_OUT_TABLE__NAME;

    /**
     * The feature id for the '<em><b>Minimized</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__MINIMIZED = ABSTACE_DB_IN_OUT_TABLE__MINIMIZED;

    /**
     * The feature id for the '<em><b>Readonly</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__READONLY = ABSTACE_DB_IN_OUT_TABLE__READONLY;

    /**
     * The feature id for the '<em><b>DB Mapper Table Entries</b></em>' containment reference list.
     * <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__DB_MAPPER_TABLE_ENTRIES = ABSTACE_DB_IN_OUT_TABLE__DB_MAPPER_TABLE_ENTRIES;

    /**
     * The feature id for the '<em><b>Table Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__TABLE_NAME = ABSTACE_DB_IN_OUT_TABLE__TABLE_NAME;

    /**
     * The feature id for the '<em><b>Join Type</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__JOIN_TYPE = ABSTACE_DB_IN_OUT_TABLE_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Alias</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__ALIAS = ABSTACE_DB_IN_OUT_TABLE_FEATURE_COUNT + 1;

    /**
     * The number of structural features of the '<em>Input Table</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated
     * @ordered
     */
    int INPUT_TABLE_FEATURE_COUNT = ABSTACE_DB_IN_OUT_TABLE_FEATURE_COUNT + 2;

    /**
     * The meta object id for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.OutputTableImpl <em>Output Table</em>}' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.OutputTableImpl
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getOutputTable()
     * @generated
     */
    int OUTPUT_TABLE = 6;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__NAME = ABSTACE_DB_IN_OUT_TABLE__NAME;

    /**
     * The feature id for the '<em><b>Minimized</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__MINIMIZED = ABSTACE_DB_IN_OUT_TABLE__MINIMIZED;

    /**
     * The feature id for the '<em><b>Readonly</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__READONLY = ABSTACE_DB_IN_OUT_TABLE__READONLY;

    /**
     * The feature id for the '<em><b>DB Mapper Table Entries</b></em>' containment reference list.
     * <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__DB_MAPPER_TABLE_ENTRIES = ABSTACE_DB_IN_OUT_TABLE__DB_MAPPER_TABLE_ENTRIES;

    /**
     * The feature id for the '<em><b>Table Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__TABLE_NAME = ABSTACE_DB_IN_OUT_TABLE__TABLE_NAME;

    /**
     * The feature id for the '<em><b>Filter Entries</b></em>' containment reference list.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__FILTER_ENTRIES = ABSTACE_DB_IN_OUT_TABLE_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the '<em>Output Table</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE_FEATURE_COUNT = ABSTACE_DB_IN_OUT_TABLE_FEATURE_COUNT + 1;

    /**
     * The meta object id for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.FilterEntryImpl <em>Filter Entry</em>}' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.FilterEntryImpl
     * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getFilterEntry()
     * @generated
     */
    int FILTER_ENTRY = 7;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FILTER_ENTRY__NAME = 0;

    /**
     * The feature id for the '<em><b>Expression</b></em>' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FILTER_ENTRY__EXPRESSION = 1;

    int FILTER_ENTRY__FILTER_KIND = 2;

    /**
     * The number of structural features of the '<em>Filter Entry</em>' class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated
     * @ordered
     */
    int FILTER_ENTRY_FEATURE_COUNT = 3;

    /**
     * Returns the meta object for class '{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapData <em>DB Map Data</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the meta object for class '<em>DB Map Data</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DBMapData
     * @generated
     */
    EClass getDBMapData();

    /**
     * Returns the meta object for the containment reference list '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.DBMapData#getVarTables <em>Var Tables</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '<em>Var Tables</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DBMapData#getVarTables()
     * @see #getDBMapData()
     * @generated
     */
    EReference getDBMapData_VarTables();

    /**
     * Returns the meta object for the containment reference list '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.DBMapData#getInputTables <em>Input Tables</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '<em>Input Tables</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DBMapData#getInputTables()
     * @see #getDBMapData()
     * @generated
     */
    EReference getDBMapData_InputTables();

    /**
     * Returns the meta object for the containment reference list '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.DBMapData#getOutputTables <em>Output Tables</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '<em>Output Tables</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DBMapData#getOutputTables()
     * @see #getDBMapData()
     * @generated
     */
    EReference getDBMapData_OutputTables();

    /**
     * Returns the meta object for class '{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry <em>DB Mapper Table Entry</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the meta object for class '<em>DB Mapper Table Entry</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry
     * @generated
     */
    EClass getDBMapperTableEntry();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getName <em>Name</em>}'.
     * <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getName()
     * @see #getDBMapperTableEntry()
     * @generated
     */
    EAttribute getDBMapperTableEntry_Name();

    /**
     * Returns the meta object for the attribute '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getExpression <em>Expression</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Expression</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getExpression()
     * @see #getDBMapperTableEntry()
     * @generated
     */
    EAttribute getDBMapperTableEntry_Expression();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getType <em>Type</em>}'.
     * <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getType()
     * @see #getDBMapperTableEntry()
     * @generated
     */
    EAttribute getDBMapperTableEntry_Type();

    /**
     * Returns the meta object for the attribute '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#isNullable <em>Nullable</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Nullable</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#isNullable()
     * @see #getDBMapperTableEntry()
     * @generated
     */
    EAttribute getDBMapperTableEntry_Nullable();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#isJoin <em>Join</em>}'.
     * <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Join</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#isJoin()
     * @see #getDBMapperTableEntry()
     * @generated
     */
    EAttribute getDBMapperTableEntry_Join();

    /**
     * Returns the meta object for the attribute '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getOperator <em>Operator</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Operator</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry#getOperator()
     * @see #getDBMapperTableEntry()
     * @generated
     */
    EAttribute getDBMapperTableEntry_Operator();

    /**
     * Returns the meta object for class '{@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable <em>Abstract DB Data Map Table</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the meta object for class '<em>Abstract DB Data Map Table</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable
     * @generated
     */
    EClass getAbstractDBDataMapTable();

    /**
     * Returns the meta object for the attribute '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#getName <em>Name</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#getName()
     * @see #getAbstractDBDataMapTable()
     * @generated
     */
    EAttribute getAbstractDBDataMapTable_Name();

    /**
     * Returns the meta object for the attribute '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#isMinimized <em>Minimized</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Minimized</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#isMinimized()
     * @see #getAbstractDBDataMapTable()
     * @generated
     */
    EAttribute getAbstractDBDataMapTable_Minimized();

    /**
     * Returns the meta object for the attribute '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#isReadonly <em>Readonly</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Readonly</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#isReadonly()
     * @see #getAbstractDBDataMapTable()
     * @generated
     */
    EAttribute getAbstractDBDataMapTable_Readonly();

    /**
     * Returns the meta object for the containment reference list '{@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#getDBMapperTableEntries <em>DB Mapper Table Entries</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>DB Mapper Table Entries</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#getDBMapperTableEntries()
     * @see #getAbstractDBDataMapTable()
     * @generated
     */
    EReference getAbstractDBDataMapTable_DBMapperTableEntries();

    /**
     * Returns the meta object for the attribute '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#getTableName <em>Table Name</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Table Name</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable#getTableName()
     * @see #getAbstractDBDataMapTable()
     * @generated
     */
    EAttribute getAbstractDBDataMapTable_TableName();

    /**
     * Returns the meta object for class '{@link org.talend.designer.dbmap.model.emf.dbmap.AbstaceDBInOutTable <em>Abstace DB In Out Table</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the meta object for class '<em>Abstace DB In Out Table</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.AbstaceDBInOutTable
     * @generated
     */
    EClass getAbstaceDBInOutTable();

    /**
     * Returns the meta object for class '{@link org.talend.designer.dbmap.model.emf.dbmap.VarTable <em>Var Table</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the meta object for class '<em>Var Table</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.VarTable
     * @generated
     */
    EClass getVarTable();

    /**
     * Returns the meta object for class '{@link org.talend.designer.dbmap.model.emf.dbmap.InputTable <em>Input Table</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the meta object for class '<em>Input Table</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.InputTable
     * @generated
     */
    EClass getInputTable();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.dbmap.model.emf.dbmap.InputTable#getJoinType <em>Join Type</em>}'.
     * <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Join Type</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.InputTable#getJoinType()
     * @see #getInputTable()
     * @generated
     */
    EAttribute getInputTable_JoinType();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.dbmap.model.emf.dbmap.InputTable#getAlias <em>Alias</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Alias</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.InputTable#getAlias()
     * @see #getInputTable()
     * @generated
     */
    EAttribute getInputTable_Alias();

    /**
     * Returns the meta object for class '{@link org.talend.designer.dbmap.model.emf.dbmap.OutputTable <em>Output Table</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the meta object for class '<em>Output Table</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.OutputTable
     * @generated
     */
    EClass getOutputTable();

    /**
     * Returns the meta object for the containment reference list '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.OutputTable#getFilterEntries <em>Filter Entries</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '<em>Filter Entries</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.OutputTable#getFilterEntries()
     * @see #getOutputTable()
     * @generated
     */
    EReference getOutputTable_FilterEntries();

    /**
     * Returns the meta object for class '{@link org.talend.designer.dbmap.model.emf.dbmap.FilterEntry <em>Filter Entry</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the meta object for class '<em>Filter Entry</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.FilterEntry
     * @generated
     */
    EClass getFilterEntry();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.dbmap.model.emf.dbmap.FilterEntry#getName <em>Name</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.FilterEntry#getName()
     * @see #getFilterEntry()
     * @generated
     */
    EAttribute getFilterEntry_Name();

    /**
     * Returns the meta object for the attribute '
     * {@link org.talend.designer.dbmap.model.emf.dbmap.FilterEntry#getExpression <em>Expression</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Expression</em>'.
     * @see org.talend.designer.dbmap.model.emf.dbmap.FilterEntry#getExpression()
     * @see #getFilterEntry()
     * @generated
     */
    EAttribute getFilterEntry_Expression();

    EAttribute getFilterEntry_FilterKind();

    /**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
    DbmapFactory getDbmapFactory();

    /**
     * <!-- begin-user-doc --> Defines literals for the meta objects that represent
     * <ul>
     * <li>each class,</li>
     * <li>each feature of each class,</li>
     * <li>each enum,</li>
     * <li>and each data type</li>
     * </ul>
     * <!-- end-user-doc -->
     * @generated
     */
    interface Literals {

        /**
         * The meta object literal for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapDataImpl <em>DB Map Data</em>}' class.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapDataImpl
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getDBMapData()
         * @generated
         */
        EClass DB_MAP_DATA = eINSTANCE.getDBMapData();

        /**
         * The meta object literal for the '<em><b>Var Tables</b></em>' containment reference list feature. <!--
         * begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference DB_MAP_DATA__VAR_TABLES = eINSTANCE.getDBMapData_VarTables();

        /**
         * The meta object literal for the '<em><b>Input Tables</b></em>' containment reference list feature. <!--
         * begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference DB_MAP_DATA__INPUT_TABLES = eINSTANCE.getDBMapData_InputTables();

        /**
         * The meta object literal for the '<em><b>Output Tables</b></em>' containment reference list feature. <!--
         * begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference DB_MAP_DATA__OUTPUT_TABLES = eINSTANCE.getDBMapData_OutputTables();

        /**
         * The meta object literal for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapperTableEntryImpl <em>DB Mapper Table Entry</em>}' class.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapperTableEntryImpl
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getDBMapperTableEntry()
         * @generated
         */
        EClass DB_MAPPER_TABLE_ENTRY = eINSTANCE.getDBMapperTableEntry();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute DB_MAPPER_TABLE_ENTRY__NAME = eINSTANCE.getDBMapperTableEntry_Name();

        /**
         * The meta object literal for the '<em><b>Expression</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute DB_MAPPER_TABLE_ENTRY__EXPRESSION = eINSTANCE.getDBMapperTableEntry_Expression();

        /**
         * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute DB_MAPPER_TABLE_ENTRY__TYPE = eINSTANCE.getDBMapperTableEntry_Type();

        /**
         * The meta object literal for the '<em><b>Nullable</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute DB_MAPPER_TABLE_ENTRY__NULLABLE = eINSTANCE.getDBMapperTableEntry_Nullable();

        /**
         * The meta object literal for the '<em><b>Join</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute DB_MAPPER_TABLE_ENTRY__JOIN = eINSTANCE.getDBMapperTableEntry_Join();

        /**
         * The meta object literal for the '<em><b>Operator</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute DB_MAPPER_TABLE_ENTRY__OPERATOR = eINSTANCE.getDBMapperTableEntry_Operator();

        /**
         * The meta object literal for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.AbstractDBDataMapTableImpl <em>Abstract DB Data Map Table</em>}' class.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.AbstractDBDataMapTableImpl
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getAbstractDBDataMapTable()
         * @generated
         */
        EClass ABSTRACT_DB_DATA_MAP_TABLE = eINSTANCE.getAbstractDBDataMapTable();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_DB_DATA_MAP_TABLE__NAME = eINSTANCE.getAbstractDBDataMapTable_Name();

        /**
         * The meta object literal for the '<em><b>Minimized</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_DB_DATA_MAP_TABLE__MINIMIZED = eINSTANCE.getAbstractDBDataMapTable_Minimized();

        /**
         * The meta object literal for the '<em><b>Readonly</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_DB_DATA_MAP_TABLE__READONLY = eINSTANCE.getAbstractDBDataMapTable_Readonly();

        /**
         * The meta object literal for the '<em><b>DB Mapper Table Entries</b></em>' containment reference list feature.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * @generated
         */
        EReference ABSTRACT_DB_DATA_MAP_TABLE__DB_MAPPER_TABLE_ENTRIES = eINSTANCE.getAbstractDBDataMapTable_DBMapperTableEntries();

        /**
         * The meta object literal for the '<em><b>Table Name</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_DB_DATA_MAP_TABLE__TABLE_NAME = eINSTANCE.getAbstractDBDataMapTable_TableName();

        /**
         * The meta object literal for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.AbstaceDBInOutTableImpl <em>Abstace DB In Out Table</em>}' class.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.AbstaceDBInOutTableImpl
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getAbstaceDBInOutTable()
         * @generated
         */
        EClass ABSTACE_DB_IN_OUT_TABLE = eINSTANCE.getAbstaceDBInOutTable();

        /**
         * The meta object literal for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.VarTableImpl <em>Var Table</em>}' class.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.VarTableImpl
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getVarTable()
         * @generated
         */
        EClass VAR_TABLE = eINSTANCE.getVarTable();

        /**
         * The meta object literal for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.InputTableImpl <em>Input Table</em>}' class.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.InputTableImpl
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getInputTable()
         * @generated
         */
        EClass INPUT_TABLE = eINSTANCE.getInputTable();

        /**
         * The meta object literal for the '<em><b>Join Type</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute INPUT_TABLE__JOIN_TYPE = eINSTANCE.getInputTable_JoinType();

        /**
         * The meta object literal for the '<em><b>Alias</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute INPUT_TABLE__ALIAS = eINSTANCE.getInputTable_Alias();

        /**
         * The meta object literal for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.OutputTableImpl <em>Output Table</em>}' class.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.OutputTableImpl
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getOutputTable()
         * @generated
         */
        EClass OUTPUT_TABLE = eINSTANCE.getOutputTable();

        /**
         * The meta object literal for the '<em><b>Filter Entries</b></em>' containment reference list feature. <!--
         * begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference OUTPUT_TABLE__FILTER_ENTRIES = eINSTANCE.getOutputTable_FilterEntries();

        /**
         * The meta object literal for the '{@link org.talend.designer.dbmap.model.emf.dbmap.impl.FilterEntryImpl <em>Filter Entry</em>}' class.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.FilterEntryImpl
         * @see org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapPackageImpl#getFilterEntry()
         * @generated
         */
        EClass FILTER_ENTRY = eINSTANCE.getFilterEntry();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute FILTER_ENTRY__NAME = eINSTANCE.getFilterEntry_Name();

        /**
         * The meta object literal for the '<em><b>Expression</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * @generated
         */
        EAttribute FILTER_ENTRY__EXPRESSION = eINSTANCE.getFilterEntry_Expression();

        EAttribute FILTER_ENTRY__FILTER_KIND = eINSTANCE.getFilterEntry_FilterKind();

    }

} // DbmapPackage
