
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultSyncMObjects complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultSyncMObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mObjStatusList" type="{http://www.marketo.com/mktows/}ArrayOfMObjStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultSyncMObjects", propOrder = {
    "mObjStatusList"
})
public class ResultSyncMObjects {

    @XmlElement(required = true)
    protected ArrayOfMObjStatus mObjStatusList;

    /**
     * Obtient la valeur de la propriété mObjStatusList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMObjStatus }
     *     
     */
    public ArrayOfMObjStatus getMObjStatusList() {
        return mObjStatusList;
    }

    /**
     * Définit la valeur de la propriété mObjStatusList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMObjStatus }
     *     
     */
    public void setMObjStatusList(ArrayOfMObjStatus value) {
        this.mObjStatusList = value;
    }

}
