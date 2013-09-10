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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioPerustietoType;
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
    private static final String OSOITE_FIELD = "osoite"; 
    
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
        
        for (OrganisaatioPerustietoType curOppilaitos : this.orgContainer.getOppilaitokset()) {
                try {
                    bos.write(toLatin1(createRecord(curOppilaitos)));   
                    bos.flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }
        
        for (OrganisaatioPerustietoType curToimipiste : this.orgContainer.getToimipisteet()) {
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

    private String createRecord(OrganisaatioPerustietoType organisaatio) {
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

    private String getYhtId(OrganisaatioPerustietoType organisaatio) {
        Organisaatio orgE = kelaDAO.findOrganisaatioByOid(organisaatio.getOid());
        return StringUtils.leftPad(String.format("%s", kelaDAO.getKayntiosoiteIdForOrganisaatio(orgE.getId())), 10, '0');
    }

}
