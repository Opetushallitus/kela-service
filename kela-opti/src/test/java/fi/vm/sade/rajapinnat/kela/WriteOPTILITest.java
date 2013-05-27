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
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.rajapinnat.kela.dao.HakukohdeDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.tarjonta.service.TarjontaPublicService;
import fi.vm.sade.tarjonta.service.types.HaeHakukohteetKyselyTyyppi;
import fi.vm.sade.tarjonta.service.types.HaeHakukohteetVastausTyyppi;
import fi.vm.sade.tarjonta.service.types.HaeKoulutuksetKyselyTyyppi;
import fi.vm.sade.tarjonta.service.types.HaeHakukohteetVastausTyyppi.HakukohdeTulos;
import fi.vm.sade.tarjonta.service.types.HaeKoulutuksetVastausTyyppi.KoulutusTulos;
import fi.vm.sade.tarjonta.service.types.HaeKoulutuksetVastausTyyppi;
import fi.vm.sade.tarjonta.service.types.HakukohdeListausTyyppi;
import fi.vm.sade.tarjonta.service.types.KoodistoKoodiTyyppi;
import fi.vm.sade.tarjonta.service.types.KoulutusListausTyyppi;
import fi.vm.sade.tarjonta.service.types.TarjoajaTyyppi;

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
        optiliWriter.setOrganisaatioService(organisaatioServiceMock);
        optiliWriter.setTarjontaService(tarjontaServiceMock);
        optiliWriter.setHakukohdeDAO(hakukohdeDaoMock);
        
    }
    
    @Test
    public void testWriteOptiliHappyPath() {
        try {
            
            
            HaeHakukohteetVastausTyyppi vastaus = new HaeHakukohteetVastausTyyppi();
            vastaus.getHakukohdeTulos().add(createHakukohdetulos("hakukohteet_000#1", 0));
            vastaus.getHakukohdeTulos().add(createHakukohdetulos("hakukohteet_011#1", 1));
            when(tarjontaServiceMock.haeHakukohteet((HaeHakukohteetKyselyTyyppi)anyObject())).thenReturn(vastaus);
            
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

    private HakukohdeTulos createHakukohdetulos(String koodistonimi, long id) {
        HakukohdeTulos hakukohdeT = new HakukohdeTulos();
        HakukohdeListausTyyppi hakukohde = new HakukohdeListausTyyppi();
        hakukohde.setOid(koodistonimi);
        hakukohde.setKoodistoNimi(koodistonimi);
        hakukohde.setKoulutuksenAlkamiskausiUri(ALKAMISKAUSI_KEVAT);
        
        TarjoajaTyyppi tarjoaja = new TarjoajaTyyppi();
        tarjoaja.setTarjoajaOid(TARJOAJA_OID);
        hakukohde.setTarjoaja(tarjoaja);
        
        OrganisaatioDTO tarjoajaDTO = new OrganisaatioDTO();
        tarjoajaDTO.setOid(TARJOAJA_OID);
        tarjoajaDTO.getTyypit().add(OrganisaatioTyyppi.OPETUSPISTE);
        tarjoajaDTO.getTyypit().add(OrganisaatioTyyppi.OPPILAITOS);
        tarjoajaDTO.setOpetuspisteenJarjNro(TARJOAJA_OPJARJNRO);
        tarjoajaDTO.setOppilaitosKoodi(TARJOAJA_OLKOODI);
        
        when(organisaatioServiceMock.findByOid(TARJOAJA_OID)).thenReturn(tarjoajaDTO);
        
        Hakukohde hakukE = new Hakukohde();
        hakukE.setId(Long.valueOf(id));
        
        when(hakukohdeDaoMock.findHakukohdeByOid(koodistonimi)).thenReturn(hakukE);
        
        HaeKoulutuksetVastausTyyppi koulutusVastaus = new HaeKoulutuksetVastausTyyppi();
        KoulutusTulos koulutusTulos = new KoulutusTulos();
        KoulutusListausTyyppi koulutus = new KoulutusListausTyyppi();
        
        KoodistoKoodiTyyppi koodiT = new KoodistoKoodiTyyppi();
        koodiT.setUri(KOULUTUSKOODI);
        koodiT.setVersio(1);
        koulutus.setKoulutuskoodi(koodiT);
        koulutusTulos.setKoulutus(koulutus);
        koulutusVastaus.getKoulutusTulos().add(koulutusTulos);
        
        when(tarjontaServiceMock.haeKoulutukset((HaeKoulutuksetKyselyTyyppi)anyObject())).thenReturn(koulutusVastaus);
        
        hakukohdeT.setHakukohde(hakukohde);
        
        return hakukohdeT;
    }

}
