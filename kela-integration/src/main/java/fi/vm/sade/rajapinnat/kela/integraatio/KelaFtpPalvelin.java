package fi.vm.sade.rajapinnat.kela.integraatio;

import java.io.InputStream;

public interface KelaFtpPalvelin {

    void lahetaTiedosto(String tiedostonNimi, InputStream tiedosto);
}
