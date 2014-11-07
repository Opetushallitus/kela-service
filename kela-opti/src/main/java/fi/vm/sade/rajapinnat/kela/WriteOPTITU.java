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

	private final static String [] errors = {
		"could not write tutkintokoodisto (Kelatutkinto '%s', oph-tutkinto: '%s') : invalid values.",
	};
    
	private final static String [] warnings = {
		"no value for koulutuskoodisto (%s) koodi (%s) in oph ophKoulutusastekoodisto (%s)",
		"no value for koulutuskoodisto (%s) koodi (%s) in oph ophOpintoalakoodisto (%s)",
		"no value for ophopintoalakoodisto (%s) koodi (%s) in oph kelaopintoala (%s)",
		"no value for ophKoulutusastekoodisto (%s) koodi (%s) in oph kelakoulutusalakoodisto (%s)",
	};

	private String FILEIDENTIFIER;
    private String ALKUTIETUE;
    private String LOPPUTIETUE;

    public WriteOPTITU() {
        super();
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
    
	@Override
	public void composeRecords() throws IOException, UserStopRequestException {
        SearchKoodisByKoodistoCriteriaType criteria = new SearchKoodisByKoodistoCriteriaType();
        criteria.setKoodistoUri(koulutuskoodisto);
        criteria.setKoodistoVersioSelection(SearchKoodisByKoodistoVersioSelectionType.LATEST);

        List<KoodiType> koulutuskoodit = this.koodiService.searchKoodisByKoodisto(criteria);

        for (KoodiType curKoulutuskoodi : koulutuskoodit) {
			KoodiType koulutusasteoph =getSisaltyvaKoodi(curKoulutuskoodi, ophKoulutusastekoodisto);
			if (null == koulutusasteoph) {
				debug(1,koulutuskoodisto,curKoulutuskoodi.getKoodiArvo(), ophKoulutusastekoodisto);
				continue;
			}			
			KoodiType koulutusastekela = getRinnasteinenKoodi(koulutusasteoph, kelaKoulutusastekoodisto);
			if (null == koulutusastekela) {
				debug(4,ophKoulutusastekoodisto,koulutusasteoph.getKoodiArvo(), kelaKoulutusastekoodisto);
				continue;
			}
			
			KoodiType koulutusalaoph = getSisaltyvaKoodi(curKoulutuskoodi, ophOpintoalakoodisto);
        	if (null == koulutusalaoph) {
        		debug(2,koulutuskoodisto,curKoulutuskoodi.getKoodiArvo(), ophOpintoalakoodisto);
        		continue;
        	}
        	KoodiType koulutusalakela = getRinnasteinenKoodi(koulutusalaoph, kelaOpintoalakoodisto);
       		if (null == koulutusalakela) {
       			debug(3,ophOpintoalakoodisto,koulutusalaoph.getKoodiArvo(), kelaOpintoalakoodisto);
       			continue;
       		}
			try {
				writeRecord(curKoulutuskoodi, koulutusastekela, koulutusalakela);
			} catch (OPTFormatException e) {
				LOG.error(String.format(errors[0], curKoulutuskoodi.getKoodiArvo(),curKoulutuskoodi.getKoodiArvo()));
			} 
        }
	}

	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		KoodiType koulutusKoodi = (KoodiType) args[0];
    	KoodiType koulutusastekela = (KoodiType) args[1];
        KoodiType koulutusalakela =(KoodiType) args[2];
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", //26 fields
        		StringUtils.leftPad("",10),
                getOphTutkintotunniste(koulutusKoodi),//Tilastokeskuksen koulutuskoodi
                DEFAULT_DATE,//Alkupaiva, voimassaolon alku
                StringUtils.leftPad("", 10),//ALA_ID
                StringUtils.leftPad("", 3),//Ammattitutkinto
                StringUtils.leftPad("", 10),//KAS_ID
                getKoodiPvm(koulutusKoodi.getVoimassaLoppuPvm()),//Loppupaiva, voimassaolon loppu
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
                numFormatter(koulutusalakela.getKoodiArvo(),10,"koulutusa-ala-kela"),
                numFormatter(koulutusastekela.getKoodiArvo(),10,"koulutusa-aste-kela")
                );
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
	public String[] getErrors() {
		return errors;
	}

	@Override
	public String[] getWarnings() {
		return warnings;
	}

	@Override
	public String[] getInfos() {
		return null;
	}
}
