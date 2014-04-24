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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.vm.sade.koodisto.service.KoodiService;
import fi.vm.sade.koodisto.service.KoodistoService;
import fi.vm.sade.koodisto.service.types.SearchKoodisByKoodistoCriteriaType;
import fi.vm.sade.koodisto.service.types.SearchKoodistosCriteriaType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.service.types.common.KoodistoType;
import fi.vm.sade.koodisto.util.KoodiServiceSearchCriteriaBuilder;
import fi.vm.sade.koodisto.util.KoodistoServiceSearchCriteriaBuilder;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;
import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;

/**
 * 
 * @author Markus
 *
 */
@Component
@Configurable
public class OrganisaatioContainer {
    
    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioContainer.class);
    
    /*@Autowired
    protected OrganisaatioService organisaatioService;*/
    
    @Autowired
    protected OrganisaatioSearchService organisaatioSearchService;


    @Autowired
    protected KelaDAO kelaDAO;
    
    @Autowired
    protected KoodiService koodiService;
    
    @Autowired
    protected KoodistoService koodistoService;
    
    private List<OrganisaatioPerustieto> oppilaitokset;


    private Map<String,OrganisaatioPerustieto> oppilaitosoidOppilaitosMap;
    private List<OrganisaatioPerustieto> toimipisteet;
    private List<String> orgOidList;
    
    protected String oppilaitosnumerokoodisto;
    protected String toimipistekoodisto;
    
    //TOINEN ASTE KOODI URIS
    protected String opTyyppiLukiot;
    protected String opTyyppiLukiotJaPeruskoulut;
    protected String opTyyppiAmmatillisetOppilaitokset;
    protected String opTyyppiAmmattillisetErityisoppilaitokset;
    protected String opTyyppiAmmatillisetErikoisoppilaitokset;
    protected String opTyyppiAmmatillisetAikuiskoulutuseskukset;
    protected String opTyyppiKansanopistot;
    protected String opTyyppiMusiikkioppilaitokset;
    
    
    public void fetchOrgnaisaatiot() {
        oppilaitosoidOppilaitosMap = new HashMap<String, OrganisaatioPerustieto>();
        oppilaitokset = new ArrayList<OrganisaatioPerustieto>();
        orgOidList = new ArrayList<String>();
        OrganisaatioSearchCriteria criteria = new OrganisaatioSearchCriteria();
        criteria.setOrganisaatioTyyppi(OrganisaatioTyyppi.OPPILAITOS.value());
        List<OrganisaatioPerustieto> oppilaitoksetR = organisaatioSearchService.searchBasicOrganisaatios(criteria);
        
        for (OrganisaatioPerustieto curOppilaitos : oppilaitoksetR) {
            LOG.debug("Oppilaitos: " + curOppilaitos.getNimi("fi"));
            if (isOppilaitosWritable(curOppilaitos)) {
                oppilaitosoidOppilaitosMap.put(curOppilaitos.getOid(), curOppilaitos);
                oppilaitokset.add(curOppilaitos);
                orgOidList.add(curOppilaitos.getOid());
            }
        }
        
        toimipisteet = new ArrayList<OrganisaatioPerustieto>();
        criteria = new OrganisaatioSearchCriteria();
        criteria.setOrganisaatioTyyppi(OrganisaatioTyyppi.OPETUSPISTE.value());
        criteria.getOidRestrictionList().addAll(orgOidList);
        
        List<OrganisaatioPerustieto> opetuspisteet = organisaatioSearchService.searchBasicOrganisaatios(criteria);
        
        for (OrganisaatioPerustieto curToimipiste : opetuspisteet) {
            LOG.debug("Toimipiste: " + curToimipiste.getNimi("fi"));
            if (isToimipisteWritable(curToimipiste)) {
                toimipisteet.add(curToimipiste);
                orgOidList.add(curToimipiste.getOid());
            }
        }
        LOG.info(String.format("found valid oppilaitos: %s (%s rejected) and valid toimipiste: %s (%s rejected)", 
        		oppilaitokset.size(),
        		oppilaitoksetR.size()-oppilaitokset.size(),
        		toimipisteet.size(),
        		opetuspisteet.size()-toimipisteet.size()));
    }
    
    public boolean isOppilaitosWritable(OrganisaatioPerustieto curOppilaitos) {
        return isOppilaitosInKoodisto(curOppilaitos) 
                && isOppilaitosToinenAste(curOppilaitos) 
                && hasOppilaitosIntactYhteystiedot(curOppilaitos);
    }
    
    
    
    public boolean hasOppilaitosIntactYhteystiedot(
            OrganisaatioPerustieto curOppilaitos) {
        Organisaatio orgE = kelaDAO.findOrganisaatioByOid(curOppilaitos.getOid());
        return kelaDAO.getKayntiosoiteIdForOrganisaatio(orgE.getId()) != null;
    }


    public boolean isOppilaitosInKoodisto(OrganisaatioPerustieto curOppilaitos) {
        LOG.debug("isOppilaitosInKoodisto: " + curOppilaitos.getNimi("fi") + ", " + curOppilaitos.getOppilaitosKoodi());
        String oppilaitoskoodi = curOppilaitos.getOppilaitosKoodi();
        List<KoodiType> koodit = new ArrayList<KoodiType>();
        if (!StringUtils.isEmpty(oppilaitoskoodi)) {
            koodit = getKoodisByArvoAndKoodisto(oppilaitoskoodi, oppilaitosnumerokoodisto);
        }
        return koodit != null && !koodit.isEmpty();
    }

    public boolean isOppilaitosToinenAste(
            OrganisaatioPerustieto curOppilaitos) {
        String opTyyppi = curOppilaitos.getOppilaitostyyppi();
        return  isTyyppiToinenaste(opTyyppi);
    }
    

    protected boolean isTyyppiToinenaste(String opTyyppi) {
        return opTyyppiAmmatillisetAikuiskoulutuseskukset.equals(opTyyppi) 
                || opTyyppiAmmatillisetErikoisoppilaitokset.equals(opTyyppi)
                || opTyyppiAmmatillisetOppilaitokset.equals(opTyyppi)
                || opTyyppiAmmattillisetErityisoppilaitokset.equals(opTyyppi)
                || opTyyppiKansanopistot.equals(opTyyppi)
                || opTyyppiLukiot.equals(opTyyppi)
                || opTyyppiLukiotJaPeruskoulut.equals(opTyyppi)
                || opTyyppiMusiikkioppilaitokset.equals(opTyyppi);
    }
    
    public boolean isToimipisteWritable(OrganisaatioPerustieto curToimipiste) {
        LOG.debug("isToimpisteWRitable method " + curToimipiste.getNimi("fi"));
        if (curToimipiste.getParentOid() == null) {
            return false;
        }
        
        OrganisaatioPerustieto parentToimipiste = oppilaitosoidOppilaitosMap.get(curToimipiste.getParentOid());
        if (parentToimipiste == null) {
            return false;
        }
        
        Organisaatio toimipisteE = kelaDAO.findOrganisaatioByOid(curToimipiste.getOid());
        
        if (null==toimipisteE) {
        	LOG.warn(String.format("There is no oraganisaatio for oid %s although it was found in index",curToimipiste.getOid()));
        }
        List<KoodiType> koodit = new ArrayList<KoodiType>();
        if (null!= toimipisteE && !StringUtils.isEmpty(parentToimipiste.getOppilaitosKoodi()) && !StringUtils.isEmpty(toimipisteE.getOpetuspisteenJarjNro())) {
            String toimipistearvo = String.format("%s%s", parentToimipiste.getOppilaitosKoodi(), toimipisteE.getOpetuspisteenJarjNro());
            koodit = getKoodisByArvoAndKoodisto(toimipistearvo, toimipistekoodisto);
        }
        
        return null!=toimipisteE && null!=koodit && !koodit.isEmpty() && null!=(kelaDAO.getKayntiosoiteIdForOrganisaatio(toimipisteE.getId()));
    }
    
    public List<OrganisaatioPerustieto> getOppilaitokset() {
        return oppilaitokset;
    }

    public Map<String, OrganisaatioPerustieto> getOppilaitosoidOppilaitosMap() {
        return oppilaitosoidOppilaitosMap;
    }

    public List<OrganisaatioPerustieto> getToimipisteet() {
        return toimipisteet;
    }

    public List<String> getOrgOidList() {
        return orgOidList;
    }
    
    @Value("${koodisto-uris.oppilaitosnumero}")
    public void setOppilaitosnumerokoodisto(String oppilaitosnumerokoodisto) {
        this.oppilaitosnumerokoodisto = oppilaitosnumerokoodisto;
    }
    
    @Value("${koodisto-uris.opetuspisteet}")
    public void setToimipistekoodisto(String toimipistekoodisto) {
        this.toimipistekoodisto = toimipistekoodisto;
    }
    
    @Value("${lukiot-uri}")
    public void setOpTyyppiLukiot(String opTyyppiLukiot) {
        this.opTyyppiLukiot = opTyyppiLukiot;
    }

    @Value("${lukiotjaperuskoulut-uri}")
    public void setOpTyyppiLukiotJaPeruskoulut(String opTyyppiLukiotJaPeruskoulut) {
        this.opTyyppiLukiotJaPeruskoulut = opTyyppiLukiotJaPeruskoulut;
    }
    
    @Value("${ammatillisetoppilaitokset-uri}")
    public void setOpTyyppiAmmatillisetOppilaitokset(
            String opTyyppiAmmatillisetOppilaitokset) {
        this.opTyyppiAmmatillisetOppilaitokset = opTyyppiAmmatillisetOppilaitokset;
    }

    @Value("${ammatilliseterityisoppilaitokset-uri}")
    public void setOpTyyppiAmmattillisetErityisoppilaitokset(
            String opTyyppiAmmattillisetErityisoppilaitokset) {
        this.opTyyppiAmmattillisetErityisoppilaitokset = opTyyppiAmmattillisetErityisoppilaitokset;
    }

    @Value("${ammatilliseterikoisoppilaitokset-uri}")
    public void setOpTyyppiAmmatillisetErikoisoppilaitokset(
            String opTyyppiAmmatillisetErikoisoppilaitokset) {
        this.opTyyppiAmmatillisetErikoisoppilaitokset = opTyyppiAmmatillisetErikoisoppilaitokset;
    }

    @Value("${ammatillisetaikuiskoulutuskeskukset-uri}")
    public void setOpTyyppiAmmatillisetAikuiskoulutuseskukset(
            String opTyyppiAmmatillisetAikuiskoulutuseskukset) {
        this.opTyyppiAmmatillisetAikuiskoulutuseskukset = opTyyppiAmmatillisetAikuiskoulutuseskukset;
    }

    @Value("${kansanopistot-uri}")
    public void setOpTyyppiKansanopistot(String opTyyppiKansanopistot) {
        this.opTyyppiKansanopistot = opTyyppiKansanopistot;
    }

    @Value("${musiikkioppilaitokset-uri}")
    public void setOpTyyppiMusiikkioppilaitokset(String opTyyppiMusiikkioppilaitokset) {
        this.opTyyppiMusiikkioppilaitokset = opTyyppiMusiikkioppilaitokset;
    }
    
    public List<KoodiType> getKoodisByArvoAndKoodisto(String arvo, String koodistoUri) {
        try {
            SearchKoodistosCriteriaType koodistoSearchCriteria = KoodistoServiceSearchCriteriaBuilder.latestKoodistoByUri(koodistoUri);

            List<KoodistoType> koodistoResult = koodistoService.searchKoodistos(koodistoSearchCriteria);
            if(koodistoResult.size() != 1) {
                // FIXME: Throw something other than RuntimeException?
                throw new RuntimeException("No koodisto found for koodisto URI " + koodistoUri);
            }
            KoodistoType koodisto = koodistoResult.get(0);

            SearchKoodisByKoodistoCriteriaType koodiSearchCriteria = KoodiServiceSearchCriteriaBuilder.koodisByArvoAndKoodistoUriAndKoodistoVersio(arvo,
                    koodistoUri, koodisto.getVersio());
            return koodiService.searchKoodisByKoodisto(koodiSearchCriteria);
        } catch (Exception exp) {
            return null;
        }
    }
    /*
    public void setOrganisaatioService(OrganisaatioService organisaatioService) {
        this.organisaatioService = organisaatioService;
    }*/

    public void setHakukohdeDAO(KelaDAO hakukohdeDAO) {
        this.kelaDAO = hakukohdeDAO;
    }

    public void setKoodiService(KoodiService koodiService) {
        this.koodiService = koodiService;
    }

    public void setKoodistoService(KoodistoService koodistoService) {
        this.koodistoService = koodistoService;
    }

    public void setOrganisaatioSearchService(
            OrganisaatioSearchService organisaatioSearchService) {
        this.organisaatioSearchService = organisaatioSearchService;
        
    }

}
