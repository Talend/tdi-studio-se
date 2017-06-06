/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.mapper.model.emf.mapper;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage;

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
 * @see org.talend.designer.mapper.model.emf.mapper.MapperFactory
 * @model kind="package"
 * @generated
 */
public interface MapperPackage extends EPackage {
    /**
     * The package name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNAME = "mapper";

    /**
     * The package namespace URI.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_URI = "http://www.talend.org/mapper";

    /**
     * The package namespace name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_PREFIX = "TalendMapper";

    /**
     * The singleton instance of the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    MapperPackage eINSTANCE = org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl.init();

    /**
     * The meta object id for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.MapperDataImpl <em>Data</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperDataImpl
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getMapperData()
     * @generated
     */
    int MAPPER_DATA = 0;

    /**
     * The feature id for the '<em><b>Ui Properties</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MAPPER_DATA__UI_PROPERTIES = TalendFilePackage.ABSTRACT_EXTERNAL_DATA_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Var Tables</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MAPPER_DATA__VAR_TABLES = TalendFilePackage.ABSTRACT_EXTERNAL_DATA_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Output Tables</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MAPPER_DATA__OUTPUT_TABLES = TalendFilePackage.ABSTRACT_EXTERNAL_DATA_FEATURE_COUNT + 2;

    /**
     * The feature id for the '<em><b>Input Tables</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MAPPER_DATA__INPUT_TABLES = TalendFilePackage.ABSTRACT_EXTERNAL_DATA_FEATURE_COUNT + 3;

    /**
     * The number of structural features of the '<em>Data</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MAPPER_DATA_FEATURE_COUNT = TalendFilePackage.ABSTRACT_EXTERNAL_DATA_FEATURE_COUNT + 4;

    /**
     * The meta object id for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.MapperTableEntryImpl <em>Table Entry</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperTableEntryImpl
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getMapperTableEntry()
     * @generated
     */
    int MAPPER_TABLE_ENTRY = 1;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MAPPER_TABLE_ENTRY__NAME = 0;

    /**
     * The feature id for the '<em><b>Expression</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MAPPER_TABLE_ENTRY__EXPRESSION = 1;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MAPPER_TABLE_ENTRY__TYPE = 2;

    /**
     * The feature id for the '<em><b>Nullable</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MAPPER_TABLE_ENTRY__NULLABLE = 3;

    /**
     * The feature id for the '<em><b>Operator</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MAPPER_TABLE_ENTRY__OPERATOR = 4;

    /**
     * The number of structural features of the '<em>Table Entry</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MAPPER_TABLE_ENTRY_FEATURE_COUNT = 5;

    /**
     * The meta object id for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractDataMapTableImpl <em>Abstract Data Map Table</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.mapper.model.emf.mapper.impl.AbstractDataMapTableImpl
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getAbstractDataMapTable()
     * @generated
     */
    int ABSTRACT_DATA_MAP_TABLE = 2;

    /**
     * The feature id for the '<em><b>Size State</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_DATA_MAP_TABLE__SIZE_STATE = 0;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_DATA_MAP_TABLE__NAME = 1;

    /**
     * The feature id for the '<em><b>Minimized</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_DATA_MAP_TABLE__MINIMIZED = 2;

    /**
     * The feature id for the '<em><b>Mapper Table Entries</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_DATA_MAP_TABLE__MAPPER_TABLE_ENTRIES = 3;

    /**
     * The number of structural features of the '<em>Abstract Data Map Table</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_DATA_MAP_TABLE_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractInOutTableImpl <em>Abstract In Out Table</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.mapper.model.emf.mapper.impl.AbstractInOutTableImpl
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getAbstractInOutTable()
     * @generated
     */
    int ABSTRACT_IN_OUT_TABLE = 3;

