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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTIOL extends AbstractOPTIWriter {
    
    private static final String OPTIOL = ".OPTIOL";
    
    private static final String ALKUTIETUE = "00000ALKU\n";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????\n";
    
    public WriteOPTIOL() {
        super();
        
    }
    
    @Override
    public void writeFile() throws IOException {
        createFileName("", OPTIOL);
        bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
        bos.write(toLatin1(ALKUTIETUE));
        
        for (OrganisaatioPerustietoType curOppilaitos : this.orgContainer.getOppilaitokset()) {
                bos.write(toLatin1(createRecord(curOppilaitos)));   
                bos.flush();
        }

        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();
    }

    private String createRecord(OrganisaatioPerustietoType curOppilaitos) {
        Organisaatio orgE = kelaDAO.findOrganisaatioByOid(curOppilaitos.getOid());
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",//18 fields + EOL
                getOppilaitosNro(curOppilaitos),//OPPIL_NRO
                StringUtils.leftPad("", 4),//Tyhjaa
                StringUtils.leftPad("", 10),//Koulutuksen jarjestajan tunnus
                getYhteystietojenTunnus(orgE),//Yhteystietojen tunnus
                getOppilaitostyyppitunnus(curOppilaitos),//OTY_ID
                getZeros(), //0-merkkeja
                StringUtils.leftPad("", 5),//AMK_OPNRO
                getKotikunta(orgE),//Oppilaitoksen kotikunta
                DEFAULT_DATE,//01.01.0001-merkkijono
                DEFAULT_DATE,//01.01.0001-merkkijono
                getDateStrOrDefault(curOppilaitos.getAlkuPvm()),//Oppilaitoksen perustamisajankohta
                getDateStrOrDefault(curOppilaitos.getLakkautusPvm()),//Oppilaitoksen lakkauttamisajankohta
                DEFAULT_DATE,//Viimeinen paivityspaiva
                StringUtils.leftPad("", 30),//Viimeisin paivittaja
                StringUtils.leftPad("", 15),//Tyhjaa
                StringUtils.leftPad("", 6),//HANKI-koulutuksen kayttama menolaji
                StringUtils.leftPad("", 20),//HANKI-koulutuksen kayttama tilikoodi
                getZeros(),//0-merkkeja
                "\n");
        return record;
    }

    private String getZeros() {
        return "0000000000";
    }

}
