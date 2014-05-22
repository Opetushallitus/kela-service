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

import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
/**
 * @author Janne
 */
@Component
@Configurable
public class WriteORGOID extends AbstractOPTIWriter {
	private enum OrgType {
		OPPILAITOS, 
		TOIMIPISTE
	}

	@Autowired
	private ApplicationContext appContext;
	
	private String FILENAME_SUFFIX;
	private String PARENTPATH_SEPARATOR;
	private String ALKUTIETUE;
	private String LOPPUTIETUE;

	private final static String ERR_MESS_ORGOID_1="could not write oppilaitos %s : invalid values.";
	private final static String ERR_MESS_ORGOID_2="could not write toimipiste %s : invalid values.";
	private final static String ERR_MESS_ORGOID_3="incorrect OID : '%s'";
	private final static String ERR_MESS_ORGOID_4="could not find oppilaitos for toimipiste with OID %s";
	
	private final static String WARN_MESS_ORGOID_1="perhaps toimipiste %s should not have oppilaitoskoodi (%s)";

	public WriteORGOID() {
		super();
	}
	
	@Override
	public void composeRecords() throws IOException {
		for (OrganisaatioPerustieto ol : this.orgContainer.getOppilaitokset()) {
			try {
				this.writeRecord(ol, OrgType.OPPILAITOS);
			} catch (OPTFormatException e) {
				LOG.error(String.format(ERR_MESS_ORGOID_1, ol.getOid()));
			}
		}
		for (OrganisaatioPerustieto tp : this.orgContainer.getToimipisteet()) {
			try {
				this.writeRecord(tp, OrgType.TOIMIPISTE);
			} catch (OPTFormatException e) {
				LOG.error(String.format(ERR_MESS_ORGOID_2, tp.getOid()));
			}
		}
	}
	
	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		Organisaatio org = getOrganisaatio((OrganisaatioPerustieto) args[0]);
		OrgType orgType = (OrgType) args[1];
		String record = String.format("%s%s%s%s", // 4 fields + EOL
				getOPPIL_NRO(org, orgType), 
				getOPJNO(org, orgType), 
				getOID(org, orgType), 
				"\n");
		return record;
	}

	private String findOppilaitosNumero(Organisaatio org, OrgType orgType) throws OPTFormatException {
		String olKoodi = org.getOppilaitoskoodi();
		if (orgType.equals(OrgType.TOIMIPISTE)) {
			if (org.getOppilaitoskoodi() != null) {
				warn(String.format(WARN_MESS_ORGOID_1, org.getOid(), org.getOppilaitoskoodi()));
			} else {
				String[] parentsOids = org.getParentOidPath().split("" + PARENTPATH_SEPARATOR);
				for (String parentOID : parentsOids) {
					if (parentOID.length() > 0 && this.orgContainer.getOppilaitosoidOppilaitosMap().containsKey(parentOID)) {
						olKoodi = this.orgContainer.getOppilaitosoidOppilaitosMap().get(parentOID).getOppilaitosKoodi();
						break;
					}
				}
			}
		}
		return olKoodi;
	}

	private String getOPPIL_NRO(Organisaatio org, OrgType orgType) throws OPTFormatException {
		String olKoodi = findOppilaitosNumero(org, orgType);
		if (olKoodi == null || olKoodi.length() == 0) {
			error(String.format(ERR_MESS_ORGOID_4, org.getOid()));
		}
		return strFormatter(olKoodi, orgType, null, 5, "oppilaitoskoodi");
	}

	private String getOPJNO(Organisaatio org, OrgType orgType) throws OPTFormatException {
		return strFormatter(org.getOpetuspisteenJarjNro(), orgType, OrgType.TOIMIPISTE, 2, "opetuspisteen järjestysnumero");
	}

	private String getOID(Organisaatio org, OrgType orgType) throws OPTFormatException {
		String oid = org.getOid().substring(org.getOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(String.format(ERR_MESS_ORGOID_3, org.getOid()));
		}
		return strFormatter(oid, orgType, null, 22, "OID");
	}
	
	private String strFormatter(String str, OrgType orgType, OrgType reqOrgType, int len, String humanName) throws OPTFormatException {
		if (null == reqOrgType || reqOrgType.equals(orgType)) {
			if (null == str || str.length() > len) {
				error(String.format(ERR_MESS_2, humanName, str, len));
			}
			return StringUtils.rightPad(str, len);
		}
		return StringUtils.rightPad("", len);
	}

	private Organisaatio getOrganisaatio(OrganisaatioPerustieto orgPerustieto) {
		Organisaatio org = kelaDAO.findOrganisaatioByOid(orgPerustieto.getOid());
		return org;
	}

	@Value("${organisaatiot.parentPathSeparator:\\|}")
    public void setParentPathSeparator(String parentPathSeparator) {
        this.PARENTPATH_SEPARATOR = parentPathSeparator;
    }
	
	@Value("${ORGOID.alkutietue}")
    public void setAlkutietue(String alkutietue) {
        this.ALKUTIETUE = alkutietue;
    }
	
	@Value("${ORGOID.lopputietue}")
    public void setLopputietue(String lopputietue) {
        this.LOPPUTIETUE = lopputietue;
    }
	
	@Value("${ORGOID.filenameSuffix:.ORGOID}")
    public void setFilenameSuffix(String filenameSuffix) {
        this.FILENAME_SUFFIX = filenameSuffix;
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