    /**
     * The feature id for the '<em><b>Size State</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_IN_OUT_TABLE__SIZE_STATE = ABSTRACT_DATA_MAP_TABLE__SIZE_STATE;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_IN_OUT_TABLE__NAME = ABSTRACT_DATA_MAP_TABLE__NAME;

    /**
     * The feature id for the '<em><b>Minimized</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_IN_OUT_TABLE__MINIMIZED = ABSTRACT_DATA_MAP_TABLE__MINIMIZED;

    /**
     * The feature id for the '<em><b>Mapper Table Entries</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_IN_OUT_TABLE__MAPPER_TABLE_ENTRIES = ABSTRACT_DATA_MAP_TABLE__MAPPER_TABLE_ENTRIES;

    /**
     * The feature id for the '<em><b>Expression Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_IN_OUT_TABLE__EXPRESSION_FILTER = ABSTRACT_DATA_MAP_TABLE_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Activate Expression Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_IN_OUT_TABLE__ACTIVATE_EXPRESSION_FILTER = ABSTRACT_DATA_MAP_TABLE_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Activate Condensed Tool</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_IN_OUT_TABLE__ACTIVATE_CONDENSED_TOOL = ABSTRACT_DATA_MAP_TABLE_FEATURE_COUNT + 2;

    /**
     * The feature id for the '<em><b>Activate Column Name Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_IN_OUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER = ABSTRACT_DATA_MAP_TABLE_FEATURE_COUNT + 3;

    /**
     * The feature id for the '<em><b>Column Name Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_IN_OUT_TABLE__COLUMN_NAME_FILTER = ABSTRACT_DATA_MAP_TABLE_FEATURE_COUNT + 4;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_IN_OUT_TABLE__ID = ABSTRACT_DATA_MAP_TABLE_FEATURE_COUNT + 5;

    /**
     * The number of structural features of the '<em>Abstract In Out Table</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT = ABSTRACT_DATA_MAP_TABLE_FEATURE_COUNT + 6;

    /**
     * The meta object id for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.UiPropertiesImpl <em>Ui Properties</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.mapper.model.emf.mapper.impl.UiPropertiesImpl
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getUiProperties()
     * @generated
     */
    int UI_PROPERTIES = 4;

    /**
     * The feature id for the '<em><b>Shell Maximized</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int UI_PROPERTIES__SHELL_MAXIMIZED = 0;

    /**
     * The number of structural features of the '<em>Ui Properties</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int UI_PROPERTIES_FEATURE_COUNT = 1;

    /**
     * The meta object id for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.VarTableImpl <em>Var Table</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.mapper.model.emf.mapper.impl.VarTableImpl
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getVarTable()
     * @generated
     */
    int VAR_TABLE = 5;

    /**
     * The feature id for the '<em><b>Size State</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VAR_TABLE__SIZE_STATE = ABSTRACT_DATA_MAP_TABLE__SIZE_STATE;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VAR_TABLE__NAME = ABSTRACT_DATA_MAP_TABLE__NAME;

    /**
     * The feature id for the '<em><b>Minimized</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VAR_TABLE__MINIMIZED = ABSTRACT_DATA_MAP_TABLE__MINIMIZED;

    /**
     * The feature id for the '<em><b>Mapper Table Entries</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VAR_TABLE__MAPPER_TABLE_ENTRIES = ABSTRACT_DATA_MAP_TABLE__MAPPER_TABLE_ENTRIES;

    /**
     * The number of structural features of the '<em>Var Table</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int VAR_TABLE_FEATURE_COUNT = ABSTRACT_DATA_MAP_TABLE_FEATURE_COUNT + 0;

    /**
     * The meta object id for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.OutputTableImpl <em>Output Table</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.mapper.model.emf.mapper.impl.OutputTableImpl
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getOutputTable()
     * @generated
     */
    int OUTPUT_TABLE = 6;

    /**
     * The feature id for the '<em><b>Size State</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__SIZE_STATE = ABSTRACT_IN_OUT_TABLE__SIZE_STATE;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__NAME = ABSTRACT_IN_OUT_TABLE__NAME;

    /**
     * The feature id for the '<em><b>Minimized</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__MINIMIZED = ABSTRACT_IN_OUT_TABLE__MINIMIZED;

    /**
     * The feature id for the '<em><b>Mapper Table Entries</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__MAPPER_TABLE_ENTRIES = ABSTRACT_IN_OUT_TABLE__MAPPER_TABLE_ENTRIES;

    /**
     * The feature id for the '<em><b>Expression Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__EXPRESSION_FILTER = ABSTRACT_IN_OUT_TABLE__EXPRESSION_FILTER;

    /**
     * The feature id for the '<em><b>Activate Expression Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__ACTIVATE_EXPRESSION_FILTER = ABSTRACT_IN_OUT_TABLE__ACTIVATE_EXPRESSION_FILTER;

    /**
     * The feature id for the '<em><b>Activate Condensed Tool</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__ACTIVATE_CONDENSED_TOOL = ABSTRACT_IN_OUT_TABLE__ACTIVATE_CONDENSED_TOOL;

    /**
     * The feature id for the '<em><b>Activate Column Name Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER = ABSTRACT_IN_OUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER;

    /**
     * The feature id for the '<em><b>Column Name Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__COLUMN_NAME_FILTER = ABSTRACT_IN_OUT_TABLE__COLUMN_NAME_FILTER;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__ID = ABSTRACT_IN_OUT_TABLE__ID;

    /**
     * The feature id for the '<em><b>Reject</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__REJECT = ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Reject Inner Join</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__REJECT_INNER_JOIN = ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Is Error Reject Table</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__IS_ERROR_REJECT_TABLE = ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT + 2;

    /**
     * The feature id for the '<em><b>Is Join Table Of</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE__IS_JOIN_TABLE_OF = ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT + 3;

    /**
     * The number of structural features of the '<em>Output Table</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OUTPUT_TABLE_FEATURE_COUNT = ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT + 4;

    /**
     * The meta object id for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.InputTableImpl <em>Input Table</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.mapper.model.emf.mapper.impl.InputTableImpl
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getInputTable()
     * @generated
     */
    int INPUT_TABLE = 7;

