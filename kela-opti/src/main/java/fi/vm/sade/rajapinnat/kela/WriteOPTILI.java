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
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.tarjonta.service.types.HaeHakukohteetKyselyTyyppi;
import fi.vm.sade.tarjonta.service.types.HaeHakukohteetVastausTyyppi;
import fi.vm.sade.tarjonta.service.types.HaeHakukohteetVastausTyyppi.HakukohdeTulos;
import fi.vm.sade.tarjonta.service.types.HaeKoulutuksetKyselyTyyppi;
import fi.vm.sade.tarjonta.service.types.HaeKoulutuksetVastausTyyppi;
import fi.vm.sade.tarjonta.service.types.HaeKoulutuksetVastausTyyppi.KoulutusTulos;
import fi.vm.sade.koodisto.service.types.common.KoodiType;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTILI extends AbstractOPTIWriter {
    
    private static final Logger LOG = LoggerFactory.getLogger(KelaGenerator.class);
    
    private static final String OPTILI = ".OPTILI";
    private static final String ALKUTIETUE = "0000000000ALKU\n";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????\n";
    private static final String KOULUTUSLAJI = "N ";
    
    public WriteOPTILI() {
        super();
    }
    
    @Override
    public void writeFile() throws IOException {
        createFileName("", OPTILI);
        bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
        bos.write(toLatin1(ALKUTIETUE));
        HaeHakukohteetKyselyTyyppi kysely = new HaeHakukohteetKyselyTyyppi();
        HaeHakukohteetVastausTyyppi vastaus = tarjontaService.haeHakukohteet(kysely);
        for (HakukohdeTulos curTulos : vastaus.getHakukohdeTulos()) {
            String tarjoajaOid = curTulos.getHakukohde().getTarjoaja().getTarjoajaOid();
            OrganisaatioDTO organisaatioDTO = this.organisaatioService.findByOid(tarjoajaOid);
            if (isHakukohdeToinenaste(tarjoajaOid)) {
                bos.write(toLatin1(createRecord(curTulos, organisaatioDTO)));
            }
        }
        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();
    }

    private boolean isHakukohdeToinenaste(String tarjoajaOid) {
        return this.orgContainer.getOrgOidList().contains(tarjoajaOid);
    }

    private String createRecord(HakukohdeTulos curTulos, OrganisaatioDTO organisaatioDTO) {
        return String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", //51 fields + line ending
                getHakukohdeId(curTulos),//Sisainen koodi
                getOppilaitosnumero(curTulos, organisaatioDTO),//OPPIL_NRO
                getOpetuspisteenJarjNro(curTulos, organisaatioDTO),//OPJNO
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
                DEFAULT_DATE,  //Loppupaiva
                DEFAULT_DATE, //Viimeisin paivityspaiva
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
        kausi = kausi.substring(0, 1);
        return StringUtils.rightPad(kausi, 12);
    }

    private String getAlkupvm() {
        return DEFAULT_DATE;
    }
    
    private Object getHakukohdeKoodi(HakukohdeTulos curTulos) {
        String koodiUri = curTulos.getHakukohde().getKoodistoNimi();
        List<KoodiType> koodis = getKoodisByUriAndVersio(koodiUri);
        return (koodis.isEmpty()) ? StringUtils.leftPad("", 3) : koodis.get(0).getKoodiArvo();
    }
    
    

    private Object getTutkintotunniste(HakukohdeTulos curTulos) {
        LOG.debug("HaeTutkintotunniste: " + curTulos.getHakukohde().getOid());
        HaeKoulutuksetKyselyTyyppi kysely = new HaeKoulutuksetKyselyTyyppi();
        kysely.getHakukohdeOids().add(curTulos.getHakukohde().getOid());
        HaeKoulutuksetVastausTyyppi vastaus = tarjontaService.haeKoulutukset(kysely);
        LOG.debug("Koulutustulos size: " + vastaus.getKoulutusTulos().size());
        if (vastaus == null || vastaus.getKoulutusTulos().isEmpty()) {
            LOG.warn("\n\n!!!Tutkintotunniste empty for hakukohde: " + curTulos.getHakukohde().getOid() + "\n\n");
            StringUtils.leftPad("", 10);
        }
        KoulutusTulos tulos = vastaus.getKoulutusTulos().get(0);
        String koodiUri = tulos.getKoulutus().getKoulutuskoodi().getUri();
        List<KoodiType> koodis = this.getKoodisByUriAndVersio(koodiUri);        
        KoodiType koulutuskoodi = null;
        if (!koodis.isEmpty()) {
            koulutuskoodi = koodis.get(0);
        }
        KoodiType kelaKoodi = getRinnasteinenKoodi(koulutuskoodi, kelaTutkintokoodisto);
        return (kelaKoodi == null) ? StringUtils.leftPad("", 10) : StringUtils.leftPad(kelaKoodi.getKoodiArvo(), 10);
    }

    private String getKoulutuslaji() {
        return KOULUTUSLAJI;
    }

    private String getOpetuspisteenJarjNro(HakukohdeTulos curTulos, OrganisaatioDTO organisaatio) {
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE)) {
            return String.format("%s", organisaatio.getOpetuspisteenJarjNro());
        } 
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            Organisaatio organisaatioE = kelaDAO.findFirstChildOrganisaatio(curTulos.getHakukohde().getTarjoaja().getTarjoajaOid());
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
        Hakukohde hakukE = kelaDAO.findHakukohdeByOid(curTulos.getHakukohde().getOid());
        String hakukohdeId = String.format("%s", hakukE.getId());
        return StringUtils.leftPad(hakukohdeId, 10, '0');
    }
}
