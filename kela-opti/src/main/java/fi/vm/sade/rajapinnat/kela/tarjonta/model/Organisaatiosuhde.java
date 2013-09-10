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
package fi.vm.sade.rajapinnat.kela.tarjonta.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * 
 * @author Markus
 */
@Entity
@Table(name="organisaatiosuhde")
public class Organisaatiosuhde {
    
    /**
     * Relation types.
     * Possible extension point for different types of relations (belongs, relates, ...)
     */
    public enum OrganisaatioSuhdeTyyppi {
        /**
         * This is used when Organisation is "moved" so that the old relation is stored for later history browsing.
         */
         HISTORIA,
        /**
         * When old Organisation ceases to exist, old ones should be attached to it with this type of information.
         */
        LIITOS
    };
    

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganisaatioSuhdeTyyppi suhdeTyyppi = OrganisaatioSuhdeTyyppi.HISTORIA;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Organisaatio parent;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Organisaatio child;

    @Temporal(TemporalType.TIMESTAMP)
    private Date alkuPvm;
    
    
    public OrganisaatioSuhdeTyyppi getSuhdeTyyppi() {
        return suhdeTyyppi;
    }

    public void setSuhdeTyyppi(OrganisaatioSuhdeTyyppi suhdeTyyppi) {
        this.suhdeTyyppi = suhdeTyyppi;
    }

    public Organisaatio getParent() {
        return parent;
    }

    public void setParent(Organisaatio parent) {
        this.parent = parent;
    }

    public Organisaatio getChild() {
        return child;
    }

    public void setChild(Organisaatio child) {
        this.child = child;
    }

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
