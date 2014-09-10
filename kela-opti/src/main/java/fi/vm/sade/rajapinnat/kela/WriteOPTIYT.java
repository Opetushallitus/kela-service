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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.NonUniqueResultException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.organisaatio.resource.OrganisaatioResource;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.OrganisaatioPerustieto;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTIYT extends AbstractOPTIWriter {

	@Autowired
	private ApplicationContext appContext;

	private String FILEIDENTIFIER;
	private String ALKUTIETUE;
	private String LOPPUTIETUE;
	private String POSTINUMERO_FIELD = "postinumeroUri";
	private String OSOITE_FIELD = "osoite";

    private final static String [] errors = {
    	"could not write oppilaitos %s : invalid values.",
    	"could not write toimipiste %s : invalid values.",
    };
	
    private final static String [] warnings = {
    	"Yhteystieto of %s - %s is empty (org.oid=%s).",
    	"Yhteystieto of %s - %s is not unique (org.oid=%s).",
    	"Käyntisoite is empty (org.oid=%s)",
    };


	public WriteOPTIYT() {
		super();
	}

	public void composeRecords() throws IOException, UserStopRequestException {
		if (organisaatioResource == null) {
			organisaatioResource = (OrganisaatioResource) appContext.getBean("organisaatioResource"); // @Autowired did not work
		}
		for (OrganisaatioPerustieto curOppilaitos : this.orgContainer.getOppilaitokset()) {
			try {
				writeRecord(curOppilaitos);
			} catch (OPTFormatException e) {
				LOG.error(String.format(errors[0], curOppilaitos.getOid()+" "+curOppilaitos.getNimi()));
			} 
		}

		for (OrganisaatioPerustieto curToimipiste : this.orgContainer.getToimipisteet()) {
			try {
				writeRecord(curToimipiste);
			} catch (OPTFormatException e) {
				LOG.error(String.format(errors[1], curToimipiste.getOid()+" "+curToimipiste.getNimi()));
			}
		}
	}

	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		OrganisaatioPerustieto organisaatio = (OrganisaatioPerustieto) args[0];
		
		OrganisaatioRDTO orgR = this.organisaatioResource.getOrganisaatioByOID(organisaatio.getOid());
		
		String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",// 16 fields + EOL
				getYhtId(organisaatio),// YHT_ID
				getPostinumero(orgR.getPostiosoite()),// POS_NUMERO
				StringUtils.leftPad("", 3),// Postinumeroon liittyva maatunnus
				DEFAULT_DATE,// 01.01.0001-merkkijono
				getKatuosoite(orgR.getKayntiosoite(), organisaatio),// Katuosoite tai kayntiosoite
				getPostilokero(orgR.getPostiosoite(), organisaatio),// Postilokero
				getSimpleYhteystietoPuhnro(orgR),// Puhelinnumero
				getSimpleYhteystietoSPosti(orgR),// Sahkopostiosoite
				getSimpleYhteystieto(orgR.getFaksinumero(), 20),// Fax-numero
				getSimpleYhteystieto(orgR.getWwwOsoite(), 80),// Kotisivujen osoite
				StringUtils.leftPad("", 15),// Postinumero (YHT_ULK_PTNUMERO)
				StringUtils.leftPad("", 25),// Postitoimipaikka (YHT_ULK_PTPAIKKA)
				StringUtils.leftPad("", 40),// YHT_ULK_ALUE
				DEFAULT_DATE,// Viimeisin paivityspaiva
				StringUtils.leftPad("", 30),// Viimeisin paivittaja
				getPostinumero(orgR.getKayntiosoite()),// Postinumero POS_NRO
				"\n");
		return record;
	}


	private String getSimpleYhteystietoPuhnro(OrganisaatioRDTO org) throws OPTFormatException {
		String pnro = null;
		try {
			pnro = kelaDAO.getPuhelinnumero(org.getOid());
		} catch (NonUniqueResultException e) {
			warn(2, org.getNimi(), "puhelinnro", org.getOid());
		}
		String yhteystieto = getSimpleYhteystieto(pnro,60);
		if (yhteystieto==null || StringUtils.isEmpty(yhteystieto.trim())) {
			warn(1, org.getNimi(), "puhelinnro", org.getOid());
		}
		return yhteystieto;
	}
	
	private String getSimpleYhteystietoSPosti(OrganisaatioRDTO org) throws OPTFormatException {
		String sp=null;
		try {
			sp = kelaDAO.getEmail(org.getOid());
		} catch (NonUniqueResultException e) {
			warn(2, org.getNimi(), "email", org.getOid());
		}
		String yhteystieto = getSimpleYhteystieto(sp,80);
		if (yhteystieto==null || StringUtils.isEmpty(yhteystieto.trim())) {
			warn(1, org.getNimi(), "email", org.getOid());
		}
		return yhteystieto;
	}

	private String getSimpleYhteystieto(String yhteystieto, int pad) throws OPTFormatException {
		if (yhteystieto != null && yhteystieto.length() > pad) {
			yhteystieto = yhteystieto.substring(0, pad);
		}
		return (yhteystieto != null) ? strFormatter(yhteystieto, pad, "yhteystieto") : StringUtils.rightPad("", pad);
	}

	private String getKatuosoite(Map<String, String> osoite, OrganisaatioPerustieto organisaatio) throws OPTFormatException {
		String osoiteStr = osoite.get(OSOITE_FIELD);
		if (osoiteStr != null && osoiteStr.length() > 50) {
			osoiteStr = osoiteStr.substring(0, 50);
		}
		if (null == osoiteStr) {
			warn(3, organisaatio.getOid()+" "+organisaatio.getNimi());
			return strFormatter("", 50, "katuosoite");
		}
		return strFormatter(osoiteStr, 50, "katuosoite");
	}
	
	private String getPostilokero(Map<String, String> postiosoite, OrganisaatioPerustieto organisaatio) throws OPTFormatException {
		String katuos = postiosoite.get(OSOITE_FIELD);
		if (!StringUtils.isEmpty(katuos) && katuos.startsWith("PL") && katuos.length() < 11) {
			return strFormatter(katuos, 10, "postilokero");
		}
		return StringUtils.rightPad("", 10);
	}

	private String getPostinumero(Map<String, String> osoite) throws OPTFormatException {
		String postinumeroUri = osoite.get(POSTINUMERO_FIELD);
		List<KoodiType> koodit = (postinumeroUri != null) ? this.getKoodisByUriAndVersio(postinumeroUri) : new ArrayList<KoodiType>();
		String postinro = "";
		if (koodit != null && !koodit.isEmpty()) {
			postinro = koodit.get(0).getKoodiArvo();
		}
		return strFormatter(postinro, 5, "postinumero");
	}

	private String getYhtId(OrganisaatioPerustieto organisaatio) throws OPTFormatException {
		Organisaatio orgE = kelaDAO.findOrganisaatioByOid(organisaatio.getOid());
		return getYhteystietojenTunnus(organisaatio);
	}
	
	@Value("${OPTIYT.postinumeroField:postinumeroUri}")
    public void setPostinumeroField(String postinumeroField) {
        this.POSTINUMERO_FIELD = postinumeroField;
    }
	
	@Value("${OPTIYT.osoiteField:osoite}")
    public void setosoiteField(String osoiteField) {
        this.OSOITE_FIELD = osoiteField;
    }
	
	@Value("${OPTIYT.alkutietue}")
    public void setAlkutietue(String alkutietue) {
        this.ALKUTIETUE = alkutietue;
    }
	
	@Value("${OPTIYT.lopputietue}")
    public void setLopputietue(String lopputietue) {
        this.LOPPUTIETUE = lopputietue;
    }
	
	@Value("${OPTIYT.fileIdentifier:OPTIYT}")
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
