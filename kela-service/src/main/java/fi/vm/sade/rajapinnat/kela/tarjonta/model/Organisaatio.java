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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

    @Column(name="kotipaikka")
    private String kotipaikka;
    
    @Column(name="oppilaitostyyppi")
    private String oppilaitosTyyppi;
    
    @Column(name="oppilaitoskoodi")
    private String oppilaitoskoodi;

    @Column(name="organisaatiotyypitstr")
    private String organisaatiotyypitstr;
    
    @Column(name="ytunnus")
    private String ytunnus;

    @Column(name="virastotunnus")
    private String virastotunnus;
    
    @Column(name="yhteishaunkoulukoodi")
    private String yhteishaunkoulukoodi;
    
    @Column(name="alkupvm")
    private Date alkupvm;
    
    @Column(name="lakkautuspvm")
    private Date lakkautuspvm;

    
    public String getOppilaitosTyyppi() {
        return oppilaitosTyyppi;
    }

    public void setOppilaitosTyyppi(String oppilaitosTyyppi) {
        this.oppilaitosTyyppi = oppilaitosTyyppi;
    }

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

    public String getParentOidPath() {
        return parentOidPath;
    }

    public void setParentOidPath(String parentOidPath) {
        this.parentOidPath = parentOidPath;
    }

    public String getKotipaikka() {
        return kotipaikka;
    }

    public void setKotipaikka(String kotipaikka) {
        this.kotipaikka = kotipaikka;
    }
    
    public String getYtunnus() {
        return ytunnus;
    }

    public void setYtunnus(String ytunnus) {
        this.ytunnus = ytunnus;
    }

    public String getVirastotunnus() {
        return virastotunnus;
    }

    public void setVirastotunnus(String virastotunnus) {
        this.virastotunnus = virastotunnus;
    }
    
    public String getOppilaitoskoodi() {
        return oppilaitoskoodi;
    }

    public void setOppilaitoskoodi(String oppilaitoskoodi) {
        this.oppilaitoskoodi = oppilaitoskoodi;
    }

	public String getYhteishaunkoulukoodi() {
		return yhteishaunkoulukoodi;
	}

	public void setYhteishaunkoulukoodi(String yhteishaunkoulukoodi) {
		this.yhteishaunkoulukoodi = yhteishaunkoulukoodi;
	}

	public String getOrganisaatiotyypitstr() {
		return organisaatiotyypitstr;
	}

	public void setOrganisaatiotyypitstr(String organisaatiotyypitstr) {
		this.organisaatiotyypitstr = organisaatiotyypitstr;
	}

	public Date getAlkupvm() {
		return alkupvm;
	}

	public void setAlkupvm(Date alkupvm) {
		this.alkupvm = alkupvm;
	}

	public Date getLakkautuspvm() {
		return lakkautuspvm;
	}

	public void setLakkautuspvm(Date lakkautuspvm) {
		this.lakkautuspvm = lakkautuspvm;
	}
}