/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.rajapinnat.kela.dao.impl;

import fi.vm.sade.rajapinnat.kela.TasoJaLaajuusContainer;
import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import javax.inject.Inject;
import javax.inject.Named;

import fi.vm.sade.rajapinnat.kela.tarjonta.model.Koulutusmoduuli;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.KoulutusmoduuliToteutus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Tests for KelaDAOImpl.
 * 
 * @author antto.sierla
 */
@ContextConfiguration(locations = "classpath:spring/test-context.xml")
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class KelaDAOImplITest {
    @Inject
    @Named("kelaDAO")
    private KelaDAOImpl testDao;

    private KelaDAOImpl partiallyMockedDao = Mockito.spy(KelaDAOImpl.class);
    
    public KelaDAOImplITest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of findHakukohdeByOid method, of class KelaDAOImpl.
     */
    @org.junit.Test
    public void testFindNonExistentHakukohdeByOid() {
        String oid = "1.2.246.562.20.70402217619";
        Hakukohde expResult = null;
        Hakukohde result = testDao.findHakukohdeByOid(oid);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of findHakukohdeByOid method, of class KelaDAOImpl.
     */
    @org.junit.Test
    public void testFindExistingHakukohdeByOid() {
        String oid = "1.2.246.562.20.70402217699";
        Hakukohde result = testDao.findHakukohdeByOid(oid);
        assertNotNull(result);
        assertEquals("Y15", result.getKelaLinjaKoodi());
    }


    @org.junit.Test
    public void testLukioTutkinnonTaso() {
        String komotoOid = "kmt1";
        String komoOid = "km1";

        KoulutusmoduuliToteutus komoto = new KoulutusmoduuliToteutus();
        komoto.setOid(komotoOid);
        Koulutusmoduuli komo = new Koulutusmoduuli();
        komo.setOid(komoOid);
        komo.setKoulutustyyppi_uri("|koulutustyyppi_2|");
        komoto.setKoulutusmoduuli(komo);

        doReturn(new ArrayList<String>()).when(partiallyMockedDao).getChildrenOids(komoOid);
        doReturn(komo).when(partiallyMockedDao).getKoulutusmoduuli(komoOid);

        TasoJaLaajuusContainer tutkinnonTaso = partiallyMockedDao.getKKTutkinnonTaso(komoto);
        assertEquals("001", tutkinnonTaso.getTasoCode());
    }

    @org.junit.Test
    public void testAmmatillinenPerustutkintoTutkinnonTaso() {
        String komotoOid = "kmt2";
        String komoOid = "km2";

        KoulutusmoduuliToteutus komoto = new KoulutusmoduuliToteutus();
        komoto.setOid(komotoOid);
        Koulutusmoduuli komo = new Koulutusmoduuli();
        komo.setOid(komoOid);
        komo.setKoulutustyyppi_uri("|koulutustyyppi_26|koulutustyyppi_4|koulutustyyppi_1|");
        komoto.setKoulutusmoduuli(komo);

        doReturn(new ArrayList<String>()).when(partiallyMockedDao).getChildrenOids(komoOid);
        doReturn(komo).when(partiallyMockedDao).getKoulutusmoduuli(komoOid);

        TasoJaLaajuusContainer tutkinnonTaso = partiallyMockedDao.getKKTutkinnonTaso(komoto);
        assertEquals("002", tutkinnonTaso.getTasoCode());
    }
}
