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

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.springframework.stereotype.Repository;

import fi.vm.sade.rajapinnat.kela.dao.HakukohdeDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Yhteystieto;

/**
 * 
 * @author Markus
 */
@Repository
public class HakukohdeDAOImpl implements HakukohdeDAO { 
    
    private EntityManager tarjontaEm;
    private EntityManager organisaatioEm;
    
    private static final String KAYNTIOSOITE = "kaynti";
    
    @PostConstruct
    public void initEntityManagers () {
        tarjontaEm = Persistence.createEntityManagerFactory("tarjonta").createEntityManager();
        organisaatioEm = Persistence.createEntityManagerFactory("organisaatio").createEntityManager();
    }

    @Override
    public Hakukohde findHakukohdeByOid(String oid) {
        return (Hakukohde) tarjontaEm.createQuery("FROM "+Hakukohde.class.getName()+" WHERE oid=?")
                                .setParameter(1, oid)
                                .getSingleResult();
    }

    @Override
    public Organisaatio findOrganisaatioByOid(String oid) {
        return (Organisaatio) organisaatioEm.createQuery("FROM "+Organisaatio.class.getName()+" WHERE oid=?")
                                .setParameter(1, oid)
                                .getSingleResult();
    }

    @Override
    public Organisaatio findFirstChildOrganisaatio(String oid) {
        return (Organisaatio) organisaatioEm.createQuery("FROM " + Organisaatio.class.getName() + " WHERE parentOidPath like ? ")
                .setParameter(1, oid)
                .getSingleResult();
    }

    @Override
    public Long getKayntiosoiteIdForOrganisaatio(Long id) {
        return (Long) organisaatioEm.createQuery("SELECT id FROM " + Yhteystieto.class.getName() + " WHERE organisaatioId = ? AND osoiteTyyppi = ")
                .setParameter(1, id)
                .setParameter(2, KAYNTIOSOITE)
                .getSingleResult();
    }

}
