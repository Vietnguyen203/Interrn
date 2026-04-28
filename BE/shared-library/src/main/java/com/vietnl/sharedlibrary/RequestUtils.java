package com.vietnl.sharedlibrary;

import com.eps.shared.core.constants.HeaderKeys;
import com.eps.shared.core.json.JsonParserUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

public class RequestUtils {

  private static final WebClient webClient = WebClient.builder().build();
  private static final int DEFAULT_RETRY_ATTEMPTS = 3;
  private static final Duration RETRY_DELAY = Duration.ofMillis(500);

  public static Mono<String> request(
      String url,
      HttpMethod method,
      Map<String, Object> params,
      Object body,
      Map<String, String> headers) {

    String finalUrl = url;

    // Gắn query params nếu có
    if (params != null && !params.isEmpty()) {
      StringBuilder urlBuilder = new StringBuilder(url);
      urlBuilder.append(url.contains("?") ? "&" : "?");
      params.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
      finalUrl = urlBuilder.toString();
      if (finalUrl.endsWith("&")) {
        finalUrl = finalUrl.substring(0, finalUrl.length() - 1);
      }
    }

    WebClient.RequestBodySpec requestSpec = webClient.method(method).uri(URI.create(finalUrl));

    if (headers != null) {
      requestSpec.headers(httpHeaders -> headers.forEach(httpHeaders::add));
    }

    requestSpec.header(HeaderKeys.USER, MDC.get(HeaderKeys.USER));

    requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    if (body != null) {
      requestSpec.body(BodyInserters.fromValue(body));
    }

    return requestSpec
        .exchangeToMono(
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("{}")
                    .map(
                        bodyStr -> {
                          int status = response.statusCode().value();
                          Map<String, Object> bodyMap = JsonParserUtils.toObjectMap(bodyStr);
                          bodyMap.put("statusCode", status);
                          return JsonParserUtils.toJson(bodyMap);
                        }))
        .retryWhen(
            Retry.fixedDelay(DEFAULT_RETRY_ATTEMPTS, RETRY_DELAY)
                .filter(
                    throwable ->
                        throwable instanceof WebClientResponseException ex
                            && ex.getStatusCode().value() >= 500));
  }
}
