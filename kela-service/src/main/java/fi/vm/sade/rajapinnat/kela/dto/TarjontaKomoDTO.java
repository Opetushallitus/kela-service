package fi.vm.sade.rajapinnat.kela.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.tarjonta.service.resources.dto.KomoDTO;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TarjontaKomoDTO implements Serializable {
    public TarjontaKoodiDTO opintojenLaajuusarvo;
}
