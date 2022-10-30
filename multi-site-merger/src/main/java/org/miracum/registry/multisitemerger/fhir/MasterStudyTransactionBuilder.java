package org.miracum.registry.multisitemerger.fhir;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MasterStudyTransactionBuilder {

  private static final Logger log = LoggerFactory.getLogger(MasterStudyTransactionBuilder.class);

  private final FhirSystems fhirSystems;

  @Autowired
  public MasterStudyTransactionBuilder(FhirSystems fhirSystems) {
    this.fhirSystems = fhirSystems;
  }

  /**
   * Builds a FHIR transaction from the given list of master ResearchStudy resources.
   *
   * @param masterStudies List of ResearchStudy master records.
   * @return the FHIR transaction.
   */
  public Bundle buildTransaction(List<ResearchStudy> masterStudies) {
    var trx = new Bundle().setType(Bundle.BundleType.TRANSACTION);

    for (var masterStudy : masterStudies) {
      var updateUrl = constructUpdateUrl(masterStudy.getIdentifier());

      trx.addEntry()
          .setFullUrl("urn:uuid:" + UUID.randomUUID())
          .setResource(masterStudy)
          .getRequest()
          .setMethod(Bundle.HTTPVerb.PUT)
          .setUrl(updateUrl);
    }

    return trx;
  }

  /**
   * Constructs a FHIR update URL matching a master study record with any of the given identifiers.
   *
   * @param identifiers list of identifiers used to find the master study record.
   * @return the constructed URL as a string.
   */
  private String constructUpdateUrl(List<Identifier> identifiers) {
    var sb = new StringBuilder("ResearchStudy?identifier=");

    for (int i = 0; i < identifiers.size(); i++) {
      var id = identifiers.get(i);
      var idEncoded = URLEncoder.encode(id.getValue(), StandardCharsets.UTF_8);

      sb.append(String.format("%s|%s", id.getSystem(), idEncoded));

      // appends an "," to all but the last identifier
      if (i < identifiers.size() - 1) {
        sb.append(",");
      }
    }

    // the update only refers to master studies
    sb.append(String.format("&_tag=%s|%s", fhirSystems.getStudyRoleTag(), "master"));
    return sb.toString();
  }
}
