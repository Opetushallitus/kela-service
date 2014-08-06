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
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
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
    private static final String PUHELIN = "puhelin";
    private static final String POSTI = "posti";
    private static final String WWW = "Www";
    
    private String tarjontaDbUrl;
    private String tarjontaDbUsername;
    private String tarjontaDbPassword;
    private String organisaatioDbUrl;
    private String organisaatioDbUsername;
    private String organisaatioDbPassword;
    
    @PostConstruct
    public void initEntityManagers () {
        Map<String, String> tarjontaDbProperties = new HashMap<String, String>(); 
        tarjontaDbProperties.put("hibernate.connection.url", tarjontaDbUrl);
        tarjontaDbProperties.put("hibernate.connection.username", tarjontaDbUsername);
        tarjontaDbProperties.put("hibernate.connection.password", tarjontaDbPassword);
        
        Map<String, String> organisaatioDbProperties = new HashMap<String, String>(); 
        organisaatioDbProperties.put("hibernate.connection.url", organisaatioDbUrl);
        organisaatioDbProperties.put("hibernate.connection.username", organisaatioDbUsername);
        organisaatioDbProperties.put("hibernate.connection.password", organisaatioDbPassword);
        
        tarjontaEm = Persistence.createEntityManagerFactory("tarjontaKela", tarjontaDbProperties).createEntityManager();
        organisaatioEm = Persistence.createEntityManagerFactory("organisaatioKela", organisaatioDbProperties).createEntityManager();
    }

    @Override
    public Hakukohde findHakukohdeByOid(String oid) {
    	try {
            return (Hakukohde) tarjontaEm.createQuery("FROM "+Hakukohde.class.getName()+" WHERE oid=? and tila='JULKAISTU'")
                                .setParameter(1, oid)
                                .getSingleResult();
        } catch (NoResultException ex) {
            return null;

        } catch (NonUniqueResultException ex) {
            return null;
        }    
    }

    @Override
    public Koulutusmoduuli getKoulutusmoduuli(String oid) {
        try {
        	Koulutusmoduuli koulutusmoduuli = (Koulutusmoduuli) tarjontaEm.createQuery("FROM "+Koulutusmoduuli.class.getName()+" WHERE oid=? and tila='JULKAISTU'")
            .setParameter(1, oid)
            .getSingleResult();
            return koulutusmoduuli;
        } catch (NoResultException ex) {
        	return null;
        } catch (NonUniqueResultException ex) {
        	return null;
        }
    }

    @Override
    public KoulutusmoduuliToteutus getKoulutusmoduuliToteutus(String oid) {
        try {
        	KoulutusmoduuliToteutus koulutusmoduuliToteutus = (KoulutusmoduuliToteutus) tarjontaEm.createQuery("FROM "+KoulutusmoduuliToteutus.class.getName()+" WHERE oid=? and tila='JULKAISTU'")
            .setParameter(1, oid)
            .getSingleResult();
            return koulutusmoduuliToteutus;
	    } catch (NoResultException ex) {
	        return null;
	
	    } catch (NonUniqueResultException ex) {
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
				" km.tila='JULKAISTU' and "+
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
				" km.tila='JULKAISTU' and "+
				" ks.parent_id=km2.id and "+
				" km2.tila='JULKAISTU' and "+
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
	    } catch (NoResultException ex) {
	        return null;
	
	    } catch (NonUniqueResultException ex) {
	        return null;
	    }
    }

  
    @Override
    public Organisaatio findFirstChildOrganisaatio(String oid) {
        try {
            return (Organisaatio) organisaatioEm.createQuery("FROM " + Organisaatio.class.getName() + " WHERE parentOidPath like ? ")
                    .setParameter(1, oid)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;

        } catch (NonUniqueResultException ex) {
            return null;
        }
    }

    @Override
    public String getPuhelinnumero(String orgOid) {
    	@SuppressWarnings("unchecked")
        List<String> resultList =  organisaatioEm.createQuery("SELECT puhelinnumero FROM " + Yhteystieto.class.getName() + " WHERE organisaatio_id IN (SELECT id FROM "+ Organisaatio.class.getName() + " WHERE OID=?) AND tyyppi = ? AND dType='Puhelinnumero' order by id desc")
                .setParameter(1, orgOid)
                .setParameter(2, PUHELIN)
                .getResultList();
        if (resultList==null || resultList.size()==0) {
        	return null;
        }
        return resultList.get(0);
    }

    @Override
    public String getEmail(String orgOid) {
    	@SuppressWarnings("unchecked")
    	List<String> resultList =  organisaatioEm.createQuery("SELECT email FROM " + Yhteystieto.class.getName() + " WHERE organisaatio_id IN (SELECT id FROM "+ Organisaatio.class.getName() + " WHERE OID=?) AND dType='Email' order by id desc")
                .setParameter(1, orgOid)
                .getResultList();
    	if (resultList==null || resultList.size()==0) {
    		return null;
    	}
    	return resultList.get(0);
    }
    
    private Long _getKayntiosoiteIdForOrganisaatio(Long id, String osoiteTyyppi) {
        @SuppressWarnings("unchecked")
		List<Long> resultList = organisaatioEm.createQuery("SELECT id FROM " + Yhteystieto.class.getName() + " WHERE organisaatioId = ? AND osoiteTyyppi = ? order by id desc")
				  .setParameter(1, id)
                  .setParameter(2, osoiteTyyppi)
                  .getResultList();
         
         if (resultList==null || resultList.size()==0) {
        	 return null;
        }
        return resultList.get(0);
    }

    private Long _getWwwIdForOrganisaatio(Long id) {
        @SuppressWarnings("unchecked")
		List<Long> resultList = organisaatioEm.createQuery("SELECT id FROM " + Yhteystieto.class.getName() + " WHERE organisaatioId = ? AND dType = ? order by id desc")
				  .setParameter(1, id)
                  .setParameter(2, WWW)
                  .getResultList();
         
        if (resultList==null || resultList.size()==0) {
        	 return null;
        }
        return resultList.get(0);
    }

    @Override
    public Long getKayntiosoiteIdForOrganisaatio(Long id) {
    	Long kayntiOsoiteId  = _getKayntiosoiteIdForOrganisaatio(id, KAYNTIOSOITE);
    	if (null != kayntiOsoiteId) return kayntiOsoiteId;
    	//fallback to postiosoite
    	kayntiOsoiteId  = _getKayntiosoiteIdForOrganisaatio(id, POSTI);
    	if (null != kayntiOsoiteId) return kayntiOsoiteId;
    	//fallback to www
    	return _getWwwIdForOrganisaatio(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Organisaatiosuhde> findAllLiitokset() {
        return (List<Organisaatiosuhde>) organisaatioEm.createQuery("FROM " + Organisaatiosuhde.class.getName() + " WHERE suhdetyyppi = ?") 
                 .setParameter(1, OrganisaatioSuhdeTyyppi.LIITOS.name())
                 .getResultList();
    }
    
    @Value("${kela-tarjontadb.url}")
    public void setTarjontaDbUrl(String tarjontaDbUrl) {
        this.tarjontaDbUrl = tarjontaDbUrl;
    }

    public String getTarjontaDbUrl() {
        return tarjontaDbUrl;
    }

    @Value("${kela-tarjontadb.username}")
	public void setTarjontaDbUsername(String tarjontaDbUsername) {
		this.tarjontaDbUsername = tarjontaDbUsername;
	}

    public String getTarjontaDbUsername() {
		return tarjontaDbUsername;
	}
    
    @Value("${kela-tarjontadb.password}")
	public void setTarjontaDbPassword(String tarjontaDbPassword) {
		this.tarjontaDbPassword = tarjontaDbPassword;
	}

	public String getTarjontaDbPassword() {
		return tarjontaDbPassword;
	}

	@Value("${kela-organisaatiodb.url}")
    public void setOrganisaatioDbUrl(String organisaatioDbUrl) {
        this.organisaatioDbUrl = organisaatioDbUrl;
    }

	public String getOrganisaatioDbUrl() {
        return organisaatioDbUrl;
    }

	@Value("${kela-organisaatiodb.username}")
	public void setOrganisaatioDbUsername(String organisaatioDbUsername) {
		this.organisaatioDbUsername = organisaatioDbUsername;
	}

	public String getOrganisaatioDbUsername() {
		return organisaatioDbUsername;
	}

	@Value("${kela-organisaatiodb.password}")
	public void setOrganisaatioDbPassword(String organisaatioDbPassword) {
		this.organisaatioDbPassword = organisaatioDbPassword;
	}

	public String getOrganisaatioDbPassword() {
		return organisaatioDbPassword;
	}
}
