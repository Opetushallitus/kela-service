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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Koulutusmoduuli;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.KoulutusmoduuliToteutus;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.tarjonta.service.search.HakukohdePerustieto;
import fi.vm.sade.tarjonta.service.search.HakukohteetKysely;
import fi.vm.sade.tarjonta.service.search.HakukohteetVastaus;
import fi.vm.sade.tarjonta.service.search.KoulutuksetKysely;
import fi.vm.sade.tarjonta.service.search.KoulutuksetVastaus;
import fi.vm.sade.tarjonta.service.search.KoulutusPerustieto;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTILI extends AbstractOPTIWriter {

	private static final Logger LOG = LoggerFactory.getLogger(KelaGenerator.class);
    
    private String FILEIDENTIFIER;
    private String ALKUTIETUE;
    private String LOPPUTIETUE;
    private String KOULUTUSLAJI;
    
    private final static String[] errors = {
    	"could not write hakukohde %s, tarjoaja %s : invalid values.",
    	"hakukohde %s not found in DB although found in index.",
    	"incorrect OID : '%s'",
    	"OID cannot not be null",
    	"hakukohde %s has no koulutukset",
    	"invalid OID: '%s'",
    	"komotoOID cannot not be null",
    	"OPPIL_NRO may not be missing (org.oid=%s).",
    	"HAK_NIMI may not be missing (org.oid=%s)."
    };
	
    private final static String[] infos = {
    	"fetched %s hakukohde from index."
    };
    
    
    
    public WriteOPTILI() {
        super();
    }

    private boolean isHakukohdeOppilaitos(String tarjoajaOid) {
        return this.orgContainer.getOrgOidList().contains(tarjoajaOid);
    }

    private String getVuosi(HakukohdePerustieto curTulos) {
    	if (null != curTulos && null != curTulos.getKoulutuksenAlkamisvuosi()){
    		return curTulos.getKoulutuksenAlkamisvuosi().toString();
    	}
    	return null;
    }

    private String getTila() {
        return "PAAT";
    }

    private String getAlkamiskausi(HakukohdePerustieto curTulos) {
        String kausi = curTulos.getKoulutuksenAlkamiskausi().getUri();
        if (null != kausi && kausi.length()>=1 && kausi.startsWith("kausi_")) {
        	kausi = kausi.substring(6, 7).toUpperCase(); //S or K
        	return StringUtils.rightPad(kausi, 12);
        }
        return StringUtils.rightPad("", 12);
    }

    private String getAlkupvm() {
        return DEFAULT_DATE;
    }
    
    @SuppressWarnings("unused")
	private Object getHakukohdeKoodi(HakukohdePerustieto curTulos) {
        String koodiUri = curTulos.getKoodistoNimi();
        List<KoodiType> koodis = getKoodisByUriAndVersio(koodiUri);
        return (koodis.isEmpty()) ? StringUtils.leftPad("", 3) : koodis.get(0).getKoodiArvo();
    }
    
    private Object getTutkintotunniste(KoulutusPerustieto koulutusPerustieto) throws OPTFormatException {
    	if (koulutusPerustieto.getKoulutusKoodi()==null) {
    		error(8);
    	}
    	return getTutkintotunniste(koulutusPerustieto.getKoulutusKoodi(),"koulutusPerustieto nimi: "+koulutusPerustieto.getNimi()+" komoto-oid: "+koulutusPerustieto.getKomotoOid());
    }

    private String getKoulutuslaji() {
        return KOULUTUSLAJI;
    }

	private String getOpetuspisteenJarjNro(HakukohdePerustieto curTulos, OrganisaatioDTO organisaatio) {
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE)) {
            return String.format("%s", organisaatio.getOpetuspisteenJarjNro());
        } 
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            Organisaatio organisaatioE = kelaDAO.findFirstChildOrganisaatio(curTulos.getTarjoajaOid());
            return (organisaatioE != null && organisaatioE.getOpetuspisteenJarjNro() != null) ? organisaatioE.getOpetuspisteenJarjNro() : "01";
        }
        return "01";
    }

    private String getOppilaitosnumero(OrganisaatioDTO organisaatio) throws OPTFormatException {
    	String oppil_nro = "";
    	if (null != organisaatio.getTyypit()) {
    		if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
    			oppil_nro = String.format("%s", organisaatio.getOppilaitosKoodi());
    		} else if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE)) {
    			oppil_nro = String.format("%s", organisaatioService.findByOid(organisaatio.getParentOid()).getOppilaitosKoodi());
    		}
    	}
    	if (StringUtils.isEmpty(oppil_nro.trim())){
    		error(8,organisaatio.getOid()+" "+organisaatio.getNimi());
    	}
        return strFormatter(oppil_nro, 5, "OPPIL_NRO");
    }

    private String getHakukohdeId(HakukohdePerustieto curTulos) throws OPTFormatException {
        Hakukohde hakukE = kelaDAO.findHakukohdeByOid(curTulos.getOid());
        if (hakukE==null) {
        	error(2,curTulos.getOid()+" "+curTulos.getNimi());
        }
        String hakukohdeId = String.format("%s", hakukE.getId());
        return numFormatter(hakukohdeId, 10, "hakukohdeid");
    }

	@Value("${OPTILI.alkutietue}")
    public void setAlkutietue(String alkutietue) {
        this.ALKUTIETUE = alkutietue;
    }
	
	@Value("${OPTILI.lopputietue}")
    public void setLopputietue(String lopputietue) {
        this.LOPPUTIETUE = lopputietue;
    }
	
	@Value("${OPTILI.fileIdentifier:OPTILI}")
    public void setFilenameSuffix(String fileIdentifier) {
        this.FILEIDENTIFIER = fileIdentifier;
    }

	@Value("${OPTILI.koulutuslaji:N }")
    public void setKoulutuslaji(String koulutuslaji) {
        this.KOULUTUSLAJI = koulutuslaji;
    }
	
	private KoulutuksetVastaus haeKoulutukset(String hakukohdeOid) {
        KoulutuksetKysely kysely = new KoulutuksetKysely();
        kysely.getHakukohdeOids().add(hakukohdeOid);
        return tarjontaSearchService.haeKoulutukset(kysely);

	}
	@Override
	public void composeRecords() throws IOException, UserStopRequestException{
        HakukohteetKysely kysely = new HakukohteetKysely();
        HakukohteetVastaus vastaus = tarjontaSearchService.haeHakukohteet(kysely);
        info(1, vastaus.getHitCount());
        for (HakukohdePerustieto curTulos : vastaus.getHakukohteet()) {
        	String tarjoajaOid = curTulos.getTarjoajaOid();//getHakukohde().getTarjoaja().getTarjoajaOid();
            try {
            	if (isHakukohdeOppilaitos(tarjoajaOid)) {
            		KoulutuksetVastaus koulutuksetVastaus = haeKoulutukset(curTulos.getOid());
            		if (koulutuksetVastaus.getHitCount()==0) {
            			error(5, curTulos.getOid()+" "+curTulos.getNimi());
            		}
            		OrganisaatioDTO organisaatioDTO = this.organisaatioService.findByOid(tarjoajaOid);
            		for (KoulutusPerustieto koulutusPerustieto : koulutuksetVastaus.getKoulutukset()) {
            			this.writeRecord(curTulos, organisaatioDTO, koulutusPerustieto);
            		}
                } 
            } catch (OPTFormatException e) {
					LOG.error(String.format(errors[0], (curTulos.getOid()+" "+curTulos.getNimi()), tarjoajaOid));
			}
        }
    }

	boolean emptyString(String s) {
		return (s==null || s.length()==0);
	}
	
	boolean ylempi(String s) {
		return s!=null && s.startsWith("koulutus_") && s.charAt(9)=='7';
	}

	boolean alempi(String s) {
		return s!=null && s.startsWith("koulutus_") && s.charAt(9)=='6';
	}

	private String getTutkinnonTaso(KoulutusPerustieto koulutusPerustieto) throws OPTFormatException {
		/*
		 * 1) jos hakukohteen koulutusmoduulin toteutuksella on kandi_koulutus_uri tai koulutus_uri käytetään näitä koulutusmoduulin sijasta
		 */
		String koulutus_uri;
		String kandi_koulutus_uri;
		KoulutusmoduuliToteutus komoto = kelaDAO.getKoulutusmoduuliToteutus(koulutusPerustieto.getKomotoOid());
		Koulutusmoduuli koulutusmoduuli = kelaDAO.getKoulutusmoduuli(koulutusPerustieto.getKoulutusmoduuli());
		if (komoto==null || koulutusmoduuli==null) {
			return "   "; //ei JULKAISTU
		}
		koulutus_uri = emptyString(komoto.getKoulutusUri()) ? koulutusmoduuli.getKoulutusUri() : komoto.getKandi_koulutus_uri();
		kandi_koulutus_uri = emptyString(komoto.getKandi_koulutus_uri()) ? koulutusmoduuli.getKandi_koulutus_uri() : komoto.getKandi_koulutus_uri();
		
		/*
		 * 2) jos koulutusmoduulilla sekä koulutus_uri (ylempi) ja kandi_koulutus_uri ei tyhjä => 060 = alempi+ylempi
		 */
		if (ylempi(koulutus_uri) && !emptyString(kandi_koulutus_uri)) {
			 return "060";
		}
		/*
		 * 3) haetaan lapsi- ja emokoulutusmoduulit (ei sisaruksia l. toteutuksia) yo. lisäksi:
		 */
		String rootOid=koulutusPerustieto.getKoulutusmoduuli();
		List<String> relativesList = kelaDAO.getChildrenOids(rootOid);
		relativesList.addAll(kelaDAO.getParentOids(rootOid));
		relativesList.add(rootOid);
		
		boolean ylempia=false;
		boolean alempia=false;
		for (String oid : relativesList) {
			koulutusmoduuli = kelaDAO.getKoulutusmoduuli(oid);
			if (koulutusmoduuli!=null) {
				if (!ylempia) {
					ylempia = ylempi(koulutusmoduuli.getKoulutusUri());
				}
				if (!alempia) {
					alempia = alempi(koulutusmoduuli.getKoulutusUri());
				}
				if (ylempia && alempia) {
					break;
				}
			}
		}
		/*
		 * 4) jos pelkkiä ylempiä => 061 (erillinen ylempi kk.tutkinto)
		 */
		if(ylempia && !alempia) {
			return "061";
		}
		/*
		 * 5) jos pelkkiä alempia => 050  (alempi kk.tutkinto)
		 */
		if(!ylempia && alempia) {
			return "050";
		}
		/*
		 * 6) jos väh. 1 ylempiä ja väh. 1 => 060 (alempi+ylempi)
		 */
		if(ylempia && alempia) {
			return "060";
		}
		/*
		 * 7) jos ei kumpiakaan : koulutuksen tasoa ei merkitä
		 */
		return "   ";
	}
	
	Pattern isInteger;
	
	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		if (isInteger==null) {
			 isInteger = Pattern.compile("\\d+");
		}
		HakukohdePerustieto curTulos=(HakukohdePerustieto) args[0];
		OrganisaatioDTO organisaatioDTO=(OrganisaatioDTO) args[1];
		KoulutusPerustieto koulutusPerustieto = (KoulutusPerustieto) args[2];
		return String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", //44 fields + line ending
                getHakukohdeId(curTulos),//Sisainen koodi
                getOppilaitosnumero(organisaatioDTO),//OPPIL_NRO
                getOrgOid(organisaatioDTO), //OrganisaatioOID
                getOpetuspisteenJarjNro(curTulos, organisaatioDTO),//OPJNO
                StringUtils.leftPad("",12),//Op. linjan tai koulutuksen jarjestysnro
                getKoulutuslaji(),//Koulutuslaji
                StringUtils.leftPad("",2),//Opintolinjan kieli
                StringUtils.leftPad("",2),//OPL_LASPER
                StringUtils.leftPad("",2),//Valintakoeryhma
                StringUtils.leftPad("",10),//Kokeilun yksiloiva tunniste
                StringUtils.leftPad("",5), //OPL_SUUNT
                getHakukohteenNimi(curTulos),//Hakukohteen nimi
                StringUtils.leftPad("",1),//filler
                getTutkinnonTaso(koulutusPerustieto),//TUT_TASO
                getTutkintotunniste(koulutusPerustieto),//TUT_ID = koulutuskoodi
                getOrgOid(curTulos), //hakukohde OID
                StringUtils.leftPad("",3), //filler
                StringUtils.leftPad("",3), //OPL_OTTOALUE
                StringUtils.leftPad("",3), //Opetusmuoto
                StringUtils.leftPad("",2), //Koulutustyyppi
                StringUtils.leftPad("",3), //OPL-EKASITTELY
                StringUtils.leftPad("",2), //OPL_PKR_TUNNUS
                StringUtils.leftPad("",2), //OPL_VKOEJAR
                StringUtils.leftPad("",2), //OPL_VKOEKUT
                StringUtils.leftPad("",7), //Alinkeskiarvo
                StringUtils.leftPad("",12), //Koulutuksen kesto
                StringUtils.leftPad("",3), //OPL_KKERROIN
                getAlkupvm(), //Alkupaiva, voimassaolon alku
                DEFAULT_DATE, //Loppupaiva
                DEFAULT_DATE, //Viimeisin paivityspaiva
                StringUtils.leftPad("",30), //Viimeisin paivittaja
                StringUtils.leftPad("",6), //El-harpis
                getAlkamiskausi(curTulos), //ALKUKAUSI
                getTila(), //TILA
                getVuosi(curTulos), //VUOSI 
                DEFAULT_DATE, //Alkupaiva, voimassaolon alku
                DEFAULT_DATE, //Loppupaiva, voimassaolon loppu
                StringUtils.leftPad("",1), //OPL_TULOSTUS
                StringUtils.leftPad("",15), //OPL_OMISTAJA
                getKomotoOid(koulutusPerustieto) ,//KOU_OID
                "\n");
	}

	private String getKomotoOid(KoulutusPerustieto koulutusPerustieto) throws OPTFormatException {
		if(null==koulutusPerustieto.getKomotoOid()) {
			error(7);
		}
		String oid = koulutusPerustieto.getKomotoOid().substring(koulutusPerustieto.getKomotoOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(3, koulutusPerustieto.getKomotoOid()+" "+koulutusPerustieto.getNimi());
		}
		if (!isInteger.matcher(oid).matches()) {
			error(6, koulutusPerustieto.getKomotoOid()+" "+koulutusPerustieto.getNimi());
		}
		return strFormatter(oid, 22, "KOMOTOOID");
	}

	private String getOrgOid(OrganisaatioDTO org) throws OPTFormatException {
		if(null==org.getOid()) {
			error(4);
		}
		String oid = org.getOid().substring(org.getOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(3, org.getOid()+" "+org.getNimi());
		}
		return strFormatter(oid, 22, "OID");
	}
	
	private String getOrgOid(HakukohdePerustieto pt) throws OPTFormatException {
		if(null==pt.getOid()) {
			error(4);
		}
		String oid = pt.getOid().substring(pt.getOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(3, oid+" "+pt.getNimi());
		}
		if (!isInteger.matcher(oid).matches()) {
			error(6, pt.getOid()+" "+pt.getNimi());
		}
		return strFormatter(oid, 22, "OID");
	}
	
	private String getHakukohteenNimi(HakukohdePerustieto curTulos) throws OPTFormatException {
		String hakNimi=curTulos.getNimi("fi");
		if (StringUtils.isEmpty(hakNimi)) {
			hakNimi=curTulos.getNimi("sv");
		}
		if (StringUtils.isEmpty(hakNimi)) {
			hakNimi=curTulos.getNimi("en");
		}
		if (StringUtils.isEmpty(hakNimi)) {
			error(9, curTulos.getOid()+" "+curTulos.getNimi());
		}
		return strCutter(hakNimi, 40, "hakukohteen nimi", false);
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
		return null;
	}

	@Override
	public String[] getInfos() {
		return infos;
	}
}
