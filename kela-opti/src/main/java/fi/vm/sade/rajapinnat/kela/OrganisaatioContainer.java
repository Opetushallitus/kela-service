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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
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
    
    protected String [] toinenAsteUris;
    protected String [] korkeakouluUris;
    
    private final static String[] errors = {
		"there is no organisaatio in DB although it was found in index (org.oid: %s)  - skipped",
		"has no parentoid (org.oid: %s) - skipped",
		"parentoid (parentoid: %s) not found in filtered oppilaitokset (org.oid: %s)  - skipped",
		"there is no organisaatio in DB although it was found in index (org.oid: %s)  - skipped"
    };

    private final static String[] warnings = {
    	"has not valid entry in 'oppilaitosnumero -koodisto' (oppilaitoskoodi: %s) (org.oid: %s)",
    	"has not valid entry in käyntiosoite -entry in yhteystieto (org.oid: %s)",
    	"has not valid entry in 'opetuspiste -koodisto' (value: %s) (org.oid: %s)",
    	"has not valid entry in käyntiosoite -entry in yhteystieto (org.oid: %s)"
    };

    
    private List<OrganisaatioPerustieto> searchBasicOrganisaatios(OrganisaatioSearchCriteria criteria,OrganisaatioTyyppi tyyppi) {
    	//because OrganisaatioTyyppi filter of criteria does not work!
    	List<OrganisaatioPerustieto> ret = new ArrayList<OrganisaatioPerustieto>();
    	SearchCriteria searchCriteria = new SearchCriteria();
    	searchCriteria.setLakkautetut(false);
    	searchCriteria.setAktiiviset(true);
    	List<OrganisaatioPerustieto> all=organisaatioSearchService.searchHierarchy(searchCriteria);
        if (all.size()>=10000) {
        	KelaGenerator.warn("Query resulted >= 10000 rows, which is the hard limit for returned rows. There may be more rows that are not returned because of this limitation! Please check.");
        }
        for (OrganisaatioPerustieto curOppilaitos : all) {
        	if (!orgOidList.contains(curOppilaitos.getOid()) && curOppilaitos.getOrganisaatiotyypit().contains(tyyppi)) {
        		ret.add(curOppilaitos);
        	}
        }
        return ret;
    }

    private static class OrganisaatioCounts {
    	int total=0;
    	int rejectedTotal=0;
    	int notInKoodisto=0;
    	int notOppilaitosToinenAsteOrKk=0;
    	int noParentToimipiste=0;
    	int notFoundInDb = 0;
    	int notIntactYhteystiedot=0;
    }
    
    private OrganisaatioCounts oppilaitosCounts;
    private OrganisaatioCounts toimipisteCounts;
 
    private boolean stopThread=false;
    public void stop() {
    	info("Container stopping.");
    	stopThread=true;
    }
    
    private void checkStop() throws UserStopRequestException {
    	if (stopThread) {
    		throw new UserStopRequestException();
    	}
    }
    
    public void fetchOrganisaatiot() throws UserStopRequestException {
    	long startTime = System.currentTimeMillis();
        oppilaitosoidOppilaitosMap = new HashMap<String, OrganisaatioPerustieto>();
        oppilaitokset = new ArrayList<OrganisaatioPerustieto>();
        orgOidList = new ArrayList<String>();
        OrganisaatioSearchCriteria criteria = new OrganisaatioSearchCriteria();
        info("Retrieving oppilaitokset....");
        List<OrganisaatioPerustieto> oppilaitoksetR = searchBasicOrganisaatios(criteria,OrganisaatioTyyppi.OPPILAITOS);

        /* for debug:
		try {
			BufferedOutputStream bostr = new BufferedOutputStream(new FileOutputStream(new File("oppilaitokset.txt")));
			for (OrganisaatioPerustieto curOppilaitos : oppilaitoksetR) {
				bostr.write((curOppilaitos.getOid() + "\n").getBytes());

				bostr.flush();
				if (!curOppilaitos.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
					bostr.write(("ei oppilaitos " + curOppilaitos.getNimi("FI") + " : " + curOppilaitos.getOrganisaatiotyypit()).getBytes());
				}
			}
			bostr.close();
			System.out.println("OPPILAITOS lkm :" + oppilaitoksetR.size());
		} catch (IOException e) {
			e.printStackTrace();
		}*/

        
        oppilaitosCounts = new OrganisaatioCounts();
        toimipisteCounts = new OrganisaatioCounts();
        for (OrganisaatioPerustieto curOppilaitos : oppilaitoksetR) {
        	checkStop();
            if (isOppilaitosWritable(curOppilaitos)) {
                oppilaitosoidOppilaitosMap.put(curOppilaitos.getOid(), curOppilaitos);
                oppilaitokset.add(curOppilaitos);
                orgOidList.add(curOppilaitos.getOid());
            }
        }
        oppilaitosCounts.total=oppilaitokset.size();
        oppilaitosCounts.rejectedTotal=oppilaitoksetR.size()-oppilaitokset.size();
        info("Generation time: " + (System.currentTimeMillis() - startTime)/1000.0 + " seconds");

        info("Retrieving toimipisteet....");
        toimipisteet = new ArrayList<OrganisaatioPerustieto>();
        criteria = new OrganisaatioSearchCriteria();
        List<OrganisaatioPerustieto> opetuspisteet = searchBasicOrganisaatios(criteria,OrganisaatioTyyppi.TOIMIPISTE);
        
        /* for debug:
		try {
			BufferedOutputStream bostr = new BufferedOutputStream(new FileOutputStream(new File("toimipisteet.txt")));
			for (OrganisaatioPerustieto curToimipiste : opetuspisteet) {
				bostr.write((curToimipiste.getOid() + "\n").getBytes());
				if (!curToimipiste.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.TOIMIPISTE)) {
					bostr.write(("ei toimipiste " + curToimipiste.getNimi("FI") + " : " + curToimipiste.getOrganisaatiotyypit()).getBytes());
				}
			}
			bostr.close();
			System.out.println("TOIMIPISTE lkm :" + opetuspisteet.size());
		} catch (IOException e) {
			e.printStackTrace();
		}*/

        startTime = System.currentTimeMillis();
        for (OrganisaatioPerustieto curToimipiste : opetuspisteet) {
        	checkStop();
            if (isToimipisteWritable(curToimipiste)) {
                toimipisteet.add(curToimipiste);
                orgOidList.add(curToimipiste.getOid());
            }
        }
        toimipisteCounts.total = opetuspisteet.size();
        toimipisteCounts.rejectedTotal = opetuspisteet.size()-toimipisteet.size();
        
        logToimipisteHaku(oppilaitosCounts,toimipisteCounts);
        info("Generation time: " + (System.currentTimeMillis() - startTime)/1000.0 + " seconds");
    }
    
    private void logToimipisteHaku(OrganisaatioCounts oppilaitosCounts, OrganisaatioCounts toimipisteCounts) {
    	info(              "Oppilaitos fetched from solr:");
    	info(String.format("    total valid oppilaitos: %s",oppilaitosCounts.total));
    	info(String.format("    total rejected oppilaitos: %s",oppilaitosCounts.rejectedTotal));
    	info(              "    oppilaitos warnings:");
    	info(String.format("        oppilaitos not found in oppilaitosnumero-koodisto: %s", oppilaitosCounts.notInKoodisto));
    	info(String.format("        oppilaitos has not valid käyntiosoite: %s", oppilaitosCounts.notIntactYhteystiedot));
    	info(              "    oppilaitos skipped:");
    	info(String.format("        oppilaitos not found in DB: %s",oppilaitosCounts.notFoundInDb));
    	info(String.format("        oppilaitos not toinen aste or korkeakoulu %s",oppilaitosCounts.notOppilaitosToinenAsteOrKk));
    	info(              "Toimipisteet fetched from solr:");
    	info(String.format("    total valid toimipiste: %s",toimipisteCounts.total));
    	info(String.format("    total rejected toimipiste: %s",toimipisteCounts.rejectedTotal));
    	info(String.format("    toimipiste warnings:"));
    	info(String.format("        toimipiste not found in opetuspisteet-koodisto: %s", toimipisteCounts.notInKoodisto));
    	info(String.format("        toimipiste has not valid käyntiosoite: %s", toimipisteCounts.notIntactYhteystiedot));
    	info(              "    toimipiste skipped:");
    	info(String.format("        toimipiste has no parent defined or not in oppilaitokset: %s",toimipisteCounts.noParentToimipiste));
    	info(String.format("        toimipiste not found in DB: %s",toimipisteCounts.notFoundInDb));
    }

    public boolean isOppilaitosWritable(OrganisaatioPerustieto curOppilaitos) {
    	if (!isOppilaitosToinenAsteOrKorkeakoulu(curOppilaitos)) {
    		debug("Not korkeakoulu or toinen aste - skipped");
    		oppilaitosCounts.notOppilaitosToinenAsteOrKk++;
    		return false;
    	}

    	Organisaatio orgE = kelaDAO.findOrganisaatioByOid(curOppilaitos.getOid());
    	if (null == orgE) {
    		error(1,curOppilaitos.getOid()+" "+curOppilaitos.getNimi());
    		oppilaitosCounts.notFoundInDb++;
    		return false;
    	}
    	
    	if (!isOppilaitosInKoodisto(curOppilaitos)) {
    		warn(1,curOppilaitos.getOppilaitosKoodi(),curOppilaitos.getOid()+" "+curOppilaitos.getNimi());
    		oppilaitosCounts.notInKoodisto++;
    		//return false; --not fatal?
    	}
    	
    	if (!hasOppilaitosIntactYhteystiedot(orgE)) {
    		warn(2,curOppilaitos.getOid()+" "+curOppilaitos.getNimi());
    		oppilaitosCounts.notIntactYhteystiedot++;
    		//return false; --not fatal?
    	}
    	return true;
    }

    private boolean hasOppilaitosIntactYhteystiedot(Organisaatio orgE) {
        return kelaDAO.getKayntiosoiteIdForOrganisaatio(orgE.getId()) != null;
    }

    public boolean isOppilaitosInKoodisto(OrganisaatioPerustieto curOppilaitos) {
        String oppilaitoskoodi = curOppilaitos.getOppilaitosKoodi();
        List<KoodiType> koodit = new ArrayList<KoodiType>();
        if (!StringUtils.isEmpty(oppilaitoskoodi)) {
            koodit = getKoodisByArvoAndKoodisto(oppilaitoskoodi, oppilaitosnumerokoodisto);
        }
        return koodit != null && !koodit.isEmpty();
    }

    public boolean isOppilaitosToinenAsteOrKorkeakoulu(
            OrganisaatioPerustieto curOppilaitos) {
        String opTyyppi = curOppilaitos.getOppilaitostyyppi();
        return  isTyyppiToinenaste(opTyyppi) || isTyyppiKorkeakoulu(opTyyppi);
    }
    
    int toinenAste=0;
    protected boolean isTyyppiToinenaste(String opTyyppi) {
    	for (String toinenAsteUri : this.toinenAsteUris) {
    		if (toinenAsteUri.equals(opTyyppi)) {
    			toinenAste++;
    			return true;
    		}
    	}
    	return false;
    }
    
    int korkeakoulu=0;
    protected boolean isTyyppiKorkeakoulu(String opTyyppi) {
    	for (String korkeakouluUri : this.korkeakouluUris) {
    		if (korkeakouluUri.equals(opTyyppi)) {
    			korkeakoulu++;
    			return true;
			}
    	}
    	return false;
    }

    public boolean isToimipisteWritable(OrganisaatioPerustieto curToimipiste) {
        debug("isToimpisteWRitable method " + curToimipiste.getNimi("fi"));
        if (curToimipiste.getParentOid() == null) {
        	toimipisteCounts.noParentToimipiste++;
        	error(2,curToimipiste.getOid()+" "+curToimipiste.getNimi());
            return false;
        }
 
        OrganisaatioPerustieto parentToimipiste = oppilaitosoidOppilaitosMap.get(curToimipiste.getParentOid());
        if (parentToimipiste == null) {
        	error(3,curToimipiste.getParentOid(),
        			curToimipiste.getOid()+" "+curToimipiste.getNimi());
        	toimipisteCounts.noParentToimipiste++;
            return false;
        }
        
        Organisaatio toimipisteE = kelaDAO.findOrganisaatioByOid(curToimipiste.getOid());
        if (null==toimipisteE) {
        	error(4,curToimipiste.getOid()+" "+curToimipiste.getNimi());
        	toimipisteCounts.notFoundInDb++;
        	return false;
        }
        
        List<KoodiType> koodit = new ArrayList<KoodiType>();
        if (null!= toimipisteE && !StringUtils.isEmpty(parentToimipiste.getOppilaitosKoodi()) && !StringUtils.isEmpty(toimipisteE.getOpetuspisteenJarjNro())) {
            String toimipistearvo = String.format("%s%s", parentToimipiste.getOppilaitosKoodi(), toimipisteE.getOpetuspisteenJarjNro());
            koodit = getKoodisByArvoAndKoodisto(toimipistearvo, toimipistekoodisto);
            if (koodit.isEmpty()) {
            	warn(3,toimipistearvo,toimipisteE.getOid()+" "+toimipisteE.getNimi());
            	toimipisteCounts.notInKoodisto++;
            }
        }
        
        if (null == kelaDAO.getKayntiosoiteIdForOrganisaatio(toimipisteE.getId())){
        	warn(4,toimipisteE.getOid()+" "+toimipisteE.getNimi());
        	toimipisteCounts.notIntactYhteystiedot++;
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

    private  List<KoodistoType> cachedKoodistoResult;
    private String cachedKoodistoUri;
    private List<KoodistoType> searchKoodistos(String koodistoUri) {
    	if(cachedKoodistoResult==null || !cachedKoodistoUri.equals(koodistoUri)) {
    		cachedKoodistoUri=koodistoUri;
    		SearchKoodistosCriteriaType koodistoSearchCriteria = KoodistoServiceSearchCriteriaBuilder.latestKoodistoByUri(cachedKoodistoUri);
    		cachedKoodistoResult = koodistoService.searchKoodistos(koodistoSearchCriteria);;
            if(cachedKoodistoResult.size() != 1) {
                // FIXME: Throw something other than RuntimeException?
                throw new RuntimeException("No koodisto found for koodisto URI " + koodistoUri);
            }
    	}
    	 return cachedKoodistoResult;
    }

    public List<KoodiType> getKoodisByArvoAndKoodisto(String arvo, String koodistoUri) {
        try {
            KoodistoType koodisto = searchKoodistos(koodistoUri).get(0);

            SearchKoodisByKoodistoCriteriaType koodiSearchCriteria = KoodiServiceSearchCriteriaBuilder.koodisByArvoAndKoodistoUriAndKoodistoVersio(arvo,
                    koodistoUri, koodisto.getVersio());
            return koodiService.searchKoodisByKoodisto(koodiSearchCriteria);
        } catch (Exception exp) {
            return null;
        }
    }

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
