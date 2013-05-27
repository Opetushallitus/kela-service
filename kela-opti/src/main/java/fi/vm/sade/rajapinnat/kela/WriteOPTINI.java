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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import fi.vm.sade.koodisto.service.types.common.KieliType;
import fi.vm.sade.koodisto.service.types.common.KoodiMetadataType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchCriteriaDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTINI extends AbstractOPTIWriter {
    
    private static final String OPTINI = ".OPTINI";
    
    private static final String ALKUTIETUE = "0000000000ALKU\n";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????\n";
    private Map<String,String> oppilaitosoidOppilaitosnumeroMap;
    
    
    public WriteOPTINI() {
        super();
        this.createFileName("", OPTINI);
    }
    
    public WriteOPTINI(String path) {
        super();
        this.createFileName(path, OPTINI);
    }
    
    @Override
    public void writeFile() throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
        bos.write(toLatin1(ALKUTIETUE));
        
        oppilaitosoidOppilaitosnumeroMap = new HashMap<String, String>(); 
        OrganisaatioSearchCriteriaDTO criteria = new OrganisaatioSearchCriteriaDTO();
        criteria.setOrganisaatioTyyppi(OrganisaatioTyyppi.OPPILAITOS.value());
        List<OrganisaatioPerustietoType> oppilaitokset = organisaatioService.searchBasicOrganisaatios(criteria);
        for (OrganisaatioPerustietoType curOppilaitos : oppilaitokset) {
            if (isOppilaitosWritable(curOppilaitos)) {
                oppilaitosoidOppilaitosnumeroMap.put(curOppilaitos.getOid(), curOppilaitos.getOppilaitosKoodi());
                bos.write(toLatin1(createRecord(curOppilaitos)));   
                bos.flush();
            }
        }
        
        criteria = new OrganisaatioSearchCriteriaDTO();
        criteria.getOidResctrictionList().addAll(oppilaitosoidOppilaitosnumeroMap.keySet());
        criteria.setOrganisaatioTyyppi(OrganisaatioTyyppi.OPETUSPISTE.value());
        
        List<OrganisaatioPerustietoType> toimipisteet = organisaatioService.searchBasicOrganisaatios(criteria);
        for (OrganisaatioPerustietoType curToimipiste : toimipisteet) {
            if (isToimipisteWritable(curToimipiste)) {
                bos.write(toLatin1(createRecord(curToimipiste)));
                bos.flush();
            }
        }
        
        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();
    }
    
    
    private String createRecord(OrganisaatioPerustietoType curOrganisaatio) {
        Organisaatio orgE = hakukohdeDAO.findOrganisaatioByOid(curOrganisaatio.getOid());
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s",//12 fields + EOL
                getSisainenKoodi(orgE),//Sisainen koodi
                getOpPisteenOppilaitosnumero(curOrganisaatio),//OPE_OPPILNRO
                getOpPisteenJarjNro(orgE),//OPE_OPJNO
                getOppilaitosNro(curOrganisaatio),//OPPILNRO
                StringUtils.leftPad("", 3),//KIELI
                getOrganisaatioNimi(curOrganisaatio, orgE.getKielet()),//Nimi
                getOrganisaatioLyhytNimi(curOrganisaatio, orgE, orgE.getKielet()),//Nimen lyhenne
                StringUtils.leftPad(DEFAULT_DATE, 10),//Viimeisin paivityspaiva
                StringUtils.leftPad("", 30),//Viimeisin paivittaja
                "X",//Nimi on virallinen
                StringUtils.leftPad(DEFAULT_DATE, 10),//Alkupaiva, voimassaolon alku
                StringUtils.leftPad(DEFAULT_DATE, 10),//Loppupaiva, voimassaolon loppu
                "\n");
        System.out.println("\nRecord: " + record + "\n");
        return record;
    }
    
    
    
    private String getOrganisaatioLyhytNimi(
            OrganisaatioPerustietoType curOrganisaatio, Organisaatio orgE, List<String> kielet) {
        List<KoodiType> koodit = new ArrayList<KoodiType>();
        if (curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            koodit = getKoodisByArvoAndKoodisto(curOrganisaatio.getOppilaitosKoodi(), oppilaitosnumerokoodisto);
        } else if (curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE)) {
            String opArvo = String.format("%s%s", getOpPisteenOppilaitosnumero(curOrganisaatio), getOpPisteenJarjNro(orgE));
            koodit = getKoodisByArvoAndKoodisto(opArvo, toimipistekoodisto);
        }
        String lyhytNimi = "";
        if (koodit != null && !koodit.isEmpty()) {
            lyhytNimi = getLyhytNimiFromKoodi(koodit.get(0), kielet);
        }
        return StringUtils.leftPad(lyhytNimi, 40);
    }

    private String getLyhytNimiFromKoodi(KoodiType koodi,
            List<String> kielet) {
        KoodiMetadataType kmdt = null;
        String nimi = "";
        if (kielet.contains(kieliFi)) {
            kmdt = getKoodiMetadataForLanguage(koodi, KieliType.FI);
        } else if (kielet.contains(kieliSv)) {
            kmdt = getKoodiMetadataForLanguage(koodi, KieliType.SV);
        } else if (kielet.contains(kieliEn)) {
            kmdt = getKoodiMetadataForLanguage(koodi, KieliType.EN);
        }
        if (kmdt == null) {
            kmdt = getAvailableKoodiMetadata(koodi);
        }
        if (kmdt != null) {
            nimi = kmdt.getLyhytNimi();
        }
        return nimi;
    }

    private String getOrganisaatioNimi(
            OrganisaatioPerustietoType curOrganisaatio, List<String> kielet) {
        String nimi = "";
        if (kielet.contains(kieliFi) && curOrganisaatio.getNimiFi() != null) {
            nimi = curOrganisaatio.getNimiFi();
        } else if (kielet.contains(kieliSv) && curOrganisaatio.getNimiSv() != null) {
            nimi = curOrganisaatio.getNimiSv();
        } else if (kielet.contains(kieliEn) && curOrganisaatio.getNimiEn() != null) {
            nimi = curOrganisaatio.getNimiEn();
        }
        if (nimi.isEmpty()) {
            nimi = getAvailableName(curOrganisaatio);
        }
        return StringUtils.leftPad(nimi, 180);
    }

    private String getAvailableName(OrganisaatioPerustietoType curOrganisaatio) {
        if (curOrganisaatio.getNimiFi() != null) {
            return curOrganisaatio.getNimiFi();
        }
        if (curOrganisaatio.getNimiSv() != null) {
            return curOrganisaatio.getNimiSv();
        }
        if (curOrganisaatio.getNimiEn() != null) {
            return curOrganisaatio.getNimiEn();
        }
        return "";
    }

    private String getOppilaitosNro(OrganisaatioPerustietoType curOrganisaatio) {
        String opnro = "";
        if (curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            opnro = curOrganisaatio.getOppilaitosKoodi();
        }
        return StringUtils.leftPad(opnro, 5);
    }

    private String getOpPisteenJarjNro(Organisaatio orgE) {
        String opPisteenJarjNro = "";
        if (orgE.getOpetuspisteenJarjNro() != null) {
            opPisteenJarjNro = orgE.getOpetuspisteenJarjNro();
        }
        return StringUtils.leftPad(opPisteenJarjNro, 2);
    }

    private String getOpPisteenOppilaitosnumero(
            OrganisaatioPerustietoType curOrganisaatio) {
        if (curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE) 
                && !curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            return StringUtils.leftPad(this.oppilaitosoidOppilaitosnumeroMap.get(curOrganisaatio.getParentOid()), 5);
        } 
        if (curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE)) {
            return StringUtils.leftPad(curOrganisaatio.getOppilaitosKoodi(), 5);
        }
        return StringUtils.leftPad("", 5);
    }

    private String getSisainenKoodi(Organisaatio orgE) {
        System.out.println("getSisainenKoodi: " + orgE.getNimi());
        return StringUtils.leftPad(String.format("%s", orgE.getNimi().getId()), 10);
    }

    private boolean isToimipisteWritable(OrganisaatioPerustietoType curToimipiste) {
        Organisaatio toimipisteE = hakukohdeDAO.findOrganisaatioByOid(curToimipiste.getOid());
        String toimipistearvo = String.format("%s%s", toimipisteE.getOpetuspisteenJarjNro(), oppilaitosoidOppilaitosnumeroMap.get(curToimipiste.getParentOid()));
        List<KoodiType> koodit = this.getKoodisByArvoAndKoodisto(toimipistearvo, toimipistekoodisto);
        return koodit != null && !koodit.isEmpty();
    }
    
    private boolean isOppilaitosWritable(OrganisaatioPerustietoType curOppilaitos) {
        return isOppilaitosInKoodisto(curOppilaitos) 
                && isOppilaitosToinenAste(curOppilaitos);
    }
    
    private boolean isOppilaitosInKoodisto(OrganisaatioPerustietoType curOppilaitos) {
        String oppilaitoskoodi = curOppilaitos.getOppilaitosKoodi();
        List<KoodiType> koodit = getKoodisByArvoAndKoodisto(oppilaitoskoodi, oppilaitosnumerokoodisto);
        return koodit != null && !koodit.isEmpty();
    }

    private boolean isOppilaitosToinenAste(
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

}
