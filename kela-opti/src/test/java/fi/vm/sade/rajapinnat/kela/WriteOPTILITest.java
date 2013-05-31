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
import static org.mockito.Mockito.*;

@ContextConfiguration(locations = "classpath:spring/test-context.xml")
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
    })
@RunWith(SpringJUnit4ClassRunner.class)
public class WriteOPTILITest {
    
    @Autowired
    WriteOPTILI optiliWriter;
    @Autowired
    OrganisaatioContainer orgContainer;
    
    private OrganisaatioService organisaatioServiceMock;
    private KelaDAO kelaDaoMock;
    private TarjontaPublicService tarjontaServiceMock;
    private OrganisaatioResource orgRMock;
    
    private TestDataGenerator testDataGenerator;

    
    
    @Before
    public void initialize() {
        
        
        tarjontaServiceMock = mock(TarjontaPublicService.class);
        organisaatioServiceMock = mock(OrganisaatioService.class);
        orgRMock = mock(OrganisaatioResource.class);   
        kelaDaoMock = mock(KelaDAO.class);
        
        setMockServices(optiliWriter);
        
        orgContainer.setHakukohdeDAO(kelaDaoMock);
        orgContainer.setOrganisaatioService(organisaatioServiceMock);
        
        
        testDataGenerator = new TestDataGenerator();
        testDataGenerator.setHakukohdeDaoMock(kelaDaoMock);
        testDataGenerator.setOrganisaatioServiceMock(organisaatioServiceMock);
        testDataGenerator.setTarjontaServiceMock(tarjontaServiceMock);
        testDataGenerator.setOrgRMock(orgRMock);
        
        testDataGenerator.generateTarjontaData();
        testDataGenerator.createOrganisaatioData();
    }
    
    @Test
    public void testWriteOptiliHappyPath() {
        try {
            orgContainer.fetchOrgnaisaatiot();
            optiliWriter.writeFile();
            
            FileInputStream fstream = new FileInputStream(optiliWriter.getFileName());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            
            int lineCount = 0;
            while ((strLine = br.readLine()) != null)   {
                if (lineCount == 1) {
                    assertTrue(strLine.contains(" 000 "));
                } else if (lineCount == 2) {
                    assertTrue(strLine.contains(" 011 "));
                } else if (lineCount > 3){
                    fail();
                }
                ++lineCount;
            }
            
            in.close();
            
        } catch (Exception ex) {
           fail();
        }
    }
    
    private void setMockServices(AbstractOPTIWriter kelaWriter) {
        kelaWriter.setOrganisaatioService(organisaatioServiceMock);
        kelaWriter.setTarjontaService(tarjontaServiceMock);
        kelaWriter.setHakukohdeDAO(kelaDaoMock);
        kelaWriter.setOrganisaatioResource(orgRMock);
    }
    


}
