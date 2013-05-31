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
import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.utils.TestDataGenerator;
import fi.vm.sade.tarjonta.service.TarjontaPublicService;

import static org.junit.Assert.*;

@ContextConfiguration(locations = "classpath:spring/test-context.xml")
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
    })
@RunWith(SpringJUnit4ClassRunner.class)
public class WriteOPTINITest {
    
    @Autowired
    private WriteOPTINI optiniWriter;
    @Autowired
    OrganisaatioContainer orgContainer;

    private OrganisaatioService organisaatioServiceMock;
    private KelaDAO kelaDaoMock;
    private TarjontaPublicService tarjontaServiceMock;
    private OrganisaatioResource orgRMock;
    
    private TestDataGenerator generator;
    
    @Before
    public void initialize() {
        tarjontaServiceMock = mock(TarjontaPublicService.class);
        organisaatioServiceMock = mock(OrganisaatioService.class);
        orgRMock = mock(OrganisaatioResource.class);
        
        kelaDaoMock = mock(KelaDAO.class);
        optiniWriter.setOrganisaatioService(organisaatioServiceMock);
        optiniWriter.setTarjontaService(tarjontaServiceMock);
        optiniWriter.setHakukohdeDAO(kelaDaoMock);
        optiniWriter.setOrganisaatioResource(orgRMock);
        
        generator = new TestDataGenerator();
        generator.setHakukohdeDaoMock(kelaDaoMock);
        generator.setOrganisaatioServiceMock(organisaatioServiceMock);
        generator.setTarjontaServiceMock(tarjontaServiceMock);
        generator.setOrgRMock(orgRMock);
        
        orgContainer.setHakukohdeDAO(kelaDaoMock);
        orgContainer.setOrganisaatioService(organisaatioServiceMock);
        
        generator.createOrganisaatioData();
        
    }
    
    @Test
    public void testWriteOptiniHappyPath() {
        try {
            orgContainer.fetchOrgnaisaatiot();
            optiniWriter.writeFile();
            
            FileInputStream fstream = new FileInputStream(optiniWriter.getFileName());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            
            int lineCount = 0;
            while ((strLine = br.readLine()) != null)   {
                if (lineCount == 1) {
                    assertTrue(strLine.contains(" Harman lukio "));
                    assertTrue(strLine.contains(TestDataGenerator.OLKOODI1));
                } else if (lineCount == 2) {
                    assertTrue(strLine.contains(" Alajarven lukio "));
                    assertTrue(strLine.contains(TestDataGenerator.OLKOODI2));
                } else if (lineCount == 3) {
                    assertTrue(strLine.contains(" Harman lukio "));
                } else if (lineCount == 4) {
                    assertTrue(strLine.contains(" Alajarven lukio "));
                }
                else if (lineCount > 5){
                    fail();
                }
                ++lineCount;
            }
            assertTrue(lineCount == 6);
            
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }
    
    

}
