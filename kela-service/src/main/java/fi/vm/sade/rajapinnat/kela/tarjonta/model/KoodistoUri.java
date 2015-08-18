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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.apache.commons.lang.StringUtils;

/**
 * Strongly typed class to enforce structure of an uri and to make the code more
 * readable that uses Koodisto uri's (instead of just using strings).
 *
 * Wrapper for Koodisto uri. Use as @Embedded or inside @ElementCollection.
 *
 */
@Embeddable
public class KoodistoUri implements Serializable {

    private static final long serialVersionUID = 6772772416321895399L;

    @Column(name = "koodi_uri", nullable = false)
    private String koodiUri;

    /**
     * JPA constructor.
     */
    protected KoodistoUri() {
    }

    /**
     * @param koodiUri a non-empty Uri
     */
    public KoodistoUri(String koodiUri) {
        this();
        assert StringUtils.isNotEmpty(koodiUri) : "koodiUri cannot be empty string";
        this.koodiUri = koodiUri;
    }

    /**
     * Returns a non-null koodi uri.
     *
     * @return
     */
    public String getKoodiUri() {
        return koodiUri;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KoodistoUri other = (KoodistoUri) obj;
        if ((this.koodiUri == null) ? (other.koodiUri != null) : !this.koodiUri.equals(other.koodiUri)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.koodiUri != null ? this.koodiUri.hashCode() : 0);
        return hash;
    }

}
