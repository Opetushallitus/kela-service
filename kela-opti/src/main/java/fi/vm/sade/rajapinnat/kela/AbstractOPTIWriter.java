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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import fi.vm.sade.koodisto.service.types.common.KoodiUriAndVersioType;
import fi.vm.sade.koodisto.service.types.common.KoodistoType;
import fi.vm.sade.koodisto.service.types.common.SuhteenTyyppiType;
import fi.vm.sade.koodisto.util.KoodiServiceSearchCriteriaBuilder;
import fi.vm.sade.koodisto.util.KoodistoHelper;
import fi.vm.sade.koodisto.util.KoodistoServiceSearchCriteriaBuilder;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.rajapinnat.kela.dao.HakukohdeDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.tarjonta.service.TarjontaPublicService;

/**
 * 
 * @author Markus
 */
@Configurable
public abstract class AbstractOPTIWriter {

    protected static final Charset LATIN1 = Charset.forName("ISO8859-1");
    protected static final String DATE_PATTERN_FILE = "ddMMyy";
    protected static final String DATE_PATTERN_RECORD = "dd.MM.yyyy";
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
    
    protected Map<String,OrganisaatioPerustietoType> oppilaitosoidOppilaitosMap;


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
    protected String kelaOppilaitostyyppikoodisto;
    protected String oppilaitosnumerokoodisto;
    protected String toimipistekoodisto;
    protected String yhKoulukoodiKoodisto;
    protected String koulutuskoodisto;
    protected String kelaOpintoalakoodisto;
    protected String kelaKoulutusastekoodisto;
    
    
    
