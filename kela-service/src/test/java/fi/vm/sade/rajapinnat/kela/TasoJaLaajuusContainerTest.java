package fi.vm.sade.rajapinnat.kela;

import fi.vm.sade.organisaatio.resource.api.TasoJaLaajuusDTO;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class TasoJaLaajuusContainerTest {

    private TarjontaClient tarjontaMock = Mockito.mock(TarjontaClient.class);

    private String komoIdLaajuus130 = "1";
    private String komoIdLaajuus170 = "2";
    private String komoIdLaajuus180Plus170 = "3";
    private String komoIdLaajuus180Plus120Kautta150 = "4";
    private String komoIdLaajuusNull = "5";
    private String komoIdLaajuusLaajuus80 = "6";
    private String komoIdLaajuus7 = "7";
    private String komoIdLaajuusTyhja = "8";


    @Before
    public void setup() {
        when(tarjontaMock.getLaajuus(komoIdLaajuus130)).thenReturn("130");
        when(tarjontaMock.getLaajuus(komoIdLaajuus170)).thenReturn("170");
        when(tarjontaMock.getLaajuus(komoIdLaajuus180Plus170)).thenReturn("180+120");
        when(tarjontaMock.getLaajuus(komoIdLaajuus180Plus120Kautta150)).thenReturn("180+120/150");
        when(tarjontaMock.getLaajuus(komoIdLaajuusNull)).thenReturn(null);
        when(tarjontaMock.getLaajuus(komoIdLaajuusLaajuus80)).thenReturn("80");
        when(tarjontaMock.getLaajuus(komoIdLaajuus7)).thenReturn(komoIdLaajuus7);
        when(tarjontaMock.getLaajuus(komoIdLaajuusTyhja)).thenReturn("");
    }

    @Test
    public void testLaakis170() {
        TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
        cont.laakis(komoIdLaajuus170);
        TasoJaLaajuusDTO resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals("170", resp.getLaajuus1());
    }

    @Test
    public void TestLaakis180plus120() {
        TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
        cont.laakis(komoIdLaajuus180Plus170);
        TasoJaLaajuusDTO resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals("180", resp.getLaajuus1());
        Assert.assertEquals("120", resp.getLaajuus2());
    }

    @Test
    public void testLaakis330() {
        TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
        cont.laakis(komoIdLaajuus180Plus120Kautta150);
        TasoJaLaajuusDTO resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals("330", resp.getLaajuus1());
        Assert.assertEquals(null, resp.getLaajuus2());
    }

    @Test
    public void testAlempiYlempi130plus170() {
        TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
        cont.alempiYlempi(komoIdLaajuus130, komoIdLaajuus170);
        TasoJaLaajuusDTO resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals("130", resp.getLaajuus1());
        Assert.assertEquals("170", resp.getLaajuus2());
    }

    @Test
    public void testYlempiAlempi180plus120() {
        TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
        cont.alempiYlempi(komoIdLaajuus180Plus170, komoIdLaajuus130);
        TasoJaLaajuusDTO resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals("180", resp.getLaajuus1());
        Assert.assertEquals("120", resp.getLaajuus2());
    }

    @Test
    public void testAlempiYlempi180plus120() {
        TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
        cont.alempiYlempi(komoIdLaajuus130, komoIdLaajuus180Plus170);
        TasoJaLaajuusDTO resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals("180", resp.getLaajuus1());
        Assert.assertEquals("120", resp.getLaajuus2());
    }

    @Test
    public void testAlempiYlempi() {
        TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
        cont.alempiYlempi(komoIdLaajuus130, komoIdLaajuusNull);
        TasoJaLaajuusDTO resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals("130", resp.getLaajuus1());
        Assert.assertEquals(null, resp.getLaajuus2());
    }

    @Test
    public void testAlempiYlempi80Plus7() {
        TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
        cont.alempiYlempi(komoIdLaajuusLaajuus80, komoIdLaajuus7);
        TasoJaLaajuusDTO resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals("080", resp.getLaajuus1());
        Assert.assertEquals("007", resp.getLaajuus2());
    }

    @Test
    public void testEmptyString() {
        TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
        cont.alempiYlempi(komoIdLaajuus7, komoIdLaajuusTyhja);
        TasoJaLaajuusDTO resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals("007", resp.getLaajuus1());
        Assert.assertEquals(null, resp.getLaajuus2());
    }

    @Test
    public void testOnlyAlempi() {
        TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
        cont.onlyAlempi(komoIdLaajuus180Plus170);
        TasoJaLaajuusDTO resp = cont.toDTO(tarjontaMock);
        Assert.assertEquals("180", resp.getLaajuus1());
        Assert.assertEquals(null, resp.getLaajuus2());
    }

}
