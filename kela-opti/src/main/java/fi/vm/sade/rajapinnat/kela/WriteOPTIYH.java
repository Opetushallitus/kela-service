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
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatiosuhde;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTIYH extends AbstractOPTIWriter {
    private String FILEIDENTIFIER;
    private String ALKUTIETUE;
    private String LOPPUTIETUE;
    
	private final static String ERR_MESS_OPTIYH_1="incorrect OID : '%s'";
	private final static String ERR_MESS_OPTIYH_2="invalid format for liitos OID1:%s OID2:%s";
	private final static String ERR_MESS_OPTIYH_3="OID cannot not be null";
	
    public WriteOPTIYH() {
        super();  
    }

	@Override
	public void composeRecords() throws IOException, UserStopRequestException {
		List<Organisaatiosuhde> liitokset = this.kelaDAO.findAllLiitokset();
		if (liitokset != null) {
			for (Organisaatiosuhde curLiitos : liitokset) {
				try {
					if (!StringUtils.isEmpty(curLiitos.getParent().getOppilaitoskoodi()) && !StringUtils.isEmpty(curLiitos.getChild().getOppilaitoskoodi())) {
						this.writeRecord(curLiitos);
					}
				} catch (OPTFormatException e) {
					LOG.error(String.format(ERR_MESS_OPTIYH_2, curLiitos.getChild().getOid()+" "+curLiitos.getChild().getNimi(), curLiitos.getParent().getOid()+" "+curLiitos.getParent().getNimi()));
				}
			}
		}
	}

	@Override
    public String composeRecord(Object... argv) throws OPTFormatException {
		Organisaatiosuhde liitos=(Organisaatiosuhde) argv[0];
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",//14 fields + EOL
               getLiitosId(liitos),//YHD_ID
               StringUtils.leftPad("", 10),//KAS_ID
               StringUtils.leftPad("", 10),//ALA_ID
               StringUtils.leftPad("", 5),//OPE_OPPILNRO
               StringUtils.leftPad("", 2),//OPE_OPJNRO
               getOppilaitosnumero(liitos.getChild()), //OPPILNRO
               getOrgOid(liitos.getChild()), //Vanhan oppl. organisaatio-oid
               StringUtils.leftPad("", 38),//Suomenkielinen selite
               getDateStrOrDefault(liitos.getAlkuPvm()),//Oppilaitoksen yhdistamispaiva
               getOppilaitosnumero(liitos.getParent()), //KOHDE_ONRO
               StringUtils.leftPad("", 2),//KOHDE_OPJNRO
               DEFAULT_DATE,//Viimeisin paivityspvm
               getOrgOid(liitos.getParent()), //uuden oppl. organisaatio-oid
               StringUtils.leftPad("", 8),//Viimeisin paivittaja
               "\n");
        return record;
    }
    
	private String getOrgOid(Organisaatio org) throws OPTFormatException {
		if(null==org.getOid()) {
			error(String.format(ERR_MESS_OPTIYH_3));
		}
		String oid = org.getOid().substring(org.getOid().lastIndexOf('.') + 1);
		if (oid == null || oid.length() == 0) {
			error(String.format(ERR_MESS_OPTIYH_1, org.getOid()+" "+org.getNimi()));
		}
		return strFormatter(oid, 22, "OID");
	}
    
    private String getOppilaitosnumero(Organisaatio organisaatio) throws OPTFormatException {
    	return numFormatter(organisaatio.getOppilaitoskoodi(), 5, "oppilaitosnumero");
    }

    private String getLiitosId(Organisaatiosuhde liitos) throws OPTFormatException {
    	return numFormatter(""+liitos.getId(),10,"id");
    }
    
	@Value("${OPTIYH.alkutietue}")
    public void setAlkutietue(String alkutietue) {
        this.ALKUTIETUE = alkutietue;
    }
	
	@Value("${OPTIYH.lopputietue}")
    public void setLopputietue(String lopputietue) {
        this.LOPPUTIETUE = lopputietue;
    }
	
	@Value("${OPTIYH.fileIdentifier:OPTIYH}")
    public void setFilenIdentifier(String fileIdentifier) {
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
}
