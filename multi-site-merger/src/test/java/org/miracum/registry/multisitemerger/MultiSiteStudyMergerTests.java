package org.miracum.registry.multisitemerger;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miracum.registry.multisitemerger.fhir.FhirSystems;

class MultiSiteStudyMergerTests {

  private FhirSystems fhirSystems;
  private MultiSiteStudyMerger sut;

  public MultiSiteStudyMergerTests() {
    fhirSystems = new FhirSystems();
    fhirSystems.setMultiSiteContactExtension("C");
    fhirSystems.setMultiSiteStatusExtension("S");
    fhirSystems.setStudyAcronymExtension("A");
    fhirSystems.setStudyRoleTag("M");
  }

  @BeforeEach
  void setUp() {
    sut = new MultiSiteStudyMerger(fhirSystems);
  }

  @Test
  void mergeToMasterStudy_withSingleStudy_preservesContentsAndSetsMetaTagToMaster() {
    var input = new ResearchStudy().setTitle("Test").setDescription("Test Description");

    var result = sut.mergeToMasterStudy(List.of(input));

    assertThat(result.getTitle()).isEqualTo(input.getTitle());
    assertThat(result.getDescription()).isEqualTo(input.getDescription());

    assertThat(result.getMeta().getTag())
        .anyMatch(
            tag ->
                tag.getSystem().equals(fhirSystems.getStudyRoleTag())
                    && tag.getCode().equals("master"));
  }

  @Test
  void mergeToMasterStudy_withGivenStudies_setsRecruitmentStatusPerSite() {
    var input =
        List.of(
            new ResearchStudy()
                .setStatus(ResearchStudy.ResearchStudyStatus.ACTIVE)
                .setSite(List.of(new Reference("Location/1"))),
            new ResearchStudy()
                .setStatus(ResearchStudy.ResearchStudyStatus.COMPLETED)
                .setSite(List.of(new Reference("Location/2"))));

    var result = sut.mergeToMasterStudy(input);

    assertThat(result.getSite()).hasSize(2);
    assertThat(result.getSite())
        .allMatch(site -> site.hasExtension(fhirSystems.getMultiSiteStatusExtension()));

    var site1 =
        result.getSite().stream()
            .filter(site -> site.getReference().equals("Location/1"))
            .findFirst()
            .orElseThrow();
    var site1Status =
        ((CodeableConcept)
                site1.getExtensionByUrl(fhirSystems.getMultiSiteStatusExtension()).getValue())
            .getCodingFirstRep()
            .getCode();
    assertThat(site1Status).isEqualTo(ResearchStudy.ResearchStudyStatus.ACTIVE.toCode());

    var site2 =
        result.getSite().stream()
            .filter(site -> site.getReference().equals("Location/2"))
            .findFirst()
            .orElseThrow();
    var site2Status =
        ((CodeableConcept)
                site2.getExtensionByUrl(fhirSystems.getMultiSiteStatusExtension()).getValue())
            .getCodingFirstRep()
            .getCode();
    assertThat(site2Status).isEqualTo(ResearchStudy.ResearchStudyStatus.COMPLETED.toCode());
  }

  @Test
  void mergeToMasterStudy_withGivenStudies_setsContactPerSite() {
    var input =
        List.of(
            new ResearchStudy()
                .setContact(List.of(new ContactDetail().setName("A")))
                .setSite(List.of(new Reference("Location/1"))),
            new ResearchStudy()
                .setContact(List.of(new ContactDetail().setName("B")))
                .setSite(List.of(new Reference("Location/2"))));

    var result = sut.mergeToMasterStudy(input);

    assertThat(result.getSite()).hasSize(2);
    assertThat(result.getSite())
        .allMatch(site -> site.hasExtension(fhirSystems.getMultiSiteContactExtension()));

    var site1 =
        result.getSite().stream()
            .filter(site -> site.getReference().equals("Location/1"))
            .findFirst()
            .orElseThrow();
    var site1Contact =
        ((ContactDetail)
            site1.getExtensionByUrl(fhirSystems.getMultiSiteContactExtension()).getValue());
    assertThat(site1Contact.getName()).isEqualTo("A");

    var site2 =
        result.getSite().stream()
            .filter(site -> site.getReference().equals("Location/2"))
            .findFirst()
            .orElseThrow();
    var site2Contact =
        ((ContactDetail)
            site2.getExtensionByUrl(fhirSystems.getMultiSiteContactExtension()).getValue());
    assertThat(site2Contact.getName()).isEqualTo("B");
  }

  @Test
  void mergeToMasterStudy_withAtLeatOneStudyActive_setsStatusToActive() {
    var input =
        List.of(
            new ResearchStudy().setStatus(ResearchStudy.ResearchStudyStatus.COMPLETED),
            new ResearchStudy().setStatus(ResearchStudy.ResearchStudyStatus.ACTIVE));

    var result = sut.mergeToMasterStudy(input);

    assertThat(result.getStatus()).isEqualTo(ResearchStudy.ResearchStudyStatus.ACTIVE);
  }

  @Test
  void mergeToMasterStudy_withOnlyTranslatedTitle_setsTitleToTranslation() {
    var langExt = new Extension().setUrl("lang").setValue(new StringType("en"));
    var titleTransExt = new Extension().setUrl("content").setValue(new StringType("Title-En"));
    var titleExtension =
        new Extension().setUrl("http://hl7.org/fhir/StructureDefinition/translation");
    titleExtension.addExtension(langExt);
    titleExtension.addExtension(titleTransExt);

    var titleElement = new StringType();
    titleElement.addExtension(titleExtension);
    var input = List.of(new ResearchStudy().setTitleElement(titleElement));

    var result = sut.mergeToMasterStudy(input);

    assertThat(result.getTitle()).isEqualTo("Title-En");
  }

  @Test
  void mergeToMasterStudy_withTitleAlreadySet_doesNotOverrideWithTranslation() {
    var langExt = new Extension().setUrl("lang").setValue(new StringType("en"));
    var titleTransExt = new Extension().setUrl("content").setValue(new StringType("Title-En"));
    var titleExtension =
        new Extension().setUrl("http://hl7.org/fhir/StructureDefinition/translation");
    titleExtension.addExtension(langExt);
    titleExtension.addExtension(titleTransExt);

    var titleElement = new StringType();
    titleElement.addExtension(titleExtension);

    var input =
        List.of(
            new ResearchStudy().setTitle("Original Title"),
            new ResearchStudy().setTitleElement(titleElement));

    var result = sut.mergeToMasterStudy(input);

    assertThat(result.getTitle()).isEqualTo("Original Title");
  }
}
