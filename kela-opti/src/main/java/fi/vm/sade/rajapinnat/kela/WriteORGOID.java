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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.resource.OrganisaatioResource;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
/**
 * 
 * @author Janne
 */
@Component
@Configurable
public class WriteORGOID extends AbstractOPTIWriter {
    
    @Autowired
    private ApplicationContext appContext;
    
    

    private static final String ORGOID = ".ORGOID";
    private static final int OPPILAITOSKOODI_LEN=5;
    
    private static final String ALKUTIETUE = "0000000000ALKU\n";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????\n";
    private static final String POSTINUMERO_FIELD = "postinumeroUri"; 
    private static final String OSOITE_FIELD = "osoite"; 
    
    private enum OrgType {
    	OPPILAITOS,
    	TOIMIPISTE
    }
    
    public WriteORGOID() {
        super();
        
    }
    
    @Override
    public void writeFile() throws IOException {
        createFileName("", ORGOID);
        if (organisaatioResource == null) {
            organisaatioResource = (OrganisaatioResource)appContext.getBean("organisaatioResource"); //@Autowired did not work
        }
        bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
        //TODO: tarvitaanko?
        bos.write(toLatin1(ALKUTIETUE));
        
        for (OrganisaatioPerustieto curOppilaitos : this.orgContainer.getOppilaitokset()) {
                try {
                    bos.write(toLatin1(createRecord(curOppilaitos, OrgType.OPPILAITOS)));   
                    bos.flush();
                } catch(OPTFormatException e) {
                	System.err.println("Error writing oppilaitos : "+curOppilaitos.getOid()+" : invalid values");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println("Error writing "+curOppilaitos.getOid()+" OID: "+ex.getMessage());
                }
        }
        
        for (OrganisaatioPerustieto curToimipiste : this.orgContainer.getToimipisteet()) {
                try {
                    bos.write(toLatin1(createRecord(curToimipiste, OrgType.TOIMIPISTE)));
                    bos.flush();
                } catch(OPTFormatException e) {
                	System.err.println("Error writing toimipiste : "+curToimipiste.getOid()+" : invalid values");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println("Error writing "+curToimipiste.getOid()+" OID: "+ex.getMessage());
                }
        }
        //TODO: tarvitaanko?
        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();

    }
    
    private String getOPPIL_NRO(Organisaatio organisaatio, OrgType orgType) throws OPTFormatException {
    	String oppilaitosKoodi = organisaatio.getOppilaitoskoodi(); 
    	if (orgType==OrgType.TOIMIPISTE && organisaatio.getOppilaitoskoodi()!=null) {
    		//TODO : poista
    		System.out.println("JOJOJOJOJOJOJO");
    	}
    	if (orgType==OrgType.TOIMIPISTE && organisaatio.getOppilaitoskoodi()==null) {
    		String [] parentsOids = organisaatio.getParentOidPath().split("\\|");
    		System.out.print("toimipiste "+organisaatio.getOid()+" parents:"+organisaatio.getParentOidPath());
    		for (String parentOID : parentsOids) {
    				System.out.print("->"+parentOID);
    				if (parentOID.length()>0 && this.orgContainer.getOppilaitosoidOppilaitosMap().containsKey(parentOID)) {
    					System.out.print(" match");
    					oppilaitosKoodi=this.orgContainer.getOppilaitosoidOppilaitosMap().get(parentOID).getOppilaitosKoodi();
    					break;
    				}
    		}
    		System.out.println();
    	}	
    	return strFormatter(oppilaitosKoodi, orgType, null, 5, "oppilaitoskoodi" );
    }
    private String getOPJNO(Organisaatio organisaatio, OrgType orgType) throws OPTFormatException {
    	return strFormatter(organisaatio.getOpetuspisteenJarjNro(), orgType, OrgType.TOIMIPISTE, 2, "opetuspisteenjärjestysnumero" );
    }
    private String getOID(Organisaatio organisaatio, OrgType orgType) throws OPTFormatException {
    	String oid = organisaatio.getOid().substring(organisaatio.getOid().lastIndexOf('.')+1);
    	return strFormatter(oid, orgType, null, 22, "OID" );
    }
    private String strFormatter(String str, OrgType orgType, OrgType reqOrgType, int len, String name) throws OPTFormatException {
    	if (null == reqOrgType || reqOrgType.equals(orgType)) {
    		if( null==str || str.length()>len ) {
    			error("Error: length of "+name+" ('"+str+"') should be "+len);
    		}
    		return StringUtils.rightPad(str, len);
    	}
    	return StringUtils.rightPad("", len);
    }
    
