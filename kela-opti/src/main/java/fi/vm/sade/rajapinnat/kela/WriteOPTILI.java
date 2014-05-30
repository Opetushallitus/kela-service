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
    
    private String FILENAME_SUFFIX;
    private String ALKUTIETUE;
    private String LOPPUTIETUE;
    private String KOULUTUSLAJI;
    
    private final static String ERR_MESS_OPTILI_1="could not write hakukohde %s, tarjoaja %s : invalid values.";
    private final static String ERR_MESS_OPTILI_2="hakukohde %s not found in DB although found in index.";
    
	private final static String ERR_MESS_OPTILI_3="incorrect OID : '%s'";
	private final static String ERR_MESS_OPTILI_4="OID cannot not be null";
	
    private final static String WARN_MESS_OPTILI_1="Tutkintotunniste empty for hakukohde: %s";
    private final static String INFO_MESS_OPTILI_1="fetched %s hakukohde from index.";
    
    public WriteOPTILI() {
        super();
    }

    private boolean isHakukohdeToinenaste(String tarjoajaOid) {
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
        kausi = kausi.substring(0, 1);
        return StringUtils.rightPad(kausi, 12);
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

    private Object getTutkintotunniste(HakukohdePerustieto curTulos) throws OPTFormatException {
        LOG.debug("HaeTutkintotunniste: " + curTulos.getOid());
        KoulutuksetKysely kysely = new KoulutuksetKysely();
        kysely.getHakukohdeOids().add(curTulos.getOid());
        KoulutuksetVastaus vastaus = tarjontaSearchService.haeKoulutukset(kysely);
        LOG.debug("Koulutustulos size: " + vastaus.getKoulutukset().size());
        if (vastaus == null || vastaus.getKoulutukset().isEmpty()) {
            warn(String.format(WARN_MESS_OPTILI_1, curTulos.getOid()));
            return StringUtils.leftPad("", 6);
        }
        KoulutusPerustieto tulos = vastaus.getKoulutukset().get(0);
        String koodiUri = tulos.getKoulutuskoodi().getUri();
        List<KoodiType> koodis = this.getKoodisByUriAndVersio(koodiUri);        
        KoodiType koulutuskoodi = null;
        if (!koodis.isEmpty()) {
            koulutuskoodi = koodis.get(0);
        }
        KoodiType kelaKoodi = getRinnasteinenKoodi(koulutuskoodi, kelaTutkintokoodisto);
        if (null == kelaKoodi) {
        	return StringUtils.leftPad("", 6);
        }
        //KelaKoodi.getKoodiArvo() is 10 chars long. 4 left ones should be zeroes, and 6 right ones make up the kelakoodi. Otherwise it is error.
        return stripPreceedingZeros(kelaKoodi.getKoodiArvo(), 6,  "kelakoodi");
    }

    private String getKoulutuslaji() {
        return KOULUTUSLAJI;
    }

    @SuppressWarnings("unused")
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

    private String getOppilaitosnumero(OrganisaatioDTO organisaatio) {
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            return String.format("%s", organisaatio.getOppilaitosKoodi());
        } else if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE)) {
            return String.format("%s", organisaatioService.findByOid(organisaatio.getParentOid()).getOppilaitosKoodi());
        }
        return StringUtils.leftPad("", 5);
    }

    private String getHakukohdeId(HakukohdePerustieto curTulos) throws OPTFormatException {
        Hakukohde hakukE = kelaDAO.findHakukohdeByOid(curTulos.getOid());
        if (hakukE==null) {
        	error(String.format(ERR_MESS_OPTILI_2,curTulos.getOid()));
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
	
	@Value("${OPTILI.filenameSuffix:.OPTILI}")
    public void setFilenameSuffix(String filenameSuffix) {
        this.FILENAME_SUFFIX = filenameSuffix;
    }

	@Value("${OPTILI.koulutuslaji:N }")
    public void setKoulutuslaji(String koulutuslaji) {
        this.KOULUTUSLAJI = koulutuslaji;
    }
	
	@Override
	public void composeRecords() throws IOException {
        HakukohteetKysely kysely = new HakukohteetKysely();
        HakukohteetVastaus vastaus = tarjontaSearchService.haeHakukohteet(kysely);
        info(String.format(INFO_MESS_OPTILI_1, vastaus.getHitCount()));
        for (HakukohdePerustieto curTulos : vastaus.getHakukohteet()) {
        	String tarjoajaOid = curTulos.getTarjoajaOid();//getHakukohde().getTarjoaja().getTarjoajaOid();
            try {
            	if (isHakukohdeToinenaste(tarjoajaOid)) {
            		OrganisaatioDTO organisaatioDTO = this.organisaatioService.findByOid(tarjoajaOid);
            		this.writeRecord(curTulos, organisaatioDTO);
                } 
            } catch (OPTFormatException e) {
					LOG.error(String.format(ERR_MESS_OPTILI_1, curTulos.getOid(), tarjoajaOid));
			}
        }
    }

	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		HakukohdePerustieto curTulos=(HakukohdePerustieto) args[0];
		OrganisaatioDTO organisaatioDTO=(OrganisaatioDTO) args[1];
		return String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", //44 fields + line ending
                getHakukohdeId(curTulos),//Sisainen koodi
                getOppilaitosnumero(organisaatioDTO),//OPPIL_NRO
                getOrgOid(organisaatioDTO), //OrganisaatioOID
                StringUtils.leftPad("",2),//OPJNO
                StringUtils.leftPad("",12),//Op. linjan tai koulutuksen jarjestysnro
                getKoulutuslaji(),//Koulutuslaji
                StringUtils.leftPad("",2),//Opintolinjan kieli
                StringUtils.leftPad("",2),//OPL_LASPER
                StringUtils.leftPad("",2),//Valintakoeryhma
                StringUtils.leftPad("",10),//Kokeilun yksiloiva tunniste
                StringUtils.leftPad("",5), //OPL_SUUNT
                getHakukohteenNimi(curTulos),//Hakukohteen nimi
                StringUtils.leftPad("",1),//filler
                getTutkinnonTaso(curTulos),//Hakukohteen nimi
                getTutkintotunniste(curTulos),//TUT_ID = koulutuskoodi
                getOrgOid(curTulos), //hakukohde OrgOID
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
                DEFAULT_DATE,  //Loppupaiva
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
                StringUtils.leftPad("",3), //OPL_KUNTA
                StringUtils.leftPad("",3), //OPL_PAINOALUE
                StringUtils.leftPad("",1), //OPL_TYOHONSIJOITUSSEUR
                StringUtils.leftPad("",1), //OPL_LYHYTKESTO
                StringUtils.leftPad("",14), //Tyhjaa
                "\n");

	}

	private String getOrgOid(OrganisaatioDTO org) throws OPTFormatException {
		if(null==org.getOid()) {
			error(String.format(ERR_MESS_OPTILI_4));
		}
		String oid = org.getOid().substring(org.getOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(String.format(ERR_MESS_OPTILI_3, org.getOid()));
		}
		return strFormatter(oid, 22, "OID");
	}

	private String getOrgOid(HakukohdePerustieto org) throws OPTFormatException {
		if(null==org.getOid()) {
			error(String.format(ERR_MESS_OPTILI_4));
		}
		String oid = org.getOid().substring(org.getOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(String.format(ERR_MESS_OPTILI_3, org.getOid()));
		}
		return strFormatter(oid, 22, "OID");
	}
	
	private String getHakukohteenNimi(HakukohdePerustieto curTulos) throws OPTFormatException {
		return strCutter(curTulos.getNimi("fi"), 40, "hakukohteen nimi");
	}
	
	boolean warned=false;
	private String getTutkinnonTaso(HakukohdePerustieto curTulos) throws OPTFormatException {
		if (!warned) {
			warn("'Tutkinnon taso (TUTTASO)' is fixed always to be '050 (alempi kk.tutkinto)'");
			warned=true;
		}
        return StringUtils.leftPad("050", 3);
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
	public String getFilenameSuffix() {
		return FILENAME_SUFFIX;
	}

	@Override
	public String getPath() {
		return "";
	}
}
