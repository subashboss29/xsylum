package net.jodah.xsylum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A named XML element.
 * 
 * @author Jonathan Halterman
 */
public final class XmlElement extends XmlSearchable<Element> {
  public XmlElement(Element element) {
    super(element);
  }

  /**
   * Get the value of the {@code attribute}.
   * 
   * @throws XsylumException if the {@code attribute} cannot be found
   */
  public String attribute(String attribute) throws XsylumException {
    String value = source.getAttribute(attribute);
    if (value == null)
      throw new XsylumException("Attribute %s does not exist", attribute);
    return value;
  }

  /**
   * Get the value of the {@code attribute} as a boolean. Returns true for "true", "1", "yes", "y"
   * ignoring case, else returns false.
   * 
   * @throws XsylumException if the {@code attribute} cannot be found
   */
  public boolean attributeAsBoolean(String attribute) throws XsylumException {
    return Converter.booleanConverter.convert(attribute(attribute));
  }

  /**
   * Get the value of the {@code attribute} as a double.
   * 
   * @throws XsylumException if the {@code attribute} cannot be found
   * @throws NumberFormatException if the attribute is not a valid double
   */
  public double attributeAsDouble(String attribute) throws XsylumException {
    return Converter.doubleConverter.convert(attribute(attribute));
  }

  /**
   * Get the value of the {@code attribute} as an enum of type {@code V}. Returns null if value
   * cannot be parsed to an enum of type {@code V}.
   * 
   * @param <V> enum type
   * @throws XsylumException if the {@code attribute} cannot be found
   */
  public <V extends Enum<V>> V attributeAsEnum(String attribute, Class<V> targetEnum)
      throws XsylumException {
    return enumConverterFor(targetEnum).convert(attribute(attribute));
  }

  /**
   * Get the value of the {@code attribute} as an integer.
   * 
   * @throws XsylumException if the {@code attribute} cannot be found
   * @throws NumberFormatException if the attribute is not a valid int
   */
  public int attributeAsInt(String attribute) throws XsylumException {
    return Converter.intConverter.convert(attribute(attribute));
  }

  /**
   * Get the value of the {@code attribute} as a long.
   * 
   * @throws XsylumException if the {@code attribute} cannot be found
   * @throws NumberFormatException if the attribute is not a valid long
   */
  public long attributeAsLong(String attribute) throws XsylumException {
    return Converter.longConverter.convert(attribute(attribute));
  }

  /**
   * Builds and returns a map of the element's attributes, else empty map if the element has no
   * attributes.
   */
  public Map<String, String> attributes() {
    if (!source.hasAttributes())
      return Collections.emptyMap();

    NamedNodeMap attributes = source.getAttributes();
    Map<String, String> result = new HashMap<String, String>();
    for (int i = 0; i < attributes.getLength(); i++) {
      Node node = attributes.item(i);
      result.put(node.getNodeName(), node.getNodeValue());
    }

    return result;
  }

