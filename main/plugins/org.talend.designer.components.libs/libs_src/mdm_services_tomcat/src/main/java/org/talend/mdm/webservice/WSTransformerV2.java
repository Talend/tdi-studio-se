
package org.talend.mdm.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WSTransformerV2 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WSTransformerV2"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="processSteps" type="{http://www.talend.com/mdm}WSTransformerProcessStep" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="withAdminPermissions" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSTransformerV2", propOrder = {
    "description",
    "name",
    "processSteps",
    "withAdminPermissions"
})
public class WSTransformerV2 {

    protected String description;
    protected String name;
    @XmlElement(nillable = true)
    protected List<WSTransformerProcessStep> processSteps;
    protected Boolean withAdminPermissions;

    /**
     * Default no-arg constructor
     * 
     */
    public WSTransformerV2() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public WSTransformerV2(final String description, final String name, final List<WSTransformerProcessStep> processSteps, final Boolean withAdminPermissions) {
        this.description = description;
        this.name = name;
        this.processSteps = processSteps;
        this.withAdminPermissions = withAdminPermissions;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the processSteps property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the processSteps property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProcessSteps().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WSTransformerProcessStep }
     * 
     * 
     */
    public List<WSTransformerProcessStep> getProcessSteps() {
        if (processSteps == null) {
            processSteps = new ArrayList<WSTransformerProcessStep>();
        }
        return this.processSteps;
    }

    /**
     * Gets the value of the withAdminPermissions property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWithAdminPermissions() {
        return withAdminPermissions;
    }

    /**
     * Sets the value of the withAdminPermissions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWithAdminPermissions(Boolean value) {
        this.withAdminPermissions = value;
    }
    
}