    /**
     * The feature id for the '<em><b>Size State</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__SIZE_STATE = ABSTRACT_IN_OUT_TABLE__SIZE_STATE;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__NAME = ABSTRACT_IN_OUT_TABLE__NAME;

    /**
     * The feature id for the '<em><b>Minimized</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__MINIMIZED = ABSTRACT_IN_OUT_TABLE__MINIMIZED;

    /**
     * The feature id for the '<em><b>Mapper Table Entries</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__MAPPER_TABLE_ENTRIES = ABSTRACT_IN_OUT_TABLE__MAPPER_TABLE_ENTRIES;

    /**
     * The feature id for the '<em><b>Expression Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__EXPRESSION_FILTER = ABSTRACT_IN_OUT_TABLE__EXPRESSION_FILTER;

    /**
     * The feature id for the '<em><b>Activate Expression Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__ACTIVATE_EXPRESSION_FILTER = ABSTRACT_IN_OUT_TABLE__ACTIVATE_EXPRESSION_FILTER;

    /**
     * The feature id for the '<em><b>Activate Condensed Tool</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__ACTIVATE_CONDENSED_TOOL = ABSTRACT_IN_OUT_TABLE__ACTIVATE_CONDENSED_TOOL;

    /**
     * The feature id for the '<em><b>Activate Column Name Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER = ABSTRACT_IN_OUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER;

    /**
     * The feature id for the '<em><b>Column Name Filter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__COLUMN_NAME_FILTER = ABSTRACT_IN_OUT_TABLE__COLUMN_NAME_FILTER;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__ID = ABSTRACT_IN_OUT_TABLE__ID;

    /**
     * The feature id for the '<em><b>Matching Mode</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__MATCHING_MODE = ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Lookup Mode</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__LOOKUP_MODE = ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Global Map Keys Values</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__GLOBAL_MAP_KEYS_VALUES = ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT + 2;

    /**
     * The feature id for the '<em><b>Inner Join</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__INNER_JOIN = ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT + 3;

    /**
     * The feature id for the '<em><b>Persistent</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE__PERSISTENT = ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT + 4;

    /**
     * The number of structural features of the '<em>Input Table</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int INPUT_TABLE_FEATURE_COUNT = ABSTRACT_IN_OUT_TABLE_FEATURE_COUNT + 5;

    /**
     * The meta object id for the '{@link org.talend.designer.mapper.model.emf.mapper.SizeState <em>Size State</em>}' enum.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.mapper.model.emf.mapper.SizeState
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getSizeState()
     * @generated
     */
    int SIZE_STATE = 8;

    /**
     * The meta object id for the '{@link org.talend.designer.mapper.model.emf.mapper.Operator <em>Operator</em>}' enum.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.talend.designer.mapper.model.emf.mapper.Operator
     * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getOperator()
     * @generated
     */
    int OPERATOR = 9;

    /**
     * Returns the meta object for class '{@link org.talend.designer.mapper.model.emf.mapper.MapperData <em>Data</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Data</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.MapperData
     * @generated
     */
    EClass getMapperData();

    /**
     * Returns the meta object for the containment reference '{@link org.talend.designer.mapper.model.emf.mapper.MapperData#getUiProperties <em>Ui Properties</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Ui Properties</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.MapperData#getUiProperties()
     * @see #getMapperData()
     * @generated
     */
    EReference getMapperData_UiProperties();

