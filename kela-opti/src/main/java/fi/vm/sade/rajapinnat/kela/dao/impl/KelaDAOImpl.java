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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
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
