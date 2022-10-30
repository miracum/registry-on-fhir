package org.miracum.registry.multisitemerger;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miracum.registry.multisitemerger.fhir.FhirSystems;
import org.miracum.registry.multisitemerger.fhir.MasterStudyTransactionBuilder;

class MasterStudyTransactionBuilderTest {

  private FhirSystems fhirSystems;
  private MasterStudyTransactionBuilder sut;

  public MasterStudyTransactionBuilderTest() {
    fhirSystems = new FhirSystems();
    fhirSystems.setMultiSiteContactExtension("C");
    fhirSystems.setMultiSiteStatusExtension("S");
    fhirSystems.setStudyAcronymExtension("A");
    fhirSystems.setStudyRoleTag("M");
  }

  @BeforeEach
  void setUp() {
    sut = new MasterStudyTransactionBuilder(fhirSystems);
  }

  @Test
  void buildTransaction_withStudyWithIdentifiers_includesAllIdentifierInTransactionUpdateUrl() {
    var input =
        List.of(
            new ResearchStudy()
                .setStatus(ResearchStudy.ResearchStudyStatus.ACTIVE)
                .addIdentifier(new Identifier().setSystem("drks.de").setValue("A"))
                .addIdentifier(new Identifier().setSystem("clinicaltrials.gov").setValue("123"))
                .addIdentifier(new Identifier().setSystem("eudract.eu").setValue("qwe")));

    var trx = sut.buildTransaction(input);

    var updateUrl = trx.getEntryFirstRep().getRequest().getUrl();

    var expectedUrl =
        String.format(
            "ResearchStudy?identifier=drks.de|A,clinicaltrials.gov|123,eudract.eu|qwe&_tag=%s|master",
            fhirSystems.getStudyRoleTag());

    assertThat(updateUrl).isEqualTo(expectedUrl);
  }
}
