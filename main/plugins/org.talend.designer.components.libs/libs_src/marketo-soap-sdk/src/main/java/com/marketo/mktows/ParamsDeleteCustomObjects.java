
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsDeleteCustomObjects complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsDeleteCustomObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objTypeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="customObjKeyLists" type="{http://www.marketo.com/mktows/}ArrayOfKeyList"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsDeleteCustomObjects", propOrder = {
    "objTypeName",
    "customObjKeyLists"
})
@XmlRootElement(name = "paramsDeleteCustomObjects")
public class ParamsDeleteCustomObjects {

    @XmlElement(required = true)
    protected String objTypeName;
    @XmlElement(required = true)
    protected ArrayOfKeyList customObjKeyLists;

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
     * Obtient la valeur de la propriété customObjKeyLists.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfKeyList }
     *     
     */
    public ArrayOfKeyList getCustomObjKeyLists() {
        return customObjKeyLists;
    }

    /**
     * Définit la valeur de la propriété customObjKeyLists.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfKeyList }
     *     
     */
    public void setCustomObjKeyLists(ArrayOfKeyList value) {
        this.customObjKeyLists = value;
    }

}
