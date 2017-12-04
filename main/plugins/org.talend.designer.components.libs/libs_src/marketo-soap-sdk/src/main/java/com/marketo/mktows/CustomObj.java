
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour CustomObj complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="CustomObj">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="customObjKeyList" type="{http://www.marketo.com/mktows/}ArrayOfAttribute" minOccurs="0"/>
 *         &lt;element name="customObjAttributeList" type="{http://www.marketo.com/mktows/}ArrayOfAttribute" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustomObj", propOrder = {
    "customObjKeyList",
    "customObjAttributeList"
})
public class CustomObj {

    @XmlElementRef(name = "customObjKeyList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfAttribute> customObjKeyList;
    @XmlElementRef(name = "customObjAttributeList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfAttribute> customObjAttributeList;

    /**
     * Obtient la valeur de la propriété customObjKeyList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}
     *     
     */
    public JAXBElement<ArrayOfAttribute> getCustomObjKeyList() {
        return customObjKeyList;
    }

    /**
     * Définit la valeur de la propriété customObjKeyList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}
     *     
     */
    public void setCustomObjKeyList(JAXBElement<ArrayOfAttribute> value) {
        this.customObjKeyList = value;
    }

    /**
     * Obtient la valeur de la propriété customObjAttributeList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}
     *     
     */
    public JAXBElement<ArrayOfAttribute> getCustomObjAttributeList() {
        return customObjAttributeList;
    }

    /**
     * Définit la valeur de la propriété customObjAttributeList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}
     *     
     */
    public void setCustomObjAttributeList(JAXBElement<ArrayOfAttribute> value) {
        this.customObjAttributeList = value;
    }

}
