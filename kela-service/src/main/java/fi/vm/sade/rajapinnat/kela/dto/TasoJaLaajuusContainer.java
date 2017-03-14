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
        tasoCode = "071";
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer hammasLaakis(String komoId) {
        tasoCode = "070";
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer onlyAlempi(String komoId) {
        tasoCode = "050";
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer onlyYlempi(String komoId) {
        tasoCode = "061";
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer ylempiAlempi(String komoId1, String komoId2) {
        tasoCode = "060";
        this.komoId1 = komoId1;
        this.komoId2 = komoId2;
        return this;
    }

    public TasoJaLaajuusContainer eiTasoa() {
        tasoCode = "   ";
        return this;
    }

    public boolean isYlempi() { return "061".equals(tasoCode); }
    public boolean isLaakis() { return "071".equals(tasoCode); }
    public boolean isHammaslaakis() {
        return "071".equals(tasoCode);
    }
    public boolean isAlempi() { return "050".equals(tasoCode); }
    public boolean isYlempiAlempi() { return "060".equals(tasoCode); }

    public boolean hasTaso() {
        return this.tasoCode != null && "   ".equals(tasoCode) == false;
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
        if(komoId1 == null || "".equals(komoId1)) {
            return null;
        }
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(5000);
        factory.setConnectTimeout(5000);
        RestTemplate restTemplate = new RestTemplate(factory);
        try {
            TarjontaKomoDTO resp = restTemplate.getForObject(urlConfiguration.url("tarjonta-service.komo.byid", komoId), TarjontaKomoDTO.class);
            if(resp.opintojenLaajuusarvo != null && resp.opintojenLaajuusarvo.arvo != null) {
                return resp.opintojenLaajuusarvo.arvo;
            } else {
                LOG.info("Tarjonta Komo:" + komoId + " didnt return opintojenlaajuus.");
                return "";
            }
        } catch (Exception e) {
            LOG.error("Error querying KOMO ID:" + komoId +" from tarjonta.", e);
        }
        return "";
    }

}

