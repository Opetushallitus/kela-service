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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
 * 
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
	private char PARENTPATH_SEPARATOR;
	private String ALKUTIETUE;
	private String LOPPUTIETUE;

	private final static String ERR_MESS_1="ERROR: Error writing oppilaitos %s : invalid values.";
	private final static String ERR_MESS_2="ERROR: Error writing oppilaitos %s : %s.";
	private final static String ERR_MESS_3="ERROR: Error writing toimipiste %s : invalid values.";
	private final static String ERR_MESS_4="ERROR: Error writing toimipiste %s : %s.";
	private final static String ERR_MESS_5="ERROR: length of %s ('%s') should be max %s.";
	private final static String ERR_MESS_6="ERROR: incorrect OID : '%s'";
	private final static String ERR_MESS_7="ERROR: could not find oppilaitos for toimipiste with OID %s";
	
	private final static String WARN_MESS_1="WARN: perhaps toimipiste %s should not have oppilaitoskoodi (%s)";

	public WriteORGOID() {
		super();
	}

	@Override
	public void writeFile() throws IOException {
		createFileName("", FILENAME_SUFFIX);
		bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
		bos.write(toLatin1(ALKUTIETUE + "\n"));

		for (OrganisaatioPerustieto ol : this.orgContainer.getOppilaitokset()) {
			try {
				bos.write(toLatin1(createRecord(ol, OrgType.OPPILAITOS)));
				bos.flush();
			} catch (OPTFormatException e) {
				System.err.println(String.format(ERR_MESS_1, ol.getOid()));
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println(String.format(ERR_MESS_2, ol.getOid(), ex.getMessage()));
			}
		}

		for (OrganisaatioPerustieto tp : this.orgContainer.getToimipisteet()) {
			try {
				bos.write(toLatin1(createRecord(tp, OrgType.TOIMIPISTE)));
				bos.flush();
			} catch (OPTFormatException e) {
				System.err.println(String.format(ERR_MESS_3, tp.getOid()));
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println(String.format(ERR_MESS_4, tp.getOid(), ex.getMessage()));
			}
		}

		bos.write(toLatin1(LOPPUTIETUE + "\n"));
		bos.flush();
		bos.close();
	}

	private String findOppilaitosNumero(Organisaatio org, OrgType orgType) throws OPTFormatException {
		String olKoodi = org.getOppilaitoskoodi();
		if (orgType.equals(OrgType.TOIMIPISTE)) {
			if (org.getOppilaitoskoodi() != null) {
				warn(String.format(WARN_MESS_1, org.getOid(), org.getOppilaitoskoodi()));
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
			error(String.format(ERR_MESS_7, org.getOid()));
		}
		return strFormatter(olKoodi, orgType, null, 5, "oppilaitoskoodi");
	}

	private String getOPJNO(Organisaatio org, OrgType orgType) throws OPTFormatException {
		return strFormatter(org.getOpetuspisteenJarjNro(), orgType, OrgType.TOIMIPISTE, 2, "opetuspisteen järjestysnumero");
	}

	private String getOID(Organisaatio org, OrgType orgType) throws OPTFormatException {
		String oid = org.getOid().substring(org.getOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(String.format(ERR_MESS_6, org.getOid()));
		}
		return strFormatter(oid, orgType, null, 22, "OID");
	}

	private String strFormatter(String str, OrgType orgType, OrgType reqOrgType, int len, String humanName) throws OPTFormatException {
		if (null == reqOrgType || reqOrgType.equals(orgType)) {
			if (null == str || str.length() > len) {
				error(String.format(ERR_MESS_5, humanName, str, len));
			}
			return StringUtils.rightPad(str, len);
		}
		return StringUtils.rightPad("", len);
	}

	private void error(String errorMsg) throws OPTFormatException {
		System.err.println(errorMsg);
		throw new OPTFormatException();
	}

	private void warn(String warnMsg) {
		System.out.println(warnMsg);
	}

	private Organisaatio getOrganisaatio(OrganisaatioPerustieto orgPerustieto) {
		Organisaatio org = kelaDAO.findOrganisaatioByOid(orgPerustieto.getOid());
		return org;
	}

	private String createRecord(OrganisaatioPerustieto organisaatio, OrgType orgType) throws OPTFormatException {
		Organisaatio org = getOrganisaatio(organisaatio);
		String record = String.format("%s%s%s%s", // 4 fields + EOL
				getOPPIL_NRO(org, orgType), 
				getOPJNO(org, orgType), 
				getOID(org, orgType), 
				"\n");
		return record;
	}
	
	@Value("${organisaatiot.parentPathSeparator:|}")
    public void setParentPathSeparator(char parentPathSeparator) {
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
}
