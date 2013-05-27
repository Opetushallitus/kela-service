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
package fi.vm.sade.rajapinnat.kela;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import fi.vm.sade.koodisto.service.KoodiService;
import fi.vm.sade.koodisto.service.KoodistoService;
import fi.vm.sade.koodisto.service.types.SearchKoodisByKoodistoCriteriaType;
import fi.vm.sade.koodisto.service.types.SearchKoodisCriteriaType;
import fi.vm.sade.koodisto.service.types.SearchKoodistosCriteriaType;
import fi.vm.sade.koodisto.service.types.common.KieliType;
import fi.vm.sade.koodisto.service.types.common.KoodiMetadataType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.service.types.common.KoodistoType;
import fi.vm.sade.koodisto.util.KoodiServiceSearchCriteriaBuilder;
import fi.vm.sade.koodisto.util.KoodistoHelper;
import fi.vm.sade.koodisto.util.KoodistoServiceSearchCriteriaBuilder;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.rajapinnat.kela.dao.HakukohdeDAO;
import fi.vm.sade.tarjonta.service.TarjontaPublicService;

/**
 * 
 * @author Markus
 */
@Configurable
public abstract class AbstractOPTIWriter {

    protected static final Charset LATIN1 = Charset.forName("ISO8859-1");
    protected static final String DATE_PATTERN = "ddMMyy";
    protected static final String NAMEPREFIX = "RY.WYZ.SR.D";
    protected static final String DEFAULT_DATE = "01.01.0001";
    
    @Autowired
    protected TarjontaPublicService tarjontaService;

    @Autowired
    protected OrganisaatioService organisaatioService;
    
    @Autowired
    protected KoodiService koodiService;
    
    @Autowired
    protected KoodistoService koodistoService;
    
    @Autowired
    protected HakukohdeDAO hakukohdeDAO; 

    protected String fileName;


    //TOINEN ASTE KOODI URIS
    protected String opTyyppiLukiot;
    protected String opTyyppiLukiotJaPeruskoulut;
    protected String opTyyppiAmmatillisetOppilaitokset;
    protected String opTyyppiAmmattillisetErityisoppilaitokset;
    protected String opTyyppiAmmatillisetErikoisoppilaitokset;
    protected String opTyyppiAmmatillisetAikuiskoulutuseskukset;
    protected String opTyyppiKansanopistot;
    protected String opTyyppiMusiikkioppilaitokset;

    protected String kieliFi;
    protected String kieliSv;
    protected String kieliEn;
    
