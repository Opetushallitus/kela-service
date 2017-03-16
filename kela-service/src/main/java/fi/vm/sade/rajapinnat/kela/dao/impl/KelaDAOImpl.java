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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import fi.vm.sade.rajapinnat.kela.dto.TasoJaLaajuusContainer;
import org.springframework.stereotype.Repository;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.KoodistoUri;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Koulutusmoduuli;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.KoulutusmoduuliToteutus;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.OrganisaatioPerustieto;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatiosuhde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatiosuhde.OrganisaatioSuhdeTyyppi;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Yhteystieto;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

/**
 *
 * @author Markus
 */
@Repository
public class KelaDAOImpl implements KelaDAO {

    private EntityManager tarjontaEm;
    private EntityManager organisaatioEm;

    @Inject
    @Named("tarjontaEntityManagerFactory")
    @PersistenceUnit(unitName = "tarjontaKela")
    private EntityManagerFactory tarjontaEmf;

    @Inject
    @Named("organisaatioEntityManagerFactory")
    @PersistenceUnit(unitName = "organisaatioKela")
    private EntityManagerFactory organisaatioEmf;

    private static final String KAYNTIOSOITE = "kaynti";
    private static final String POSTI = "posti";
    private static final String WWW = "Www";

    private static long generated_yht_id = 9000000001L;
    private static HashMap<Long, Long> yht_id_map = new HashMap<Long, Long>(); //id of organisation, yht_id 

    @PostConstruct
    public void initEntityManagers() {
        tarjontaEm = tarjontaEmf.createEntityManager();
        organisaatioEm = organisaatioEmf.createEntityManager();
    }

    @Override
    public Hakukohde findHakukohdeByOid(String oid) {
        try {
            return (Hakukohde) getTarjontaEntityManager().createQuery("FROM " + Hakukohde.class.getName() + " WHERE oid=? ")//and tila='JULKAISTU'")
                    .setParameter(1, oid)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;

        } catch (NonUniqueResultException ex) {
            return null;
        }
    }

    @Override
    public Koulutusmoduuli getKoulutusmoduuli(String oid) {
        try {
            Koulutusmoduuli koulutusmoduuli = (Koulutusmoduuli) getTarjontaEntityManager().createQuery("FROM " + Koulutusmoduuli.class.getName() + " WHERE oid=? ")//and tila='JULKAISTU'")
                    .setParameter(1, oid)
                    .getSingleResult();
            return koulutusmoduuli;
        } catch (NoResultException ex) {
            return null;
        } catch (NonUniqueResultException ex) {
            return null;
        }
    }

    @Override
    public KoulutusmoduuliToteutus getKoulutusmoduuliToteutus(String oid) {
        try {
            KoulutusmoduuliToteutus koulutusmoduuliToteutus = (KoulutusmoduuliToteutus) getTarjontaEntityManager().createQuery("FROM " + KoulutusmoduuliToteutus.class.getName() + " WHERE oid=? ")// and tila='JULKAISTU'")
                    .setParameter(1, oid)
                    .getSingleResult();
            return koulutusmoduuliToteutus;
        } catch (NoResultException ex) {
            return null;

        } catch (NonUniqueResultException ex) {
            return null;
        }
    }

    @Override
    public List<String> getParentOids(String oid) {
        ArrayList<String> resultList = new ArrayList<String>();
        _getParentOids(oid, resultList);
        return resultList;
    }

    @SuppressWarnings("unchecked")
    private void _getParentOids(String rootOid, List<String> resultList) {
        if (resultList.contains(rootOid)) {
            return;
        }
        String qString
                = "select km.oid "
                + "from koulutus_sisaltyvyys ks,"
                + "	koulutusmoduuli km,"
                + "	koulutusmoduuli km2,"
                + "	koulutus_sisaltyvyys_koulutus ksk "
                + "   where "
                + //" km.tila='JULKAISTU' and "+
                " km.id=ks.parent_id and "
                + " ks.id=ksk.koulutus_sisaltyvyys_id and "
                + " ksk.koulutusmoduuli_id=km2.id and "
                + " km2.oid = ?";
        for (String oid : (List<String>) getTarjontaEntityManager().createNativeQuery(qString).setParameter(1, rootOid).getResultList()) {
            _getParentOids(oid, resultList);
            resultList.add(oid);
        }
    }

    @Override
    public List<String> getChildrenOids(String oid) {
        ArrayList<String> resultList = new ArrayList<String>();
        _getChildrenOids(oid, resultList);
        return resultList;
    }

