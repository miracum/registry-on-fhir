package org.miracum.registry.multisitemerger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IUpdateWithQueryTyped;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@SpringBootTest
class MultiSiteMergerApplicationTests {

  // @Test - disabled as mocking IGenericClient doesn't seem to work
  void contextLoads() {}

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    public RetryTemplate neverRetryTemplate() {
      var template = new RetryTemplate();
      template.setRetryPolicy(new NeverRetryPolicy());
      return template;
    }

    @Bean
    @Primary
    public IGenericClient mockFhirClient() {
      var client = mock(IGenericClient.class, new ReturnsDeepStubs());

      Object when =
          client
              .update()
              .resource(any(IBaseResource.class))
              .conditional()
              .where((ICriterion<?>) any())
              .execute();

      when(when).thenReturn(mock(IUpdateWithQueryTyped.class));

      return client;
    }
  }
}
