package fi.vm.sade.rajapinnat.kela;

import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchCriteriaDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.rajapinnat.kela.dao.HakukohdeDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.MonikielinenTeksti;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
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
public class WriteOPTINITest {
    
    @Autowired
    private WriteOPTINI optiniWriter;

    private OrganisaatioService organisaatioServiceMock;
    private HakukohdeDAO hakukohdeDaoMock;
    private TarjontaPublicService tarjontaServiceMock;
    
    private static final String OLKOODI1 = "00001";
    private static final String OLKOODI2 = "00002";
    private static final String OPETUSPISTENRO = "01";
    private static final String OLTYYPPI = "oppilaitostyyppi_15#1";
    
    @Before
    public void initialize() {
        tarjontaServiceMock = mock(TarjontaPublicService.class);
        organisaatioServiceMock = mock(OrganisaatioService.class);
        
        hakukohdeDaoMock = mock(HakukohdeDAO.class);
        optiniWriter.setOrganisaatioService(organisaatioServiceMock);
        optiniWriter.setTarjontaService(tarjontaServiceMock);
        optiniWriter.setHakukohdeDAO(hakukohdeDaoMock);
        
    }
    
    @Test
    public void testWriteOptiniHappyPath() {
        try {
            
            List<OrganisaatioPerustietoType> organisaatiot = new ArrayList<OrganisaatioPerustietoType>();
            organisaatiot.addAll(createOrganisaatiot());
            when(organisaatioServiceMock.searchBasicOrganisaatios((OrganisaatioSearchCriteriaDTO)anyObject())).thenReturn(organisaatiot);
            
            optiniWriter.writeFile();
            
            FileInputStream fstream = new FileInputStream(optiniWriter.getFileName());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            
            int lineCount = 0;
            while ((strLine = br.readLine()) != null)   {
                if (lineCount == 1) {
                    assertTrue(strLine.contains(" Harman lukio "));
                    assertTrue(strLine.contains(OLKOODI1));
                } else if (lineCount == 2) {
                    assertTrue(strLine.contains(" Alajarven lukio "));
                    assertTrue(strLine.contains(OLKOODI2));
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
    
    private List<OrganisaatioPerustietoType> createOrganisaatiot() {
        List<OrganisaatioPerustietoType> organisaatiot = new ArrayList<OrganisaatioPerustietoType>();

        organisaatiot.add(createOrg("OID:" + OLKOODI1, 1, "Harman lukio", OLKOODI1, OLTYYPPI, OrganisaatioTyyppi.OPPILAITOS));
        
        organisaatiot.add(createOrg("OID:" + OLKOODI2, 2, "Alajarven lukio", OLKOODI2, OLTYYPPI, OrganisaatioTyyppi.OPPILAITOS));
        OrganisaatioPerustietoType opetuspiste1 = createOrg("OID:" + OLKOODI1 + OPETUSPISTENRO, 3, "Harman lukio", null, null, OrganisaatioTyyppi.OPETUSPISTE);
        opetuspiste1.setParentOid("OID:" + OLKOODI1);
        organisaatiot.add(opetuspiste1);
        OrganisaatioPerustietoType opetuspiste2 = createOrg("OID:" + OLKOODI2 + OPETUSPISTENRO, 4, "Alajarven lukio", null, null, OrganisaatioTyyppi.OPETUSPISTE);
        opetuspiste2.setParentOid("OID:" + OLKOODI2);
        organisaatiot.add(opetuspiste2);
        
        return organisaatiot;
    }
    
    private OrganisaatioPerustietoType createOrg(String oid, long id, String nimi, String olkoodi, String olTyyppi, OrganisaatioTyyppi orgTyyppi) {
        OrganisaatioPerustietoType ol1 = new OrganisaatioPerustietoType();
        ol1.setOid(oid);
        ol1.setNimiFi(nimi);
        ol1.setOppilaitosKoodi(olkoodi);
        ol1.setOppilaitostyyppi(olTyyppi);
        ol1.getTyypit().add(orgTyyppi);
        
        Organisaatio orgE = new Organisaatio();
        orgE.setOid(oid);
        orgE.setId(Long.valueOf(id));
        orgE.setOpetuspisteenJarjNro(OPETUSPISTENRO);
        orgE.getKielet().add("kielivalikoima_fi");
        MonikielinenTeksti nimiE = new MonikielinenTeksti();
        nimiE.setId(id);
        orgE.setNimi(nimiE);
        when(hakukohdeDaoMock.findOrganisaatioByOid(oid)).thenReturn(orgE);
        
        return ol1;
    }

}