    @SuppressWarnings("unchecked")
    private void _getChildrenOids(String rootOid, List<String> resultList) {
        if (resultList.contains(rootOid)) {
            return;
        }
        String qString
                = "select km.oid "
                + "from koulutus_sisaltyvyys ks,"
                + "	koulutusmoduuli km,"
                + "	koulutusmoduuli km2,"
                + "	koulutus_sisaltyvyys_koulutus ksk "
                + " where "
                + " ks.id=ksk.koulutus_sisaltyvyys_id and "
                + " ksk.koulutusmoduuli_id=km.id and  "
                + //" km.tila='JULKAISTU' and "+
                " ks.parent_id=km2.id and "
                + //" km2.tila='JULKAISTU' and "+
                " km2.oid = ?";
        for (String oid : (List<String>) getTarjontaEntityManager().createNativeQuery(qString).setParameter(1, rootOid).getResultList()) {
            _getChildrenOids(oid, resultList);
            resultList.add(oid);
        }
    }

    @Override
    public Organisaatio findOrganisaatioByOid(String oid) {
        try {
            return (Organisaatio) getOrganisaatioEntityManager().createQuery("FROM " + Organisaatio.class.getName() + " WHERE oid=?")
                    .setParameter(1, oid)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;

        } catch (NonUniqueResultException ex) {
            return null;
        }
    }

    @Override
    public Organisaatio findFirstChildOrganisaatio(String oid) {
        try {
            return (Organisaatio) getOrganisaatioEntityManager().createQuery("FROM " + Organisaatio.class.getName() + " WHERE parentOidPath like ? ")
                    .setParameter(1, oid)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;

        } catch (NonUniqueResultException ex) {
            return null;
        }
    }

    private Long _getKayntiosoiteIdForOrganisaatio(Long id, String osoiteTyyppi) {
        @SuppressWarnings("unchecked")
        List<Long> resultList = getOrganisaatioEntityManager().createQuery("SELECT id FROM " + Yhteystieto.class.getName() + " WHERE organisaatioId = ? AND osoiteTyyppi = ? order by id desc")
                .setParameter(1, id)
                .setParameter(2, osoiteTyyppi)
                .getResultList();

        if (resultList == null || resultList.size() == 0) {
            return null;
        }
        return resultList.get(0);
    }

    private Long _getWwwIdForOrganisaatio(Long id) {
        @SuppressWarnings("unchecked")
        List<Long> resultList = getOrganisaatioEntityManager().createQuery("SELECT id FROM " + Yhteystieto.class.getName() + " WHERE organisaatioId = ? AND dType = ? order by id desc")
                .setParameter(1, id)
                .setParameter(2, WWW)
                .getResultList();

        if (resultList == null || resultList.size() == 0) {
            return null;
        }
        return resultList.get(0);
    }

