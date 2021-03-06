//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.02.18 at 02:05:14 PM CET 
//


package domainXMLModel;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the domainXMLModel package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Domain_QNAME = new QName("", "domain");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: domainXMLModel
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ArgumentType }
     * 
     */
    public ArgumentType createArgumentType() {
        return new ArgumentType();
    }

    /**
     * Create an instance of {@link RelationListType }
     * 
     */
    public RelationListType createRelationListType() {
        return new RelationListType();
    }

    /**
     * Create an instance of {@link PddlRequirementsListType }
     * 
     */
    public PddlRequirementsListType createPddlRequirementsListType() {
        return new PddlRequirementsListType();
    }

    /**
     * Create an instance of {@link OperatorListType }
     * 
     */
    public OperatorListType createOperatorListType() {
        return new OperatorListType();
    }

    /**
     * Create an instance of {@link NodeType }
     * 
     */
    public NodeType createNodeType() {
        return new NodeType();
    }

    /**
     * Create an instance of {@link OperatorType }
     * 
     */
    public OperatorType createOperatorType() {
        return new OperatorType();
    }

    /**
     * Create an instance of {@link StateVariableListType }
     * 
     */
    public StateVariableListType createStateVariableListType() {
        return new StateVariableListType();
    }

    /**
     * Create an instance of {@link StateVariableType }
     * 
     */
    public StateVariableType createStateVariableType() {
        return new StateVariableType();
    }

    /**
     * Create an instance of {@link ExpressionType }
     * 
     */
    public ExpressionType createExpressionType() {
        return new ExpressionType();
    }

    /**
     * Create an instance of {@link DomainType }
     * 
     */
    public DomainType createDomainType() {
        return new DomainType();
    }

    /**
     * Create an instance of {@link ClassTreeType }
     * 
     */
    public ClassTreeType createClassTreeType() {
        return new ClassTreeType();
    }

    /**
     * Create an instance of {@link SlotType }
     * 
     */
    public SlotType createSlotType() {
        return new SlotType();
    }

    /**
     * Create an instance of {@link DomainProperties }
     * 
     */
    public DomainProperties createDomainProperties() {
        return new DomainProperties();
    }

    /**
     * Create an instance of {@link ValueRangeType }
     * 
     */
    public ValueRangeType createValueRangeType() {
        return new ValueRangeType();
    }

    /**
     * Create an instance of {@link ChildrenListType }
     * 
     */
    public ChildrenListType createChildrenListType() {
        return new ChildrenListType();
    }

    /**
     * Create an instance of {@link RelationType }
     * 
     */
    public RelationType createRelationType() {
        return new RelationType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DomainType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "domain")
    public JAXBElement<DomainType> createDomain(DomainType value) {
        return new JAXBElement<DomainType>(_Domain_QNAME, DomainType.class, null, value);
    }

}
