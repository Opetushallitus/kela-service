package fi.vm.sade.rajapinnat.kela.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TarjontaRespDTO {
    public TarjontaKomoDTO result;
}
