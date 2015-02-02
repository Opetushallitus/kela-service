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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    	"<removed>",
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
		
		OrganisaatioRDTO orgR = this.organisaatioResource.getOrganisaatioByOID(organisaatio.getOid(), false);
		
		String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",// 16 fields
				getYhtId(organisaatio),// YHT_ID
				getPostinumero(orgR.getPostiosoite()),// POS_NUMERO
				StringUtils.leftPad("", 3),// Postinumeroon liittyva maatunnus
				DEFAULT_DATE,// 01.01.0001-merkkijono
				getKatuosoite(orgR.getKayntiosoite(), organisaatio),// Katuosoite tai kayntiosoite
				getPostilokero(orgR.getPostiosoite(), organisaatio),// Postilokero
				getSimpleYhteystietoPuhnro(orgR),// Puhelinnumero
				getSimpleYhteystietoSPosti(orgR),// Sahkopostiosoite
				getSimpleYhteystietoFaksi(orgR),// Fax-numero
				getSimpleYhteystietoWWW(orgR),// Kotisivujen osoite
				//getSimpleYhteystieto(orgR.getFaksinumero(), 20),// Fax-numero
				//getSimpleYhteystieto(orgR.getWwwOsoite(), 80),// Kotisivujen osoite
				StringUtils.leftPad("", 15),// Postinumero (YHT_ULK_PTNUMERO)
				StringUtils.leftPad("", 25),// Postitoimipaikka (YHT_ULK_PTPAIKKA)
				StringUtils.leftPad("", 40),// YHT_ULK_ALUE
				DEFAULT_DATE,// Viimeisin paivityspaiva
				StringUtils.leftPad("", 30),// Viimeisin paivittaja
				getPostinumero(orgR.getKayntiosoite())// Postinumero POS_NRO
				);
		return record;
	}

	// TODO: JRE 1.8: when Predicates are supported use:
	/* 
	 * public static class YhteystietoPredicates 
	{
		public static Predicate<Map<String,String>> isLanguage(String lang) {
			return p -> p.containsKey("kieli") && p.get("kieli").equalsIgnoreCase(lang);
		}
	
		public static Predicate<Map<String,String>> isTyyppi(String tyyppi) {
			return p -> p.containsKey("tyyppi") && p.get("tyyppi").equalsIgnoreCase(tyyppi);
		}
	
		public static Predicate<Map<String,String>> isWWW() {
			return p -> p.containsKey("www");
		}
	
		public static Predicate<Map<String,String>> isEmail() {
		    return p -> p.containsKey("email");	    
		}
	}
	
	private Map<String,String> getAttributes(Predicate<Map<String,String>> p, List<Map<String,String>> yhteystiedot) {
		for (String lang : new String [] {"kieli_fi#1","kieli_sv#1","kieli_en#1"}) {
			Map<String,String> result = yhteystiedot.stream().filter(isLanguage(lang)).filter(p).findFirst().get();
			if (null != result) {
				return result;
			}
		}
		return new HashMap<String,String>();
	}
	*/
	

	//JRE 1.6:
	public static interface Pred {
		boolean is(Map<String,String> p);
	}
	
	public static class YhteystietoPredicates {
		
		protected static class isLanguage implements Pred{
			String lang;
			public isLanguage(String lang) {
				this.lang = lang;
			}
			@Override
			public boolean is(Map<String, String> p) {
				return p.containsKey("kieli") && p.get("kieli").equalsIgnoreCase(lang);			}
		}
		
		protected static class isTyyppi implements Pred {
			String tyyppi;
			public isTyyppi(String tyyppi) {
				this.tyyppi = tyyppi;
			}
			@Override
			public boolean is(Map<String, String> p) {
				return p.containsKey("tyyppi") && p.get("tyyppi").equalsIgnoreCase(tyyppi);
			}
		}
		
		protected static class isWWW implements Pred {
			@Override
			public boolean is(Map<String, String> p) {
				return p.containsKey("www");
			}
		}

		protected static class isEmail implements Pred {
			@Override
			public boolean is(Map<String, String> p) {
				return p.containsKey("email");
			}
		}
	}
	
	private Pred [] langPreds = new Pred [] 
			{ 	new YhteystietoPredicates.isLanguage("kieli_fi#1"),
				new YhteystietoPredicates.isLanguage("kieli_sv#1"),
				new YhteystietoPredicates.isLanguage("kieli_en#1") } ;
	
	private Map<String,String> getAttributes(Pred pred, List<Map<String,String>> yhteystiedot) {
		for (Pred langPred : langPreds ) { 
				for (Map<String,String> map :yhteystiedot) {
					if (langPred.is(map) && pred.is(map)) {
						return map;
				}
			}
		}
		return new HashMap<String,String>();
	}
	
	private Pred isTyyppiPuhelin = new YhteystietoPredicates.isTyyppi("puhelin");
	private Pred isEmail =  new YhteystietoPredicates.isEmail();
	private Pred isWWW =  new YhteystietoPredicates.isWWW();
	private Pred isFaksi =  new YhteystietoPredicates.isTyyppi("faksi");
	
	private String getSimpleYhteystietoPuhnro(OrganisaatioRDTO org) throws OPTFormatException {
		String pnro = null;
		// JRE 1.8: pnro = getAttributes(YhteystietoPredicates.isTyyppi("puhelin"), org.getYhteystiedot()).get("numero");
		// JRE 1.6:
		pnro = getAttributes(isTyyppiPuhelin, org.getYhteystiedot()).get("numero");
		String yhteystieto = getSimpleYhteystieto(pnro,60);
		if (yhteystieto==null || StringUtils.isEmpty(yhteystieto.trim())) {
			debug(1, org.getNimi(), "puhelinnro", org.getOid());
		}
		return yhteystieto;
	}
	
	private String getSimpleYhteystietoSPosti(OrganisaatioRDTO org) throws OPTFormatException {
		String sp=null;
		sp = getAttributes(isEmail, org.getYhteystiedot()).get("email");
		String yhteystieto = getSimpleYhteystieto(sp,80);
		if (yhteystieto==null || StringUtils.isEmpty(yhteystieto.trim())) {
			debug(1, org.getNimi(), "email", org.getOid());
		}
		return yhteystieto;
	}

	private String getSimpleYhteystietoFaksi(OrganisaatioRDTO org) throws OPTFormatException {
		String fax=null;
		fax = getAttributes(isFaksi, org.getYhteystiedot()).get("numero");
		String yhteystieto = getSimpleYhteystieto(fax,20);
		if (yhteystieto==null || StringUtils.isEmpty(yhteystieto.trim())) {
			debug(1, org.getNimi(), "fax", org.getOid());
		}
		return yhteystieto;
	}
	
	private String getSimpleYhteystietoWWW(OrganisaatioRDTO org) throws OPTFormatException {
		String www=null;
		www = getAttributes(isWWW, org.getYhteystiedot()).get("www");
		String yhteystieto = getSimpleYhteystieto(www,80);
		if (yhteystieto==null || StringUtils.isEmpty(yhteystieto.trim())) {
			debug(1, org.getNimi(), "www", org.getOid());
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
			debug(3, organisaatio.getOid()+" "+organisaatio.getNimi());
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
