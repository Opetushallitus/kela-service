package fi.vm.sade.rajapinnat.kela.formaatti;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.tulos.service.TulosService;

/**
 * 
 * @author Jussi Jartamo
 * 
 */
@ContextConfiguration(classes = KelaFakeFtpSuite.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class KelaHaeSijoittelunTuloksetTesti {

    @Autowired
    TulosService tulosService;// = mock(TulosService.class);

    @Test
    public void haeDataaSijoittelunTulosApilla() {
        /*
         * HaeSijoitteluajotKriteeritTyyppi kriteerit = new
         * HaeSijoitteluajotKriteeritTyyppi();
         * kriteerit.getSijoitteluIdLista().add(1L); List<SijoitteluajoTyyppi>
         * sijoittelut = tulosService.haeSijoitteluajot(kriteerit);
         * 
         * for (SijoitteluajoTyyppi s : sijoittelut) {
         * 
         * }
         * 
         * tulosService.haeHakukohteet(123L,
         * null).get(0).getValintatapajonos().get
         * (0).getHakijaList().get(0).getTila();
         */
    }

}
