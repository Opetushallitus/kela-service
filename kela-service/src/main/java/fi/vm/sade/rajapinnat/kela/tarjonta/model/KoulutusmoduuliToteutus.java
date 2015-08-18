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

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

@Entity
@Table(name = "koulutusmoduuli_toteutus")
public class KoulutusmoduuliToteutus {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue
    private Long id;

    @Column(name = "oid", unique = true)
    private String oid;
    @Column(name = "koulutus_uri")
    private String koulutusUri;

    @Column(name = "alkamiskausi_uri")
    private String alkamiskausi_uri;
    @Column(name = "alkamisvuosi")
    private Integer alkamisvuosi;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "koulutusmoduuli_toteutus_sisaltyvat_koulutuskoodit",
            joinColumns = @JoinColumn(name = "koulutusmoduuli_toteutus_id")
    )
    private Set<KoodistoUri> sisaltyvatKoulutuskoodit = new HashSet<KoodistoUri>();

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Koulutusmoduuli koulutusmoduuli;

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

    public Koulutusmoduuli getKoulutusmoduuli() {
        return koulutusmoduuli;
    }

    public void setKoulutusmoduuli(Koulutusmoduuli koulutusmoduuli) {
        this.koulutusmoduuli = koulutusmoduuli;
    }

    public String getAlkamiskausi_uri() {
        return alkamiskausi_uri;
    }

    public void setAlkamiskausi_uri(String alkamiskausi_uri) {
        this.alkamiskausi_uri = alkamiskausi_uri;
    }

    public Integer getAlkamisvuosi() {
        return alkamisvuosi;
    }

    public void setAlkamisvuosi(Integer alkamisvuosi) {
        this.alkamisvuosi = alkamisvuosi;
    }

    public Set<KoodistoUri> getSisaltyvatKoulutuskoodit() {
        return this.sisaltyvatKoulutuskoodit;
    }

    public void setSisaltyvatKoulutuskoodit(Set<KoodistoUri> sisaltyvatKoulutuskoodit) {
        this.sisaltyvatKoulutuskoodit = sisaltyvatKoulutuskoodit;
    }

}
