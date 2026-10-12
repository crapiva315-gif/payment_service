package dev.alexeev.payment_service.client;

import dev.alexeev.payment_service.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

@Slf4j
@Component
public class RandomNumberClient {

  private final RestClient restClient;
  private final URI uri;

  public RandomNumberClient(
          @Value("${app.random-api.url}") String url,
          @Value("${app.random-api.timeout-ms:3000}") long timeoutMs) {
    Duration timeout = Duration.ofMillis(timeoutMs);
    JdkClientHttpRequestFactory requestFactory =
            new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(timeout).build());
    requestFactory.setReadTimeout(timeout);

    this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    this.uri = URI.create(url);
  }

  public int nextNumber() {
    try {
      String body = restClient.get().uri(uri).retrieve().body(String.class);
      if (body == null) {
        throw new ExternalServiceException("Random number service returned an empty response", null);
      }
      int number = Integer.parseInt(body.trim());
      log.debug("Random number received: {}", number);
      return number;
    } catch (RestClientException | NumberFormatException e) {
      log.warn("Random number service call failed: {}", e.getMessage());
      throw new ExternalServiceException("Random number service is unavailable or returned an invalid response", e);
    }
  }
}