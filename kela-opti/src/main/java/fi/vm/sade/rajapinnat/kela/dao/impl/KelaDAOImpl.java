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
package fi.vm.sade.rajapinnat.kela.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Koulutusmoduuli;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.KoulutusmoduuliToteutus;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatiosuhde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatiosuhde.OrganisaatioSuhdeTyyppi;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Yhteystieto;

/**
 * 
 * @author Markus
 */
@Repository
public class KelaDAOImpl implements KelaDAO { 
    
    private EntityManager tarjontaEm;
    private EntityManager organisaatioEm;
    
    private static final String KAYNTIOSOITE = "kaynti";
    
    private String tarjontaDbUrl;
    private String organisaatioDbUrl;
    
    @PostConstruct
    public void initEntityManagers () {
        Map<String, String> tarjontaDbProperties = new HashMap<String, String>(); 
        tarjontaDbProperties.put("hibernate.connection.url", tarjontaDbUrl);
        
        Map<String, String> organisaatioDbProperties = new HashMap<String, String>(); 
        organisaatioDbProperties.put("hibernate.connection.url", organisaatioDbUrl);
        
        tarjontaEm = Persistence.createEntityManagerFactory("tarjontaKela", tarjontaDbProperties).createEntityManager();
        organisaatioEm = Persistence.createEntityManagerFactory("organisaatioKela", organisaatioDbProperties).createEntityManager();
    }

    @Override
    public Hakukohde findHakukohdeByOid(String oid) {
        try {
            return (Hakukohde) tarjontaEm.createQuery("FROM "+Hakukohde.class.getName()+" WHERE oid=?")
                                .setParameter(1, oid)
                                .getSingleResult();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Koulutusmoduuli getKoulutusmoduuli(String oid) {
        try {
        	Koulutusmoduuli koulutusmoduuli = (Koulutusmoduuli) tarjontaEm.createQuery("FROM "+Koulutusmoduuli.class.getName()+" WHERE oid=?")
            .setParameter(1, oid)
            .getSingleResult();
            return koulutusmoduuli;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public KoulutusmoduuliToteutus getKoulutusmoduuliToteutus(String oid) {
        try {
        	KoulutusmoduuliToteutus koulutusmoduuliToteutus = (KoulutusmoduuliToteutus) tarjontaEm.createQuery("FROM "+KoulutusmoduuliToteutus.class.getName()+" WHERE oid=?")
            .setParameter(1, oid)
            .getSingleResult();
            return koulutusmoduuliToteutus;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public  List<String> getParentOids(String oid) {
    	ArrayList<String> resultList = new  ArrayList<String>();
    	_getParentOids(oid,resultList);
    	return resultList;
    }

    @SuppressWarnings("unchecked")
	private void _getParentOids(String rootOid,List<String> resultList) {
    	if (resultList.contains(rootOid)) {
    		return;
    	}
		String qString=
				"select km.oid "+
				"from koulutus_sisaltyvyys ks,"+
				"	koulutusmoduuli km,"+
				"	koulutusmoduuli km2,"+
				"	koulutus_sisaltyvyys_koulutus ksk "+
				"   where "+
				" km.id=ks.parent_id and "+
				" ks.id=ksk.koulutus_sisaltyvyys_id and "+
				" ksk.koulutusmoduuli_id=km2.id and "+
				" km2.oid = ?";
		for (String oid : (List<String>) tarjontaEm.createNativeQuery(qString).setParameter(1, rootOid).getResultList()) {
			_getParentOids(oid, resultList);
			resultList.add(oid);
		}
    }
    
    @Override
    public  List<String> getChildrenOids(String oid) {
    	ArrayList<String> resultList = new  ArrayList<String>();
    	_getChildrenOids(oid,resultList);
    	return resultList;
    }

    @SuppressWarnings("unchecked")
	private void _getChildrenOids(String rootOid,List<String> resultList) {
    	if (resultList.contains(rootOid)) {
    		return;
    	}
		String qString=
				"select km.oid "+
				"from koulutus_sisaltyvyys ks,"+
				"	koulutusmoduuli km,"+
				"	koulutusmoduuli km2,"+
				"	koulutus_sisaltyvyys_koulutus ksk "+
				" where "+
				" ks.id=ksk.koulutus_sisaltyvyys_id and "+
				" ksk.koulutusmoduuli_id=km.id and  "+
				" ks.parent_id=km2.id and "+
				" km2.oid = ?";
		for (String oid : (List<String>) tarjontaEm.createNativeQuery(qString).setParameter(1, rootOid).getResultList()) {
			_getChildrenOids(oid, resultList);
			resultList.add(oid);
		}
    }
    
    @Override
    public Organisaatio findOrganisaatioByOid(String oid) {
        try {
            return (Organisaatio) organisaatioEm.createQuery("FROM "+Organisaatio.class.getName()+" WHERE oid=?")
                                .setParameter(1, oid)
                                .getSingleResult();
        } catch (Exception ex) {
            return null;
        }
    }

  
    @Override
    public Organisaatio findFirstChildOrganisaatio(String oid) {
        try {
            return (Organisaatio) organisaatioEm.createQuery("FROM " + Organisaatio.class.getName() + " WHERE parentOidPath like ? ")
                    .setParameter(1, oid)
                    .getSingleResult();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Long getKayntiosoiteIdForOrganisaatio(Long id) {
        try {
            return (Long) organisaatioEm.createQuery("SELECT id FROM " + Yhteystieto.class.getName() + " WHERE organisaatioId = ? AND osoiteTyyppi = ?")
                .setParameter(1, id)
                .setParameter(2, KAYNTIOSOITE)
                .getSingleResult();
        } catch (Exception ex) {
            return null;
        }
    }
    

    @SuppressWarnings("unchecked")
    @Override
    public List<Organisaatiosuhde> findAllLiitokset() {
        return (List<Organisaatiosuhde>) organisaatioEm.createQuery("FROM " + Organisaatiosuhde.class.getName() + " WHERE suhdetyyppi = ?") 
                 .setParameter(1, OrganisaatioSuhdeTyyppi.LIITOS.name())
                 .getResultList();
    }
    
    public String getTarjontaDbUrl() {
        return tarjontaDbUrl;
    }

    @Value("${kela-tarjontadburl}")
    public void setTarjontaDbUrl(String tarjontaDbUrl) {
        this.tarjontaDbUrl = tarjontaDbUrl;
    }

    public String getOrganisaatioDbUrl() {
        return organisaatioDbUrl;
    }

    @Value("${kela-organisaatiodburl}")
    public void setOrganisaatioDbUrl(String organisaatioDbUrl) {
        this.organisaatioDbUrl = organisaatioDbUrl;
    }
}
