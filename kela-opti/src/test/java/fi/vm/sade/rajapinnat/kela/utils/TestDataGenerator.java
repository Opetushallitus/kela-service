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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.resource.OrganisaatioResource;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.MonikielinenTeksti;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatiosuhde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Yhteystieto;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatiosuhde.OrganisaatioSuhdeTyyppi;
import fi.vm.sade.tarjonta.service.search.HakukohdeListaus;
import fi.vm.sade.tarjonta.service.search.HakukohteetKysely;
import fi.vm.sade.tarjonta.service.search.HakukohteetVastaus;
import fi.vm.sade.tarjonta.service.search.HakukohteetVastaus.HakukohdeTulos;
import fi.vm.sade.tarjonta.service.search.KoulutuksetKysely;
import fi.vm.sade.tarjonta.service.search.KoulutuksetVastaus;
import fi.vm.sade.tarjonta.service.search.KoulutuksetVastaus.KoulutusTulos;
import fi.vm.sade.tarjonta.service.search.TarjontaSearchService;
import fi.vm.sade.tarjonta.service.types.KoodistoKoodiTyyppi;
import fi.vm.sade.tarjonta.service.types.KoulutusListausTyyppi;
import fi.vm.sade.tarjonta.service.types.TarjoajaTyyppi;

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
    public static final String POSTIOSOITE = "posti";
    public static final String OID_PREFIX = "oid:";
    public static final String DATE_PATTERN = "dd.MM.yyyy";
    public static final String KAYNTI_POSTINUMERO = "posti_62501";
    public static final String KAYNTI_KATUOSOITE = "Kayntikatu 1";
    
    public static final String POSTI_POSTINUMERO = "posti_02100";
    public static final String POSTI_KATUOSOITE = "postikatu 1";
    
    
    public static final String OL_PUHELIN_PREFIX = "050";
    public static final String OL_FAX_PREFIX = "019";
    public static final String OP_PUHELIN_PREFIX = "044";
    public static final String OP_FAX_PREFIX = "05";
    public static final String EMAIL_SUFFIX = "@oph.fi";
    public static final String OL_WWW_PREFIX = "http://oppilaitos.fi/";
    public static final String OP_WWW_PREFIX = "http://opetuspiste.fi/";
    
    
    public static final String OSOITETYYPPI_FIELD = "osoiteTyyppi";
    public static final String POSTINUMERO_FIELD = "postinumeroUri";
    public static final String KATUOSOITE_FIELD = "osoite";
    
    
    private static final String TARJOAJA_OID = OID_PREFIX + OLKOODI1;
    private static final String TARJOAJA_OLKOODI = "12345";
    private static final String TARJOAJA_OPJARJNRO = "01";
    private static final String KOULUTUSKOODI = "koulutus_301000";
    private static final String ALKAMISKAUSI_KEVAT = "KEVAT";
    
    
    
    private OrganisaatioService organisaatioServiceMock;
    private KelaDAO kelaDaoMock;
    private TarjontaSearchService tarjontaServiceMock;
    private OrganisaatioResource orgRMock;
    private OrganisaatioSearchService organisaatioSearchServiceMock;


    Calendar curCal = Calendar.getInstance();

    public List<OrganisaatioPerustieto> createOrganisaatiot() {
        List<OrganisaatioPerustieto> organisaatiot = new ArrayList<OrganisaatioPerustieto>();

        organisaatiot.add(createOrg(OID_PREFIX + OLKOODI1, 1, "Harman lukio", OLKOODI1, OLTYYPPI, OrganisaatioTyyppi.OPPILAITOS));
        
        organisaatiot.add(createOrg(OID_PREFIX + OLKOODI2, 2, "Alajarven lukio", OLKOODI2, OLTYYPPI, OrganisaatioTyyppi.OPPILAITOS));
        OrganisaatioPerustieto opetuspiste1 = createOrg(OID_PREFIX + OLKOODI1 + OPETUSPISTENRO, 3, "Harman lukio", null, null, OrganisaatioTyyppi.OPETUSPISTE);
        opetuspiste1.setParentOid(OID_PREFIX + OLKOODI1);
        organisaatiot.add(opetuspiste1);
        OrganisaatioPerustieto opetuspiste2 = createOrg(OID_PREFIX + OLKOODI2 + OPETUSPISTENRO, 4, "Alajarven lukio", null, null, OrganisaatioTyyppi.OPETUSPISTE);
        opetuspiste2.setParentOid(OID_PREFIX + OLKOODI2);
        organisaatiot.add(opetuspiste2);
        
        return organisaatiot;
    }
    
    private OrganisaatioPerustieto createOrg(String oid, long id, String nimi, String olkoodi, String olTyyppi, OrganisaatioTyyppi orgTyyppi) {
        OrganisaatioPerustieto ol1 = new OrganisaatioPerustieto();
        ol1.setOid(oid);
        ol1.setNimiFi(nimi);
        ol1.setOppilaitosKoodi(olkoodi);
        ol1.setOppilaitostyyppi(olTyyppi);
        ol1.getOrganisaatiotyypit().add(orgTyyppi);
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
        orgE.setOppilaitoskoodi(olkoodi);
        MonikielinenTeksti nimiE = new MonikielinenTeksti();
        nimiE.setId(id);
        orgE.setNimi(nimiE);
        when(kelaDaoMock.findOrganisaatioByOid(oid)).thenReturn(orgE);
        
        Yhteystieto yt = new Yhteystieto();
        yt.setId(orgE.getId() + 5);
        yt.setOrganisaatioId(orgE.getId());
        yt.setOsoiteTyyppi(KAYNTIOSOITE);
        when(kelaDaoMock.getKayntiosoiteIdForOrganisaatio(orgE.getId())).thenReturn(orgE.getId() + 5);
        
        OrganisaatioRDTO orgR = new OrganisaatioRDTO();
        orgR.setOid(oid);
        Map<String,String> kayntiOsoite = new HashMap<String,String>();
        kayntiOsoite.put(OSOITETYYPPI_FIELD, KAYNTIOSOITE);
        kayntiOsoite.put(KATUOSOITE_FIELD, KAYNTI_KATUOSOITE + id);
        kayntiOsoite.put(POSTINUMERO_FIELD, KAYNTI_POSTINUMERO);
        orgR.setKayntiosoite(kayntiOsoite);
        
        Map<String,String> postiOsoite = new HashMap<String,String>();
        postiOsoite.put(OSOITETYYPPI_FIELD, POSTIOSOITE);
        postiOsoite.put(KATUOSOITE_FIELD, POSTI_KATUOSOITE + id);
        postiOsoite.put(POSTINUMERO_FIELD, POSTI_POSTINUMERO);
        orgR.setPostiosoite(postiOsoite);
        
        
        
        if (olkoodi != null) {
            orgR.setFaksinumero(OL_FAX_PREFIX + " " + olkoodi);
            orgR.setPuhelinnumero(OL_PUHELIN_PREFIX + " " + olkoodi);
            orgR.setWwwOsoite(OL_WWW_PREFIX + olkoodi);
            orgR.setEmailOsoite(olkoodi + EMAIL_SUFFIX);
        } else {
            orgR.setFaksinumero(OP_FAX_PREFIX + " " + OPETUSPISTENRO + id);
            orgR.setPuhelinnumero(OP_PUHELIN_PREFIX + " " + OPETUSPISTENRO + id);
            orgR.setWwwOsoite(OP_WWW_PREFIX + OPETUSPISTENRO  + id);
            orgR.setEmailOsoite(OPETUSPISTENRO + id + EMAIL_SUFFIX);
        }
        
        when(orgRMock.getOrganisaatioByOID(oid)).thenReturn(orgR);
        
        return ol1;
    }
    

    public void generateTarjontaData() {
        HakukohteetVastaus vastaus = new HakukohteetVastaus();
        vastaus.getHakukohdeTulos().add(createHakukohdetulos("hakukohteet_000#1", 0));
        vastaus.getHakukohdeTulos().add(createHakukohdetulos("hakukohteet_011#1", 1));
        when(tarjontaServiceMock.haeHakukohteet((HakukohteetKysely)anyObject())).thenReturn(vastaus);
    }
    
    private HakukohdeTulos createHakukohdetulos(String koodistonimi, long id) {
        HakukohdeTulos hakukohdeT = new HakukohdeTulos();
        HakukohdeListaus hakukohde = new HakukohdeListaus();
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
        tarjoajaDTO.setOppilaitosTyyppi(OLTYYPPI);
        
        when(organisaatioServiceMock.findByOid(TARJOAJA_OID)).thenReturn(tarjoajaDTO);
        
        Hakukohde hakukE = new Hakukohde();
        hakukE.setId(Long.valueOf(id));
        
        when(kelaDaoMock.findHakukohdeByOid(koodistonimi)).thenReturn(hakukE);
        
        KoulutuksetVastaus koulutusVastaus = new KoulutuksetVastaus();
        KoulutusTulos koulutusTulos = new KoulutusTulos();
        KoulutusListausTyyppi koulutus = new KoulutusListausTyyppi();
        
        KoodistoKoodiTyyppi koodiT = new KoodistoKoodiTyyppi();
        koodiT.setUri(KOULUTUSKOODI);
        koodiT.setVersio(1);
        koulutus.setKoulutuskoodi(koodiT);
        koulutusTulos.setKoulutus(koulutus);
        koulutusVastaus.getKoulutusTulos().add(koulutusTulos);
        
        when(tarjontaServiceMock.haeKoulutukset((KoulutuksetKysely)anyObject())).thenReturn(koulutusVastaus);
        
        hakukohdeT.setHakukohde(hakukohde);
        
        return hakukohdeT;
    }
    
    public OrganisaatioService getOrganisaatioServiceMock() {
        return organisaatioServiceMock;
    }
    
    public void setOrganisaatioServiceMock(
            OrganisaatioService organisaatioServiceMock) {
        this.organisaatioServiceMock = organisaatioServiceMock;
    }
    
    public OrganisaatioSearchService getOrganisaatioSearchServiceMock() {
        return organisaatioSearchServiceMock;
    }
    
    public void setOrganisaatioSearchServiceMock(
            OrganisaatioSearchService organisaatioSearchServiceMock) {
        this.organisaatioSearchServiceMock = organisaatioSearchServiceMock;
    }
    
    public KelaDAO getHakukohdeDaoMock() {
        return kelaDaoMock;
    }
    
    public void setHakukohdeDaoMock(KelaDAO hakukohdeDaoMock) {
        this.kelaDaoMock = hakukohdeDaoMock;
    }
    
    public TarjontaSearchService getTarjontaServiceMock() {
        return tarjontaServiceMock;
    }
    
    public void setTarjontaServiceMock(TarjontaSearchService tarjontaServiceMock) {
        this.tarjontaServiceMock = tarjontaServiceMock;
    }
    
    
    public OrganisaatioResource getOrgRMock() {
        return orgRMock;
    }

    public void setOrgRMock(OrganisaatioResource orgRMock) {
        this.orgRMock = orgRMock;
    }

    public void createOrganisaatioData() {
        List<OrganisaatioPerustieto> organisaatiot = new ArrayList<OrganisaatioPerustieto>();
        organisaatiot.addAll(createOrganisaatiot());
        when(organisaatioSearchServiceMock.searchBasicOrganisaatios((OrganisaatioSearchCriteria)anyObject())).thenReturn(organisaatiot); 
    }
    
    public void createLiitosData() {
        List<Organisaatiosuhde> liitokset = new ArrayList<Organisaatiosuhde>();
        liitokset .addAll(createLiitokset());
        when(kelaDaoMock.findAllLiitokset()).thenReturn(liitokset);
    }
   
    private List<Organisaatiosuhde> createLiitokset() {
        String parentOlkoodi = "00000";
        String child1Olkoodi = "00001";
        String child2Olkoodi = "00002";
        
        List<Organisaatiosuhde> liitokset = new ArrayList<Organisaatiosuhde>();
        
        Organisaatio parentOrg = new Organisaatio();
        parentOrg.setOppilaitoskoodi(parentOlkoodi);
        
        Organisaatio childOrg1 = new Organisaatio();
        childOrg1.setOppilaitoskoodi(child1Olkoodi);
        
        Organisaatio childOrg2 = new Organisaatio();
        childOrg2.setOppilaitoskoodi(child2Olkoodi);
        
        liitokset.add(createLiitos(1, parentOrg, childOrg1));
        liitokset.add(createLiitos(2, parentOrg, childOrg2));
        return liitokset;
    }
    
    private Organisaatiosuhde createLiitos(long id, Organisaatio parent, Organisaatio child) {
        Organisaatiosuhde liitos = new Organisaatiosuhde();
        liitos.setId(Long.valueOf(id));
        liitos.setAlkuPvm(new Date());
        liitos.setChild(child);
        liitos.setParent(parent);
        liitos.setSuhdeTyyppi(OrganisaatioSuhdeTyyppi.LIITOS);
        return liitos;
    }

    public String getCurrentDateStr() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return sdf.format(curCal.getTime());
    }
}
