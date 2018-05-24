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

import javax.persistence.*;

/**
 * 
 * @author Markus
 */
@Entity
@Table(name="koulutusmoduuli")
public class Koulutusmoduuli {
    
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue
    private Long id;
    
    @Column(name = "oid", unique=true)
    private String oid;
    @Column(name = "koulutus_uri")
    private String koulutusUri;
    @Column(name = "koulutusaste_uri")
    private String koulutusaste_uri;
    @Column(name = "koulutustyyppi_uri")
    private String koulutustyyppi_uri;

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

	public String getKoulutusUri() {
		return koulutusUri;
	}

	public void setKoulutusUri(String koulutusUri) {
		this.koulutusUri = koulutusUri;
	}

	public String getKoulutusaste_uri() {
		return koulutusaste_uri;
	}

	public void setKoulutusaste_uri(String koulutusaste_uri) {
		this.koulutusaste_uri = koulutusaste_uri;
	}

    public String getKoulutustyyppi_uri() {
        return koulutustyyppi_uri;
    }

    public void setKoulutustyyppi_uri(String koulutustyyppi_uri) {
        this.koulutustyyppi_uri = koulutustyyppi_uri;
    }
}
