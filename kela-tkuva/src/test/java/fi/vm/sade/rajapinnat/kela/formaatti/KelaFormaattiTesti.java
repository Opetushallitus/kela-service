package fi.vm.sade.rajapinnat.kela.formaatti;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.rajapinnat.kela.tkuva.data.TKUVAALKU;
import fi.vm.sade.rajapinnat.kela.tkuva.data.TKUVALOPPU;
import fi.vm.sade.rajapinnat.kela.tkuva.data.TKUVAYHVA;

/**
 * 
 * @author Jussi Jartamo
 * 
 */
@Configuration
@ContextConfiguration(classes = KelaFormaattiTesti.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class KelaFormaattiTesti {

    @Test
    public void testaaTietueenPituus() throws Exception {

        TKUVAYHVA tietue = new TKUVAYHVA.Builder().setHenkilotunnus("010478123X").setEtunimet("Älfö Ölfär")
                .setOppilaitosnumero("").setHakukohde("").setOrganisaatio("").setSukunimi("Åke åkersön").setPoimintapaivamaara(new Date())
                .setValintapaivamaara(new Date()).setLukuvuosi(new Date()).setAjankohtaSyksy(true).build();
        Assert.assertTrue("Yksittäisen TKUVAYHVA tietueen koko tulee olla 150 merkkiä!",
                tietue.toByteArray().length == 150);
    }

    @Test
    public void testaaAlkuTietueenPituus() throws Exception {

        TKUVAALKU tietue = new TKUVAALKU.Builder().setAineistonnimi("AAAAA").setAjopaivamaara(new Date())
                .setOrganisaationimi("OOOOO").build();
        Assert.assertTrue("Yksittäisen alkutietueen koko tulee olla 150 merkkiä!", tietue.toByteArray().length == 150);
    }

    @Test
    public void testaaLoppuTietueenPituus() throws Exception {

        TKUVALOPPU tietue = new TKUVALOPPU.Builder().setAjopaivamaara(new Date()).setTietuelukumaara(232).build();
        Assert.assertTrue("Yksittäisen alkutietueen koko tulee olla 150 merkkiä!", tietue.toByteArray().length == 150);
    }

}