    //USED KOODISTO URIS
    protected String kelaTutkintokoodisto;
    protected String oppilaitosnumerokoodisto;
    protected String toimipistekoodisto;
    
    
    protected void createFileName(String path, String name) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        fileName =  path + NAMEPREFIX + sdf.format(new Date()) + name; 
    }
    
    
    protected byte[] toLatin1(String text) {
        return text.getBytes(LATIN1);
    }
    
    @Value("$fi-uri}")
    public void setKieliFi(String kieliFi) {
        this.kieliFi = kieliFi;
    }

    @Value("${sv-uri}")
    public void setKieliSv(String kieliSv) {
        this.kieliSv = kieliSv;
    }

    @Value("${en-uri}")
    public void setKieliEn(String kieliEn) {
        this.kieliEn = kieliEn;
    }

    @Value("${koodisto-uris.tutkintokela}")
    public void setKelaTutkintokoodisto(String kelaTutkintokoodisto) {
        this.kelaTutkintokoodisto = kelaTutkintokoodisto;
    }
    
    @Value("${koodisto-uris.oppilaitosnumero}")
    public void setOppilaitosnumerokoodisto(String oppilaitosnumerokoodisto) {
        this.oppilaitosnumerokoodisto = oppilaitosnumerokoodisto;
    }
    
    
    @Value("${koodisto-uris.opetuspisteet}")
    public void setToimipistekoodisto(String toimipistekoodisto) {
        this.toimipistekoodisto = toimipistekoodisto;
    }
    
    @Value("${lukiot-uri}")
    public void setOpTyyppiLukiot(String opTyyppiLukiot) {
        this.opTyyppiLukiot = opTyyppiLukiot;
    }

    @Value("${lukiotjaperuskoulut-uri}")
    public void setOpTyyppiLukiotJaPeruskoulut(String opTyyppiLukiotJaPeruskoulut) {
        this.opTyyppiLukiotJaPeruskoulut = opTyyppiLukiotJaPeruskoulut;
    }
    
    @Value("${ammatillisetoppilaitokset-uri}")
    public void setOpTyyppiAmmatillisetOppilaitokset(
            String opTyyppiAmmatillisetOppilaitokset) {
        this.opTyyppiAmmatillisetOppilaitokset = opTyyppiAmmatillisetOppilaitokset;
    }

    @Value("${ammatilliseterityisoppilaitokset-uri}")
    public void setOpTyyppiAmmattillisetErityisoppilaitokset(
            String opTyyppiAmmattillisetErityisoppilaitokset) {
        this.opTyyppiAmmattillisetErityisoppilaitokset = opTyyppiAmmattillisetErityisoppilaitokset;
    }

    @Value("${ammatilliseterikoisoppilaitokset-uri}")
    public void setOpTyyppiAmmatillisetErikoisoppilaitokset(
            String opTyyppiAmmatillisetErikoisoppilaitokset) {
        this.opTyyppiAmmatillisetErikoisoppilaitokset = opTyyppiAmmatillisetErikoisoppilaitokset;
    }

    @Value("${ammatillisetaikuiskoulutuskeskukset-uri}")
    public void setOpTyyppiAmmatillisetAikuiskoulutuseskukset(
            String opTyyppiAmmatillisetAikuiskoulutuseskukset) {
        this.opTyyppiAmmatillisetAikuiskoulutuseskukset = opTyyppiAmmatillisetAikuiskoulutuseskukset;
    }

    @Value("${kansanopistot-uri}")
    public void setOpTyyppiKansanopistot(String opTyyppiKansanopistot) {
        this.opTyyppiKansanopistot = opTyyppiKansanopistot;
    }

    @Value("${musiikkioppilaitokset-uri}")
    public void setOpTyyppiMusiikkioppilaitokset(
            String opTyyppiMusiikkioppilaitokset) {
        this.opTyyppiMusiikkioppilaitokset = opTyyppiMusiikkioppilaitokset;
    }
    
    protected List<KoodiType> getKoodisByArvoAndKoodisto(String arvo, String koodistoUri) {
        try {
            SearchKoodistosCriteriaType koodistoSearchCriteria = KoodistoServiceSearchCriteriaBuilder.latestKoodistoByUri(koodistoUri);

            List<KoodistoType> koodistoResult = koodistoService.searchKoodistos(koodistoSearchCriteria);
            if(koodistoResult.size() != 1) {
                // FIXME: Throw something other than RuntimeException?
                throw new RuntimeException("No koodisto found for koodisto URI " + koodistoUri);
            }
            KoodistoType koodisto = koodistoResult.get(0);

            SearchKoodisByKoodistoCriteriaType koodiSearchCriteria = KoodiServiceSearchCriteriaBuilder.koodisByArvoAndKoodistoUriAndKoodistoVersio(arvo,
                    koodistoUri, koodisto.getVersio());
            return koodiService.searchKoodisByKoodisto(koodiSearchCriteria);
        } catch (Exception exp) {
            return null;
        }
    }
    
    protected List<KoodiType> getKoodisByUriAndVersio(String koodiUri) {
        return this.koodiService.searchKoodis(createUriVersioCriteria(koodiUri));
    }
    
    /**
     * Get koodi metadata by locale with language fallback to FI
     *
     * @param koodiType
     * @param locale
     * @return
     */
    public KoodiMetadataType getKoodiMetadataForLanguage(KoodiType koodiType, KieliType kieli) {
        KoodiMetadataType kmdt = KoodistoHelper.getKoodiMetadataForLanguage(koodiType, kieli);
        return kmdt;
    }
    
    public KoodiMetadataType getAvailableKoodiMetadata(KoodiType koodi) {
        KoodiMetadataType kmdt = KoodistoHelper.getKoodiMetadataForLanguage(koodi, KieliType.FI);
        if (kmdt == null && !koodi.getMetadata().isEmpty()) {
            kmdt = koodi.getMetadata().get(0);
        }
        return kmdt;
    }
    
    public void setTarjontaService(TarjontaPublicService tarjontaService) {
        this.tarjontaService = tarjontaService;
    }


    public void setOrganisaatioService(OrganisaatioService organisaatioService) {
        this.organisaatioService = organisaatioService;
    }
        
    public void setHakukohdeDAO(HakukohdeDAO hakukohdeDAO) {
        this.hakukohdeDAO = hakukohdeDAO;
    }
    
    
   public String getFileName() {
       return fileName;
   }
    
    private SearchKoodisCriteriaType createUriVersioCriteria(String koodiUri) {
        SearchKoodisCriteriaType criteria = new SearchKoodisCriteriaType();
        int versio = -1;
        if (koodiUri.contains("#")) {
            int endIndex = koodiUri.lastIndexOf('#');
            versio = Integer.parseInt(koodiUri.substring(endIndex + 1));
            koodiUri = koodiUri.substring(0, endIndex);
        }
        criteria.getKoodiUris().add(koodiUri);
        if (versio > -1) {
            criteria.setKoodiVersio(versio);
        }
        return criteria;
    }
    
    public abstract void writeFile() throws IOException;

}
