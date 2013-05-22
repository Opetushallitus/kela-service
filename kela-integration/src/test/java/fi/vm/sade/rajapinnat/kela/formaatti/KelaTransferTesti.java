package fi.vm.sade.rajapinnat.kela.formaatti;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.rajapinnat.kela.integraatio.KelaFtpPalvelin;
import fi.vm.sade.rajapinnat.kela.integraatio.TKUVAYHVA;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         Camel FTPS-reitityksen kokeiluun lokaalisti!
 */
@Ignore
@Configuration
@ImportResource({ "classpath:META-INF/spring/kela-context.xml" })
@ContextConfiguration(classes = KelaTransferTesti.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class KelaTransferTesti {

    @Autowired
    private KelaFtpPalvelin ftpService;

    @Test
    public void testaaFtpsIntegraatio() throws Exception {

        TKUVAYHVA tietue = new TKUVAYHVA.Builder().setHenkilotunnus("010478123X").setEtunimet("Älfö Ölfär")
                .setOppilaitos("").setLinjakoodi("").setSukunimi("Åke åkersön").setPoimintapaivamaara(new Date())
                .setValintapaivamaara(new Date()).setLukuvuosi(new Date()).setAjankohtaSyksy(true).build();

        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        StringBuilder nimi = new StringBuilder();
        nimi.append("RO.WOT.SR.D").append(formatter.format(new Date())).append(".YHVA14");
        ftpService.lahetaTiedosto(nimi.toString(), new ByteArrayInputStream(tietue.toByteArray()));

    }
}
