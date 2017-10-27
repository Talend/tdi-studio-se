
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultImportToList complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultImportToList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="importStatus" type="{http://www.marketo.com/mktows/}ImportToListStatusEnum"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultImportToList", propOrder = {
    "importStatus"
})
public class ResultImportToList {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ImportToListStatusEnum importStatus;

    /**
     * Obtient la valeur de la propriété importStatus.
     * 
     * @return
     *     possible object is
     *     {@link ImportToListStatusEnum }
     *     
     */
    public ImportToListStatusEnum getImportStatus() {
        return importStatus;
    }

    /**
     * Définit la valeur de la propriété importStatus.
     * 
     * @param value
     *     allowed object is
     *     {@link ImportToListStatusEnum }
     *     
     */
    public void setImportStatus(ImportToListStatusEnum value) {
        this.importStatus = value;
    }

}
