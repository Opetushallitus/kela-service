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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.OrganisaatioPerustieto;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTIOL extends AbstractOPTIWriter {
	
	private String FILEIDENTIFIER;
	private String ALKUTIETUE;
	private String LOPPUTIETUE;
    
	private final static String [] errors = {
		"could not write oppilaitos %s : invalid values.",
		"incorrect OID : '%s'",
		"OID cannot not be null",
		"could not write toimipiste %s : invalid values.",
		"oppilaitosnro not found (org.oidO=%s)."
	};

	public WriteOPTIOL() {
        super();
    }
    
	@Override
	public void composeRecords() throws IOException, UserStopRequestException {
		for (OrganisaatioPerustieto ol : this.orgContainer.getOppilaitokset()) {
			try {
				//this.writeRecord(ol, "02");
				this.writeRecord(ol, OrgType.OPPILAITOS);
			} catch (OPTFormatException e) {
				LOG.error(String.format(errors[0], ol.getOid()+" "+ol.getNimi()));
			}
		}
		for (OrganisaatioPerustieto tp : this.orgContainer.getToimipisteet()) {
			try {
				//this.writeRecord(tp, "03");
				this.writeRecord(tp, OrgType.TOIMIPISTE);
			} catch (OPTFormatException e) {
				LOG.error(String.format(errors[3], tp.getOid()+" "+tp.getNimi()));
			}
		}
	}

	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		OrganisaatioPerustieto curOppilaitos = (OrganisaatioPerustieto) args[0];
		OrgType organisaatioTyyppi = (OrgType) args[1];
        Organisaatio orgE = kelaDAO.findOrganisaatioByOid(curOppilaitos.getOid());
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",//19 fields
                _getOppilaitosNro(curOppilaitos),//OPPIL_NRO
                getOrgOid(orgE),
                organisaatioTyyppi.equals(OrgType.OPPILAITOS) ? "02" : "03",
                StringUtils.leftPad("", 10),//Koulutuksen jarjestajan tunnus
                getYhteystietojenTunnus(orgE),//Yhteystietojen tunnus
                organisaatioTyyppi.equals(OrgType.OPPILAITOS) ? getOppilaitostyyppitunnus(curOppilaitos):
                	//TODO: ei toimi jos parentoid ei ole oppilaitos
                			  //getOppilaitostyyppitunnus(this.orgContainer.getOppilaitosoidOppilaitosMap().get(curOppilaitos.getParentOid())),//OTY_ID
                getOppilaitostyyppitunnus(this.orgContainer.getOppilaitosoidOppilaitosMap().get(curOppilaitos.getParentOppilaitosOid())),//OTY_ID
                numFormatter("0", 10, null), //0-merkkeja
                StringUtils.leftPad("", 5),//AMK_OPNRO
                getKotikunta(orgE),//Oppilaitoksen kotikunta
                DEFAULT_DATE,//01.01.0001-merkkijono
                DEFAULT_DATE,//01.01.0001-merkkijono
                getDateStrOrDefault(orgE.getAlkupvm()),//Oppilaitoksen perustamisajankohta
                getDateStrOrDefault(orgE.getLakkautuspvm()),//Oppilaitoksen lakkauttamisajankohta
                DEFAULT_DATE,//Viimeinen paivityspaiva
                StringUtils.leftPad("", 10),//Viimeisin paivittaja
                StringUtils.leftPad("", 15),//Tyhjaa
                StringUtils.leftPad("", 6),//HANKI-koulutuksen kayttama menolaji
                StringUtils.leftPad("", 20),//HANKI-koulutuksen kayttama tilikoodi
                numFormatter("0", 10, null) //0-merkkeja
                );
        return record;
	}

	private String _getOppilaitosNro(OrganisaatioPerustieto org) throws OPTFormatException {
		String oppil_nro=getOppilaitosNro(org);
		if (StringUtils.isEmpty(oppil_nro)) {
			error(5, org.getOid()+" "+org.getNimi());
		}
		return oppil_nro;
	}
	
	private String getOrgOid(Organisaatio org) throws OPTFormatException {
		if(null==org.getOid()) {
			error(3);
		}
		String oid = org.getOid().substring(org.getOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(2, org.getOid()+" "+org.getNimi());
		}
		return strFormatter(oid, 22, "OID");
	}

	public String getAlkutietue() {
		return ALKUTIETUE;
	}

	public String getLopputietue() {
		return LOPPUTIETUE;
	}

	public String getFileIdentifier() {
		return FILEIDENTIFIER;
	}
	
	@Value("${OPTIOL.fileIdentifier:OPTIOL}")
	public void setFileIdentifier(String fileIdentifier) {
		this.FILEIDENTIFIER = fileIdentifier;
	}

	@Value("${OPTIOL.alkutietue}")
	public void setAlkutietue(String alkutietue) {
		this.ALKUTIETUE = alkutietue;
	}

	@Value("${OPTIOL.lopputietue}")
	public void setLopputietue(String lopputietue) {
		this.LOPPUTIETUE = lopputietue;
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
		return null;
	}
}
