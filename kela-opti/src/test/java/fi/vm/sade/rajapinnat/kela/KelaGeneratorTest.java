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
public class KelaGeneratorTest {

    @Autowired
    private WriteOPTILI optiliWriter;
    @Autowired
    private WriteOPTINI optiniWriter;
    @Autowired
    private WriteOPTIOL optiolWriter;
    @Autowired
    private WriteOPTIOP optiopWriter;
    @Autowired
    private WriteOPTITU optituWriter;
    @Autowired
    private WriteOPTIYH optiyhWriter;
    @Autowired
    private WriteOPTIYT optiytWriter;
    @Autowired
    private KelaGenerator kelaGenerator;
    @Autowired
    private OrganisaatioContainer orgContainer;
    
    private OrganisaatioService organisaatioServiceMock;
    private HakukohdeDAO hakukohdeDaoMock;
    private TarjontaPublicService tarjontaServiceMock;
    private OrganisaatioResource orgRMock;
    
    private TestDataGenerator testDataGenerator;
    
    @Before
    public void initialize() {
        
        tarjontaServiceMock = mock(TarjontaPublicService.class);
        organisaatioServiceMock = mock(OrganisaatioService.class);
        orgRMock = mock(OrganisaatioResource.class);   
        hakukohdeDaoMock = mock(HakukohdeDAO.class);
        
        setMockServices(optiliWriter);
        setMockServices(optiniWriter);
        setMockServices(optiolWriter);
        setMockServices(optiopWriter);
        setMockServices(optituWriter);
        setMockServices(optiyhWriter);
        setMockServices(optiytWriter);
        
        orgContainer.setHakukohdeDAO(hakukohdeDaoMock);
        orgContainer.setOrganisaatioService(organisaatioServiceMock);
        
        
        testDataGenerator = new TestDataGenerator();
        testDataGenerator.setHakukohdeDaoMock(hakukohdeDaoMock);
        testDataGenerator.setOrganisaatioServiceMock(organisaatioServiceMock);
        testDataGenerator.setTarjontaServiceMock(tarjontaServiceMock);
        testDataGenerator.setOrgRMock(orgRMock);
        
        testDataGenerator.generateTarjontaData();
        testDataGenerator.createOrganisaatioData();
        
    }
    
    @Test
    public void testGenerateKelaFilesHappyPath() {
        
        kelaGenerator.generateKelaFiles();
        verifyKelaFile(optiliWriter, 4);
        verifyKelaFile(optiniWriter, 6);
        verifyKelaFile(optiolWriter, 4);
        verifyKelaFile(optiopWriter, 4);
        verifyKelaFile(optituWriter, 62);
        verifyKelaFile(optiyhWriter, 2);
        verifyKelaFile(optiytWriter, 6);
    }
    
    private void verifyKelaFile(AbstractOPTIWriter kelaWriter, int fileLength) {
        try {
            FileInputStream fstream = new FileInputStream(kelaWriter.getFileName());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            int lineCount = 0;
            while (br.readLine() != null)   {
                ++lineCount;
            }

            assertTrue(lineCount == fileLength);

            in.close();
        } catch (Exception ex) {
            fail();
        }
    }
    
    private void setMockServices(AbstractOPTIWriter kelaWriter) {
        kelaWriter.setOrganisaatioService(organisaatioServiceMock);
        kelaWriter.setTarjontaService(tarjontaServiceMock);
        kelaWriter.setHakukohdeDAO(hakukohdeDaoMock);
        kelaWriter.setOrganisaatioResource(orgRMock);
    }
    

}
