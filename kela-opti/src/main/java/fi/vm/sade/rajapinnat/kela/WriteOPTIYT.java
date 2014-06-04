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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.resource.OrganisaatioResource;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTIYT extends AbstractOPTIWriter {
    
    @Autowired
    private ApplicationContext appContext;
    
    

    private static final String OPTIYT = ".OPTIYT";
    
    private static final String ALKUTIETUE = "0000000000ALKU\n";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????\n";
    private static final String POSTINUMERO_FIELD = "postinumeroUri"; 
    
    private static final String STREET_ADDRESS = "osoite";

    private static final String ADDRESS_DATA_TYPE = "osoiteTyyppi";
    private static final String ADDRESS_DATA_TYPE_VISIT = "kaynti";
    private static final String ADDRESS_DATA_TYPE_POSTAL = "posti";
    private static final String LANG = "kieli";
    private static final String LANG_FI = "fi";
    private static final String LANG_SV = "sv";

    private static final String DATA_TYPE = "tyyppi";
    private static final String DATA_TYPE_PHONE = "puhelin";
    private static final String DATA_TYPE_FAX = "faksi";
    private static final String DATA_TYPE_PHONE_NUMBER = "numero";
    private static final String DATA_TYPE_EMAIL = "email";
    private static final String DATA_TYPE_WWW = "www";
    
    
    
    public WriteOPTIYT() {
        super();
        
    }
    
    @Override
    public void writeFile() throws IOException {
        createFileName("", OPTIYT);
        if (organisaatioResource == null) {
            organisaatioResource = (OrganisaatioResource)appContext.getBean("organisaatioResource"); //@Autowired did not work
        }
        bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
        bos.write(toLatin1(ALKUTIETUE));
        
        for (OrganisaatioPerustieto curOppilaitos : this.orgContainer.getOppilaitokset()) {
                try {
                    bos.write(toLatin1(createRecord(curOppilaitos)));   
                    bos.flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }
        
        for (OrganisaatioPerustieto curToimipiste : this.orgContainer.getToimipisteet()) {
                try {
                    bos.write(toLatin1(createRecord(curToimipiste)));
                    bos.flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }
        
        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();

    }

    private String createRecord(OrganisaatioPerustieto organisaatio) {
        OrganisaatioRDTO orgR = this.organisaatioResource.getOrganisaatioByOID(organisaatio.getOid());
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",//16 fields + EOL
                getYhtId(organisaatio),//YHT_ID
                getPostinumero(orgR.getYhteystiedot()),//orgR.getPostiosoite()),//POS_NUMERO
                StringUtils.leftPad("", 3),//Postinumeroon liittyva maatunnus
                DEFAULT_DATE,//01.01.0001-merkkijono
                getKatuosoite(orgR.getYhteystiedot()),//orgR.getKayntiosoite()),//Katuosoite tai kayntiosoite
                getPostilokero(orgR.getYhteystiedot()),//orgR.getPostiosoite()),//Postilokero
                getPhoneNumber(orgR.getYhteystiedot(), DATA_TYPE_PHONE, 60),//orgR.getPuhelinnumero(), 60),//Puhelinnumero
                getSimpleContact(orgR.getYhteystiedot(), 80, DATA_TYPE_EMAIL),//orgR.getEmailOsoite(), 80),//Sahkopostiosoite
                getPhoneNumber(orgR.getYhteystiedot(), DATA_TYPE_FAX, 20),//orgR.getFaksinumero(), 20),//Fax-numero
                getSimpleContact(orgR.getYhteystiedot(), 80, DATA_TYPE_WWW),//Kotisivujen osoite
                StringUtils.leftPad("", 15),//Postinumero (YHT_ULK_PTNUMERO)
                StringUtils.leftPad("", 25),//Postitoimipaikka (YHT_ULK_PTPAIKKA)
                StringUtils.leftPad("", 40),//YHT_ULK_ALUE
                DEFAULT_DATE,//Viimeisin paivityspaiva
                StringUtils.leftPad("", 30),//Viimeisin paivittaja
                getPostinumero(orgR.getYhteystiedot()),//Postinumero POS_NRO
                "\n");
        return record;
    }

    private String getSimpleContact(List<Map<String, String>> yhteystiedot, int pad, String fieldName) {
        
        String yhteystieto = null;
        
        Map<String,String> addrTransls = new HashMap<String,String>();
        
        for (Map<String,String> curYht : yhteystiedot) {
            
            if (curYht.containsKey(fieldName)) {
                List<KoodiType> langCodes = this.getKoodisByUriAndVersio(curYht.get(LANG));
                String langKey = null;
                if (langCodes != null && !langCodes.isEmpty()) {
                    langKey = langCodes.get(0).getKoodiArvo();
                }
                if (langKey != null) {
                    addrTransls.put(langKey, curYht.get(fieldName));
                }
            }
        }
        
        if (addrTransls.containsKey(LANG_FI)) {
             yhteystieto = addrTransls.get(LANG_FI);
        }
        if (addrTransls.containsKey(LANG_SV)) {
            yhteystieto = addrTransls.get(LANG_SV);
        }
        if (!addrTransls.isEmpty()) {
            yhteystieto = addrTransls.values().iterator().next();
        }
        
        if (yhteystieto != null && yhteystieto.length() > pad) {
            yhteystieto = yhteystieto.substring(0, pad);
        }
        return (yhteystieto != null) ? StringUtils.rightPad(yhteystieto, pad) : StringUtils.rightPad("", pad);
        
    }

    private String getPhoneNumber(List<Map<String, String>> yhteystiedot,
            String phoneType, int pad) {
        String yhteystieto = null;
        
        Map<String,String> addrTransls = new HashMap<String,String>();
        
        for (Map<String,String> curYht : yhteystiedot) {
            
            if (curYht.containsKey(DATA_TYPE_PHONE_NUMBER) && curYht.containsKey(DATA_TYPE) && curYht.get(DATA_TYPE).equals(phoneType)) {
                List<KoodiType> langCodes = this.getKoodisByUriAndVersio(curYht.get(LANG));
                String langKey = null;
                if (langCodes != null && !langCodes.isEmpty()) {
                    langKey = langCodes.get(0).getKoodiArvo();
                }
                if (langKey != null) {
                    addrTransls.put(langKey, curYht.get(DATA_TYPE_PHONE_NUMBER));
                }
            }
        }
        
        if (addrTransls.containsKey(LANG_FI)) {
             yhteystieto = addrTransls.get(LANG_FI);
        }
        if (addrTransls.containsKey(LANG_SV)) {
            yhteystieto = addrTransls.get(LANG_SV);
        }
        if (!addrTransls.isEmpty()) {
            yhteystieto = addrTransls.values().iterator().next();
        }
        
        if (yhteystieto != null && yhteystieto.length() > pad) {
            yhteystieto = yhteystieto.substring(0, pad);
        }
        return (yhteystieto != null) ? StringUtils.rightPad(yhteystieto, pad) : StringUtils.rightPad("", pad);
    }

    private String getPostilokero(List<Map<String, String>> yhteystiedot) {
        String katuos = this.getAddressStr(yhteystiedot, ADDRESS_DATA_TYPE_POSTAL, STREET_ADDRESS, false);
        if (!StringUtils.isEmpty(katuos) 
                && katuos.startsWith("PL")
                && katuos.length() < 11) {
            return StringUtils.rightPad(katuos, 10);
        }
        return StringUtils.rightPad("", 10);
    }

    private String getKatuosoite(List<Map<String, String>> yhteystiedot) {
        
        String osoiteStr = getAddressStr(yhteystiedot, ADDRESS_DATA_TYPE_VISIT, STREET_ADDRESS, false);
        
        return StringUtils.rightPad(osoiteStr, 50);
    }

    private String getAddressStr(List<Map<String, String>> yhteystiedot, String addressType, String fieldName, boolean isCode) {
        
        Map<String,String> addrTransls = new HashMap<String,String>();
        
        for (Map<String,String> curYht : yhteystiedot) {
            if (curYht.containsKey(ADDRESS_DATA_TYPE) && curYht.get(ADDRESS_DATA_TYPE).equals(addressType)) {
                List<KoodiType> langCodes = this.getKoodisByUriAndVersio(curYht.get(LANG));
                String langKey = null;
                if (langCodes != null && !langCodes.isEmpty()) {
                    langKey = langCodes.get(0).getKoodiArvo();
                }
                if (langKey != null && curYht.get(fieldName) != null) {
                    String val = null;
                    if (isCode) {
                        List<KoodiType> valCodes = this.getKoodisByUriAndVersio(curYht.get(fieldName));
                        val = valCodes != null && !valCodes.isEmpty() ? valCodes.get(0).getKoodiArvo() : null;
                    } else {
                        val = curYht.get(fieldName);
                    }
                    if (val != null) {
                        addrTransls.put(langKey.toLowerCase(), val);
                    }
                }
                
            }
        }
        
        if (addrTransls.containsKey(LANG_FI)) {
            return addrTransls.get(LANG_FI);
        }
        if (addrTransls.containsKey(LANG_SV)) {
            return addrTransls.get(LANG_SV);
        }
        if (!addrTransls.isEmpty()) {
            return addrTransls.values().iterator().next();
        }
        
        return "";
    }

    private String getPostinumero(List<Map<String, String>> yhteystiedot) {
        String postinro = this.getAddressStr(yhteystiedot, ADDRESS_DATA_TYPE_POSTAL, POSTINUMERO_FIELD, true); 
        return StringUtils.leftPad(postinro, 5);
    }

    private String getYhtId(OrganisaatioPerustieto organisaatio) {
        Organisaatio orgE = kelaDAO.findOrganisaatioByOid(organisaatio.getOid());
        return StringUtils.leftPad(String.format("%s", kelaDAO.getKayntiosoiteIdForOrganisaatio(orgE.getId())), 10, '0');
    }
    


}
