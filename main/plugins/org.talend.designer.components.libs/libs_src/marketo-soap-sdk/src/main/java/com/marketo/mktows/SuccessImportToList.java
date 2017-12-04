
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SuccessImportToList complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SuccessImportToList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://www.marketo.com/mktows/}ResultImportToList"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SuccessImportToList", propOrder = {
    "result"
})
@XmlRootElement(name = "successImportToList")
public class SuccessImportToList {

    @XmlElement(required = true)
    protected ResultImportToList result;

    /**
     * Obtient la valeur de la propriété result.
     * 
     * @return
     *     possible object is
     *     {@link ResultImportToList }
     *     
     */
    public ResultImportToList getResult() {
        return result;
    }

    /**
     * Définit la valeur de la propriété result.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultImportToList }
     *     
     */
    public void setResult(ResultImportToList value) {
        this.result = value;
    }

}
