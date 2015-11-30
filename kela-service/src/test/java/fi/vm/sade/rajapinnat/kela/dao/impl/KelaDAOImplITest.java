/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.rajapinnat.kela.dao.impl;

import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

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
}
