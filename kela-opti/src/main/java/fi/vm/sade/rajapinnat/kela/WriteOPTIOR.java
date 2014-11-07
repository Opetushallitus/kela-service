/*
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.OrganisaatioPerustieto;
/**
 * @author Janne
 */
@Component
@Configurable
public class WriteOPTIOR extends AbstractOPTIWriter {

	@Autowired
	private ApplicationContext appContext;
	
	private String FILEIDENTIFIER;
	private String ALKUTIETUE;
	private String LOPPUTIETUE;
	
	private final static String [] errors = {
		"could not write oppilaitos %s : invalid values.",
		"could not write toimipiste %s : invalid values.",
		"incorrect OID : '%s'",
		"could not find oppilaitos (OPPIL_NRO) for organisaatio with OID %s"
	};

	public WriteOPTIOR() {
		super();
	}
	
	@Override
	public void composeRecords() throws IOException, UserStopRequestException {
		for (OrganisaatioPerustieto ol : this.orgContainer.getOppilaitokset()) {
			try {
				this.writeRecord(ol, OrgType.OPPILAITOS);
			} catch (OPTFormatException e) {
				LOG.error(String.format(errors[0], ol.getOid()+" "+ol.getNimi()));
			}
		}
		for (OrganisaatioPerustieto tp : this.orgContainer.getToimipisteet()) {
			try {
				this.writeRecord(tp, OrgType.TOIMIPISTE);
			} catch (OPTFormatException e) {
				LOG.error(String.format(errors[1], tp.getOid()+" "+tp.getNimi()));
			}
		}
	}
	
	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		Organisaatio org = getOrganisaatio((OrganisaatioPerustieto) args[0]);
		OrgType orgType = (OrgType) args[1];
		String record = String.format("%s%s%s", // 4 fields
				getOPPIL_NRO(org, orgType), 
				getOPJNO(org, orgType), 
				getOID(org, orgType)
				);
		return record;
	}

	private String getOPPIL_NRO(Organisaatio org, OrgType orgType) throws OPTFormatException {
		String olKoodi = getOppilaitosNro(org, orgType);
		if (olKoodi == null || olKoodi.length() == 0) {
			error(4, org.getOid()+" "+org.getNimi());
		}
		return strFormatter(olKoodi, orgType, null, 5, "oppilaitosnumero");
	}

	private String getOPJNO(Organisaatio org, OrgType orgType) throws OPTFormatException {
		return strFormatter(org.getOpetuspisteenJarjNro(), orgType, OrgType.TOIMIPISTE, 2, "opetuspisteen järjestysnumero");
	}

	private String getOID(Organisaatio org, OrgType orgType) throws OPTFormatException {
		String oid = org.getOid().substring(org.getOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(3, org.getOid()+" "+org.getNimi());
		}
		return strFormatter(oid, orgType, null, 22, "OID");
	}
	
	private String strFormatter(String str, OrgType orgType, OrgType reqOrgType, int len, String humanName) throws OPTFormatException {
		if (null == reqOrgType || reqOrgType.equals(orgType)) {
			if (null == str || str.length() > len) {
				error(2, humanName, str, len);
			}
			return StringUtils.rightPad(str, len);
		}
		return StringUtils.rightPad("", len);
	}

	private Organisaatio getOrganisaatio(OrganisaatioPerustieto orgPerustieto) {
		Organisaatio org = kelaDAO.findOrganisaatioByOid(orgPerustieto.getOid());
		return org;
	}
	
	@Value("${OPTIOR.alkutietue}")
    public void setAlkutietue(String alkutietue) {
        this.ALKUTIETUE = alkutietue;
    }
	
	@Value("${OPTIOR.lopputietue}")
    public void setLopputietue(String lopputietue) {
        this.LOPPUTIETUE = lopputietue;
    }
	
	@Value("${OPTIOR.fileIdentifier:OPTIOR}")
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
