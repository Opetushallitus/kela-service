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
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTIOP extends AbstractOPTIWriter {
    
    private static final String OPTIOP = ".OPTIOP";
    
    private static final String ALKUTIETUE = "00000ALKU\n";
    private static final String LOPPUTIETUE = "99999LOPPU??????\n";
    
    public WriteOPTIOP() {
        super();  
    }

    @Override
    public void writeFile() throws IOException {
        createFileName("", OPTIOP);
        bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
        bos.write(toLatin1(ALKUTIETUE));
        
        for (OrganisaatioPerustietoType curToimipiste : this.orgContainer.getToimipisteet()) {
                bos.write(toLatin1(createRecord(curToimipiste)));
                bos.flush();
        }

        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();

    }

    private String createRecord(OrganisaatioPerustietoType curToimipiste) {
        Organisaatio orgE = kelaDAO.findOrganisaatioByOid(curToimipiste.getOid());
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",//18 fields + EOL
                getOpPisteenOppilaitosnumero(curToimipiste),//OPPIL_NRO
                getOpPisteenJarjNro(orgE),//OPJNO
                StringUtils.leftPad("", 5),//Koulutuksen jarjestajan tunnus
                getYhteystietojenTunnus(orgE),//Yhteystietojen tunnus
                getOpetuspisteenKieli(curToimipiste),//Opetuspisteen kieli
                getOppilaitostyyppitunnus(orgContainer.getOppilaitosoidOppilaitosMap().get(curToimipiste.getParentOid())),//OTY_ID
                getKotikunta(orgE),//Oppilaitoksen kotikunta
                StringUtils.leftPad("", 2),//OPE_ETEHTAVA
                StringUtils.leftPad("", 4),//Tyhjaa
                getDateStrOrDefault(curToimipiste.getAlkuPvm()),//Op.pisteen perustamispaiva PERUPVM
                getDateStrOrDefault(curToimipiste.getLakkautusPvm()),//Op.pisteen lakkauttamispaiva LAKKPVM
                StringUtils.leftPad("", 1),//Tyhjaa
                StringUtils.leftPad("", 1),//Opetuspisteen kaikille koulutukselle jarjestetaan kielikoe
                getYhkoodi(orgContainer.getOppilaitosoidOppilaitosMap().get(curToimipiste.getParentOid())),//YH_KOULU
                DEFAULT_DATE,//Viimeisin paivityspaiva
                StringUtils.leftPad("", 30),//Viimeisin paivittaja
                StringUtils.leftPad("", 15),//Tyhjaa
                StringUtils.leftPad("", 10),//01.01.0001,merkkijono
                "\n");
        return record;
    }

    private String getYhkoodi(
            OrganisaatioPerustietoType curOppilaitos) {
        List<KoodiType> koodis = orgContainer.getKoodisByArvoAndKoodisto(curOppilaitos.getOppilaitosKoodi(), orgContainer.oppilaitosnumerokoodisto);        
        KoodiType opNroKoodi = null;
        if (!koodis.isEmpty()) {
            opNroKoodi = koodis.get(0);
        }
        KoodiType yhKoodi = getSisaltyvaKoodi(opNroKoodi, yhKoulukoodiKoodisto);
        return (yhKoodi == null) ? StringUtils.leftPad("", 4) : StringUtils.leftPad(yhKoodi.getKoodiArvo(), 4);
    }

    private String getOpetuspisteenKieli(
            OrganisaatioPerustietoType curToimipiste) {
        if (!StringUtils.isEmpty(curToimipiste.getNimiFi())
                && !StringUtils.isEmpty(curToimipiste.getNimiSv())) {
            return StringUtils.leftPad("M", 2);
        }
        if (!StringUtils.isEmpty(curToimipiste.getNimiFi())) {
            return StringUtils.leftPad("S", 2);
        } 
        if (!StringUtils.isEmpty(curToimipiste.getNimiSv())) {
            return StringUtils.leftPad("R", 2);
        }

        return StringUtils.leftPad("S", 2);
    }

}