  /**
   * Returns the element's children, else empty list if the element has no children.
   */
  public List<XmlElement> children() {
    NodeList children = source.getChildNodes();
    if (children.getLength() == 0)
      return Collections.emptyList();

    List<XmlElement> result = new ArrayList<XmlElement>(children.getLength());
    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
      Node child = source.getChildNodes().item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE)
        result.add(new XmlElement((Element) child));
    }

    return result;
  }

  /**
   * Returns the underlying element.
   */
  public Element element() {
    return source;
  }

  /**
   * Returns the XmlElement at the {@code index}, else null if none can be found or the node at the
   * {@code index} is not an element
   */
  public XmlElement get(int index) {
    Node node = source.getChildNodes().item(index);
    return node.getNodeType() == Node.ELEMENT_NODE ? new XmlElement((Element) node) : null;
  }

  /**
   * Returns the first child XmlElement matching the {@code tagName}.
   */
  @Override
  public XmlElement get(String tagName) {
    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
      Node child = source.getChildNodes().item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tagName))
        return new XmlElement((Element) child);
    }

    return null;
  }

  /**
   * Returns all child XmlElements matching the {@code tagName}.
   */
  @Override
  public List<XmlElement> getAll(String tagName) {
    List<XmlElement> result = new ArrayList<XmlElement>();
    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
      Node child = source.getChildNodes().item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tagName))
        result.add(new XmlElement((Element) child));
    }

    return result;
  }

  /**
   * Returns whether the element contains the {@code attribute}.
   */
  public boolean hasAttribute(String attribute) {
    return source.hasAttribute(attribute);
  }

  /**
   * Returns whether the element contains attributes.
   */
  public boolean hasAttributes() {
    return source.hasAttributes();
  }

  /**
   * Returns whether the element contains any child elements with the {@code name}.
   */
  public boolean hasChild(String name) {
    for (int i = 0; i < source.getChildNodes().getLength(); i++) {
      Node child = source.getChildNodes().item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(name))
        return true;
    }

    return false;
  }

  /**
   * Returns the element's name.
   */
  public String name() {
    return source.getNodeName();
  }

  @Override
  public String toString() {
    return toXml();
  }

  /**
   * Returns a XML representation of the element, including its attributes and value.
   */
  public String toXml() {
    String name = name();
    NodeList children = source.getChildNodes();
    NamedNodeMap attributes = source.getAttributes();
    StringBuilder sb = new StringBuilder("<").append(name);

    if (attributes.getLength() > 0) {
      sb.append(' ');
      for (int i = 0; i < attributes.getLength(); i++) {
        Node n = attributes.item(i);
        if (i > 0)
          sb.append(' ');
        sb.append(n.getNodeName()).append("=").append("\"").append(n.getNodeValue()).append("\"");
      }
    }
    if (children.getLength() == 0)
      sb.append("/>");
    else {
      sb.append('>');
      for (int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        if (child.getNodeType() == Node.ELEMENT_NODE)
          sb.append(new XmlElement((Element) child));
        else if (child.getNodeType() == Node.TEXT_NODE)
          sb.append(child.getNodeValue());
        else if (child.getNodeType() == Node.CDATA_SECTION_NODE)
          sb.append("<![CDATA[").append(((CharacterData) child).getData()).append("]]>");
      }
      sb.append("</").append(name).append('>');
    }

    return sb.toString();
  }

  /**
   * Returns the text value of the element.
   */
  public String value() {
    StringBuilder sb = new StringBuilder();
    NodeList children = source.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.TEXT_NODE)
        sb.append(child.getNodeValue());
      else if (child.getNodeType() == Node.CDATA_SECTION_NODE)
        sb.append(((CharacterData) child).getData());
    }
    return sb.toString();
  }

  /**
   * Get the element value as a boolean. Returns true for "true", "1", "yes", "y" ignoring case,
   * else returns false.
   */
  public boolean valueAsBoolean() {
    return Converter.booleanConverter.convert(value());
  }

  /**
   * Get the element value as a double.
   * 
   * @throws NumberFormatException if the value is not a valid double
   */
  public double valueAsDouble() {
    return Converter.doubleConverter.convert(value());
  }

  /**
   * Get the element value as an enum of type {@code V}. Returns null if value cannot be parsed to
   * an enum of type {@code V}.
   * 
   * @param <V> enum type
   */
  public <V extends Enum<V>> V valueAsEnum(Class<V> targetEnum) {
    return enumConverterFor(targetEnum).convert(value());
  }

  /**
   * Get the element value as an integer.
   * 
   * @throws NumberFormatException if the value is not a valid int
   */
  public int valueAsInt() {
    return Converter.intConverter.convert(value());
  }

  /**
   * Get the element value as a long.
   * 
   * @throws NumberFormatException if the value is not a valid long
   */
  public long valueAsLong() {
    return Converter.longConverter.convert(value());
  }
}
