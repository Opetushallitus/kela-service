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
public class WriteOPTIYTTest {
    
    @Autowired
    private WriteOPTIYT optiytWriter;
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
        
        optiytWriter.setOrganisaatioService(organisaatioServiceMock);
        optiytWriter.setTarjontaSearchService(tarjontaServiceMock);
        optiytWriter.setHakukohdeDAO(kelaDaoMock);
        optiytWriter.setOrganisaatioResource(orgRMock);
        optiytWriter.setPath(GEN_PATH);
        
        generator = new TestDataGenerator();
        generator.setHakukohdeDaoMock(kelaDaoMock);
        generator.setOrganisaatioServiceMock(organisaatioServiceMock);
        generator.setTarjontaServiceMock(tarjontaServiceMock);
        generator.setOrgRMock(orgRMock);
        generator.setOrganisaatioSearchServiceMock(organisaatioSearchServiceMock);
        
        orgContainer.setHakukohdeDAO(kelaDaoMock);
        orgContainer.setOrganisaatioSearchService(organisaatioSearchServiceMock);
        
        generator.createOrganisaatioData();
        
    }
    
    @Test
    public void testWriteOPTIYTHappyPath() {
        try {
            orgContainer.fetchOrgnaisaatiot();
            optiytWriter.writeFile();
            
            FileInputStream fstream = new FileInputStream(optiytWriter.getFileName());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            
            int lineCount = 0;
            /*while ((strLine = br.readLine()) != null)   {
                if (lineCount == 1) {
                    assertTrue(strLine.contains(TestDataGenerator.OL_FAX_PREFIX + " " +TestDataGenerator.OLKOODI1));
                    assertTrue(strLine.contains(TestDataGenerator.OL_PUHELIN_PREFIX + " " + TestDataGenerator.OLKOODI1));
                    assertTrue(strLine.contains(TestDataGenerator.OL_WWW_PREFIX + TestDataGenerator.OLKOODI1));
                    assertTrue(strLine.contains(TestDataGenerator.OLKOODI1 + TestDataGenerator.EMAIL_SUFFIX));
                    assertTrue(strLine.contains(TestDataGenerator.KAYNTI_KATUOSOITE + 1));
                    assertTrue(strLine.contains("62501"));
                    
                } else if (lineCount == 2) {
                    assertTrue(strLine.contains(TestDataGenerator.OL_FAX_PREFIX + " " +TestDataGenerator.OLKOODI2));
                    assertTrue(strLine.contains(TestDataGenerator.OL_PUHELIN_PREFIX + " " + TestDataGenerator.OLKOODI2));
                    assertTrue(strLine.contains(TestDataGenerator.OL_WWW_PREFIX + TestDataGenerator.OLKOODI2));
                    assertTrue(strLine.contains(TestDataGenerator.OLKOODI2 + TestDataGenerator.EMAIL_SUFFIX));
                    assertTrue(strLine.contains(TestDataGenerator.KAYNTI_KATUOSOITE + 2));
                    assertTrue(strLine.contains("62501"));
                    
                } else if (lineCount == 3) {
                    assertTrue(strLine.contains(TestDataGenerator.OP_FAX_PREFIX + " " + TestDataGenerator.OPETUSPISTENRO + 3));
                    assertTrue(strLine.contains(TestDataGenerator.OP_PUHELIN_PREFIX + " " + TestDataGenerator.OPETUSPISTENRO + 3));
                    assertTrue(strLine.contains(TestDataGenerator.OP_WWW_PREFIX + TestDataGenerator.OPETUSPISTENRO + 3));
                    assertTrue(strLine.contains(TestDataGenerator.OPETUSPISTENRO + 3 + TestDataGenerator.EMAIL_SUFFIX));
                    assertTrue(strLine.contains(TestDataGenerator.KAYNTI_KATUOSOITE + 3));
                    assertTrue(strLine.contains("62501"));
                } else if (lineCount == 4) {
                    assertTrue(strLine.contains(TestDataGenerator.OP_FAX_PREFIX + " " + TestDataGenerator.OPETUSPISTENRO + 4));
                    assertTrue(strLine.contains(TestDataGenerator.OP_PUHELIN_PREFIX + " " + TestDataGenerator.OPETUSPISTENRO + 4));
                    assertTrue(strLine.contains(TestDataGenerator.OP_WWW_PREFIX + TestDataGenerator.OPETUSPISTENRO + 4));
                    assertTrue(strLine.contains(TestDataGenerator.OPETUSPISTENRO + 4 + TestDataGenerator.EMAIL_SUFFIX));
                    assertTrue(strLine.contains(TestDataGenerator.KAYNTI_KATUOSOITE + 4));
                    assertTrue(strLine.contains("62501"));
                }
                else if (lineCount > 5){
                    fail();
                }
                ++lineCount;
            }
            assertTrue(lineCount == 6);*/
            
            in.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
        
    }

}