    /**
     * Returns the meta object for the containment reference list '{@link org.talend.designer.mapper.model.emf.mapper.MapperData#getVarTables <em>Var Tables</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Var Tables</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.MapperData#getVarTables()
     * @see #getMapperData()
     * @generated
     */
    EReference getMapperData_VarTables();

    /**
     * Returns the meta object for the containment reference list '{@link org.talend.designer.mapper.model.emf.mapper.MapperData#getOutputTables <em>Output Tables</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Output Tables</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.MapperData#getOutputTables()
     * @see #getMapperData()
     * @generated
     */
    EReference getMapperData_OutputTables();

    /**
     * Returns the meta object for the containment reference list '{@link org.talend.designer.mapper.model.emf.mapper.MapperData#getInputTables <em>Input Tables</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Input Tables</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.MapperData#getInputTables()
     * @see #getMapperData()
     * @generated
     */
    EReference getMapperData_InputTables();

    /**
     * Returns the meta object for class '{@link org.talend.designer.mapper.model.emf.mapper.MapperTableEntry <em>Table Entry</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Table Entry</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.MapperTableEntry
     * @generated
     */
    EClass getMapperTableEntry();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.MapperTableEntry#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.MapperTableEntry#getName()
     * @see #getMapperTableEntry()
     * @generated
     */
    EAttribute getMapperTableEntry_Name();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.MapperTableEntry#getExpression <em>Expression</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Expression</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.MapperTableEntry#getExpression()
     * @see #getMapperTableEntry()
     * @generated
     */
    EAttribute getMapperTableEntry_Expression();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.MapperTableEntry#getType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.MapperTableEntry#getType()
     * @see #getMapperTableEntry()
     * @generated
     */
    EAttribute getMapperTableEntry_Type();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.MapperTableEntry#isNullable <em>Nullable</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Nullable</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.MapperTableEntry#isNullable()
     * @see #getMapperTableEntry()
     * @generated
     */
    EAttribute getMapperTableEntry_Nullable();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.MapperTableEntry#getOperator <em>Operator</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Operator</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.MapperTableEntry#getOperator()
     * @see #getMapperTableEntry()
     * @generated
     */
    EAttribute getMapperTableEntry_Operator();

    /**
     * Returns the meta object for class '{@link org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable <em>Abstract Data Map Table</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Abstract Data Map Table</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable
     * @generated
     */
    EClass getAbstractDataMapTable();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable#getSizeState <em>Size State</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Size State</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable#getSizeState()
     * @see #getAbstractDataMapTable()
     * @generated
     */
    EAttribute getAbstractDataMapTable_SizeState();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable#getName()
     * @see #getAbstractDataMapTable()
     * @generated
     */
    EAttribute getAbstractDataMapTable_Name();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable#isMinimized <em>Minimized</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Minimized</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable#isMinimized()
     * @see #getAbstractDataMapTable()
     * @generated
     */
    EAttribute getAbstractDataMapTable_Minimized();

    /**
     * Returns the meta object for the containment reference list '{@link org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable#getMapperTableEntries <em>Mapper Table Entries</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Mapper Table Entries</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable#getMapperTableEntries()
     * @see #getAbstractDataMapTable()
     * @generated
     */
    EReference getAbstractDataMapTable_MapperTableEntries();

    /**
     * Returns the meta object for class '{@link org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable <em>Abstract In Out Table</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Abstract In Out Table</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable
     * @generated
     */
    EClass getAbstractInOutTable();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#getExpressionFilter <em>Expression Filter</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Expression Filter</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#getExpressionFilter()
     * @see #getAbstractInOutTable()
     * @generated
     */
    EAttribute getAbstractInOutTable_ExpressionFilter();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#isActivateExpressionFilter <em>Activate Expression Filter</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Activate Expression Filter</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#isActivateExpressionFilter()
     * @see #getAbstractInOutTable()
     * @generated
     */
    EAttribute getAbstractInOutTable_ActivateExpressionFilter();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#isActivateCondensedTool <em>Activate Condensed Tool</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Activate Condensed Tool</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#isActivateCondensedTool()
     * @see #getAbstractInOutTable()
     * @generated
     */
    EAttribute getAbstractInOutTable_ActivateCondensedTool();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#isActivateColumnNameFilter <em>Activate Column Name Filter</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Activate Column Name Filter</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#isActivateColumnNameFilter()
     * @see #getAbstractInOutTable()
     * @generated
     */
    EAttribute getAbstractInOutTable_ActivateColumnNameFilter();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#getColumnNameFilter <em>Column Name Filter</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Column Name Filter</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#getColumnNameFilter()
     * @see #getAbstractInOutTable()
     * @generated
     */
    EAttribute getAbstractInOutTable_ColumnNameFilter();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#getId <em>Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Id</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable#getId()
     * @see #getAbstractInOutTable()
     * @generated
     */
    EAttribute getAbstractInOutTable_Id();

