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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

@Entity
public class MonikielinenTeksti {
    
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue
    private Long id;
    

    @ElementCollection(fetch= FetchType.EAGER)
    @MapKeyColumn(name="key")
    @Column(name="value", length=4096)
    @CollectionTable(joinColumns=@JoinColumn(name="id"))
    private Map<String, String> values = new HashMap<String, String>();

    public Map<String, String> getValues() {
        return values;
    }

    public void addString(String key, String value) {
        if (value == null) {
            getValues().remove(key);
        } else {
            getValues().put(key, value);
        }
    }

    public String getString(String key) {
        return getValues().get(key);
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
