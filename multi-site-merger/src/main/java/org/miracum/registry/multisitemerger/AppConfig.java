package org.miracum.registry.multisitemerger;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

  @Bean
  public FhirContext fhirContext() {
    return FhirContext.forR4();
  }

  @Bean
  public IGenericClient fhirClient(FhirContext fhirContext, @Value("${fhir.url}") String fhirUrl) {
    var clientFactory = fhirContext.getRestfulClientFactory();
    clientFactory.setConnectTimeout(20_000);
    clientFactory.setSocketTimeout(60_000);
    return fhirContext.newRestfulGenericClient(fhirUrl);
  }

  @Bean
  @Scope("prototype")
  public RetryTemplate retryTemplate() {
    var retryTemplate = new RetryTemplate();

    var fixedBackOffPolicy = new FixedBackOffPolicy();
    fixedBackOffPolicy.setBackOffPeriod(15_000);
    retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

    var retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(10);
    retryTemplate.setRetryPolicy(retryPolicy);

    return retryTemplate;
  }

  @Bean
  public MeterRegistryCustomizer<MeterRegistry> injectAppLabel() {
    return registry -> registry.config().commonTags("appname", "multi-site-merger");
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
