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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fi.vm.sade.koodisto.util.KoodistoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.vm.sade.koodisto.service.types.SearchKoodisByKoodistoCriteriaType;
import fi.vm.sade.koodisto.service.types.SearchKoodistosCriteriaType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.service.types.common.KoodistoType;
import fi.vm.sade.koodisto.util.KoodiServiceSearchCriteriaBuilder;
import fi.vm.sade.koodisto.util.KoodistoServiceSearchCriteriaBuilder;
import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.OrganisaatioPerustieto;

@Component
@Configurable
public class OrganisaatioContainer {

    @Autowired
    protected KelaDAO kelaDAO;

    private List<OrganisaatioPerustieto> oppilaitokset;
    private Map<String,OrganisaatioPerustieto> oppilaitosoidOppilaitosMap;
    private List<OrganisaatioPerustieto> toimipisteet;
    private List<String> orgOidList;
    
    protected String oppilaitosnumerokoodisto;
    protected String toimipistekoodisto;
    
    protected String [] toinenAsteUris;
    protected String [] korkeakouluUris;
    
    private final static String[] errors = {
		"has no parentoid (org.oid: %s) - skipped",
    };

    private final static String[] warnings = {
    };
  
    private boolean stopThread=false;
    public void stop() {
    	info("Container stopping.");
    	stopThread=true;
    }
    
    private void checkStop() throws UserStopRequestException {
    	if (stopThread) {
    		stopThread=false;
    		throw new UserStopRequestException();
    	}
    }
    
    public void fetchOrganisaatiot() throws UserStopRequestException {
    	stopThread=false;
    	long startTime = System.currentTimeMillis();
        oppilaitosoidOppilaitosMap = new HashMap<String, OrganisaatioPerustieto>();
        oppilaitokset = new ArrayList<OrganisaatioPerustieto>();
        orgOidList = new ArrayList<String>();
        info("Retrieving oppilaitokset....");
		List<String> includeUris = new LinkedList<String>();
        includeUris.addAll(Arrays.asList(this.korkeakouluUris));
        includeUris.addAll(Arrays.asList(this.toinenAsteUris));
    	List<OrganisaatioPerustieto> oppilaitoksetR = kelaDAO.findOppilaitokset(includeUris);
        
        for (OrganisaatioPerustieto curOppilaitos : oppilaitoksetR) {
        	//TODO: refactor!
        	checkStop();
            oppilaitosoidOppilaitosMap.put(curOppilaitos.getOid(), curOppilaitos);
            oppilaitokset.add(curOppilaitos);
            orgOidList.add(curOppilaitos.getOid());
        }
        info("Generation time: " + (System.currentTimeMillis() - startTime)/1000.0 + " seconds");

        List<String> excludeOids=new LinkedList<String>();
        for (fi.vm.sade.rajapinnat.kela.tarjonta.model.OrganisaatioPerustieto i : oppilaitoksetR) {
        	excludeOids.add(i.getOid());
        }
        
		info("Retrieving toimipisteet....");
        toimipisteet = new ArrayList<OrganisaatioPerustieto>();
        List<OrganisaatioPerustieto> opetuspisteet = kelaDAO.findToimipisteet(excludeOids);

        startTime = System.currentTimeMillis();
        for (OrganisaatioPerustieto curToimipiste : opetuspisteet) {
        	checkStop();
            if (isToimipisteWritable(curToimipiste)) {
                toimipisteet.add(curToimipiste);
                orgOidList.add(curToimipiste.getOid());
            }
        }
        info("Generation time: " + (System.currentTimeMillis() - startTime)/1000.0 + " seconds");
    }

    public boolean isToimipisteWritable(OrganisaatioPerustieto curToimipiste) {
        if (curToimipiste.getParentOppilaitosOid() == null) {
        	error(1,curToimipiste.getOid()+" "+curToimipiste.getNimi());
            return false;
        }
 
        OrganisaatioPerustieto parentToimipiste = oppilaitosoidOppilaitosMap.get(curToimipiste.getParentOppilaitosOid());
        if (parentToimipiste == null) {
        	debug("parent oppilaitos (parentOppilaitosoid: %s) not found in filtered oppilaitokset (org.oid: %s)  - skipped",curToimipiste.getParentOppilaitosOid(),
        			curToimipiste.getOid()+" "+curToimipiste.getNimi());
            return false;
        }
       
        return true;
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

    @Value("${toinenaste-uris}")
    public void setToinenAsteUris(String toinenasteUris) {
    	this.toinenAsteUris = toinenasteUris.split(",");
    	info("Accepting toinenaste -uris: "+Arrays.toString(this.toinenAsteUris));
    }

    @Value("${korkeakoulu-uris}")
    public void setKorkeakouluUris(String korkeakouluUris) {
        this.korkeakouluUris = korkeakouluUris.split(",");
        info("Accepting korkeakoulu -uris: "+Arrays.toString(this.korkeakouluUris));
    }

    public void setHakukohdeDAO(KelaDAO hakukohdeDAO) {
        this.kelaDAO = hakukohdeDAO;
    }

    protected void error(int i, Object... args) {
    	KelaGenerator.error("(INIT"+i+") : "+errors[i-1], args);
	}
    
    protected void warn(int i, Object... args) {
    	KelaGenerator.warn("(INIT"+i+") : "+warnings[i-1], args);
	}

    protected void info(String msg, Object... args) {
    	KelaGenerator.info(msg, args);
	}

    protected void debug(String msg, Object... args) {
    	KelaGenerator.debug(msg, args);
	}
}
