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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.rajapinnat.kela.dao.HakukohdeDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.tarjonta.service.TarjontaPublicService;
import fi.vm.sade.tarjonta.service.types.HaeHakukohteetKyselyTyyppi;
import fi.vm.sade.tarjonta.service.types.HaeHakukohteetVastausTyyppi;
import fi.vm.sade.tarjonta.service.types.HaeHakukohteetVastausTyyppi.HakukohdeTulos;
import fi.vm.sade.tarjonta.service.types.HaeKoulutuksetKyselyTyyppi;
import fi.vm.sade.tarjonta.service.types.HaeKoulutuksetVastausTyyppi;
import fi.vm.sade.tarjonta.service.types.HaeKoulutuksetVastausTyyppi.KoulutusTulos;
import fi.vm.sade.koodisto.service.KoodiService;
import fi.vm.sade.koodisto.service.KoodistoService;
import fi.vm.sade.koodisto.service.types.SearchKoodisCriteriaType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.service.types.common.KoodiUriAndVersioType;
import fi.vm.sade.koodisto.service.types.common.SuhteenTyyppiType;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTILI {
    
    @Autowired
    private TarjontaPublicService tarjontaService;
    
    @Autowired
    private OrganisaatioService organisaatioService;
    
    @Autowired
    private KoodiService koodiService;
    
    @Autowired
    private KoodistoService koodistoService;
    
    @Autowired
    private HakukohdeDAO hakukohdeDAO;
    
    private String kelaTutkintokoodisto;
    
    private static final String NAMEPREFIX = "RY.WYZ.SR.D";
    
    private static final String OPTILI = ".OPTILI";
    
    private String fileName;
    
    private final String datePattern = "ddMMyy";
    
    private static final String PADDING_RESERVE = "                               ";
    private static final String DEFAULT_DATE = "01.01.0001";
    private static final String ALKUTIETUE = "0000000000ALKU";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????";
    
    public WriteOPTILI() {
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        fileName =  NAMEPREFIX + sdf.format(new Date()) + OPTILI; 
    }
    
    public WriteOPTILI(String path) {
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        fileName = path + NAMEPREFIX + sdf.format(new Date()) + OPTILI; 
    }
    
    public void writeFile() throws IOException {
        FileWriter fstream = new FileWriter(fileName);
        BufferedWriter out = new BufferedWriter(fstream);   
        out.write(ALKUTIETUE);
        HaeHakukohteetKyselyTyyppi kysely = new HaeHakukohteetKyselyTyyppi();
        HaeHakukohteetVastausTyyppi vastaus = tarjontaService.haeHakukohteet(kysely);
        for (HakukohdeTulos curTulos : vastaus.getHakukohdeTulos()) {
            out.write(createRecord(curTulos, out));
        }
        out.write(LOPPUTIETUE);
        out.flush();
        out.close();
    }

    private String createRecord(HakukohdeTulos curTulos, BufferedWriter out) {
        return String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", //51 fields
                getHakukohdeId(curTulos),//Sisainen koodi
                getOppilaitosnumero(curTulos),//OPPIL_NRO
                getOpetuspisteenJarjNro(curTulos),//OPJNO
                getPadding(12),//Op. linjan tai koulutuksen jarjestysnro
                getKoulutuslaji(),//Koulutuslaji
                getPadding(2),//Opintolinjan kieli
                getPadding(2),//OPL_LASPER
                getPadding(2),//Valintakoeryhma
                getPadding(10),//Kokeilun yksiloiva tunniste
                getPadding(5), //OPL_SUUNT
                getTutkintotunniste(curTulos),//TUT_ID 
                getHakukohdeKoodi(curTulos),//YHLINJA
                getPadding(3), //OPL_OTTOALUE
                getPadding(3), //Opetusmuoto
                getPadding(10), //Pohjakoulutusvaat. yksiloiva tunniste
                getPadding(2), //Koulutustyyppi
                getPadding(3), //OPL-EKASITTELY
                getPadding(2), //Koul. rahoitusmuoto
                getPadding(2), //OPL_PKR_TUNNUS
                getPadding(2), //OPL_VKOEJAR
                getPadding(2), //OPL_VKOEKUT
                getPadding(7), //Alinkeskiarvo
                getPadding(12), //Koulutuksen kesto
                getPadding(3), //OPL_KKERROIN
                getPadding(10), //Keston yksikko
                getAlkupvm(), //Alkupaiva, voimassaolon alku
                getPadding(10), //Loppupaiva
                getPadding(10), //Viimeisin paivityspaiva
                getPadding(30), //Viimeisin paivittaja
                getPadding(7), //Opiskelijamaksu
                getPadding(7), //Lahiopetusta
                getPadding(6), //OPL_LAHIOP.YKS
                getPadding(2), //OPL_ERITYISVAL
                getPadding(12), //OPL_KOULUTUS-AIKA
                getPadding(6), //OPL_KOULUTUSAI-KAYKSIKKO
                getPadding(6), //El-harpis
                getAlkamiskausi(curTulos), //ALKUKAUSI
                getTila(), //TILA
                getVuosi(curTulos), //VUOSI 
                DEFAULT_DATE, //Alkupaiva, voimassaolon alku
                DEFAULT_DATE, //Loppupaiva, voimassaolon loppu
                getPadding(1), //OPL_TULOSTUS
                getPadding(15), //OPL_OMISTAJA
                getPadding(3), //OPL_KUNTA
                getPadding(3), //OPL_PAINOALUE
                getPadding(1), //OPL_TYOHONSIJOITUSSEUR
                getPadding(1), //OPL_LYHYTKESTO
                getPadding(2), //OPL_MAKSUKER.
                getPadding(10), //OPL_SOSIAALISETEDUT
                getPadding(10), //OPL_SOPIMUSNRO
                getPadding(14) //Tyhjaa
                );
        
    }
    
    private String getVuosi(HakukohdeTulos curTulos) {
        return curTulos.getHakukohde().getKoulutuksenAlkamisvuosi();
    }

    private String getTila() {
        return "PAAT";
    }

    private String getAlkamiskausi(HakukohdeTulos curTulos) {
        String kausi = curTulos.getHakukohde().getKoulutuksenAlkamiskausiUri();
        return kausi.substring(0, 1);
    }

    private String getAlkupvm() {
        return DEFAULT_DATE;
    }
    
    private Object getHakukohdeKoodi(HakukohdeTulos curTulos) {
        String koodiUri = curTulos.getHakukohde().getKoodistoNimi();
        List<KoodiType> koodis = this.koodiService.searchKoodis(createUriVersioCriteria(koodiUri));
        return (koodis.isEmpty()) ? this.getPadding(3) : koodis.get(0).getKoodiArvo();
    }
    
    

    private Object getTutkintotunniste(HakukohdeTulos curTulos) {
        HaeKoulutuksetKyselyTyyppi kysely = new HaeKoulutuksetKyselyTyyppi();
        kysely.getHakukohdeOids().add(curTulos.getHakukohde().getOid());
        HaeKoulutuksetVastausTyyppi vastaus = tarjontaService.haeKoulutukset(kysely);
        KoulutusTulos tulos = vastaus.getKoulutusTulos().get(0);
        String koodiUri = tulos.getKoulutus().getKoulutuskoodi().getUri();
        List<KoodiType> koodis = this.koodiService.searchKoodis(createUriVersioCriteria(koodiUri));
        KoodiType koulutuskoodi = null;
        if (!koodis.isEmpty()) {
            koulutuskoodi = koodis.get(0);
        }
        KoodiType kelaKoodi = getRelatedKelakoodi(koulutuskoodi);
        
        return (kelaKoodi == null) ? getPadding(10) : kelaKoodi.getKoodiArvo();
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

    private KoodiType getRelatedKelakoodi(KoodiType koulutuskoodi) {
        KoodiUriAndVersioType uriAndVersio = new KoodiUriAndVersioType();
        uriAndVersio.setKoodiUri(koulutuskoodi.getKoodiUri());
        uriAndVersio.setVersio(koulutuskoodi.getVersio());
        List<KoodiType> relatedKoodis = koodiService.listKoodiByRelation(uriAndVersio, false, SuhteenTyyppiType.RINNASTEINEN);
        for (KoodiType curKoodi : relatedKoodis) {
            if (curKoodi.getKoodisto().getKoodistoUri().equals(kelaTutkintokoodisto)) {
                return curKoodi;
            }
        }
        return null;
    }

    private String getKoulutuslaji() {
        return " n";
    }

    private String getOpetuspisteenJarjNro(HakukohdeTulos curTulos) {
        Organisaatio organisaatio = hakukohdeDAO.findOrganisaatioByOid(curTulos.getHakukohde().getTarjoaja().getTarjoajaOid());
        return String.format("%s", organisaatio.getOpetuspisteenJarjNro());
    }

    private String getOppilaitosnumero(HakukohdeTulos curTulos) {
        String tarjoajaOid = curTulos.getHakukohde().getTarjoaja().getTarjoajaOid();
        OrganisaatioDTO organisaatio = organisaatioService.findByOid(tarjoajaOid);
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            return String.format("%s", organisaatio.getOppilaitosKoodi());
        } else if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE)) {
            return String.format("%s", organisaatioService.findByOid(organisaatio.getParentOid()).getOppilaitosKoodi());
        }
        return "     ";
    }

    private String getHakukohdeId(HakukohdeTulos curTulos) {
        Hakukohde hakukE = hakukohdeDAO.findHakukohdeByOid(curTulos.getHakukohde().getOid());
        String hakukohdeId = String.format("%s", hakukE.getId());
        String paddingReserve = "          ";
        String actualPadding = paddingReserve.substring(0, 10 - hakukohdeId.length());
        return String.format("%s%s", actualPadding, hakukohdeId);
    }
    
    private String getPadding(int length) {
        return PADDING_RESERVE.substring(0, length);
    }


    @Value("${koodisto-uris.tutkintokela}")
    public void setKelaTutkintokoodisto(String kelaTutkintokoodisto) {
        this.kelaTutkintokoodisto = kelaTutkintokoodisto;
    }
}
