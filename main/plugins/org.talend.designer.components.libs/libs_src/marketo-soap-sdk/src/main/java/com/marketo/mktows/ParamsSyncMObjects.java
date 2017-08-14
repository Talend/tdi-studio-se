
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsSyncMObjects complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsSyncMObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mObjectList" type="{http://www.marketo.com/mktows/}ArrayOfMObject"/>
 *         &lt;element name="operation" type="{http://www.marketo.com/mktows/}SyncOperationEnum"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsSyncMObjects", propOrder = {
    "mObjectList",
    "operation"
})
@XmlRootElement(name = "paramsSyncMObjects")
public class ParamsSyncMObjects {

    @XmlElement(required = true)
    protected ArrayOfMObject mObjectList;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected SyncOperationEnum operation;

    /**
     * Obtient la valeur de la propriété mObjectList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMObject }
     *     
     */
    public ArrayOfMObject getMObjectList() {
        return mObjectList;
    }

    /**
     * Définit la valeur de la propriété mObjectList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMObject }
     *     
     */
    public void setMObjectList(ArrayOfMObject value) {
        this.mObjectList = value;
    }

    /**
     * Obtient la valeur de la propriété operation.
     * 
     * @return
     *     possible object is
     *     {@link SyncOperationEnum }
     *     
     */
    public SyncOperationEnum getOperation() {
        return operation;
    }

    /**
     * Définit la valeur de la propriété operation.
     * 
     * @param value
     *     allowed object is
     *     {@link SyncOperationEnum }
     *     
     */
    public void setOperation(SyncOperationEnum value) {
        this.operation = value;
    }

}
