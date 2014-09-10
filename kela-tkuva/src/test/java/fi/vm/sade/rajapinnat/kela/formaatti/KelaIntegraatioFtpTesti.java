package fi.vm.sade.rajapinnat.kela.formaatti;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.rajapinnat.kela.tkuva.data.TKUVAYHVA;
import fi.vm.sade.rajapinnat.kela.tkuva.service.KelaFtpService;
import fi.vm.sade.rajapinnat.kela.tkuva.util.KelaUtil;

/**
 * 
 * @author Jussi Jartamo
 * 
 */
@Configuration
@ContextConfiguration(classes = KelaIntegraatioFtpTesti.class)
@ImportResource({ "classpath:kela-integraatio-testi-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore
@DirtiesContext
public class KelaIntegraatioFtpTesti {

    @Autowired
    KelaFtpService ftpService;

    @BeforeClass
    public static void setPuttyDynamicProxyInAction() {
        Properties props = System.getProperties();
        props.put("socksProxyHost", "127.0.0.1");
        props.put("socksProxyPort", "21");
        System.setProperties(props);
    }

    @Test
    public void testaaSijoitteluntulostenlahetystaKelanPalvelimelle() throws IOException {
        TKUVAYHVA a = new TKUVAYHVA.Builder().setHenkilotunnus("010478123X").setEtunimet("Ölfär Åke")
                .setOppilaitosnumero("AA:B").setOrganisaatio("A:B").setHakukohde("A:").setSukunimi("Åkerson").setPoimintapaivamaara(new Date())
                .setValintapaivamaara(new Date()).setLukuvuosi(new Date()).setKevaallaAlkavaKoulutus().build();
        TKUVAYHVA b = new TKUVAYHVA.Builder().setHenkilotunnus("010578123X").setEtunimet("Jorma Pirjo")
                .setOppilaitosnumero("CC:D").setOrganisaatio("Q:P").setHakukohde("B:").setSukunimi("Lötjönen").setPoimintapaivamaara(new Date())
                .setValintapaivamaara(new Date()).setLukuvuosi(new Date()).setSyksyllaAlkavaKoulutus().build();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
			outputStream.write(a.toByteArray());
			outputStream.write(KelaUtil.RIVINVAIHTO);
			outputStream.write(b.toByteArray());

			ByteArrayInputStream input = new ByteArrayInputStream(outputStream.toByteArray());
			ftpService.lahetaTiedosto(KelaUtil.createTiedostoNimiYhva14(new Date()), input);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
}
