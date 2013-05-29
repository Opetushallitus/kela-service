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

import static org.junit.Assert.*;
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
import fi.vm.sade.rajapinnat.kela.dao.HakukohdeDAO;
import fi.vm.sade.rajapinnat.kela.utils.TestDataGenerator;
import fi.vm.sade.tarjonta.service.TarjontaPublicService;

@ContextConfiguration(locations = "classpath:spring/test-context.xml")
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
    })
@RunWith(SpringJUnit4ClassRunner.class)
public class WriteOPTIOPTest {
    
    @Autowired
    private WriteOPTIOP optiopWriter;
    
    private OrganisaatioService organisaatioServiceMock;
    private HakukohdeDAO hakukohdeDaoMock;
    private TarjontaPublicService tarjontaServiceMock;
    private OrganisaatioResource orgRMock;
    
    private TestDataGenerator generator;
    
    @Before
    public void initialize() {
        tarjontaServiceMock = mock(TarjontaPublicService.class);
        organisaatioServiceMock = mock(OrganisaatioService.class);
        orgRMock = mock(OrganisaatioResource.class);
        
        hakukohdeDaoMock = mock(HakukohdeDAO.class);
        optiopWriter.setOrganisaatioService(organisaatioServiceMock);
        optiopWriter.setTarjontaService(tarjontaServiceMock);
        optiopWriter.setHakukohdeDAO(hakukohdeDaoMock);
        optiopWriter.setOrganisaatioResource(orgRMock);
        
        generator = new TestDataGenerator();
        generator.setHakukohdeDaoMock(hakukohdeDaoMock);
        generator.setOrganisaatioServiceMock(organisaatioServiceMock);
        generator.setTarjontaServiceMock(tarjontaServiceMock);
        generator.setOrgRMock(orgRMock);
        
        generator.createOrganisaatioData();
        
    }

    @Test
    public void testWriteOptiopHappyPath() {
        try {
            
            optiopWriter.writeFile();
            
            FileInputStream fstream = new FileInputStream(optiopWriter.getFileName());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            
            int lineCount = 0;
            while ((strLine = br.readLine()) != null)   {
                if (lineCount == 1) {
                    assertTrue(strLine.startsWith(TestDataGenerator.OLKOODI1));//OPPIL_NRO
                    assertTrue(strLine.contains(generator.getCurrentDateStr()));//alkupvm
                    assertTrue(strLine.contains("1750"));//YHKoodi
                } else if (lineCount == 2) {
                    assertTrue(strLine.startsWith(TestDataGenerator.OLKOODI2));//OPPIL_NRO
                    assertTrue(strLine.contains(generator.getCurrentDateStr()));//alkupvm
                    assertTrue(strLine.contains("1751"));//YHKoodi
                }
                else if (lineCount > 3){
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
