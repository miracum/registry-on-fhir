package org.miracum.registry.multisitemerger.fhir;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fhir.systems")
public class FhirSystems {

  private String studyRoleTag;
  private String multiSiteStatusExtension;
  private String multiSiteContactExtension;
  private String studyAcronymExtension;

  public String getStudyRoleTag() {
    return studyRoleTag;
  }

  public void setStudyRoleTag(String studyRoleTag) {
    this.studyRoleTag = studyRoleTag;
  }

  public String getMultiSiteStatusExtension() {
    return multiSiteStatusExtension;
  }

  public void setMultiSiteStatusExtension(String multiSiteStatusExtension) {
    this.multiSiteStatusExtension = multiSiteStatusExtension;
  }

  public String getMultiSiteContactExtension() {
    return multiSiteContactExtension;
  }

  public void setMultiSiteContactExtension(String multiSiteContactExtension) {
    this.multiSiteContactExtension = multiSiteContactExtension;
  }

  public String getStudyAcronymExtension() {
    return studyAcronymExtension;
  }

  public void setStudyAcronymExtension(String studyAcronymExtension) {
    this.studyAcronymExtension = studyAcronymExtension;
  }
}
