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

import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.OrganisaatioPerustieto;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTINI extends AbstractOPTIWriter {

	private String FILEIDENTIFIER;
    private String ALKUTIETUE;
    private String LOPPUTIETUE;
    
    private final static String[] errors = { 
	    "could not write oppilaitos %s : invalid values.",
	    "could not write toimipiste %s : invalid values.",
	    "incorrect OID : '%s'",
	    "OID cannot not be null",
	    "NIMI cannot not be null (%s)",
	    "NIMIL cannot not be null (%s)",
	    "Opplaitosnumero not found for organisaatio %s"
    };

    public WriteOPTINI() {
        super();
    }

    private String getOrganisaatioNimi(
        OrganisaatioPerustieto curOrganisaatio, List<String> kielet) throws OPTFormatException {
        String nimi = "";
        if (kielet.contains(kieliFi) && curOrganisaatio.getNimi("fi") != null) {
            nimi = curOrganisaatio.getNimi("fi");//getNimiFi();
        } else if (kielet.contains(kieliSv) && curOrganisaatio.getNimi("sv") != null) {
            nimi = curOrganisaatio.getNimi("sv");
        } else if (kielet.contains(kieliEn) && curOrganisaatio.getNimi("en") != null) {
            nimi = curOrganisaatio.getNimi("en");
        }
        if (StringUtils.isEmpty(nimi.trim())) {
            nimi = getAvailableName(curOrganisaatio);
        }
        if (nimi.length() > 180) {
            nimi = nimi.substring(0, 180);
        }
        if (StringUtils.isEmpty(nimi.trim())) {
        	error(5, curOrganisaatio.getOid()+" "+curOrganisaatio.getNimi());
        }
        return StringUtils.rightPad(nimi, 180);
    }

    private String getAvailableName(OrganisaatioPerustieto curOrganisaatio) {
        if (!StringUtils.isEmpty(curOrganisaatio.getNimi("fi"))) {
            return curOrganisaatio.getNimi("fi");
        }
        if (!StringUtils.isEmpty(curOrganisaatio.getNimi("sv"))) {
            return curOrganisaatio.getNimi("sv");
        }
        if (!StringUtils.isEmpty(curOrganisaatio.getNimi("en"))) {
            return curOrganisaatio.getNimi("en");
        }
        return "";
    }

    private String getSisainenKoodi(Organisaatio orgE) throws OPTFormatException  {
    	return numFormatter(""+orgE.getNimi().getId(), 10, "sisainen koodi");
    }

	@Override
	public void composeRecords() throws IOException, UserStopRequestException {
		for (OrganisaatioPerustieto curOppilaitos : this.orgContainer.getOppilaitokset()) {
			try {
				writeRecord(curOppilaitos, OrgType.OPPILAITOS);
			} catch (OPTFormatException e) {
				LOG.error(String.format(errors[0], curOppilaitos.getOid()+" "+curOppilaitos.getNimi()));
			} 
		}

		for (OrganisaatioPerustieto curToimipiste : this.orgContainer.getToimipisteet()) {
			try {
				writeRecord(curToimipiste, OrgType.TOIMIPISTE);
			} catch (OPTFormatException e) {
				LOG.error(String.format(errors[1], curToimipiste.getOid()+" "+curToimipiste.getNimi()));
			}
		}
	}
	
    private String getOrganisaatioLyhytNimi(
            OrganisaatioPerustieto curOrganisaatio, List<String> kielet) throws OPTFormatException {
    	String nimi = getOrganisaatioNimi(curOrganisaatio, kielet);
    	if (StringUtils.isEmpty(nimi)) {
    		error(6, curOrganisaatio.getOid()+" "+curOrganisaatio.getNimi()+" tyyppi: "+curOrganisaatio.getOrganisaatiotyypit());
    	}
    	return strCutter(nimi, 40, "Organisaatio lyhyt nimi", false);
    }
    
	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		OrganisaatioPerustieto curOrganisaatio = (OrganisaatioPerustieto) args[0];
		OrgType orgType = (OrgType) args[1];
        Organisaatio orgE = kelaDAO.findOrganisaatioByOid(curOrganisaatio.getOid());
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s",//12 fields
                getSisainenKoodi(orgE),//Sisainen koodi
                StringUtils.leftPad("", 5),//OPE_OPPILNRO
                StringUtils.leftPad("", 2),//OPE_OPJNO
                _getOppilaitosNro(curOrganisaatio,orgType),//OPPILNRO
                StringUtils.leftPad("", 3),//KIELI
                getOrganisaatioNimi(curOrganisaatio, orgE.getKielet()),//Nimi
                //getOrganisaatioLyhytNimi(curOrganisaatio, orgE, orgE.getKielet()),//Nimen lyhenne
                getOrganisaatioLyhytNimi(curOrganisaatio, orgE.getKielet()),//Nimen lyhenne
                StringUtils.leftPad(DEFAULT_DATE, 10),//Viimeisin paivityspaiva
                getOrgOid(curOrganisaatio), //Org OID
                StringUtils.leftPad("", 8),//Viimeisin paivittaja
                "X",//Nimi on virallinen
                StringUtils.leftPad(DEFAULT_DATE, 10),//Alkupaiva, voimassaolon alku
                StringUtils.leftPad(DEFAULT_DATE, 10)//Loppupaiva, voimassaolon loppu
        		);
        return record;
	}

	private String _getOppilaitosNro(OrganisaatioPerustieto curOrganisaatio, OrgType orgType) throws OPTFormatException {
		String opnro = "";
		if (orgType.equals(OrgType.OPPILAITOS)) {
			opnro = StringUtils.leftPad(curOrganisaatio.getOppilaitosKoodi(), 5);
		} else {
			//TODO: ei toimi jos parentOid on toimipiste:
			//opnro = StringUtils.leftPad(this.orgContainer.getOppilaitosoidOppilaitosMap().get(curOrganisaatio.getParentOid()).getOppilaitosKoodi(), 5);
			opnro = StringUtils.leftPad(this.orgContainer.getOppilaitosoidOppilaitosMap().get(curOrganisaatio.getParentOppilaitosOid()).getOppilaitosKoodi(), 5);
		}
        if (null == opnro || StringUtils.isEmpty(opnro)) {
        	error(7, curOrganisaatio.getOid()+" "+curOrganisaatio.getNimi());
        }
        return opnro;
    }
    
	private String getOrgOid(OrganisaatioPerustieto org) throws OPTFormatException {
		if(null==org.getOid()) {
			error(4);
		}
		String oid = org.getOid().substring(org.getOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(3, org.getOid()+" "+org.getNimi());
		}
		return strFormatter(oid, 22, "OID");
	}
	
	@Value("${OPTINI.alkutietue}")
    public void setAlkutietue(String alkutietue) {
        this.ALKUTIETUE = alkutietue;
    }
	
	@Value("${OPTINI.lopputietue}")
    public void setLopputietue(String lopputietue) {
        this.LOPPUTIETUE = lopputietue;
    }
	
	@Value("${OPTINI.fileIdentifier:OPTINI}")
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
		return null;
	}

	@Override
	public String[] getInfos() {
		return null;
	}
}
