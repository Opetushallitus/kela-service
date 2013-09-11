/*
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.rajapinnat.kela;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.resource.OrganisaatioResource;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.utils.TestDataGenerator;
import fi.vm.sade.tarjonta.service.TarjontaPublicService;
import fi.vm.sade.tarjonta.service.search.TarjontaSearchService;

@ContextConfiguration(locations = "classpath:spring/test-context.xml")
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
    })
@RunWith(SpringJUnit4ClassRunner.class)
public class WriteOPTIYHTest {
    
    private static final String ALKUTIETUE = "0000000000ALKU";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????";
    
    @Autowired
    private WriteOPTIYH optiyhWriter;
    
    @Autowired
    OrganisaatioContainer orgContainer;
    
    private OrganisaatioService organisaatioServiceMock;
    private KelaDAO kelaDaoMock;
    private TarjontaSearchService tarjontaServiceMock;
    private OrganisaatioResource orgRMock;
    private OrganisaatioSearchService organisaatioSearchServiceMock;
    
    private TestDataGenerator generator;
    
    private static final String GEN_PATH = "target/ftps";
    
    @Before
    public void initialize() {
        tarjontaServiceMock = mock(TarjontaSearchService.class);
        organisaatioServiceMock = mock(OrganisaatioService.class);
        kelaDaoMock = mock(KelaDAO.class);
        orgRMock = mock(OrganisaatioResource.class);
        organisaatioSearchServiceMock = mock(OrganisaatioSearchService.class);
        
        optiyhWriter.setHakukohdeDAO(kelaDaoMock);
        optiyhWriter.setOrganisaatioResource(orgRMock);
        optiyhWriter.setPath(GEN_PATH);
        
        generator = new TestDataGenerator();
        generator.setHakukohdeDaoMock(kelaDaoMock);
        generator.setOrganisaatioServiceMock(organisaatioServiceMock);
        generator.setTarjontaServiceMock(tarjontaServiceMock);
        generator.setOrgRMock(orgRMock);
        generator.setOrganisaatioSearchServiceMock(organisaatioSearchServiceMock);
        
        orgContainer.setHakukohdeDAO(kelaDaoMock);
        orgContainer.setOrganisaatioSearchService(organisaatioSearchServiceMock);
        
        generator.createLiitosData();
        
    }
    
   @Test
   public void testWriteOptiyhHappyPath() {
       try {
           optiyhWriter.setPath(GEN_PATH);
           optiyhWriter.writeFile();
           
           FileInputStream fstream = new FileInputStream(optiyhWriter.getFileName());
           DataInputStream in = new DataInputStream(fstream);
           BufferedReader br = new BufferedReader(new InputStreamReader(in));
           String strLine;
           
           int lineCount = 0;
           while ((strLine = br.readLine()) != null)   {
               if (lineCount == 0) {
                   assertTrue(strLine.contains(ALKUTIETUE));
               } 
               if (lineCount == 1) {
                   assertTrue(strLine.contains("00000"));
                   assertTrue(strLine.contains("00001"));
               }
               if (lineCount == 2) {
                   assertTrue(strLine.contains("00000"));
                   assertTrue(strLine.contains("00002"));
               }
               if (lineCount == 3) {
                   assertTrue(strLine.contains(LOPPUTIETUE));
               }
               if (lineCount > 3) {
                   fail();
               }
               ++lineCount;
           }
           assertTrue(lineCount == 4);
           
           in.close();
           
       } catch (Exception ex) {
           ex.printStackTrace();
           fail();
       }
       
   }

}