    @Override
    public Long getKayntiosoiteIdForOrganisaatio(Long id) {
        if (yht_id_map.containsKey(id)) {
            return yht_id_map.get(id);
        }
        Long kayntiOsoiteId = _getKayntiosoiteIdForOrganisaatio(id, KAYNTIOSOITE);
        if (null == kayntiOsoiteId) {
            kayntiOsoiteId = _getKayntiosoiteIdForOrganisaatio(id, POSTI);
            if (null == kayntiOsoiteId) {
                kayntiOsoiteId = _getWwwIdForOrganisaatio(id);
            }
        }
        if (kayntiOsoiteId == null) {
            kayntiOsoiteId = (++generated_yht_id);
        }
        yht_id_map.put(id, kayntiOsoiteId);
        return kayntiOsoiteId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Organisaatiosuhde> findAllLiitokset() {
        return (List<Organisaatiosuhde>) getOrganisaatioEntityManager().createQuery("FROM " + Organisaatiosuhde.class.getName() + " WHERE suhdetyyppi = ?")
                .setParameter(1, OrganisaatioSuhdeTyyppi.LIITOS.name())
                .getResultList();
    }

    private OrganisaatioPerustieto applyOrganisaatio(Object[] organisaatio) {
        OrganisaatioPerustieto result = new OrganisaatioPerustieto();

        /*+"o.oid, " 0 
         +"o.oppilaitostyyppi, " 1
         +"o.oppilaitoskoodi, " 2
         +"o.organisaatiotyypitstr, " 3
         +"o.ytunnus, " 4
         +"mktv_fi.value as nimi_fi, " 5 
         +"mktv_sv.value as nimi_sv, " 6
         +"mktv_en.value as nimi_en "  7
         */
        setNimiIfNotNull("en", (String) organisaatio[7], result);
        setNimiIfNotNull("fi", (String) organisaatio[5], result);
        setNimiIfNotNull("sv", (String) organisaatio[6], result);
        result.setOid((String) organisaatio[0]);

        result.setOppilaitosKoodi((String) organisaatio[2]);
        String[] values = ((String) organisaatio[3]).split("\\|");
        if (values != null) {
            for (String value : values) {
                if (value.length() > 0) {
                    result.getOrganisaatiotyypit().add(OrganisaatioTyyppi.fromValue((String) value));
                }
            }
        }
        result.setYtunnus((String) organisaatio[4]);
        result.setOppilaitostyyppi((String) organisaatio[1]);
        return result;
    }

    private void setNimiIfNotNull(String targetLanguage, String sourceField,
            OrganisaatioPerustieto result) {
        final String nimi = sourceField;
        if (nimi != null) {
            result.setNimi(targetLanguage, nimi);
        }
    }

    @Override
    public List<OrganisaatioPerustieto> findOppilaitokset(List<String> oppilaitostyypit) {
        String csvWithQuote = oppilaitostyypit.toString().replace("[", "'").replace("]", "'")
                .replace(", ", "','");

        String sQuery
                = " select "
                + "o.oid, "
                + "o.oppilaitostyyppi, "
                + "o.oppilaitoskoodi, "
                + "o.organisaatiotyypitstr, "
                + "o.ytunnus, "
                + "mktv_fi.value as nimi_fi, "
                + "mktv_sv.value as nimi_sv, "
                + "mktv_en.value as nimi_en "
                + " from organisaatio o "
                + " left join monikielinenteksti_values mktv_fi on o.nimi_mkt = mktv_fi.id and mktv_fi.key='fi' "
                + " left join monikielinenteksti_values mktv_sv on o.nimi_mkt = mktv_sv.id and mktv_sv.key='sv' "
                + " left join monikielinenteksti_values mktv_en on o.nimi_mkt = mktv_en.id and mktv_en.key='en' "
                + " where position('Oppilaitos' in o.organisaatiotyypitstr)>0 "
                + " and not o.organisaatiopoistettu=true "
                + " and oppilaitostyyppi in (" + csvWithQuote + ")";

        @SuppressWarnings("unchecked")
        List<Object[]> organisaatiot = getOrganisaatioEntityManager().createNativeQuery(sQuery).getResultList();

        List<OrganisaatioPerustieto> organisaatioPerustiedot
                = new LinkedList<OrganisaatioPerustieto>();

        for (Object[] organisaatio : organisaatiot) {
            organisaatioPerustiedot.add(applyOrganisaatio(organisaatio));
        }
        return organisaatioPerustiedot;
    }

    private String findParentOppilaitosOid(String oid) {
        String sQuery
                = " select "
                + " o.oid "
                + " from organisaatio o "
                + " where not o.organisaatiopoistettu=true "
                + " and o.oid in (select regexp_split_to_table(parentoidpath, E'\\\\|') from organisaatio where oid='" + oid + "')"
                + " and position('Oppilaitos' in o.organisaatiotyypitstr)>0";

        @SuppressWarnings("unchecked")
        List<String> parentOids = getOrganisaatioEntityManager().createNativeQuery(sQuery).getResultList();

        if (parentOids.size() != 1) {
            return null;
        }
        return parentOids.get(0);
    }

    @Override
    public List<OrganisaatioPerustieto> findToimipisteet(List<String> excludeOids) {
        String sQuery
                = " select "
                + "o.oid, "
                + "o.oppilaitostyyppi, "
                + "o.oppilaitoskoodi, "
                + "o.organisaatiotyypitstr, "
                + "o.ytunnus, "
                + "mktv_fi.value as nimi_fi, "
                + "mktv_sv.value as nimi_sv, "
                + "mktv_en.value as nimi_en "
                + " from organisaatio o "
                + " left join monikielinenteksti_values mktv_fi on o.nimi_mkt = mktv_fi.id and mktv_fi.key='fi' "
                + " left join monikielinenteksti_values mktv_sv on o.nimi_mkt = mktv_sv.id and mktv_sv.key='sv' "
                + " left join monikielinenteksti_values mktv_en on o.nimi_mkt = mktv_en.id and mktv_en.key='en' "
                + " where position('Toimipiste' in o.organisaatiotyypitstr)>0 "
                + " and not o.organisaatiopoistettu=true ";

        @SuppressWarnings("unchecked")
        List<Object[]> organisaatiot = getOrganisaatioEntityManager().createNativeQuery(sQuery).getResultList();

        List<OrganisaatioPerustieto> organisaatioPerustiedot
                = new LinkedList<OrganisaatioPerustieto>();

        for (Object[] organisaatio : organisaatiot) {
            if (!excludeOids.contains((String) organisaatio[0])) {
                OrganisaatioPerustieto organisaatioPerustieto = applyOrganisaatio(organisaatio);
                organisaatioPerustieto.setParentOppilaitosOid(findParentOppilaitosOid(organisaatioPerustieto.getOid()));
                organisaatioPerustiedot.add(organisaatioPerustieto);
            }
        }
        return organisaatioPerustiedot;
    }

    private boolean emptyString(String s) {
        return (s == null || s.length() == 0);
    }

    private boolean ylempi(String s) {
        return s != null && s.startsWith("koulutus_") && s.charAt(9) == '7';
    }

    private boolean alempi(String s) {
        return s != null && s.startsWith("koulutus_") && s.charAt(9) == '6';
    }


    private boolean laakis(String s) {
        return s != null && s.startsWith("koulutus_772101");
    }

    private boolean hammaslaakis(String s) {
        return s != null && s.startsWith("koulutus_772201");
    }

    private boolean kk_tut_taso(String s) {
        return ylempi(s) || alempi(s) || laakis(s) || hammaslaakis(s);
    }

    @Override
    public TasoJaLaajuusContainer getKKTutkinnonTaso(KoulutusmoduuliToteutus komoto) {

        TasoJaLaajuusContainer resp = new TasoJaLaajuusContainer();

        /*
         * 1) jos hakukohteen koulutusmoduulin toteutuksella on kandi_koulutus_uri tai koulutus_uri käytetään näitä koulutusmoduulin sijasta
         */

        String koulutus_uri;
        Koulutusmoduuli koulutusmoduuli = komoto.getKoulutusmoduuli();

        if (komoto == null || koulutusmoduuli == null) {
            return resp.eiTasoa(); //ei JULKAISTU
        }
        koulutus_uri = emptyString(komoto.getKoulutusUri()) ? koulutusmoduuli.getKoulutusUri() : komoto.getKoulutusUri();

        if (!kk_tut_taso(koulutus_uri)) {
            return resp.eiTasoa(); //ei korkeakoulun ylempi eikä alempi
        }

        if (laakis(koulutus_uri)) {
            return resp.laakis(komoto.getKoulutusmoduuli().getOid());
        }
        if (hammaslaakis(koulutus_uri)) {
            return resp.hammasLaakis(komoto.getKoulutusmoduuli().getOid());
        }

        boolean alempia_sisaltyy = false;

        for (KoodistoUri koulutusUri : komoto.getSisaltyvatKoulutuskoodit()) {
            if (alempi(koulutusUri.getKoodiUri())) {
                alempia_sisaltyy = true;
                break;
            }
        }
        
        /*
         * 2) jos koulutusmoduulilla sekä koulutus_uri (ylempi) ja siihen sisältyy jokin alempi koulutuskoodi => 060 = alempi+ylempi
         */
        if (ylempi(koulutus_uri) && alempia_sisaltyy) {
            return resp.onlyYlempi(komoto.getKoulutusmoduuli().getOid());
        }

        /*
         * 3) haetaan lapsi- ja emokoulutusmoduulit (ei sisaruksia l. toteutuksia) yo. lisäksi:
         */
        String rootOid = koulutusmoduuli.getOid();
        List<String> relativesList = getChildrenOids(rootOid);
        relativesList.addAll(getParentOids(rootOid));
        relativesList.add(rootOid);

        Koulutusmoduuli ylempiKomo = null;
        Koulutusmoduuli alempiKomo = null;
        for (String oid : relativesList) {
            koulutusmoduuli = getKoulutusmoduuli(oid);
            if (koulutusmoduuli != null) {
                if(laakis(koulutusmoduuli.getKoulutusUri())) {
                    return resp.laakis(koulutusmoduuli.getOid());
                }
                if(hammaslaakis(koulutusmoduuli.getKoulutusUri())) {
                    return resp.hammasLaakis(koulutusmoduuli.getOid());
                }
                if (ylempiKomo == null && ylempi(koulutusmoduuli.getKoulutusUri())) {
                    ylempiKomo = koulutusmoduuli;
                }
                if (alempiKomo == null && alempi(koulutusmoduuli.getKoulutusUri())) {
                    alempiKomo = koulutusmoduuli;
                }
            }
        }

        /*
         * 4) jos pelkkiä ylempiä => 061 (erillinen ylempi kk.tutkinto)
         */
        if (ylempiKomo != null && alempiKomo == null) {
            return resp.onlyYlempi(ylempiKomo.getOid());
        }
        /*
         * 5) jos pelkkiä alempia => 050  (alempi kk.tutkinto)
         */
        if (ylempiKomo == null && alempiKomo != null) {
            return resp.onlyAlempi(alempiKomo.getOid());
        }
        /*
         * 6) jos väh. 1 ylempiä ja väh. 1 => 060 (alempi+ylempi)
         */
        if (ylempiKomo != null && alempiKomo != null) {
            return resp.alempiYlempi(alempiKomo.getOid(), ylempiKomo.getOid());
        }
        /*
         * 7) jos ei kumpiakaan : koulutuksen tasoa ei merkitä
         */
        return resp.eiTasoa();
    }

    private EntityManager getTarjontaEntityManager() {
        return tarjontaEm;
    }

    private EntityManager getOrganisaatioEntityManager() {
        return organisaatioEm;
    }
}