    /**
     * Returns the meta object for class '{@link org.talend.designer.mapper.model.emf.mapper.UiProperties <em>Ui Properties</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Ui Properties</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.UiProperties
     * @generated
     */
    EClass getUiProperties();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.UiProperties#isShellMaximized <em>Shell Maximized</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Shell Maximized</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.UiProperties#isShellMaximized()
     * @see #getUiProperties()
     * @generated
     */
    EAttribute getUiProperties_ShellMaximized();

    /**
     * Returns the meta object for class '{@link org.talend.designer.mapper.model.emf.mapper.VarTable <em>Var Table</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Var Table</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.VarTable
     * @generated
     */
    EClass getVarTable();

    /**
     * Returns the meta object for class '{@link org.talend.designer.mapper.model.emf.mapper.OutputTable <em>Output Table</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Output Table</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.OutputTable
     * @generated
     */
    EClass getOutputTable();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.OutputTable#isReject <em>Reject</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Reject</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.OutputTable#isReject()
     * @see #getOutputTable()
     * @generated
     */
    EAttribute getOutputTable_Reject();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.OutputTable#isRejectInnerJoin <em>Reject Inner Join</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Reject Inner Join</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.OutputTable#isRejectInnerJoin()
     * @see #getOutputTable()
     * @generated
     */
    EAttribute getOutputTable_RejectInnerJoin();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.OutputTable#isIsErrorRejectTable <em>Is Error Reject Table</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Is Error Reject Table</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.OutputTable#isIsErrorRejectTable()
     * @see #getOutputTable()
     * @generated
     */
    EAttribute getOutputTable_IsErrorRejectTable();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.OutputTable#getIsJoinTableOf <em>Is Join Table Of</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Is Join Table Of</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.OutputTable#getIsJoinTableOf()
     * @see #getOutputTable()
     * @generated
     */
    EAttribute getOutputTable_IsJoinTableOf();

    /**
     * Returns the meta object for class '{@link org.talend.designer.mapper.model.emf.mapper.InputTable <em>Input Table</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Input Table</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.InputTable
     * @generated
     */
    EClass getInputTable();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.InputTable#getMatchingMode <em>Matching Mode</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Matching Mode</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.InputTable#getMatchingMode()
     * @see #getInputTable()
     * @generated
     */
    EAttribute getInputTable_MatchingMode();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.InputTable#getLookupMode <em>Lookup Mode</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Lookup Mode</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.InputTable#getLookupMode()
     * @see #getInputTable()
     * @generated
     */
    EAttribute getInputTable_LookupMode();

    /**
     * Returns the meta object for the containment reference list '{@link org.talend.designer.mapper.model.emf.mapper.InputTable#getGlobalMapKeysValues <em>Global Map Keys Values</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Global Map Keys Values</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.InputTable#getGlobalMapKeysValues()
     * @see #getInputTable()
     * @generated
     */
    EReference getInputTable_GlobalMapKeysValues();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.InputTable#isInnerJoin <em>Inner Join</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Inner Join</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.InputTable#isInnerJoin()
     * @see #getInputTable()
     * @generated
     */
    EAttribute getInputTable_InnerJoin();

    /**
     * Returns the meta object for the attribute '{@link org.talend.designer.mapper.model.emf.mapper.InputTable#isPersistent <em>Persistent</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Persistent</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.InputTable#isPersistent()
     * @see #getInputTable()
     * @generated
     */
    EAttribute getInputTable_Persistent();

    /**
     * Returns the meta object for enum '{@link org.talend.designer.mapper.model.emf.mapper.SizeState <em>Size State</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for enum '<em>Size State</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.SizeState
     * @generated
     */
    EEnum getSizeState();

    /**
     * Returns the meta object for enum '{@link org.talend.designer.mapper.model.emf.mapper.Operator <em>Operator</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for enum '<em>Operator</em>'.
     * @see org.talend.designer.mapper.model.emf.mapper.Operator
     * @generated
     */
    EEnum getOperator();

