package com.github.springbees.pojo;

import java.util.HashMap;
import java.util.Map;

public class DependencyEntry {

  private String groupId;
  private String artifactId;
  private String version;
  private String organization;
  private String organizationUrl;
  private String homePage;
  private Map<String, String> license = new HashMap<>();

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getOrganizationUrl() {
    return organizationUrl;
  }

  public void setOrganizationUrl(String organizationUrl) {
    this.organizationUrl = organizationUrl;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getHomePage() {
    return this.homePage;
  }

  public void setHomePage(String homePage) {
    this.homePage = homePage;
  }

  public Map<String, String> getLicense() {
    return this.license;
  }

  public void addLicense(String licenseName, String licenseUrl) {
    this.license.put(licenseName, licenseUrl);
  }

  public void addLicenses(Map<String, String> license) {
    this.license.putAll(license);
  }


  @Override
  public String toString() {
    return groupId + ':' + artifactId + ":" + version + "[" + String.join(",", license.keySet())
      + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (obj instanceof DependencyEntry) {
      DependencyEntry element = (DependencyEntry) obj;
      if (element.groupId.equals(this.groupId)
        && element.artifactId.equals(this.artifactId)
        && element.version.equals(this.version)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return groupId.hashCode() * artifactId.hashCode() * version.hashCode();
  }
}
