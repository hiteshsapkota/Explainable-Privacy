package com.expriv.model;

public class Feedback {

  private String attributeName;
  private String attributeId;
  private String attributeSensitivity;
  private String attributeDescription;

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getAttributeId() {
    return attributeId;
  }

  public void setAttributeId(String attributeId) {
    this.attributeId = attributeId;
  }

  public String getAttributeSensitivity() {
    return attributeSensitivity;
  }

  public void setAttributeSensitivity(String attributeSensitivity) {
    this.attributeSensitivity = attributeSensitivity;
  }

  public String getAttributeDescription() {
    return attributeDescription;
  }

  public void setAttributeDescription(String attributeDescription) {
    this.attributeDescription = attributeDescription;
  }
}
