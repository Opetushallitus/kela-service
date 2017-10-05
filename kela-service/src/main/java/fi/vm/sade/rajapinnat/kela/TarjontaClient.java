package fi.vm.sade.rajapinnat.kela;

import fi.vm.sade.rajapinnat.kela.config.UrlConfiguration;
import fi.vm.sade.rajapinnat.kela.dto.TarjontaRespDTO;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TarjontaClient {

    private static final Logger LOG = Logger.getLogger(TarjontaClient.class);

    @Autowired
    UrlConfiguration urlConfiguration;

    public String getLaajuus(String komoId) {
        if(komoId == null || "".equals(komoId)) {
            return null;
        }
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(5000);
        factory.setConnectTimeout(5000);
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        factory.setHttpClient(httpClient);

        RestTemplate restTemplate = new RestTemplate(factory);
        try {
            TarjontaRespDTO resp = restTemplate.getForObject(urlConfiguration.url("tarjonta-service.komo.byid", komoId), TarjontaRespDTO.class);
            if(resp.result != null && resp.result.opintojenLaajuusarvo != null && resp.result.opintojenLaajuusarvo.arvo != null) {
                return resp.result.opintojenLaajuusarvo.arvo;
            } else {
                LOG.info("Tarjonta Komo:" + komoId + " didnt return opintojenlaajuus.");
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error querying KOMO ID:" + komoId +" from tarjonta.", e);
        }
        return null;
    }

}
