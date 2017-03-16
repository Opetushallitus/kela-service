package fi.vm.sade.rajapinnat.kela;

import fi.vm.sade.organisaatio.resource.api.TasoJaLaajuusDTO;
import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class TasoJaLaajuusContainerTest {

    @Test
    public void toDTOTest() {
        TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
        TarjontaClient tarjontaMock = Mockito.mock(TarjontaClient.class);
        when(tarjontaMock.getLaajuus("1")).thenReturn("130");
        when(tarjontaMock.getLaajuus("2")).thenReturn("170");
        when(tarjontaMock.getLaajuus("3")).thenReturn("180+120");
        when(tarjontaMock.getLaajuus("4")).thenReturn("180+120/150");
        when(tarjontaMock.getLaajuus("5")).thenReturn(null);

        cont.laakis("2");
        TasoJaLaajuusDTO resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals(resp.getLaajuus1(), "170");

        cont.laakis("3");
        resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals(resp.getLaajuus1(), "180");
        Assert.assertEquals(resp.getLaajuus2(), "120");

        cont.laakis("4");
        resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals(resp.getLaajuus1(), "330");
        Assert.assertEquals(resp.getLaajuus2(), null);

        cont.alempiYlempi("1", "2");
        resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals(resp.getLaajuus1(), "130");
        Assert.assertEquals(resp.getLaajuus2(), "170");

        cont.alempiYlempi("3", "1");
        resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals(resp.getLaajuus1(), "180");
        Assert.assertEquals(resp.getLaajuus2(), "120");

        cont.alempiYlempi("1", "3");
        resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals(resp.getLaajuus1(), "180");
        Assert.assertEquals(resp.getLaajuus2(), "120");

        cont.alempiYlempi("1", "5");
        resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals(resp.getLaajuus1(), "130");
        Assert.assertEquals(resp.getLaajuus2(), null);


    }

}
