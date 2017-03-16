package fi.vm.sade.rajapinnat.kela.dto;

import fi.vm.sade.organisaatio.resource.api.TasoJaLaajuusDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.rajapinnat.kela.KelaGenerator;
import fi.vm.sade.rajapinnat.kela.config.UrlConfiguration;
import org.apache.log4j.Logger;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class TasoJaLaajuusContainer {

    private static final Logger LOG = Logger.getLogger(TasoJaLaajuusContainer.class);

    private static final String ONLYALEMPI = "050";
    private static final String ALEMPIYLEMPI = "060";
    private static final String ONLYYLEMPI = "061";
    private static final String LAAKIS = "070";
    private static final String HAMMASLAAKIS = "071";
    private static final String EITASOA = "   ";

    private String tasoCode;
    private String komoId1;
    private String komoId2;

    public String getTasoCode() {
        return tasoCode;
    }

    public String getKomoId1() {
        return komoId1;
    }

    public String getKomoId2() {
        return komoId2;
    }


    public TasoJaLaajuusContainer laakis(String komoId) {
        tasoCode = LAAKIS;
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer hammasLaakis(String komoId) {
        tasoCode = HAMMASLAAKIS;
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer onlyAlempi(String komoId) {
        tasoCode = ONLYALEMPI;
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer onlyYlempi(String komoId) {
        tasoCode = ONLYYLEMPI;
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer alempiYlempi(String komoId1, String komoId2) {
        tasoCode = ALEMPIYLEMPI;
        this.komoId1 = komoId1;
        this.komoId2 = komoId2;
        return this;
    }

    public TasoJaLaajuusContainer eiTasoa() {
        tasoCode = EITASOA;
        return this;
    }

    public boolean isYlempi() { return ONLYYLEMPI.equals(tasoCode); }
    public boolean isLaakis() { return LAAKIS.equals(tasoCode); }
    public boolean isHammaslaakis() {
        return HAMMASLAAKIS.equals(tasoCode);
    }
    public boolean isAlempi() { return ONLYALEMPI.equals(tasoCode); }
    public boolean isAlempiYlempi() { return ALEMPIYLEMPI.equals(tasoCode); }

    public boolean hasTaso() {
        return this.tasoCode != null && EITASOA.equals(tasoCode) == false;
    }

    public TasoJaLaajuusDTO toDTO(UrlConfiguration urlConfiguration) {
        TasoJaLaajuusDTO resp = new TasoJaLaajuusDTO();
        resp.setTasoCode(this.tasoCode);
        resp.setLaajuus1(this.getLaajuus(this.komoId1, urlConfiguration));
        resp.setLaajuus2(this.getLaajuus(this.komoId2, urlConfiguration));
        resp.setKomoId1(this.komoId1);
        resp.setKomoId2(this.komoId2);
        return resp;
    }


    private String getLaajuus(String komoId, UrlConfiguration urlConfiguration) {
        if(komoId == null || "".equals(komoId)) {
            return null;
        }
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(5000);
        factory.setConnectTimeout(5000);
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

