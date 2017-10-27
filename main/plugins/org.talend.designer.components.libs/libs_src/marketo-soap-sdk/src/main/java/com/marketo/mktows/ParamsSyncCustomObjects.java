
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsSyncCustomObjects complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsSyncCustomObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objTypeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="customObjList" type="{http://www.marketo.com/mktows/}ArrayOfCustomObj"/>
 *         &lt;element name="operation" type="{http://www.marketo.com/mktows/}SyncOperationEnum" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsSyncCustomObjects", propOrder = {
    "objTypeName",
    "customObjList",
    "operation"
})
@XmlRootElement(name = "paramsSyncCustomObjects")
public class ParamsSyncCustomObjects {

    @XmlElement(required = true)
    protected String objTypeName;
    @XmlElement(required = true)
    protected ArrayOfCustomObj customObjList;
    @XmlElementRef(name = "operation", type = JAXBElement.class, required = false)
    protected JAXBElement<SyncOperationEnum> operation;

    /**
     * Obtient la valeur de la propriété objTypeName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjTypeName() {
        return objTypeName;
    }

    /**
     * Définit la valeur de la propriété objTypeName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjTypeName(String value) {
        this.objTypeName = value;
    }

    /**
     * Obtient la valeur de la propriété customObjList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfCustomObj }
     *     
     */
    public ArrayOfCustomObj getCustomObjList() {
        return customObjList;
    }

    /**
     * Définit la valeur de la propriété customObjList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfCustomObj }
     *     
     */
    public void setCustomObjList(ArrayOfCustomObj value) {
        this.customObjList = value;
    }

    /**
     * Obtient la valeur de la propriété operation.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link SyncOperationEnum }{@code >}
     *     
     */
    public JAXBElement<SyncOperationEnum> getOperation() {
        return operation;
    }

    /**
     * Définit la valeur de la propriété operation.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link SyncOperationEnum }{@code >}
     *     
     */
    public void setOperation(JAXBElement<SyncOperationEnum> value) {
        this.operation = value;
    }

}
