package fi.vm.sade.rajapinnat.kela.tkuva.service;

import java.io.InputStream;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         Camelin tarjoama Proxy : rajapinta tiedostojen siirtoon Kelalle
 * 
 *         Katso KelaMockFtpTesti
 * 
 */
public interface KelaFtpService {

    void lahetaTiedosto(String tiedostonNimi, InputStream tiedosto);
}
