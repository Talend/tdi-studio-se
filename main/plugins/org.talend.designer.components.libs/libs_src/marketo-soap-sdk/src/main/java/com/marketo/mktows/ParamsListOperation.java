
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsListOperation complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsListOperation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="listOperation" type="{http://www.marketo.com/mktows/}ListOperationType"/>
 *         &lt;element name="listKey" type="{http://www.marketo.com/mktows/}ListKey"/>
 *         &lt;element name="listMemberList" type="{http://www.marketo.com/mktows/}ArrayOfLeadKey"/>
 *         &lt;element name="strict" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsListOperation", propOrder = {
    "listOperation",
    "listKey",
    "listMemberList",
    "strict"
})
@XmlRootElement(name = "paramsListOperation")
public class ParamsListOperation {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ListOperationType listOperation;
    @XmlElement(required = true)
    protected ListKey listKey;
    @XmlElement(required = true)
    protected ArrayOfLeadKey listMemberList;
    @XmlElementRef(name = "strict", type = JAXBElement.class, required = false)
    protected JAXBElement<Boolean> strict;

    /**
     * Obtient la valeur de la propriété listOperation.
     * 
     * @return
     *     possible object is
     *     {@link ListOperationType }
     *     
     */
    public ListOperationType getListOperation() {
        return listOperation;
    }

    /**
     * Définit la valeur de la propriété listOperation.
     * 
     * @param value
     *     allowed object is
     *     {@link ListOperationType }
     *     
     */
    public void setListOperation(ListOperationType value) {
        this.listOperation = value;
    }

    /**
     * Obtient la valeur de la propriété listKey.
     * 
     * @return
     *     possible object is
     *     {@link ListKey }
     *     
     */
    public ListKey getListKey() {
        return listKey;
    }

    /**
     * Définit la valeur de la propriété listKey.
     * 
     * @param value
     *     allowed object is
     *     {@link ListKey }
     *     
     */
    public void setListKey(ListKey value) {
        this.listKey = value;
    }

    /**
     * Obtient la valeur de la propriété listMemberList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfLeadKey }
     *     
     */
    public ArrayOfLeadKey getListMemberList() {
        return listMemberList;
    }

    /**
     * Définit la valeur de la propriété listMemberList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfLeadKey }
     *     
     */
    public void setListMemberList(ArrayOfLeadKey value) {
        this.listMemberList = value;
    }

    /**
     * Obtient la valeur de la propriété strict.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getStrict() {
        return strict;
    }

    /**
     * Définit la valeur de la propriété strict.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setStrict(JAXBElement<Boolean> value) {
        this.strict = value;
    }

}
