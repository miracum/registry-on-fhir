package org.miracum.registry.multisitemerger;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.ResearchStudy.ResearchStudyStatus;
import org.miracum.registry.multisitemerger.fhir.FhirSystems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MultiSiteStudyMerger {
  private static final Logger log = LoggerFactory.getLogger(MultiSiteStudyFinder.class);

  private static final String MISSING_ITEM_METRIC_NAME = "multisitemerger.study.item.missing.total";
  private static final HashMap<String, Counter> missingItemMetrics = new HashMap<>();

  private final FhirSystems fhirSystems;

  public MultiSiteStudyMerger(FhirSystems fhirSystems) {
    this.fhirSystems = fhirSystems;
  }

  /**
   * Given a set of site-specific studies - all of which represent the same multi-centric study -
   * returns a new ResearchStudy resource where per-site information is merged.
   *
   * @param studies set of site-specific ResearchStudy resources belonging to the same multi-centric
   *     study
   * @return a single study record encompassing all site-specific information (such as recruitment
   *     status, contact points, etc.)
   */
  public ResearchStudy mergeToMasterStudy(List<ResearchStudy> studies) {

    var uniqueIdentifiers = new HashMap<String, Identifier>();
    var uniqueRelatedArtifacts = new HashMap<String, RelatedArtifact>();

    var masterStudy = new ResearchStudy();

    masterStudy.getMeta().addTag().setSystem(fhirSystems.getStudyRoleTag()).setCode("master");

    masterStudy.setStatus(ResearchStudyStatus.CLOSEDTOACCRUAL);

    for (var study : studies) {
      if (!masterStudy.hasTitle()) {
        if (study.hasTitle() && study.getTitle() != null) {
          masterStudy.setTitle(study.getTitle());
        } else if (study.hasTitleElement() && study.getTitleElement().hasValue()) {
          masterStudy.setTitle(study.getTitleElement().getValue());
        } else if (study
            .getTitleElement()
            .hasExtension("http://hl7.org/fhir/StructureDefinition/translation")) {

          var translation =
              study
                  .getTitleElement()
                  .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/translation");
          var titleEn = (StringType) translation.getExtensionByUrl("content").getValue();
          masterStudy.setTitle(titleEn.getValue());
        }
      }

      if (!study.hasTitle() || !study.hasTitleElement()) {
        incrementMissingItemCount("title");
      }

      if (!masterStudy.hasDescription()) {
        if (study.hasDescription() && study.getDescription() != null) {
          masterStudy.setDescription(study.getDescription());
        } else if (study.hasDescriptionElement() && study.getDescriptionElement().hasValue()) {
          masterStudy.setDescription(study.getDescriptionElement().getValue());
        } else if (study
            .getDescriptionElement()
            .hasExtension("http://hl7.org/fhir/StructureDefinition/translation")) {

          var translation =
              study
                  .getDescriptionElement()
                  .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/translation");
          var descriptionEn = (StringType) translation.getExtensionByUrl("content").getValue();
          masterStudy.setDescription(descriptionEn.getValue());
        }
      }

      if (!study.hasDescription() || !study.hasDescriptionElement()) {
        incrementMissingItemCount("description");
      }

      if (!masterStudy.hasExtension(fhirSystems.getStudyAcronymExtension())
          && study.hasExtension(fhirSystems.getStudyAcronymExtension())) {
        var acronym = study.getExtensionByUrl(fhirSystems.getStudyAcronymExtension()).getValue();
        masterStudy.addExtension().setUrl(fhirSystems.getStudyAcronymExtension()).setValue(acronym);
      }

      if (!study.hasExtension(fhirSystems.getStudyAcronymExtension())) {
        incrementMissingItemCount("acronym");
      }

      if (!masterStudy.hasKeyword() && study.hasKeyword()) {
        masterStudy.setKeyword(study.getKeyword());
      }

      if (!study.hasKeyword()) {
        incrementMissingItemCount("keyword");
      }

      if (!masterStudy.hasCondition() && study.hasCondition()) {
        masterStudy.setCondition(study.getCondition());
      }

      if (!study.hasCondition()) {
        incrementMissingItemCount("condition");
      }

      if (!masterStudy.hasCategory() && study.hasCategory()) {
        masterStudy.setCategory(study.getCategory());
      }

      if (!study.hasCategory()) {
        incrementMissingItemCount("category");
      }

      if (!masterStudy.hasContained() && study.hasContained()) {
        masterStudy.setContained(study.getContained());
      }

      if (!study.hasContained()) {
        incrementMissingItemCount("contained");
      }

      if (!masterStudy.hasEnrollment() && study.hasEnrollment()) {
        masterStudy.setEnrollment(study.getEnrollment());
      }

      if (!study.hasEnrollment()) {
        incrementMissingItemCount("enrollment");
      }

      // put all identifiers in the hashmap in order to only end up with distinct identifiers in the
      // end
      for (var identifier : study.getIdentifier()) {
        if (!identifier.hasValue() || !identifier.hasSystem()) {
          log.warn(
              "{} has identifier {} without value. Skipping.",
              study.getId(),
              identifier.getSystem());
          continue;
        }

        var id =
            String.format(
                "%s|%s",
                identifier.getSystem().toLowerCase().trim(),
                identifier.getValue().toLowerCase().trim());

        uniqueIdentifiers.putIfAbsent(id, identifier);
      }

      if (!study.hasIdentifier()) {
        incrementMissingItemCount("identifier");
      }

      for (var relatedArtifact : study.getRelatedArtifact()) {
        uniqueRelatedArtifacts.putIfAbsent(relatedArtifact.getUrl(), relatedArtifact);
      }

      if (!study.hasRelatedArtifact()) {
        incrementMissingItemCount("relatedArtifact");
      }

      // if at least one study's status is active, set the master study status to active
      if (study.getStatus() == ResearchStudy.ResearchStudyStatus.ACTIVE) {
        masterStudy.setStatus(ResearchStudy.ResearchStudyStatus.ACTIVE);
      }

      if (!study.hasStatus()) {
        incrementMissingItemCount("status");
      }

      if (study.hasSite()) {
        // this only adds the very first site specified - this is a simplification for now
        var studySite = study.getSiteFirstRep();

        if (study.hasStatus()) {
          var studyStatus = study.getStatus();
          var statusConcept = new CodeableConcept();
          statusConcept.addCoding(
              new Coding().setSystem(studyStatus.getSystem()).setCode(studyStatus.toCode()));
          studySite.addExtension(fhirSystems.getMultiSiteStatusExtension(), statusConcept);
        }

        // add each contact as a new extension instance. This is necessary as the value of an
        // extension
        // can not be a list (see https://www.hl7.org/fhir/extensibility.html).
        for (var contact : study.getContact()) {
          studySite.addExtension(fhirSystems.getMultiSiteContactExtension(), contact);
        }

        masterStudy.addSite(studySite);
      } else {
        incrementMissingItemCount("site");
      }
    }

    masterStudy.setIdentifier(new ArrayList<>(uniqueIdentifiers.values()));
    masterStudy.setRelatedArtifact(new ArrayList<>(uniqueRelatedArtifacts.values()));

    return masterStudy;
  }

  private void incrementMissingItemCount(String itemName) {
    missingItemMetrics.putIfAbsent(
        itemName, Metrics.globalRegistry.counter(MISSING_ITEM_METRIC_NAME, "item", itemName));
    missingItemMetrics.get(itemName).increment();
  }
}
