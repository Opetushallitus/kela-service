package fi.vm.sade.rajapinnat.kela.util;

import org.springframework.web.client.RestTemplate;

public class RestTemplateUtil {

    public static void addCallerIdInterceptor(RestTemplate restTemplate) {
        restTemplate.getInterceptors().add(new CallerIdUtil.SpringHttpInterceptor());
    }

}
