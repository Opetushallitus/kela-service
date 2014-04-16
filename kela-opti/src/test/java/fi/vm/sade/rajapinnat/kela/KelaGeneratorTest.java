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
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.google.gwt.editor.client.Editor.Ignore;

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
    private KelaDAO kelaDaoMock;
    private TarjontaSearchService tarjontaServiceMock;
    private OrganisaatioResource orgRMock;
    private OrganisaatioSearchService organisaatioSearchServiceMock;
    
    private TestDataGenerator testDataGenerator;
    
    private static final String FILEPATH = "c:\\";
    private static final int FTPPORT = 8919;
    private static final String FTPHOST = "127.0.0.1";
    private static final String PROTOCOL = "ftp";
    
    private static final String GEN_PATH = "target/ftps";
    
    @Before
    public void initialize() {
        
        tarjontaServiceMock = mock(TarjontaSearchService.class);
        organisaatioServiceMock = mock(OrganisaatioService.class);
        orgRMock = mock(OrganisaatioResource.class);   
        kelaDaoMock = mock(KelaDAO.class);
        organisaatioSearchServiceMock = mock(OrganisaatioSearchService.class);
        
        setMockServices(optiliWriter);
        setMockServices(optiniWriter);
        setMockServices(optiolWriter);
        setMockServices(optiopWriter);
        setMockServices(optituWriter);
        setMockServices(optiyhWriter);
        setMockServices(optiytWriter);
        
        orgContainer.setHakukohdeDAO(kelaDaoMock);
        orgContainer.setOrganisaatioSearchService(organisaatioSearchServiceMock);
        
        
        testDataGenerator = new TestDataGenerator();
        testDataGenerator.setHakukohdeDaoMock(kelaDaoMock);
        testDataGenerator.setOrganisaatioServiceMock(organisaatioServiceMock);
        testDataGenerator.setTarjontaServiceMock(tarjontaServiceMock);
        testDataGenerator.setOrgRMock(orgRMock);
        testDataGenerator.setOrganisaatioSearchServiceMock(organisaatioSearchServiceMock);
        
        testDataGenerator.generateTarjontaData();
        testDataGenerator.createOrganisaatioData();
        testDataGenerator.createLiitosData();
        
    }
    
    @Test
    public void testGenerateKelaFilesHappyPath() {
        
        kelaGenerator.generateKelaFiles();
        verifyKelaFile(optiliWriter, 4);
        verifyKelaFile(optiniWriter, 6);
        verifyKelaFile(optiolWriter, 4);
        verifyKelaFile(optiopWriter, 4);
        verifyKelaFile(optituWriter, 62);
        verifyKelaFile(optiyhWriter, 4);
        verifyKelaFile(optiytWriter, 6);
    }
    
    @Test
    public void gestTransferFiles() {
        try {
            FakeFtpServer fakeFtpServer = new FakeFtpServer();
            fakeFtpServer.addUserAccount(new UserAccount(kelaGenerator.getUsername(), kelaGenerator.getPassword(), FILEPATH));
            FileSystem fileSystem = new WindowsFakeFileSystem();
            fileSystem.add(new DirectoryEntry(FILEPATH));
            fakeFtpServer.setFileSystem(fileSystem);
            
            fakeFtpServer.setServerControlPort(FTPPORT);
            fakeFtpServer.start();
            kelaGenerator.setTargetPath("");
            kelaGenerator.setHost(FTPHOST + ":" + FTPPORT);
            kelaGenerator.setProtocol(PROTOCOL);
            
            kelaGenerator.transferFiles();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
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
        kelaWriter.setTarjontaSearchService(tarjontaServiceMock);
        kelaWriter.setHakukohdeDAO(kelaDaoMock);
        kelaWriter.setOrganisaatioResource(orgRMock);
        kelaWriter.setPath(GEN_PATH);
    }
    

}
