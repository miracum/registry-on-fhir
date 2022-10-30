package org.miracum.registry.multisitemerger.health;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FhirConnectivityCheck implements HealthIndicator {
  private RestTemplate restTemplate;
  private String fhirUrl;

  public FhirConnectivityCheck(RestTemplate restTemplate, @Value("${fhir.url}") URI fhirUrl) {
    this.restTemplate = restTemplate;

    var uriBuilder = UriComponentsBuilder.newInstance();
    uriBuilder.uri(fhirUrl).path("/metadata");
    this.fhirUrl = uriBuilder.build().toUriString();
  }

  @Override
  public Health health() {
    try {
      restTemplate.getForObject(fhirUrl, Object.class);
    } catch (Exception e) {
      return Health.down().withDetail("FHIR Server", "Not available: " + e.getMessage()).build();
    }
    return Health.up().withDetail("FHIR Server", "Available").build();
  }
}