    /**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
    MapperFactory getMapperFactory();

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
         * The meta object literal for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.MapperDataImpl <em>Data</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperDataImpl
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getMapperData()
         * @generated
         */
        EClass MAPPER_DATA = eINSTANCE.getMapperData();

        /**
         * The meta object literal for the '<em><b>Ui Properties</b></em>' containment reference feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference MAPPER_DATA__UI_PROPERTIES = eINSTANCE.getMapperData_UiProperties();

        /**
         * The meta object literal for the '<em><b>Var Tables</b></em>' containment reference list feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference MAPPER_DATA__VAR_TABLES = eINSTANCE.getMapperData_VarTables();

        /**
         * The meta object literal for the '<em><b>Output Tables</b></em>' containment reference list feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference MAPPER_DATA__OUTPUT_TABLES = eINSTANCE.getMapperData_OutputTables();

        /**
         * The meta object literal for the '<em><b>Input Tables</b></em>' containment reference list feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference MAPPER_DATA__INPUT_TABLES = eINSTANCE.getMapperData_InputTables();

        /**
         * The meta object literal for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.MapperTableEntryImpl <em>Table Entry</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperTableEntryImpl
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getMapperTableEntry()
         * @generated
         */
        EClass MAPPER_TABLE_ENTRY = eINSTANCE.getMapperTableEntry();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute MAPPER_TABLE_ENTRY__NAME = eINSTANCE.getMapperTableEntry_Name();

        /**
         * The meta object literal for the '<em><b>Expression</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute MAPPER_TABLE_ENTRY__EXPRESSION = eINSTANCE.getMapperTableEntry_Expression();

        /**
         * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute MAPPER_TABLE_ENTRY__TYPE = eINSTANCE.getMapperTableEntry_Type();

        /**
         * The meta object literal for the '<em><b>Nullable</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute MAPPER_TABLE_ENTRY__NULLABLE = eINSTANCE.getMapperTableEntry_Nullable();

        /**
         * The meta object literal for the '<em><b>Operator</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute MAPPER_TABLE_ENTRY__OPERATOR = eINSTANCE.getMapperTableEntry_Operator();

        /**
         * The meta object literal for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractDataMapTableImpl <em>Abstract Data Map Table</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.mapper.model.emf.mapper.impl.AbstractDataMapTableImpl
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getAbstractDataMapTable()
         * @generated
         */
        EClass ABSTRACT_DATA_MAP_TABLE = eINSTANCE.getAbstractDataMapTable();

        /**
         * The meta object literal for the '<em><b>Size State</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_DATA_MAP_TABLE__SIZE_STATE = eINSTANCE.getAbstractDataMapTable_SizeState();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_DATA_MAP_TABLE__NAME = eINSTANCE.getAbstractDataMapTable_Name();

        /**
         * The meta object literal for the '<em><b>Minimized</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_DATA_MAP_TABLE__MINIMIZED = eINSTANCE.getAbstractDataMapTable_Minimized();

        /**
         * The meta object literal for the '<em><b>Mapper Table Entries</b></em>' containment reference list feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference ABSTRACT_DATA_MAP_TABLE__MAPPER_TABLE_ENTRIES = eINSTANCE.getAbstractDataMapTable_MapperTableEntries();

        /**
         * The meta object literal for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractInOutTableImpl <em>Abstract In Out Table</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.mapper.model.emf.mapper.impl.AbstractInOutTableImpl
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getAbstractInOutTable()
         * @generated
         */
        EClass ABSTRACT_IN_OUT_TABLE = eINSTANCE.getAbstractInOutTable();

