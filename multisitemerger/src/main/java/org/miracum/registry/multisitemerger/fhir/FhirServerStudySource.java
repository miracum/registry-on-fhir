package org.miracum.registry.multisitemerger.fhir;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
public class FhirServerStudySource {

  private static final Logger log = LoggerFactory.getLogger(FhirServerStudySource.class);

  private IGenericClient fhirClient;
  private RetryTemplate retryTemplate;
  private FhirSystems fhirSystems;

  @Autowired
  public FhirServerStudySource(
      IGenericClient fhirClient, RetryTemplate retryTemplate, FhirSystems fhirSystems) {
    this.fhirClient = fhirClient;
    this.retryTemplate = retryTemplate;
    this.fhirSystems = fhirSystems;

    retryTemplate.registerListener(
        new RetryListenerSupport() {
          @Override
          public <T, E extends Throwable> void onError(
              RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            log.warn(
                "Trying to retrieve studies from FHIR server caused error {}. {}. attempt.",
                throwable.getMessage(),
                context.getRetryCount());
          }
        });
  }

  public List<ResearchStudy> getStudies() throws Exception {
    var allStudies = new ArrayList<ResearchStudy>();

    var searchUrl =
        String.format("ResearchStudy?_tag:not=%s|master", fhirSystems.getStudyRoleTag());

    // retrieve all non-master studies from the server
    var results =
        retryTemplate.execute(
            (RetryCallback<Bundle, Exception>)
                context ->
                    fhirClient.search().byUrl(searchUrl).returnBundle(Bundle.class).execute());

    do {
      // cast all resources in the bundle to ResearchStudy
      var studies =
          results.getEntry().stream()
              .map(entry -> (ResearchStudy) entry.getResource())
              .collect(Collectors.toList());

      allStudies.addAll(studies);

      if (results.getLink(Bundle.LINK_NEXT) != null) {
        var resultsFinal = results;
        results =
            retryTemplate.execute(
                (RetryCallback<Bundle, Exception>)
                    context -> fhirClient.loadPage().next(resultsFinal).execute());
      } else {
        results = null;
      }

    } while (results != null);

    return allStudies;
  }
}
