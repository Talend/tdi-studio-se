
package com.marketo.mktows;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ArrayOfLeadRecord complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfLeadRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="leadRecord" type="{http://www.marketo.com/mktows/}LeadRecord" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfLeadRecord", propOrder = {
    "leadRecords"
})
public class ArrayOfLeadRecord {

    @XmlElement(name = "leadRecord")
    protected List<LeadRecord> leadRecords;

    /**
     * Gets the value of the leadRecords property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the leadRecords property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLeadRecords().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LeadRecord }
     * 
     * 
     */
    public List<LeadRecord> getLeadRecords() {
        if (leadRecords == null) {
            leadRecords = new ArrayList<LeadRecord>();
        }
        return this.leadRecords;
    }

}
