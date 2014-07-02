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
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
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

	private final static String ERR_MESS_OPTITU_1 = "could not write tutkintokoodisto (Kelatutkinto '%s', oph-tutkinto: '%s') : invalid values.";
    
    private String FILEIDENTIFIER;
    private String ALKUTIETUE;
    private String LOPPUTIETUE;

    public WriteOPTITU() {
        super();
    }
    
    private String getKoulutusasteenYksiloivaTunniste(KoodiType kelaTutkinto) throws OPTFormatException {
        KoodiType koulutuastekoodi = getSisaltyvaKoodi(kelaTutkinto, kelaKoulutusastekoodisto);
        return (koulutuastekoodi == null) ?	StringUtils.leftPad("", 10, '0') : numFormatter(koulutuastekoodi.getKoodiArvo(), 10, "koulutusastekoodi");
    }

    private String getOpintoalanYksiloivaTunniste(KoodiType kelaTutkinto) throws OPTFormatException {
        
        KoodiType opintoalakoodi = getSisaltyvaKoodi(kelaTutkinto, kelaOpintoalakoodisto);
        return (opintoalakoodi == null) ? StringUtils.leftPad("", 10, '0') : numFormatter(opintoalakoodi.getKoodiArvo(), 10, "opintoalakoodi");
    }

    private String getKoodiPvm(XMLGregorianCalendar xgc) {
        if (xgc == null) {
            return DEFAULT_DATE;
        }
        return  getDateStrOrDefault(xgc.toGregorianCalendar().getTime());
    }

    private String getOphTutkintotunniste(KoodiType ophTutkinto) throws OPTFormatException {
    	return StringUtils.rightPad(ophTutkinto.getKoodiArvo(), 6, "ophtutkinto - koodiarvo");
    }

    private String getKelaTutkintoTunniste(KoodiType kelaTutkinto) {
    	return StringUtils.leftPad("",10);
    }

	@Override
	public void composeRecords() throws IOException {
        SearchKoodisByKoodistoCriteriaType criteria = new SearchKoodisByKoodistoCriteriaType();
        criteria.setKoodistoUri(kelaTutkintokoodisto);
        criteria.setKoodistoVersioSelection(SearchKoodisByKoodistoVersioSelectionType.LATEST);
        
        List<KoodiType> koodit = this.koodiService.searchKoodisByKoodisto(criteria);
        for (KoodiType curKelaTutkinto : koodit) {
            KoodiType curOphTutkinto = getRinnasteinenKoodi(curKelaTutkinto, koulutuskoodisto); // "tutkintokela" "koulutus"
            if (curOphTutkinto != null) {
    			try {
    				writeRecord(curKelaTutkinto, curOphTutkinto);
    			} catch (OPTFormatException e) {
    				LOG.error(String.format(ERR_MESS_OPTITU_1, curKelaTutkinto.getKoodiArvo(),curOphTutkinto.getKoodiArvo()));
    			} 
            }
        }
	}

	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		KoodiType kelaTutkinto = (KoodiType) args[0];
		KoodiType ophTutkinto = (KoodiType) args[1];
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
                DEFAULT_DATE,//oppisopimuspaiva
                StringUtils.leftPad("", 18),//oppisopimus diaarinumero
                DEFAULT_DATE,//TUT_TUPEPVM
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

	@Value("${OPTITU.alkutietue}")
    public void setAlkutietue(String alkutietue) {
        this.ALKUTIETUE = alkutietue;
    }
	
	@Value("${OPTITU.lopputietue}")
    public void setLopputietue(String lopputietue) {
        this.LOPPUTIETUE = lopputietue;
    }
	
	@Value("${OPTITU.fileIdentifier:OPTITU}")
    public void setFileIdentifier(String fileIdentifier) {
        this.FILEIDENTIFIER = fileIdentifier;
    }

	@Override
	public String getAlkutietue() {
		return ALKUTIETUE;
	}

	@Override
	public String getLopputietue() {
		return LOPPUTIETUE;
	}

	@Override
	public String getFileIdentifier() {
		return FILEIDENTIFIER;
	}

	@Override
	public String getPath() {
		return "";
	}
}
