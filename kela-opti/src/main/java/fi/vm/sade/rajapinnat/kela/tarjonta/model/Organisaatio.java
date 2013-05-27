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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;


/**
 * 
 * @author Markus
 */
@Entity
@Table(name="organisaatio")
public class Organisaatio {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue
    private Long id;
    
    @Column(name = "opetuspisteenjarjnro")
    private String opetuspisteenJarjNro;
    
    @Column(name = "oid")
    private String oid;
    
    @Column(name = "parentoidpath")
    private String parentOidPath;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nimi_mkt")
    private MonikielinenTeksti nimi;
    
    @ElementCollection
    @CollectionTable(name = "organisaatio_kielet", joinColumns = @JoinColumn(name = "organisaatio_id"))
    private List<String> kielet = new ArrayList<String>();
    
    public List<String> getKielet() {
        return kielet;
    }

    public void setKielet(List<String> kielet) {
        this.kielet = kielet;
    }

    public MonikielinenTeksti getNimi() {
        return nimi;
    }

    public void setNimi(MonikielinenTeksti nimi) {
        this.nimi = nimi;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpetuspisteenJarjNro() {
        return opetuspisteenJarjNro;
    }

    public void setOpetuspisteenJarjNro(String opetuspisteenJarjNro) {
        this.opetuspisteenJarjNro = opetuspisteenJarjNro;
    }
    
}
