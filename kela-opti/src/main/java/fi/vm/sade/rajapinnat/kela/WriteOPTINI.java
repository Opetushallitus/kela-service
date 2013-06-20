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
    
    
    public WriteOPTINI() {
        super();
       
    }
    
    @Override
    public void writeFile() throws IOException {
        createFileName("", OPTINI);
        bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
        bos.write(toLatin1(ALKUTIETUE));
        
        for (OrganisaatioPerustietoType curOppilaitos : this.orgContainer.getOppilaitokset()) {
                bos.write(toLatin1(createRecord(curOppilaitos)));   
                bos.flush();
        }
        for (OrganisaatioPerustietoType curToimipiste : this.orgContainer.getToimipisteet()) {
                bos.write(toLatin1(createRecord(curToimipiste)));
                bos.flush();
        }
        
        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();
    }
    
    
    private String createRecord(OrganisaatioPerustietoType curOrganisaatio) {
        Organisaatio orgE = kelaDAO.findOrganisaatioByOid(curOrganisaatio.getOid());
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
        
        return record;
    }
    
    
    
    private String getOrganisaatioLyhytNimi(
            OrganisaatioPerustietoType curOrganisaatio, Organisaatio orgE, List<String> kielet) {
        List<KoodiType> koodit = new ArrayList<KoodiType>();
        if (curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            koodit = orgContainer.getKoodisByArvoAndKoodisto(curOrganisaatio.getOppilaitosKoodi(), orgContainer.oppilaitosnumerokoodisto);
        } else if (curOrganisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE)) {
            String opArvo = String.format("%s%s", getOpPisteenOppilaitosnumero(curOrganisaatio), getOpPisteenJarjNro(orgE));
            koodit = orgContainer.getKoodisByArvoAndKoodisto(opArvo, orgContainer.toimipistekoodisto);
        }
        String lyhytNimi = "";
        if (koodit != null && !koodit.isEmpty()) {
            lyhytNimi = getLyhytNimiFromKoodi(koodit.get(0), kielet);
        }
        if (lyhytNimi.length() > 40) {
            lyhytNimi = lyhytNimi.substring(0, 40);
        }
        return StringUtils.rightPad(lyhytNimi, 40);
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
        if (nimi.length() > 180) {
            nimi = nimi.substring(0, 180);
        }
        return StringUtils.rightPad(nimi, 180);
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



    private String getSisainenKoodi(Organisaatio orgE) {
        
        return StringUtils.leftPad(String.format("%s", orgE.getNimi().getId()), 10, '0');
    }
}
