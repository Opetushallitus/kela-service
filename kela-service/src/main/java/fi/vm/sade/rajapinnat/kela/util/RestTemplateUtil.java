package fi.vm.sade.rajapinnat.kela.util;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class RestTemplateUtil {
    private static final String CALLER_ID_HEADER_NAME = "Caller-Id";
    private static final String CALLER_ID_HEADER_VALUE = "1.2.246.562.10.00000000001.kela-service";

    public static class SpringHttpInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            request.getHeaders().set(CALLER_ID_HEADER_NAME, CALLER_ID_HEADER_VALUE);
            return execution.execute(request, body);
        }
    }
    public static void addCallerIdInterceptor(RestTemplate restTemplate) {
        restTemplate.getInterceptors().add(new SpringHttpInterceptor());
    }
}
