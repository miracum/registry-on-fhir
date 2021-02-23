package org.miracum.registry.multisitemerger.controllers;

import static java.util.stream.Collectors.toList;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResearchStudy.ResearchStudyStatus;
import org.hl7.fhir.r4.model.Subscription;
import org.miracum.registry.multisitemerger.MultiSiteStudyFinder;
import org.miracum.registry.multisitemerger.MultiSiteStudyMerger;
import org.miracum.registry.multisitemerger.fhir.FhirServerStudySource;
import org.miracum.registry.multisitemerger.fhir.FhirSystems;
import org.miracum.registry.multisitemerger.fhir.MasterStudyTransactionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "/updates",
    produces = {"application/json", "application/fhir+json"})
public class MergeUpdateController {
  private static final Logger log = LoggerFactory.getLogger(MergeUpdateController.class);
  private static final Timer mergeDurationTimer =
      Metrics.globalRegistry.timer("multisitemerger.merge.duration.seconds.total");

  private final FhirServerStudySource source;
  private final MultiSiteStudyFinder finder;
  private final MultiSiteStudyMerger merger;
  private final MasterStudyTransactionBuilder trxBuilder;
  private final IGenericClient fhirClient;
  private final URL hookEndpoint;
  private final FhirSystems fhirSystems;
  private final IParser fhirParser;
  private final boolean enableSubscription;
  private final RetryTemplate retryTemplate;

  public MergeUpdateController(
      FhirServerStudySource source,
      MultiSiteStudyFinder finder,
      MultiSiteStudyMerger merger,
      MasterStudyTransactionBuilder trxBuilder,
      IGenericClient fhirClient,
      @Value("${webhook.endpoint}") URL hookEndpoint,
      FhirSystems fhirSystems,
      @Value("${fhir.subscription.enabled}") boolean enableSubscription,
      RetryTemplate retryTemplate) {
    this.source = source;
    this.finder = finder;
    this.merger = merger;
    this.trxBuilder = trxBuilder;
    this.fhirClient = fhirClient;
    this.fhirSystems = fhirSystems;
    this.fhirParser = fhirClient.getFhirContext().newJsonParser().setPrettyPrint(true);
    this.hookEndpoint = hookEndpoint;
    this.enableSubscription = enableSubscription;
    this.retryTemplate = retryTemplate;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onStartup() {
    if (enableSubscription) {
      var outcome =
          retryTemplate.execute(
              (RetryCallback<MethodOutcome, FhirClientConnectionException>)
                  retryContext -> createSubscription());

      log.info("Subscription created successfully @ '{}'", outcome.getId());
    }
  }

  private MethodOutcome createSubscription() {
    var channel =
        new Subscription.SubscriptionChannelComponent()
            .setType(Subscription.SubscriptionChannelType.RESTHOOK)
            .setEndpoint(hookEndpoint.toString())
            .setPayload("application/fhir+json");

    var criteria = String.format("ResearchStudy?_tag:not=%s|master", fhirSystems.getStudyRoleTag());

    log.info(
        "Creating subscription resource with criteria '{}' and endpoint '{}'",
        criteria,
        hookEndpoint);

    var subscription =
        new Subscription()
            .setCriteria(criteria)
            .setChannel(channel)
            .setReason("Create notifications based on screening list changes.")
            .setStatus(Subscription.SubscriptionStatus.REQUESTED);

    return fhirClient
        .update()
        .resource(subscription)
        .conditional()
        .where(Subscription.CRITERIA.matchesExactly().value(criteria))
        .execute();
  }

  private ResponseEntity<String> mergeAllServerStudies() {
    return mergeDurationTimer.record(
        () -> {
          List<ResearchStudy> nonMasterStudies;
          try {
            nonMasterStudies = source.getStudies();
          } catch (Exception e) {
            log.error("Retrieving study resources failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Retrieving study resources failed: " + e.getMessage());
          }

          log.info("Merging {} studies", nonMasterStudies.size());

          var multiSiteStudies = finder.findMultiSiteStudies(nonMasterStudies);

          var masterStudies =
              multiSiteStudies.stream()
                  .map(studyCluster -> merger.mergeToMasterStudy(new ArrayList<>(studyCluster)))
                  .filter(study -> study.getStatus() == ResearchStudyStatus.ACTIVE)
                  .collect(toList());

          var transaction = trxBuilder.buildTransaction(masterStudies);

          try {
            fhirClient.transaction().withBundle(transaction).execute();
            return ResponseEntity.ok().build();
          } catch (Exception exc) {
            log.error("Sending master study transaction failed. Not retrying.", exc);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Updating FHIR server failed: " + exc.getMessage());
          }
        });
  }

  @Scheduled(cron = "${merge.schedule:-}")
  private void scheduleMergeTask() {
    log.info("Scheduled merge execution reached");
    mergeAllServerStudies();
  }

  @RequestMapping(
      value = "/ResearchStudy/{id}",
      consumes = "application/fhir+json",
      method = {RequestMethod.POST, RequestMethod.PUT})
  public ResponseEntity<String> updates(
      @PathVariable(value = "id") String resourceId, @RequestBody String body) {
    log.info("Webhook called for ResearchStudy with id {}", resourceId);

    if (body == null) {
      log.error("Request body is null");
      return ResponseEntity.badRequest().build();
    }

    var changedStudy = fhirParser.parseResource(ResearchStudy.class, body);

    // TODO: do something with the updated study, ie. check if it needs to be merged into a master
    // record.

    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/trigger")
  public ResponseEntity<String> updates() throws Exception {
    log.info("Manually invoked trigger endpoint. Merging studies");
    return mergeAllServerStudies();
  }
}
