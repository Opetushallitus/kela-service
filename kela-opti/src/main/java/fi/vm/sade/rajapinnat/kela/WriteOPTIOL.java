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
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTIOL extends AbstractOPTIWriter {
    
	private String FILENAME_SUFFIX;
	private String ALKUTIETUE;
	private String LOPPUTIETUE;
    
	private final static String ERR_MESS_OPTIOL_1 = "could not write oppilaitos %s : invalid values.";
	private final static String ERR_MESS_OPTIOL_2="incorrect OID : '%s'";
	private final static String ERR_MESS_OPTIOL_3="OID cannot not be null";
	private final static String ERR_MESS_OPTIOL_4="Could not retreive organisaatiotyyppi";
	
    public WriteOPTIOL() {
        super();
    }
    
	//TODO: writeFile() is only for backward compatibility (deprecated)-- it must be replaced by composeRecords()
	@Override
	public  void writeFile() throws IOException {
		throw new RuntimeException("writeFile() not supported any more");
	}

    private String getKoulJarjTunnus(OrganisaatioPerustieto curOppilaitos) {
        String parentOid = curOppilaitos.getParentOid();
        String tunnus = "";
        if (curOppilaitos.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA)) {
            tunnus = (curOppilaitos.getYtunnus() != null) ? curOppilaitos.getYtunnus() : curOppilaitos.getVirastoTunnus();
        } else if (parentOid != null) {
            Organisaatio parent = this.kelaDAO.findOrganisaatioByOid(parentOid);
            tunnus = (parent.getYtunnus() != null) ? parent.getYtunnus() : parent.getVirastotunnus();
        }
        return StringUtils.leftPad(tunnus, 10, '0');
    }

	@Override
	public void composeRecords() throws IOException {
        for (OrganisaatioPerustieto curOppilaitos : this.orgContainer.getOppilaitokset()) {
            try {
            	writeRecord(curOppilaitos);
            } catch (OPTFormatException e) {
				LOG.error(String.format(ERR_MESS_OPTIOL_1, curOppilaitos.getOid()));
			}
        }
	}

	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		OrganisaatioPerustieto curOppilaitos = (OrganisaatioPerustieto) args[0];
        Organisaatio orgE = kelaDAO.findOrganisaatioByOid(curOppilaitos.getOid());
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",//19 fields + EOL
                getOppilaitosNro(curOppilaitos),//OPPIL_NRO
                getOrgOid(orgE),
                getOrganisaatioTyyppi(curOppilaitos),
                getKoulJarjTunnus(curOppilaitos),//StringUtils.leftPad("", 10),//Koulutuksen jarjestajan tunnus
                getYhteystietojenTunnus(orgE),//Yhteystietojen tunnus
                getOppilaitostyyppitunnus(curOppilaitos),//OTY_ID
                numFormatter("0", 10, null), //0-merkkeja
                StringUtils.leftPad("", 5),//AMK_OPNRO
                getKotikunta(orgE),//Oppilaitoksen kotikunta
                DEFAULT_DATE,//01.01.0001-merkkijono
                DEFAULT_DATE,//01.01.0001-merkkijono
                getDateStrOrDefault(curOppilaitos.getAlkuPvm()),//Oppilaitoksen perustamisajankohta
                getDateStrOrDefault(curOppilaitos.getLakkautusPvm()),//Oppilaitoksen lakkauttamisajankohta
                DEFAULT_DATE,//Viimeinen paivityspaiva
                StringUtils.leftPad("", 10),//Viimeisin paivittaja
                StringUtils.leftPad("", 15),//Tyhjaa
                StringUtils.leftPad("", 6),//HANKI-koulutuksen kayttama menolaji
                StringUtils.leftPad("", 20),//HANKI-koulutuksen kayttama tilikoodi
                numFormatter("0", 10, null), //0-merkkeja
                "\n");
        return record;
	}
	
	private String getOrganisaatioTyyppi(OrganisaatioPerustieto curOppilaitos) throws OPTFormatException {
		List<KoodiType> koodis = getKoodisByUriAndVersio(curOppilaitos.getOppilaitostyyppi());        
		if (koodis.isEmpty()) {
			error(ERR_MESS_OPTIOL_4);
		}
		return strFormatter(koodis.get(0).getKoodiArvo(),2,"organisaatiotyyppi");
	}
	private String getOrgOid(Organisaatio org) throws OPTFormatException {
		if(null==org.getOid()) {
			error(String.format(ERR_MESS_OPTIOL_3));
		}
		String oid = org.getOid().substring(org.getOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(String.format(ERR_MESS_OPTIOL_2, org.getOid()));
		}
		return strFormatter(oid, 22, "OID");
	}

	public String getAlkutietue() {
		return ALKUTIETUE;
	}

	public String getLopputietue() {
		return LOPPUTIETUE;
	}

	public String getFilenameSuffix() {
		return FILENAME_SUFFIX;
	}

	public String getPath() {
		return "";
	}
	
	@Value("${OPTIOL.filenameSuffix:.OPTIOL}")
	public void setFilenameSuffix(String filenameSuffix) {
		this.FILENAME_SUFFIX = filenameSuffix;
	}

	@Value("${OPTIOL.alkutietue}")
	public void setAlkutietue(String alkutietue) {
		this.ALKUTIETUE = alkutietue;
	}

	@Value("${OPTIOL.lopputietue}")
	public void setLopputietue(String lopputietue) {
		this.LOPPUTIETUE = lopputietue;
	}
	
	
}
