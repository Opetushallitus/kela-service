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
package fi.vm.sade.rajapinnat.kela.utils;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchCriteriaDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.rajapinnat.kela.dao.HakukohdeDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.MonikielinenTeksti;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Yhteystieto;
import fi.vm.sade.tarjonta.service.TarjontaPublicService;

/**
 * 
 * @author Markus
 */
public class TestDataGenerator {

    public static final String OLKOODI1 = "00001";
    public static final String OLKOODI2 = "00002";
    public static final String OPETUSPISTENRO = "01";
    public static final String OLTYYPPI = "oppilaitostyyppi_15#1";
    public static final String KOTIPAIKKA = "kunta_091";
    public static final String KIELI = "kielivalikoima_fi";
    public static final String KAYNTIOSOITE = "kaynti";
    public static final String OID_PREFIX = "oid:";
    public static final String DATE_PATTERN = "dd.MM.yyyy";
    
    private OrganisaatioService organisaatioServiceMock;
    private HakukohdeDAO hakukohdeDaoMock;
    private TarjontaPublicService tarjontaServiceMock;
    
    Calendar curCal = Calendar.getInstance();

    public List<OrganisaatioPerustietoType> createOrganisaatiot() {
        List<OrganisaatioPerustietoType> organisaatiot = new ArrayList<OrganisaatioPerustietoType>();

        organisaatiot.add(createOrg(OID_PREFIX + OLKOODI1, 1, "Harman lukio", OLKOODI1, OLTYYPPI, OrganisaatioTyyppi.OPPILAITOS));
        
        organisaatiot.add(createOrg(OID_PREFIX + OLKOODI2, 2, "Alajarven lukio", OLKOODI2, OLTYYPPI, OrganisaatioTyyppi.OPPILAITOS));
        OrganisaatioPerustietoType opetuspiste1 = createOrg(OID_PREFIX + OLKOODI1 + OPETUSPISTENRO, 3, "Harman lukio", null, null, OrganisaatioTyyppi.OPETUSPISTE);
        opetuspiste1.setParentOid(OID_PREFIX + OLKOODI1);
        organisaatiot.add(opetuspiste1);
        OrganisaatioPerustietoType opetuspiste2 = createOrg(OID_PREFIX + OLKOODI2 + OPETUSPISTENRO, 4, "Alajarven lukio", null, null, OrganisaatioTyyppi.OPETUSPISTE);
        opetuspiste2.setParentOid(OID_PREFIX + OLKOODI2);
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
        ol1.setAlkuPvm(curCal.getTime());
        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.YEAR, curCal.get(Calendar.YEAR) + 1);
        ol1.setLakkautusPvm(endCal.getTime());
        
        Organisaatio orgE = new Organisaatio();
        orgE.setOid(oid);
        orgE.setId(Long.valueOf(id));
        orgE.setOpetuspisteenJarjNro(OPETUSPISTENRO);
        orgE.getKielet().add(KIELI);
        orgE.setKotipaikka(KOTIPAIKKA);
        MonikielinenTeksti nimiE = new MonikielinenTeksti();
        nimiE.setId(id);
        orgE.setNimi(nimiE);
        when(hakukohdeDaoMock.findOrganisaatioByOid(oid)).thenReturn(orgE);
        
        Yhteystieto yt = new Yhteystieto();
        yt.setId(orgE.getId() + 5);
        yt.setOrganisaatioId(orgE.getId());
        yt.setOsoiteTyyppi(KAYNTIOSOITE);
        when(hakukohdeDaoMock.getKayntiosoiteIdForOrganisaatio(orgE.getId())).thenReturn(orgE.getId() + 5);
        
        return ol1;
    }
    
    public OrganisaatioService getOrganisaatioServiceMock() {
        return organisaatioServiceMock;
    }
    
    public void setOrganisaatioServiceMock(
            OrganisaatioService organisaatioServiceMock) {
        this.organisaatioServiceMock = organisaatioServiceMock;
    }
    
    public HakukohdeDAO getHakukohdeDaoMock() {
        return hakukohdeDaoMock;
    }
    
    public void setHakukohdeDaoMock(HakukohdeDAO hakukohdeDaoMock) {
        this.hakukohdeDaoMock = hakukohdeDaoMock;
    }
    
    public TarjontaPublicService getTarjontaServiceMock() {
        return tarjontaServiceMock;
    }
    
    public void setTarjontaServiceMock(TarjontaPublicService tarjontaServiceMock) {
        this.tarjontaServiceMock = tarjontaServiceMock;
    }

    public void createOrganisaatioData() {
        List<OrganisaatioPerustietoType> organisaatiot = new ArrayList<OrganisaatioPerustietoType>();
        organisaatiot.addAll(createOrganisaatiot());
        when(organisaatioServiceMock.searchBasicOrganisaatios((OrganisaatioSearchCriteriaDTO)anyObject())).thenReturn(organisaatiot); 
    }
   
    public String getCurrentDateStr() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return sdf.format(curCal.getTime());
    }

    

}
