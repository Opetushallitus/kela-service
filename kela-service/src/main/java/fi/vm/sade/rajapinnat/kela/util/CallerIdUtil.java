package fi.vm.sade.rajapinnat.kela.util;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallerIdUtil {
    private static final String CALLER_ID_HEADER_NAME = "Caller-Id";
    private static final String CALLER_ID_HEADER_VALUE = "1.2.246.562.10.00000000001.kela-service";

    public static class SpringHttpInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            request.getHeaders().set(CALLER_ID_HEADER_NAME, CALLER_ID_HEADER_VALUE);
            return execution.execute(request, body);
        }
    }

    public static class JaxWsInterceptor extends AbstractSoapInterceptor {

        public JaxWsInterceptor() {
            super(Phase.POST_LOGICAL);
        }

        @Override
        public void handleMessage(SoapMessage message) throws Fault {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put(CALLER_ID_HEADER_NAME, Arrays.asList(CALLER_ID_HEADER_VALUE));
            message.put(Message.PROTOCOL_HEADERS, headers);
        }
    }
}
