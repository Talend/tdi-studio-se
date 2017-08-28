
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultMergeLeads complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultMergeLeads">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mergeStatus" type="{http://www.marketo.com/mktows/}MergeStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultMergeLeads", propOrder = {
    "mergeStatus"
})
public class ResultMergeLeads {

    @XmlElement(required = true)
    protected MergeStatus mergeStatus;

    /**
     * Obtient la valeur de la propriété mergeStatus.
     * 
     * @return
     *     possible object is
     *     {@link MergeStatus }
     *     
     */
    public MergeStatus getMergeStatus() {
        return mergeStatus;
    }

    /**
     * Définit la valeur de la propriété mergeStatus.
     * 
     * @param value
     *     allowed object is
     *     {@link MergeStatus }
     *     
     */
    public void setMergeStatus(MergeStatus value) {
        this.mergeStatus = value;
    }

}