    protected void createFileName(String path, String name) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN_FILE);
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
    
    @Value("${koodisto-uris.oppilaitostyyppikela}")
    public void setKelaOppilaitostyyppikoodisto(String kelaOppilaitostyyppikoodisto) {
        this.kelaOppilaitostyyppikoodisto = kelaOppilaitostyyppikoodisto;
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
    public void setOpTyyppiMusiikkioppilaitokset(String opTyyppiMusiikkioppilaitokset) {
        this.opTyyppiMusiikkioppilaitokset = opTyyppiMusiikkioppilaitokset;
    }
    

    @Value("${koodisto-uris.yhteishaunkoulukoodi}")
    public void setYhKoulukoodiKoodisto(String yhKoulukoodiKoodisto) {
        this.yhKoulukoodiKoodisto = yhKoulukoodiKoodisto;
    }
    
    @Value("${koodisto-uris.koulutus}")
    public void setKoulutuskoodisto(String koulutuskoodisto) {
        this.koulutuskoodisto = koulutuskoodisto;
    }
    
    @Value("${koodisto-uris.opintoalakela}")
    public void setKelaOpintoalakoodisto(String kelaOpintoalakoodisto) {
        this.kelaOpintoalakoodisto = kelaOpintoalakoodisto;
    }
    
    @Value("${koodisto-uris.koulutusastekela}")
    public void setKelaKoulutusastekoodisto(String kelaKoulutusastekoodisto) {
        this.kelaKoulutusastekoodisto = kelaKoulutusastekoodisto;
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
    

    protected KoodiType getRinnasteinenKoodi(KoodiType koulutuskoodi, String targetKoodisto) {
        KoodiUriAndVersioType uriAndVersio = new KoodiUriAndVersioType();
        uriAndVersio.setKoodiUri(koulutuskoodi.getKoodiUri());
        uriAndVersio.setVersio(koulutuskoodi.getVersio());
        List<KoodiType> relatedKoodis = koodiService.listKoodiByRelation(uriAndVersio, false, SuhteenTyyppiType.RINNASTEINEN);
        KoodiType targetKoodi = null;
        for (KoodiType curKoodi : relatedKoodis) {
            if (curKoodi.getKoodisto().getKoodistoUri().equals(targetKoodisto)) {
                targetKoodi = curKoodi;
            }
        }
        if (targetKoodi == null) {
            relatedKoodis = koodiService.listKoodiByRelation(uriAndVersio, true, SuhteenTyyppiType.RINNASTEINEN);
            for (KoodiType curKoodi : relatedKoodis) {
                if (curKoodi.getKoodisto().getKoodistoUri().equals(targetKoodisto)) {
                    targetKoodi = curKoodi;
                }
            }
        }
        
        return targetKoodi;
    }
    
    protected KoodiType getSisaltyvaKoodi(KoodiType sourcekoodi, String targetKoodisto) {
        KoodiUriAndVersioType uriAndVersio = new KoodiUriAndVersioType();
        uriAndVersio.setKoodiUri(sourcekoodi.getKoodiUri());
        uriAndVersio.setVersio(sourcekoodi.getVersio());
        List<KoodiType> relatedKoodis = koodiService.listKoodiByRelation(uriAndVersio, false, SuhteenTyyppiType.SISALTYY);
        for (KoodiType curKoodi : relatedKoodis) {
            if (curKoodi.getKoodisto().getKoodistoUri().equals(targetKoodisto)) {
                return curKoodi;
            }
        }
        return null;
    }
    
    protected boolean isOppilaitosWritable(OrganisaatioPerustietoType curOppilaitos) {
        return isOppilaitosInKoodisto(curOppilaitos) 
                && isOppilaitosToinenAste(curOppilaitos);
    }
    
    protected boolean isOppilaitosInKoodisto(OrganisaatioPerustietoType curOppilaitos) {
        String oppilaitoskoodi = curOppilaitos.getOppilaitosKoodi();
        List<KoodiType> koodit = getKoodisByArvoAndKoodisto(oppilaitoskoodi, oppilaitosnumerokoodisto);
        return koodit != null && !koodit.isEmpty();
    }

    protected boolean isOppilaitosToinenAste(
            OrganisaatioPerustietoType curOppilaitos) {
        String opTyyppi = curOppilaitos.getOppilaitostyyppi();
        return  opTyyppiAmmatillisetAikuiskoulutuseskukset.equals(opTyyppi) 
                || opTyyppiAmmatillisetErikoisoppilaitokset.equals(opTyyppi)
                || opTyyppiAmmatillisetOppilaitokset.equals(opTyyppi)
                || opTyyppiAmmattillisetErityisoppilaitokset.equals(opTyyppi)
                || opTyyppiKansanopistot.equals(opTyyppi)
                || opTyyppiLukiot.equals(opTyyppi)
                || opTyyppiLukiotJaPeruskoulut.equals(opTyyppi)
                || opTyyppiMusiikkioppilaitokset.equals(opTyyppi);
    }
    
    protected boolean isToimipisteWritable(OrganisaatioPerustietoType curToimipiste) {
        
        Organisaatio toimipisteE = hakukohdeDAO.findOrganisaatioByOid(curToimipiste.getOid());
        
        if (curToimipiste.getParentOid() == null) {
            return false;
        }
        
        OrganisaatioPerustietoType parentToimipiste = oppilaitosoidOppilaitosMap.get(curToimipiste.getParentOid());
        if (parentToimipiste == null) {
            return false;
        }
        
        String toimipistearvo = String.format("%s%s", parentToimipiste.getOppilaitosKoodi(), toimipisteE.getOpetuspisteenJarjNro());
        List<KoodiType> koodit = getKoodisByArvoAndKoodisto(toimipistearvo, toimipistekoodisto);
        
        return koodit != null && !koodit.isEmpty();
    }
    

    protected String getOppilaitosNro(OrganisaatioPerustietoType curOrganisaatio) {
        String opnro = "";
        if (curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            opnro = curOrganisaatio.getOppilaitosKoodi();
        }
        return StringUtils.leftPad(opnro, 5);
    }

    protected String getOpPisteenOppilaitosnumero(
            OrganisaatioPerustietoType curOrganisaatio) {
        if (curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE) 
                && !curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            return StringUtils.leftPad(this.oppilaitosoidOppilaitosMap.get(curOrganisaatio.getParentOid()).getOppilaitosKoodi(), 5);
        } 
        if (curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE)) {
            return StringUtils.leftPad(curOrganisaatio.getOppilaitosKoodi(), 5);
        }
        return StringUtils.leftPad("", 5);
    }
    

    protected String getOpPisteenJarjNro(Organisaatio orgE) {
        String opPisteenJarjNro = "";
        if (orgE.getOpetuspisteenJarjNro() != null) {
            opPisteenJarjNro = orgE.getOpetuspisteenJarjNro();
        }
        return StringUtils.leftPad(opPisteenJarjNro, 2);
    }
    

    protected String getYhteystietojenTunnus(Organisaatio orgE) {
        return StringUtils.leftPad(String.format("%s", hakukohdeDAO.getKayntiosoiteIdForOrganisaatio(orgE.getId())), 10);
    }
    
    protected String getDateStrOrDefault(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN_RECORD);
        String dateStr = DEFAULT_DATE;
        if (date != null) {
            dateStr = sdf.format(date);
        }
        return StringUtils.leftPad(dateStr, 10);
    }
    

    protected String getOppilaitostyyppitunnus(
            OrganisaatioPerustietoType curOppilaitos) {
        List<KoodiType> koodis = getKoodisByUriAndVersio(curOppilaitos.getOppilaitostyyppi());        
        KoodiType olTyyppiKoodi = null;
        if (!koodis.isEmpty()) {
            olTyyppiKoodi = koodis.get(0);
        }
        KoodiType kelaKoodi = getRinnasteinenKoodi(olTyyppiKoodi, kelaOppilaitostyyppikoodisto);
        return (kelaKoodi == null) ? StringUtils.leftPad("", 10) : StringUtils.leftPad(kelaKoodi.getKoodiArvo(), 10);
    }

    protected String getKotikunta(Organisaatio orgE) {
        List<KoodiType> koodit = getKoodisByUriAndVersio(orgE.getKotipaikka());
        String kotikuntaArvo = "";
        if (koodit != null && !koodit.isEmpty()) {
            kotikuntaArvo = koodit.get(0).getKoodiArvo();
        }
        return StringUtils.leftPad(kotikuntaArvo, 3);
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
