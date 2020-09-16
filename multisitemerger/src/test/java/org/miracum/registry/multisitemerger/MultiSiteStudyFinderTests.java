package org.miracum.registry.multisitemerger;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MultiSiteStudyFinderTests {

  private MultiSiteStudyFinder sut;

  private static Stream<Arguments> sampleStudiesWithExpectedClusterNumber() {
    return Stream.of(
        Arguments.of(
            List.of(
                List.of(new Identifier().setSystem("drks.de").setValue("A")),
                List.of(new Identifier().setSystem("drks.de").setValue("B")),
                List.of(new Identifier().setSystem("drks.de").setValue("C")),
                List.of(new Identifier().setSystem("drks.de").setValue("D"))),
            4),
        Arguments.of(
            List.of(
                List.of(new Identifier().setSystem("drks.de").setValue("A")),
                List.of(
                    new Identifier().setSystem("drks.de").setValue("A"),
                    new Identifier().setSystem("clinicaltrials.gov").setValue("1")),
                List.of(new Identifier().setSystem("clinicaltrials.gov").setValue("1")),
                List.of(new Identifier().setSystem("drks.de").setValue("B")),
                List.of(new Identifier().setSystem("drks.de").setValue("C"))),
            3),
        Arguments.of(
            List.of(
                List.of(new Identifier().setSystem("drks.de").setValue("A")),
                List.of(
                    new Identifier().setSystem("drks.de").setValue("A"),
                    new Identifier().setSystem("clinicaltrials.gov").setValue("1")),
                List.of(
                    new Identifier().setSystem("clinicaltrials.gov").setValue("1"),
                    new Identifier().setSystem("eudract").setValue("011")),
                List.of(
                    new Identifier().setSystem("eudract").setValue("011"),
                    new Identifier().setSystem("drks.de").setValue("A"))),
            1));
  }

  @BeforeEach
  void setUp() {
    sut = new MultiSiteStudyFinder();
  }

  @Test
  void findMultiSiteStudies_whenGivenAnEmptyList_shouldReturnEmptyResult() {
    var result = sut.findMultiSiteStudies(List.of());

    assertThat(result).isEmpty();
  }

  @Test
  void findMultiSiteStudies_whenGivenSingleItemList_shouldReturnSingleCluster() {
    var input = new ResearchStudy().setTitle("Test");
    var result = sut.findMultiSiteStudies(List.of(input));

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).containsOnly(input);
  }

  @Test
  void
      findMultiSiteStudies_whenGivenTwoStudiesWithSameIdentifierAndOneWithout_shouldReturnTwoClusters() {
    var input =
        List.of(
            new ResearchStudy()
                .setStatus(ResearchStudy.ResearchStudyStatus.ACTIVE)
                .addIdentifier(new Identifier().setSystem("drks.de").setValue("A")),
            new ResearchStudy()
                .setStatus(ResearchStudy.ResearchStudyStatus.COMPLETED)
                .addIdentifier(new Identifier().setSystem("drks.de").setValue("A")),
            new ResearchStudy().addIdentifier(new Identifier().setSystem("drks.de").setValue("B")));

    var result = sut.findMultiSiteStudies(input);

    assertThat(result).hasSize(2);
    assertThat(result)
        .anySatisfy(
            set ->
                assertThat(set)
                    .allMatch(
                        study ->
                            study.getIdentifierFirstRep().getSystem().equals("drks.de")
                                && study.getIdentifierFirstRep().getValue().equals("A")));
    assertThat(result)
        .anySatisfy(
            set ->
                assertThat(set)
                    .allMatch(
                        study ->
                            study.getIdentifierFirstRep().getSystem().equals("drks.de")
                                && study.getIdentifierFirstRep().getValue().equals("B")));
  }

  @Test
  void
      findMultiSiteStudies_whenGivenStudiesWithTransitiveConnectivity_shouldReturnClusterIncludingAll() {
    var input =
        List.of(
            new ResearchStudy()
                .addIdentifier(new Identifier().setSystem("drks.de").setValue("A"))
                .addIdentifier(new Identifier().setSystem("clinicaltrials.gov").setValue("1")),
            new ResearchStudy().addIdentifier(new Identifier().setSystem("drks.de").setValue("A")),
            new ResearchStudy()
                .addIdentifier(new Identifier().setSystem("clinicaltrials.gov").setValue("1")));

    var result = sut.findMultiSiteStudies(input);

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).hasSize(3);
    assertThat(result)
        .anySatisfy(
            set ->
                assertThat(set)
                    .anyMatch(
                        study ->
                            study.getIdentifierFirstRep().getSystem().equals("drks.de")
                                && study.getIdentifierFirstRep().getValue().equals("A")));
    assertThat(result)
        .anySatisfy(
            set ->
                assertThat(set)
                    .anyMatch(
                        study ->
                            study.getIdentifierFirstRep().getSystem().equals("clinicaltrials.gov")
                                && study.getIdentifierFirstRep().getValue().equals("1")));
  }

  @Test
  void findMultiSiteStudies_whenGivenStudiesWithCyclicConnectivity_shouldReturnSingleCluster() {
    var input =
        List.of(
            new ResearchStudy()
                .addIdentifier(new Identifier().setSystem("drks.de").setValue("A"))
                .addIdentifier(new Identifier().setSystem("clinicaltrials.gov").setValue("1")),
            new ResearchStudy()
                .addIdentifier(new Identifier().setSystem("drks.de").setValue("A"))
                .addIdentifier(new Identifier().setSystem("clinicaltrials.gov").setValue("1")),
            new ResearchStudy()
                .addIdentifier(new Identifier().setSystem("clinicaltrials.gov").setValue("1"))
                .addIdentifier(new Identifier().setSystem("drks.de").setValue("A")));

    var result = sut.findMultiSiteStudies(input);

    assertThat(result).hasSize(1);
  }

  @ParameterizedTest
  @MethodSource("sampleStudiesWithExpectedClusterNumber")
  void findMultiSiteStudies_whenGivenStudies_shouldReturnExpectedNumberOfCluster(
      List<List<Identifier>> studyIdentifiers, int expectedClusterSize) {
    var input =
        studyIdentifiers.stream()
            .map(identifiers -> new ResearchStudy().setIdentifier(identifiers))
            .collect(Collectors.toList());

    var result = sut.findMultiSiteStudies(input);

    assertThat(result).hasSize(expectedClusterSize);
  }
}
