package fi.vm.sade.rajapinnat.kela.formaatti;

import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.net.util.Base64;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import fi.vm.sade.rajapinnat.kela.integraatio.TKUVAYHVA;

@Configuration
@ContextConfiguration(classes = KelaTekstiEnkoodausTesti.class)
public class KelaTekstiEnkoodausTesti {

    @Test
    public void testaaEnkoodaus() throws IOException {
        String skandinaavinenNimi = "Åke åke Äke äke Öke öke öööööäääääää";
        // final Charset LATIN1 = Charset.forName("ISO8859-1");
        TKUVAYHVA tietue = new TKUVAYHVA.Builder().setLukuvuosi(new Date()).setValintapaivamaara(new Date())
                .setPoimintapaivamaara(new Date()).setEtunimet(skandinaavinenNimi).setSukunimi(skandinaavinenNimi)
                .build();

        String kenttaanMahtuvaOsuus = skandinaavinenNimi.substring(0, 30);

        String UTF8BYTES = Base64.encodeBase64String(kenttaanMahtuvaOsuus.getBytes());
        String LATIN1BYTES = Base64.encodeBase64String(tietue.getEtunimet());

        Assert.assertTrue("Tavujen ei saa olla samoja ja Latin1 on lyhyempi!", !UTF8BYTES.equals(LATIN1BYTES)
                && UTF8BYTES.length() > LATIN1BYTES.length());

    }
}
