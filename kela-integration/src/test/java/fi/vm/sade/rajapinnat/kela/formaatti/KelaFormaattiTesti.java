package fi.vm.sade.rajapinnat.kela.formaatti;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.rajapinnat.kela.integraatio.TKUVAYHVA;

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
    public void testaaFlatpackFormaatti() throws Exception {

        TKUVAYHVA tietue = new TKUVAYHVA.Builder().setHenkilotunnus("010478123X").setEtunimet("Älfö Ölfär")
                .setOppilaitos("").setLinjakoodi("").setSukunimi("Åke åkersön").setPoimintapaivamaara(new Date())
                .setValintapaivamaara(new Date()).setLukuvuosi(new Date()).setAjankohtaSyksy(true).build();

        Assert.assertTrue("Yksittäisen TKUVAYHVA tietueen koko tulee olla 150 merkkiä!",
                tietue.toByteArray().length == 150);
    }
}
