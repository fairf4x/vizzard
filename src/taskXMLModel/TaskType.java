//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.10.27 at 02:41:28 PM CEST 
//


package taskXMLModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for taskType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="taskType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="properties" type="{}taskProperties"/>
 *         &lt;element name="constants" type="{}constantsSectionType"/>
 *         &lt;element name="relations" type="{}relationSectionType"/>
 *         &lt;element name="stateVariables" type="{}stateVariablesSectionType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taskType", propOrder = {
    "properties",
    "constants",
    "relations",
    "stateVariables"
})
public class TaskType {

    @XmlElement(required = true)
    protected TaskProperties properties;
    @XmlElement(required = true)
    protected ConstantsSectionType constants;
    @XmlElement(required = true)
    protected RelationSectionType relations;
    @XmlElement(required = true)
    protected StateVariablesSectionType stateVariables;

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link TaskProperties }
     *     
     */
    public TaskProperties getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaskProperties }
     *     
     */
    public void setProperties(TaskProperties value) {
        this.properties = value;
    }

    /**
     * Gets the value of the constants property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantsSectionType }
     *     
     */
    public ConstantsSectionType getConstants() {
        return constants;
    }

    /**
     * Sets the value of the constants property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantsSectionType }
     *     
     */
    public void setConstants(ConstantsSectionType value) {
        this.constants = value;
    }

    /**
     * Gets the value of the relations property.
     * 
     * @return
     *     possible object is
     *     {@link RelationSectionType }
     *     
     */
    public RelationSectionType getRelations() {
        return relations;
    }

    /**
     * Sets the value of the relations property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelationSectionType }
     *     
     */
    public void setRelations(RelationSectionType value) {
        this.relations = value;
    }

    /**
     * Gets the value of the stateVariables property.
     * 
     * @return
     *     possible object is
     *     {@link StateVariablesSectionType }
     *     
     */
    public StateVariablesSectionType getStateVariables() {
        return stateVariables;
    }

    /**
     * Sets the value of the stateVariables property.
     * 
     * @param value
     *     allowed object is
     *     {@link StateVariablesSectionType }
     *     
     */
    public void setStateVariables(StateVariablesSectionType value) {
        this.stateVariables = value;
    }

}