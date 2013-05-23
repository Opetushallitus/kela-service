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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
    
    private String fileName;
    
    private static final Charset LATIN1 = Charset.forName("ISO8859-1");
    private static final String DATE_PATTERN = "ddMMyy";
    private static final String NAMEPREFIX = "RY.WYZ.SR.D";
    private static final String OPTILI = ".OPTILI";
    private static final String DEFAULT_DATE = "01.01.0001";
    private static final String ALKUTIETUE = "0000000000ALKU\n";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????\n";
    
    public WriteOPTILI() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        fileName =  NAMEPREFIX + sdf.format(new Date()) + OPTILI; 
    }
    
    public WriteOPTILI(String path) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        fileName = path + NAMEPREFIX + sdf.format(new Date()) + OPTILI; 
    }
    
    public void writeFile() throws IOException {
        FileWriter fstream = new FileWriter(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
        bos.write(toLatin1(ALKUTIETUE));
        HaeHakukohteetKyselyTyyppi kysely = new HaeHakukohteetKyselyTyyppi();
        HaeHakukohteetVastausTyyppi vastaus = tarjontaService.haeHakukohteet(kysely);
        for (HakukohdeTulos curTulos : vastaus.getHakukohdeTulos()) {
            bos.write(toLatin1(createRecord(curTulos)));
        }
        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();
    }

    private String createRecord(HakukohdeTulos curTulos) {
        String tarjoajaOid = curTulos.getHakukohde().getTarjoaja().getTarjoajaOid();
        OrganisaatioDTO organisaatio = organisaatioService.findByOid(tarjoajaOid);
        return String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", //51 fields + line ending
                getHakukohdeId(curTulos),//Sisainen koodi
                getOppilaitosnumero(curTulos, organisaatio),//OPPIL_NRO
                getOpetuspisteenJarjNro(curTulos, organisaatio),//OPJNO
                StringUtils.leftPad("",12),//Op. linjan tai koulutuksen jarjestysnro
                getKoulutuslaji(),//Koulutuslaji
                StringUtils.leftPad("",2),//Opintolinjan kieli
                StringUtils.leftPad("",2),//OPL_LASPER
                StringUtils.leftPad("",2),//Valintakoeryhma
                StringUtils.leftPad("",10),//Kokeilun yksiloiva tunniste
                StringUtils.leftPad("",5), //OPL_SUUNT
                getTutkintotunniste(curTulos),//TUT_ID 
                getHakukohdeKoodi(curTulos),//YHLINJA
                StringUtils.leftPad("",3), //OPL_OTTOALUE
                StringUtils.leftPad("",3), //Opetusmuoto
                StringUtils.leftPad("",10), //Pohjakoulutusvaat. yksiloiva tunniste
                StringUtils.leftPad("",2), //Koulutustyyppi
                StringUtils.leftPad("",3), //OPL-EKASITTELY
                StringUtils.leftPad("",2), //Koul. rahoitusmuoto
                StringUtils.leftPad("",2), //OPL_PKR_TUNNUS
                StringUtils.leftPad("",2), //OPL_VKOEJAR
                StringUtils.leftPad("",2), //OPL_VKOEKUT
                StringUtils.leftPad("",7), //Alinkeskiarvo
                StringUtils.leftPad("",12), //Koulutuksen kesto
                StringUtils.leftPad("",3), //OPL_KKERROIN
                StringUtils.leftPad("",10), //Keston yksikko
                getAlkupvm(), //Alkupaiva, voimassaolon alku
                StringUtils.leftPad("",10), //Loppupaiva
                StringUtils.leftPad("",10), //Viimeisin paivityspaiva
                StringUtils.leftPad("",30), //Viimeisin paivittaja
                StringUtils.leftPad("",7), //Opiskelijamaksu
                StringUtils.leftPad("",7), //Lahiopetusta
                StringUtils.leftPad("",6), //OPL_LAHIOP.YKS
                StringUtils.leftPad("",2), //OPL_ERITYISVAL
                StringUtils.leftPad("",12), //OPL_KOULUTUS-AIKA
                StringUtils.leftPad("",6), //OPL_KOULUTUSAI-KAYKSIKKO
                StringUtils.leftPad("",6), //El-harpis
                getAlkamiskausi(curTulos), //ALKUKAUSI
                getTila(), //TILA
                getVuosi(curTulos), //VUOSI 
                DEFAULT_DATE, //Alkupaiva, voimassaolon alku
                DEFAULT_DATE, //Loppupaiva, voimassaolon loppu
                StringUtils.leftPad("",1), //OPL_TULOSTUS
                StringUtils.leftPad("",15), //OPL_OMISTAJA
                StringUtils.leftPad("",3), //OPL_KUNTA
                StringUtils.leftPad("",3), //OPL_PAINOALUE
                StringUtils.leftPad("",1), //OPL_TYOHONSIJOITUSSEUR
                StringUtils.leftPad("",1), //OPL_LYHYTKESTO
                StringUtils.leftPad("",2), //OPL_MAKSUKER.
                StringUtils.leftPad("",10), //OPL_SOSIAALISETEDUT
                StringUtils.leftPad("",10), //OPL_SOPIMUSNRO
                StringUtils.leftPad("",14), //Tyhjaa
                "\n");
        
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
        return (koodis.isEmpty()) ? StringUtils.leftPad("", 3) : koodis.get(0).getKoodiArvo();
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
        return (kelaKoodi == null) ? StringUtils.leftPad("", 10) : kelaKoodi.getKoodiArvo();
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

    private String getOpetuspisteenJarjNro(HakukohdeTulos curTulos, OrganisaatioDTO organisaatio) {
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE)) {
            return String.format("%s", organisaatio.getOpetuspisteenJarjNro());
        } 
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            Organisaatio organisaatioE = hakukohdeDAO.findFirstChildOrganisaatio(curTulos.getHakukohde().getTarjoaja().getTarjoajaOid());
            return (organisaatioE != null && organisaatioE.getOpetuspisteenJarjNro() != null) ? organisaatioE.getOpetuspisteenJarjNro() : "01";
        }
        return "01";
    }

    private String getOppilaitosnumero(HakukohdeTulos curTulos, OrganisaatioDTO organisaatio) {
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            return String.format("%s", organisaatio.getOppilaitosKoodi());
        } else if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE)) {
            return String.format("%s", organisaatioService.findByOid(organisaatio.getParentOid()).getOppilaitosKoodi());
        }
        return StringUtils.leftPad("", 5);
    }

    private String getHakukohdeId(HakukohdeTulos curTulos) {
        Hakukohde hakukE = hakukohdeDAO.findHakukohdeByOid(curTulos.getHakukohde().getOid());
        String hakukohdeId = String.format("%s", hakukE.getId());
        return StringUtils.leftPad(hakukohdeId, 10);
    }


    @Value("${koodisto-uris.tutkintokela}")
    public void setKelaTutkintokoodisto(String kelaTutkintokoodisto) {
        this.kelaTutkintokoodisto = kelaTutkintokoodisto;
    }
    
    private byte[] toLatin1(String text) {
        return text.getBytes(LATIN1);
    }
}
