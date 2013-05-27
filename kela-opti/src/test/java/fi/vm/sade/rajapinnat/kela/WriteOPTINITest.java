package fi.vm.sade.rajapinnat.kela;

import static org.mockito.Mockito.mock;

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
import fi.vm.sade.rajapinnat.kela.dao.HakukohdeDAO;
import fi.vm.sade.tarjonta.service.TarjontaPublicService;

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
    
    private static final String TARJOAJA_OID = "oid:tarjoaja1";
    private static final String TARJOAJA_OLKOODI = "12345";
    private static final String TARJOAJA_OPJARJNRO = "01";
    private static final String KOULUTUSKOODI = "koulutus_301000";
    private static final String ALKAMISKAUSI_KEVAT = "KEVAT";
    
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
            //optiniWriter.writeFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
