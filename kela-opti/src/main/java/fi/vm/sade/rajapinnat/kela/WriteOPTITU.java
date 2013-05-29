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

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import fi.vm.sade.koodisto.service.types.SearchKoodisByKoodistoCriteriaType;
import fi.vm.sade.koodisto.service.types.SearchKoodisByKoodistoVersioSelectionType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTITU extends AbstractOPTIWriter {

    private static final String OPTITU = ".OPTITU";
    
    private static final String ALKUTIETUE = "0000000000ALKU\n";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????\n";
    
    public WriteOPTITU() {
        super();
    }
    
    @Override
    public void writeFile() throws IOException {
        this.createFileName("", OPTITU);
        bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
        bos.write(toLatin1(ALKUTIETUE));
        SearchKoodisByKoodistoCriteriaType criteria = new SearchKoodisByKoodistoCriteriaType();
        criteria.setKoodistoUri(kelaTutkintokoodisto);
        criteria.setKoodistoVersioSelection(SearchKoodisByKoodistoVersioSelectionType.LATEST);
        
        List<KoodiType> koodit = this.koodiService.searchKoodisByKoodisto(criteria);
        
        for (KoodiType curKelaTutkinto : koodit) {
            KoodiType curOphTutkinto = getRinnasteinenKoodi(curKelaTutkinto, koulutuskoodisto);
            if (curOphTutkinto != null) {
                bos.write(toLatin1(createRecord(curKelaTutkinto, curOphTutkinto)));
            }
        }
        
        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();
    }

    private String createRecord(KoodiType kelaTutkinto, KoodiType ophTutkinto) {
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", //26 fields + EOL
                getKelaTutkintoTunniste(kelaTutkinto),//TUT_ID
                getOphTutkintotunniste(ophTutkinto),//Tilastokeskuksen koulutuskoodi
                DEFAULT_DATE,//Alkupaiva, voimassaolon alku
                StringUtils.leftPad("", 10),//ALA_ID
                StringUtils.leftPad("", 3),//Ammattitutkinto
                StringUtils.leftPad("", 10),//KAS_ID
                getKoodiPvm(kelaTutkinto.getVoimassaLoppuPvm()),//Loppupaiva, voimassaolon loppu
                StringUtils.leftPad("", 1),//TUT_HYVAKSYTTY
                StringUtils.leftPad("", 1),//Kehityssuunnitelman mukainen tutkinto
                StringUtils.leftPad("", 1),//Mahdollista suorittaa oppisopimuksella
                StringUtils.leftPad("", 10),//oppisopimuspaiva
                StringUtils.leftPad("", 18),//oppisopimus diaarinumero
                StringUtils.leftPad("", 10),//TUT_TUPEPVM
                StringUtils.leftPad("", 18),//TUT_TUPEDIA
                StringUtils.leftPad("", 10),//TUT_PITUUS1
                StringUtils.leftPad("", 5),//TUT_YKSIKKO1
                StringUtils.leftPad("", 10),//TUT_PITUUS2
                StringUtils.leftPad("", 5),//TUT_YKSIKKO2
                StringUtils.leftPad("", 10),//TUT_PITUUS3
                StringUtils.leftPad("", 5),//TUT_YKSIKKO3
                StringUtils.leftPad("", 10),//TUT_PITUUS4
                StringUtils.leftPad("", 5),//TUT_YKSIKKO4
                DEFAULT_DATE,//Viimeisin paivityspaiva
                StringUtils.leftPad("", 30),//Viimeisin paivittaja
                getOpintoalanYksiloivaTunniste(kelaTutkinto),//UALA_ID
                getKoulutusasteenYksiloivaTunniste(kelaTutkinto),//UKAS_ID
                "\n");
        return record;
    }
    
    

    private String getKoulutusasteenYksiloivaTunniste(KoodiType kelaTutkinto) {
        
        KoodiType koulutuastekoodi = getSisaltyvaKoodi(kelaTutkinto, kelaKoulutusastekoodisto);
        
        return (koulutuastekoodi == null) ? StringUtils.leftPad("", 10) : StringUtils.leftPad(koulutuastekoodi.getKoodiArvo(), 10);
    }

    private String getOpintoalanYksiloivaTunniste(KoodiType kelaTutkinto) {
        
        KoodiType opintoalakoodi = getSisaltyvaKoodi(kelaTutkinto, kelaOpintoalakoodisto);
        
        return (opintoalakoodi == null) ? StringUtils.leftPad("", 10) : StringUtils.leftPad(opintoalakoodi.getKoodiArvo(), 10);
    }

    private String getKoodiPvm(XMLGregorianCalendar xgc) {
        if (xgc == null) {
            return DEFAULT_DATE;
        }
        return  getDateStrOrDefault(xgc.toGregorianCalendar().getTime());
    }

    private String getOphTutkintotunniste(KoodiType ophTutkinto) {
        return StringUtils.leftPad(ophTutkinto.getKoodiArvo(), 6);
    }

    private String getKelaTutkintoTunniste(KoodiType kelaTutkinto) {
        return StringUtils.leftPad(kelaTutkinto.getKoodiArvo(), 10);
    }
    
    

}
