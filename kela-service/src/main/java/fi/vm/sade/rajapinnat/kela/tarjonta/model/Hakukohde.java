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

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import fi.vm.sade.generic.model.BaseEntity;

@Entity
@Table(name="hakukohde")
public class Hakukohde {
    
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue
    private Long id;
    
    @Column(name = "oid", unique=true)
    private String oid;

    @Column(name = "kela_linja_koodi")
    private String kelaLinjaKoodi;
    @Column(name = "kela_linja_tarkenne")
    private String kelaLinjaTarkenne;
    
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
    
    public String getKelaLinjaKoodi() {
        return kelaLinjaKoodi;
    }

    public void setKelaLinjaKoodi(String kelaLinjaKoodi) {
        this.kelaLinjaKoodi = kelaLinjaKoodi;
    }

    public String getKelaLinjaTarkenne() {
        return kelaLinjaTarkenne;
    }

    public void setKelaLinjaTarkenne(String kelaLinjaTarkenne) {
        this.kelaLinjaTarkenne = kelaLinjaTarkenne;
    }
    
    @JoinTable(name = "koulutus_hakukohde",
    joinColumns = @JoinColumn(name = "hakukohde_id", referencedColumnName = BaseEntity.ID_COLUMN_NAME),
    inverseJoinColumns = @JoinColumn(name = "koulutus_id", referencedColumnName = BaseEntity.ID_COLUMN_NAME ))
    @ManyToMany(fetch = FetchType.LAZY)
    private List<KoulutusmoduuliToteutus> koulutukset;

	public List<KoulutusmoduuliToteutus> getKoulutukset() {
		return koulutukset;
	}

	public void setKoulutukset(List<KoulutusmoduuliToteutus> koulutukset) {
		this.koulutukset = koulutukset;
	}
    
}