        /**
         * The meta object literal for the '<em><b>Expression Filter</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_IN_OUT_TABLE__EXPRESSION_FILTER = eINSTANCE.getAbstractInOutTable_ExpressionFilter();

        /**
         * The meta object literal for the '<em><b>Activate Expression Filter</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_IN_OUT_TABLE__ACTIVATE_EXPRESSION_FILTER = eINSTANCE.getAbstractInOutTable_ActivateExpressionFilter();

        /**
         * The meta object literal for the '<em><b>Activate Condensed Tool</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_IN_OUT_TABLE__ACTIVATE_CONDENSED_TOOL = eINSTANCE.getAbstractInOutTable_ActivateCondensedTool();

        /**
         * The meta object literal for the '<em><b>Activate Column Name Filter</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_IN_OUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER = eINSTANCE.getAbstractInOutTable_ActivateColumnNameFilter();

        /**
         * The meta object literal for the '<em><b>Column Name Filter</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_IN_OUT_TABLE__COLUMN_NAME_FILTER = eINSTANCE.getAbstractInOutTable_ColumnNameFilter();

        /**
         * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute ABSTRACT_IN_OUT_TABLE__ID = eINSTANCE.getAbstractInOutTable_Id();

        /**
         * The meta object literal for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.UiPropertiesImpl <em>Ui Properties</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.mapper.model.emf.mapper.impl.UiPropertiesImpl
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getUiProperties()
         * @generated
         */
        EClass UI_PROPERTIES = eINSTANCE.getUiProperties();

        /**
         * The meta object literal for the '<em><b>Shell Maximized</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute UI_PROPERTIES__SHELL_MAXIMIZED = eINSTANCE.getUiProperties_ShellMaximized();

        /**
         * The meta object literal for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.VarTableImpl <em>Var Table</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.mapper.model.emf.mapper.impl.VarTableImpl
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getVarTable()
         * @generated
         */
        EClass VAR_TABLE = eINSTANCE.getVarTable();

        /**
         * The meta object literal for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.OutputTableImpl <em>Output Table</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.mapper.model.emf.mapper.impl.OutputTableImpl
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getOutputTable()
         * @generated
         */
        EClass OUTPUT_TABLE = eINSTANCE.getOutputTable();

        /**
         * The meta object literal for the '<em><b>Reject</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute OUTPUT_TABLE__REJECT = eINSTANCE.getOutputTable_Reject();

        /**
         * The meta object literal for the '<em><b>Reject Inner Join</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute OUTPUT_TABLE__REJECT_INNER_JOIN = eINSTANCE.getOutputTable_RejectInnerJoin();

        /**
         * The meta object literal for the '<em><b>Is Error Reject Table</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute OUTPUT_TABLE__IS_ERROR_REJECT_TABLE = eINSTANCE.getOutputTable_IsErrorRejectTable();

        /**
         * The meta object literal for the '<em><b>Is Join Table Of</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute OUTPUT_TABLE__IS_JOIN_TABLE_OF = eINSTANCE.getOutputTable_IsJoinTableOf();

        /**
         * The meta object literal for the '{@link org.talend.designer.mapper.model.emf.mapper.impl.InputTableImpl <em>Input Table</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.mapper.model.emf.mapper.impl.InputTableImpl
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getInputTable()
         * @generated
         */
        EClass INPUT_TABLE = eINSTANCE.getInputTable();

        /**
         * The meta object literal for the '<em><b>Matching Mode</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute INPUT_TABLE__MATCHING_MODE = eINSTANCE.getInputTable_MatchingMode();

        /**
         * The meta object literal for the '<em><b>Lookup Mode</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute INPUT_TABLE__LOOKUP_MODE = eINSTANCE.getInputTable_LookupMode();

        /**
         * The meta object literal for the '<em><b>Global Map Keys Values</b></em>' containment reference list feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference INPUT_TABLE__GLOBAL_MAP_KEYS_VALUES = eINSTANCE.getInputTable_GlobalMapKeysValues();

        /**
         * The meta object literal for the '<em><b>Inner Join</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute INPUT_TABLE__INNER_JOIN = eINSTANCE.getInputTable_InnerJoin();

        /**
         * The meta object literal for the '<em><b>Persistent</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute INPUT_TABLE__PERSISTENT = eINSTANCE.getInputTable_Persistent();

        /**
         * The meta object literal for the '{@link org.talend.designer.mapper.model.emf.mapper.SizeState <em>Size State</em>}' enum.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.mapper.model.emf.mapper.SizeState
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getSizeState()
         * @generated
         */
        EEnum SIZE_STATE = eINSTANCE.getSizeState();

        /**
         * The meta object literal for the '{@link org.talend.designer.mapper.model.emf.mapper.Operator <em>Operator</em>}' enum.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see org.talend.designer.mapper.model.emf.mapper.Operator
         * @see org.talend.designer.mapper.model.emf.mapper.impl.MapperPackageImpl#getOperator()
         * @generated
         */
        EEnum OPERATOR = eINSTANCE.getOperator();

    }

} //MapperPackage