    private void error(String errorMessage) throws OPTFormatException {
		System.err.println(errorMessage);
    	throw new OPTFormatException();
    }
    
    private Organisaatio getOrganisaatio(OrganisaatioPerustieto organisaatioPerustieto) {
    	Organisaatio organisaatio = kelaDAO.findOrganisaatioByOid(organisaatioPerustieto.getOid());
    	return organisaatio;
    }
    
    private String createRecord(OrganisaatioPerustieto organisaatio, OrgType orgType) throws OPTFormatException{
    	//OrganisaatioRDTO orgR = this.organisaatioResource.getOrganisaatioByOID(organisaatio.getOid());
    	Organisaatio org = getOrganisaatio(organisaatio);
    	String record = String.format("%s%s%s%s", //4 fields + EOL
    			getOPPIL_NRO(org, orgType),
    			getOPJNO(org,orgType),
    			getOID(org,orgType),
    			"\n");
        return record;
    }
    
/*    private String getOPJNO(OrganisaatioPerustieto organisaatio, OrgType orgType) {
    	return strFormatter(organisaatio.getOppilaitosKoodi(), orgType, OrgType.TOIMIPISTE, 2, "toimipistekoodi" );
    }*/

    /*
    private String createRecord(OrganisaatioPerustieto organisaatio, OrgType orgType) {
    	String record = String.format("%s%s%s%s", //4 fields + EOL
    			getOppilNro(organisaatio, orgType),
    			Kun organisaation tyyppi oppilaitos, niin oppilaitosnumero + organisaatiooid
        OrganisaatioRDTO orgR = this.organisaatioResource.getOrganisaatioByOID(organisaatio.getOid());
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",//16 fields + EOL
                getYhtId(organisaatio),//YHT_ID
                getPostinumero(orgR.getPostiosoite()),//POS_NUMERO
                StringUtils.leftPad("", 3),//Postinumeroon liittyva maatunnus
                DEFAULT_DATE,//01.01.0001-merkkijono
                getKatuosoite(orgR.getKayntiosoite()),//Katuosoite tai kayntiosoite
                getPostilokero(orgR.getPostiosoite()),//Postilokero
                getSimpleYhteystieto(orgR.getPuhelinnumero(), 60),//Puhelinnumero
                getSimpleYhteystieto(orgR.getEmailOsoite(), 80),//Sahkopostiosoite
                getSimpleYhteystieto(orgR.getFaksinumero(), 20),//Fax-numero
                getSimpleYhteystieto(orgR.getWwwOsoite(), 80),//Kotisivujen osoite
                StringUtils.leftPad("", 15),//Postinumero (YHT_ULK_PTNUMERO)
                StringUtils.leftPad("", 25),//Postitoimipaikka (YHT_ULK_PTPAIKKA)
                StringUtils.leftPad("", 40),//YHT_ULK_ALUE
                DEFAULT_DATE,//Viimeisin paivityspaiva
                StringUtils.leftPad("", 30),//Viimeisin paivittaja
                getPostinumero(orgR.getKayntiosoite()),//Postinumero POS_NRO
                "\n");
        return record;
    }
    
    private String error(String errorMessage) {
		System.err.println(errorMessage);
    	return "";
    }
    
    private String strFormatter(String str, OrgType orgType, OrgType reqOrgType, int len, String name) {
    	if (null == reqOrgType || reqOrgType.equals(orgType)) {
    		if( null==str || str.length()>len ) {
    			return error("Error: length of "+name+" ('"+str+"') should be "+len);
    		}
    		return StringUtils.rightPad(str, len);
    	}
    	return StringUtils.rightPad("", len);
    }
    
    private String getOPPIL_NRO(OrganisaatioPerustieto organisaatio, OrgType orgType) {
    	return strFormatter(organisaatio.getOppilaitosKoodi(), orgType, null, 5, "oppilaitoskoodi" );
    }
    private String getOPJNO(OrganisaatioPerustieto organisaatio, OrgType orgType) {
    	return strFormatter(organisaatio.getOppilaitosKoodi(), orgType, OrgType.TOIMIPISTE, 2, "toimipistekoodi" );
    }
    private String getOppilNro(OrganisaatioPerustieto organisaatio, OrgType orgType) {
    	final int FIELD_LEN = 2;
    	String ret="";
    	if (orgType.equals(OrgType.TOIMIPISTE)) {
    		String oppilaitosKoodi = organisaatio.getOppilaitosKoodi();
    		if (oid == null || oid.length()+OPPILAITOSKOODI_LEN>FIELD_LEN)) {
    			return error("Error: invalid length of oppilaitoskoodi ('"+organisaatio.getOppilaitosKoodi()+"')");
    		} 
   			ret=oppilaitosKoodi;
    	}
    	return StringUtils.rightPad(ret, FIELD_LEN);
    }
    
    private String getOPJNro(OrganisaatioPerustieto organisaatio, OrgType orgType) {
    	final int FIELD_LEN = 2;
    	String ret="";
    	if (orgType.equals(OrgType.TOIMIPISTE)) {
    		String oppilaitosKoodi = organisaatio.getOppilaitosKoodi();
    		String oid = organisaatio.getOid();
    		if (null == oppilaitosKoodi || oppilaitosKoodi.length()!=OPPILAITOSKOODI_LEN ||
    			oid == null || oid.length()+OPPILAITOSKOODI_LEN>FIELD_LEN) {
    			String errorMessage="Error: invalid length of oid ('"+organisaatio.getOid()+"') or oppilaitoskoodi ('"+organisaatio.getOppilaitosKoodi()+"')";
    			System.err.println(errorMessage);
    			throw new RuntimeException(errorMessage);
    		} else {
    			ret=oppilaitosKoodi+oid;
    		}
    	}
    	return StringUtils.rightPad(ret, FIELD_LEN);
    }

    
    private String getPostilokero(Map<String, String> postiosoite) {
        String katuos = postiosoite.get(OSOITE_FIELD);
        if (!StringUtils.isEmpty(katuos) 
                && katuos.startsWith("PL")
                && katuos.length() < 11) {
            return StringUtils.rightPad(katuos, 10);
        }
        return StringUtils.rightPad("", 10);
    }

    private String getSimpleYhteystieto(String yhteystieto, int pad) {
        if (yhteystieto != null && yhteystieto.length() > pad) {
            yhteystieto = yhteystieto.substring(0, pad);
        }
        return (yhteystieto != null) ? StringUtils.rightPad(yhteystieto, pad) : StringUtils.rightPad("", pad);
    }

    private String getKatuosoite(Map<String, String> osoite) {
        String osoiteStr = osoite.get(OSOITE_FIELD);
        if (osoiteStr != null && osoiteStr.length() > 50) {
            osoiteStr = osoiteStr.substring(0, 50);
        }
        return StringUtils.rightPad(osoiteStr, 50);
    }

    private String getPostinumero(Map<String, String> osoite) {
        String postinumeroUri = osoite.get(POSTINUMERO_FIELD);
        List<KoodiType> koodit = (postinumeroUri != null) ? this.getKoodisByUriAndVersio(postinumeroUri) : new ArrayList<KoodiType>();
        String postinro = "";
        if (koodit != null && !koodit.isEmpty()) {
            postinro = koodit.get(0).getKoodiArvo();
        }
        return StringUtils.leftPad(postinro, 5);
    }

    private String getYhtId(OrganisaatioPerustieto organisaatio) {
        Organisaatio orgE = kelaDAO.findOrganisaatioByOid(organisaatio.getOid());
        return StringUtils.leftPad(String.format("%s", kelaDAO.getKayntiosoiteIdForOrganisaatio(orgE.getId())), 10, '0');
    }
*/
}
